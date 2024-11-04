# FastAPI imports
from fastapi import FastAPI, Depends, HTTPException, status, Body, APIRouter
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import OAuth2PasswordRequestForm, OAuth2PasswordBearer

router = APIRouter()

import logging

# Database and ORM imports
from sqlalchemy import select
from sqlalchemy.orm import Session
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker
from models import Base, User, Goal, Budget, BudgetCategory, Report, Transaction, TransactionCategory, Notification, NotificationType
from database import engine, get_db

from passlib.context import CryptContext

# JWT and Password Security imports
from jose import JWTError, jwt

# Standard libraries
from datetime import datetime, timedelta
from typing import List
from sqlalchemy.exc import IntegrityError

# Schema imports
from schemas import (
    UserCreate, UserRead,
    BudgetCreate, BudgetRead,
    GoalsCreate, GoalsRead,
    ReportCreate, ReportRead,
    TransactionCreate, TransactionRead,
    NotificationCreate, NotificationRead,
    TokenResponse, LoginRequest,
    BudgetCategoryRead, TransactionCategoryRead,
    ReportTypeRead, NotificationTypeRead,
    UsernameRecoveryRequest
)

import secrets

# CRUD operations for each entity
from crud import (
    get_user, create_user, get_user_by_username, get_users,
    get_budget, create_budget, get_budgets,
    get_goal, create_goal, get_goals,
    get_report, create_report, get_reports,
    get_transaction, create_transaction, get_transactions,
    get_notification, create_notification, get_notifications,
    get_user_by_email,
)

# Uvicorn server import
import uvicorn

# Import for utils
from utils import verify_password

# JWT Token settings and password hashing context
SECRET_KEY = secrets.token_urlsafe(32)
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

# OAuth2 token scheme definition
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="login")

# Create database tables if they do not exist
Base.metadata.create_all(bind=engine)

# Initialize the FastAPI app
app = FastAPI()

# Middleware for CORS settings
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins; change to specific URLs for production
    allow_credentials=True,
    allow_methods=["*"],  # Allow all HTTP methods
    allow_headers=["*"]   # Allow all headers
)
# Custom HTTP Exception Handler
from fastapi.responses import JSONResponse

@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc):
    logger.error(f"HTTP Exception occurred: {exc.detail}")  # Log the error details
    return JSONResponse(
        status_code=exc.status_code,
        content={"detail": exc.detail},
    )
    
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def hash_password(plain_password: str) -> str:
    return pwd_context.hash(plain_password)

# Utility Functions for Authentication
def authenticate_user(db: Session, username: str, password: str):
    user = db.query(User).filter(User.username == username).first()
    logger.info(f"Retrieved user: {user}")  # Log the retrieved user object

    if user:
        is_valid_password = verify_password(password, user.password)
        logger.info(f"Password verification result: {is_valid_password}")  # Log the password verification result
        
        if is_valid_password:
            return user
            
    return None

def create_access_token(data: dict, expires_delta: timedelta = None) -> str:
    to_encode = data.copy()
    
    # Set expiration time
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES))
    to_encode.update({"exp": expire})
    
    if 'id' in data:
        to_encode["id"] = data['id']
    else:
        raise ValueError("User ID must be provided in the token payload")

    # Encode the JWT
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    
    try:
        logger.info(f"Decoding token: {token}")
        # Decode the JWT token
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: int = payload.get("id")
        logger.info(f"Extracted user ID from token: {user_id}")

        if user_id is None:
            logger.error("User ID not found in token payload")
            raise credentials_exception
        
        # Fetch the user from the database using the user_id
        user = db.query(User).filter(User.id == user_id).first()
        if user is None:
            logger.error(f"User not found for ID: {user_id}")
            raise credentials_exception
        
    except JWTError as e:
        # Log the JWT error for debugging purposes
        logger.error(f"JWT error: {e}")
        raise credentials_exception
    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}")
        raise credentials_exception

    logger.info(f"Authenticated user: {user}")
    return user  # Return the user object

# Create an asynchronous engine
DATABASE_URL = "mysql+aiomysql://root:your_password@localhost/personal_finance_db"
engine = create_async_engine(DATABASE_URL, echo=True)

# Create an asynchronous session factory
async_session = sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,
)

# Use session in an async context
async def get_session():
    async with async_session() as session:
        yield session

# Email sending function (placeholder)
def send_email(to_email: str, subject: str, body: str):
    import smtplib
    from email.mime.text import MIMEText

    from_email = "your_email@example.com"
    password = "your_email_password"

    msg = MIMEText(body)
    msg["Subject"] = subject
    msg["From"] = from_email
    msg["To"] = to_email

    with smtplib.SMTP("smtp.example.com", 587) as server:
        server.starttls()
        server.login(from_email, password)
        server.sendmail(from_email, to_email, msg.as_string())

# Username Recovery Endpoint
@app.post("/recover-username/")
async def recover_username(request: UsernameRecoveryRequest, db: Session = Depends(get_db)):
    user = get_user_by_email(db, request.email)
    if user is None:
        raise HTTPException(status_code=404, detail="Email not found")

    username = user.username  
    email_subject = "Username Recovery"
    email_body = f"Your username is: {username}"

    send_email(request.email, email_subject, email_body)
    return {"message": "An email with your username has been sent."}

from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

app = FastAPI()

# User endpoints
@app.post("/users/")
async def create_new_user(user: UserCreate, db: Session = Depends(get_db)):
    existing_user = get_user_by_email(db, user.email)
    if existing_user:
        raise HTTPException(status_code=400, detail="Email already registered")
    
    else:
    # Proceed with user creation
     new_user = User(
        username=user.username,
        email=user.email,
        password=user.password,
        first_name=user.first_name,
        last_name=user.last_name,
        phone_number=user.phone_number
    )
    db.add(new_user)
    db.commit()
    return new_user


@app.get("/users/", response_model=List[UserRead])
def read_users(skip: int = 0, limit: int = 250, db: Session = Depends(get_db)):
    users = get_users(db, skip=skip, limit=limit)
    return users

@app.get("/user/{user_id}", response_model=UserRead)
def read_user(user_id: int, db: Session = Depends(get_db)):
    db_user = get_user(db, user_id=user_id)  # No need for 'await'
    if db_user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return db_user

@app.post("/login/", response_model=TokenResponse)
async def login(
    login_request: LoginRequest = Body(...),
    db: Session = Depends(get_db)
):
    user = authenticate_user(db, login_request.username, login_request.password)

    if not user:
        logger.info(f"Failed login attempt for username: {login_request.username}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": user.username, "id": user.id},
        expires_delta=access_token_expires
    )

    logger.info(f"User {user.username} logged in successfully.")
    
    return TokenResponse(
        access_token=access_token,
        token_type="bearer",
        id=user.id,
        username=user.username,
        email=user.email
    )
    
# Budget endpoints
@app.post("/budgets/", response_model=BudgetRead)
def create_new_budget(
    budget: BudgetCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    logger.info(f"Creating budget for user_id: {current_user.id}")
    db_budget = create_budget(db=db, budget=budget, user_id=current_user.id)

    return BudgetRead(
        id = db_budget.id,
        amount = db_budget.amount,
        start_date = db_budget.start_date,
        end_date = db_budget.end_date
    )

@app.get("/budgets/", response_model=List[BudgetRead])
def read_budgets(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    return get_budgets(db, skip=skip, limit=limit)

@app.get("/budgets/{budget_id}", response_model=BudgetRead)
def read_budget(budget_id: int, db: Session = Depends(get_db)):
    db_budget = get_budget(db, budget_id=budget_id)
    if db_budget is None:
        raise HTTPException(status_code=404, detail="budget not found")
    return db_budget

@app.put("/budgets/{budget_id}", response_model=BudgetRead)
def update_budget(budget_id: int, budget: BudgetCreate, db: Session = Depends(get_db)):
    db_budget = db.query(Budget).filter(Budget.id == budget_id).first()
    if not db_budget:
        raise HTTPException(status_code=404, detail="Budget not found")
    
    db_budget.budget_category_id = budget.budget_category_id
    db_budget.amount = budget.amount
    db_budget.start_date = budget.start_date
    db_budget.end_date = budget.end_date

    db.commit()
    db.refresh(db_budget)
    return db_budget

@app.delete("/budgets/{budget_id}")
def delete_budget(budget_id: int, db: Session = Depends(get_db)):
    logger.info(f"Deleting budget with ID: {budget_id}")
    db_budget = db.query(Budget).filter(Budget.id == budget_id).first()
    if not db_budget:
        logger.warning(f"Budget with ID: {budget_id} not found")
        raise HTTPException(status_code=404, detail="Budget not found")
    
    db.delete(db_budget)
    db.commit()
    logger.info(f"Budget deleted successfully: {budget_id}")

    return {"detail": "Budget deleted successfully"}

@app.get("/budget/categories/", response_model=List[BudgetCategoryRead])
def get_budget_categories(db: Session = Depends(get_db)):
    logger.info("Fetching budget categories")
    categories = db.query(BudgetCategory).all()
    logger.info(f"Budget categories retrieved: {len(categories)} found")

    return categories

# Goals endpoints
@app.post("/goals/", response_model=GoalsRead)
def create_new_goal(
    goal: GoalsCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    logger.info(f"Creating goal for user_id: {current_user.id}")
    db_goal = create_goal(db=db, goal=goal, user_id=current_user.id)

    # Return the created goal as GoalsRead model
    return GoalsRead(
        id = db_goal.id,
        name = db_goal.name,
        targetAmount = db_goal.target_amount,
        currentAmount = db_goal.current_amount,
        deadline = db_goal.deadline,
        description = db_goal.description
    )

@app.get("/goals/", response_model=List[GoalsRead])
def read_goals(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    return get_goals(db, skip=skip, limit=limit)

@app.get("/goal/{goal_id}", response_model=GoalsRead)
def read_goal(goal_id: int, db: Session = Depends(get_db)):
    db_goal = get_goal(db, goal_id=goal_id)
    if db_goal is None:
        raise HTTPException(status_code=404, detail="Goal not found")
    return db_goal

@app.put("/goals/{goal_id}", response_model=GoalsRead)
def update_goal(goal_id: int, goal: GoalsCreate, db: Session = Depends(get_db)):
    db_goal = db.query(Goal).filter(Goal.id == goal_id).first()
    if not db_goal:
        raise HTTPException(status_code=404, detail="Goal not found")
    
    db_goal.name = goal.name
    db_goal.target_amount = goal.target_amount
    db_goal.current_amount = goal.current_amount
    db_goal.deadline = goal.deadline
    db_goal.description = goal.description

    db.commit()
    db.refresh(db_goal)
    return db_goal

@app.delete("/goals/{goal_id}")
def delete_goal(goal_id: int, db: Session = Depends(get_db)):
    db_goal = db.query(Goal).filter(Goal.id == goal_id).first()
    if not db_goal:
        raise HTTPException(status_code=404, detail="Goal not found")
    
    db.delete(db_goal)
    db.commit()
    return {"detail": "Goal deleted successfully"}


# Report endpoints
@app.post("/reports/", response_model=ReportRead)
def create_new_report(
    report: ReportCreate, 
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
    ):
    
    logger.info(f"Creating report for user_id: {current_user.id}")
    db_report = create_report(db=db, report=report, user_id=current_user.id)
    
    return ReportRead(
        id = db_report.id,
        report_type_id = db_report.report_type_id,
        data = db_report.data,
        generated_at = db_report.generated_at       
    )

@app.get("/reports/", response_model=List[ReportRead])
def read_reports(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    return get_reports(db, skip=skip, limit=limit)

@app.get("/report/{report_id}", response_model=ReportRead)
def read_report(report_id: int, db: Session = Depends(get_db)):
    db_report = get_report(db, report_id=report_id)
    if db_report is None:
        raise HTTPException(status_code=404, detail="Report not found")
    return db_report

@app.put("/reports/{report_id}", response_model=ReportRead)
def update_report(report_id: int, report: ReportCreate, db: Session = Depends(get_db)):
    db_report = db.query(Report).filter(Report.id == report_id).first()
    if not db_report:
        raise HTTPException(status_code=404, detail="Report not found")
    
    db_report.report_type_id = report.report_type_id,
    db_report.data = report.data,
    
    db.commit()
    db.refresh(db_report)
    return db_report

@app.delete("/reports/{report_id}")
def delete_report(report_id: int, db: Session = Depends(get_db)):
    logger.info(f"Deleting report with ID: {report_id}")
    db_report = db.query(Report).filter(report_id == report_id).first()
    if not db_report:
        logger.warning(f"Report with ID: {report_id} not found")
        raise HTTPException(status_code=404, detail="Report not found")
    
    db.delete(db_report)
    db.commit()
    logger.info(f"Report deleted successfully: {report_id}")
    
    return {"Detail": "Report deketed successfully"}
    
# Transaction endpoints
@app.post("/transactions/", response_model=TransactionRead)
def create_new_transaction(
    transaction: TransactionCreate, 
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
    ):
    logger.info(f"Creating transaction for user_id: {current_user.id}")
    db_transaction = create_transaction(db=db, transaction=transaction, user_id=current_user.id)
    
    return TransactionRead(
        id = db_transaction.id,
        amount = db_transaction.amount,
        date = db_transaction.description,
        transaction_category_id = db_transaction.transaction_category_id
    )

@app.get("/transactions/", response_model=List[TransactionRead])
def read_transactions(skip: int = 0, limit: int = 10, db: Session = Depends(get_db),current_user: User = Depends(get_current_user)):
    return get_transactions(db=db, user_id=current_user.id, skip=skip, limit=limit)

@app.get("/transaction/{transaction_id}", response_model=TransactionRead)
def read_transaction(transaction_id: int, db: Session = Depends(get_db)):
    db_transaction = get_transaction(db, transaction_id=transaction_id)
    if db_transaction is None:
        raise HTTPException(status_code=404, detail="Transaction not found")
    return db_transaction

@app.put("/transactions/{transaction_id}", response_model=TransactionRead)
def update_transaction(transaction_id: int, transaction: TransactionCreate, db: Session = Depends(get_db)):
    db_transaction = db.query(Transaction).filter(Transaction.id == transaction_id).first()
    if not db_transaction:
        raise HTTPException(status_code=404, detail="Transaction not found")
    
    db_transaction.amount = transaction.amount
    db_transaction.date = transaction.date
    db_transaction.transaction_category_id = transaction.transaction_category_id
    
    db.commit()
    db.refresh(db_transaction)
    return db_transaction

@app.delete("/transaction/{transaction_id}")
def delete_transaction(transaction_id: int, db: Session = Depends(get_db)):
    logger.info(f"Deleting trainsaction with ID: {transaction_id}")
    db_transaction = db.query(Transaction).filter(Transaction.id == transaction_id).first()
    if not db_transaction:
        logger.warning(f"Transaction with ID: {transaction_id} not found")
        raise HTTPException(status_code=404, detail="Transaction not found")
    
    db.delete(db_transaction)
    db.commit()
    logger.info(f"Transaction deleted successfully: {transaction_id}")
    
    return {"detail" : "Transaction deleted successfully"}

@app.get("/transaction/categories/", response_model=List[TransactionCategoryRead])
def get_transaction_categories(db: Session = Depends(get_db)):
    logger.info("Fetching transaction categories")
    categories = db.query(TransactionCategory).all()
    logger.info(f"Transaction categories retrieved: {len(categories)} found")

    return categories

# Notification endpoints
@app.post("/notifications/", response_model=NotificationRead)
def create_new_notification(notification: NotificationCreate, db: Session = Depends(get_db)):
    return create_notification(db=db, notification=notification)

@app.get("/notifications/", response_model=List[NotificationRead])
def read_notifications(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    return get_notifications(db, skip=skip, limit=limit)

@app.get("/notification/{notification_id}", response_model=NotificationRead)
def read_notification(notification_id: int, db: Session = Depends(get_db)):
    db_notification = get_notification(db, notification_id=notification_id)
    if db_notification is None:
        raise HTTPException(status_code=404, detail="Notification not found")
    return db_notification

@app.get("/users/email/{email}", response_model=bool)
def check_user_exists_by_email(email: str, db: Session = Depends(get_db)):
    # Query the database to check if the email already exists
    user = db.query(User).filter(User.email == email).first()
    return user is not None

@app.get("/user/username/{username}", response_model=bool)
def get_user_by_username(username: str, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.username == username).first()
    return user is not None
    
# Entry point to run the server
if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)