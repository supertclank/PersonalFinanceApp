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
from models import Base, User
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
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        if username is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    user = get_user_by_username(db, username=username)
    if user is None:
        raise credentials_exception
    return user

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
async def read_users(skip: int = 0, limit: int = 250, db: Session = Depends(get_db)):
    users = await get_users(db, skip=skip, limit=limit)
    return users


@app.get("/user/{user_id}", response_model=UserRead)
async def read_user(user_id: int, db: Session = Depends(get_db)):
    db_user = await get_user(db, user_id=user_id)  # Await this call
    if db_user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return db_user  # Return the user information if found

# Login endpoint
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
        data={"sub": user.username}, expires_delta=access_token_expires
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
async def create_new_budget(budget: BudgetCreate, db: AsyncSession = Depends(get_db)):
    return await create_budget(db=db, budget=budget)

@app.get("/budgets/", response_model=List[BudgetRead])
async def read_budgets(skip: int = 0, limit: int = 10, db: AsyncSession = Depends(get_db)):
    return await get_budgets(db, skip=skip, limit=limit)

@app.get("/budget/{budget_id}", response_model=BudgetRead)
async def read_budget(budget_id: int, db: AsyncSession = Depends(get_db)):
    db_budget = await get_budget(db, budget_id=budget_id)
    if db_budget is None:
        raise HTTPException(status_code=404, detail="Budget not found")
    return db_budget

# Goals endpoints
@app.post("/goals/", response_model=GoalsRead)
async def create_new_goal(goal: GoalsCreate, db: AsyncSession = Depends(get_db)):
    return await create_goal(db=db, goal=goal)

@app.get("/goals/", response_model=List[GoalsRead])
async def read_goals(skip: int = 0, limit: int = 10, db: AsyncSession = Depends(get_db)):
    return await get_goals(db, skip=skip, limit=limit)

@app.get("/goal/{goal_id}", response_model=GoalsRead)
async def read_goal(goal_id: int, db: AsyncSession = Depends(get_db)):
    db_goal = await get_goal(db, goal_id=goal_id)
    if db_goal is None:
        raise HTTPException(status_code=404, detail="Goal not found")
    return db_goal

# Report endpoints
@app.post("/reports/", response_model=ReportRead)
async def create_new_report(report: ReportCreate, db: AsyncSession = Depends(get_db)):
    return await create_report(db=db, report=report)

@app.get("/reports/", response_model=List[ReportRead])
async def read_reports(skip: int = 0, limit: int = 10, db: AsyncSession = Depends(get_db)):
    return await get_reports(db, skip=skip, limit=limit)

@app.get("/report/{report_id}", response_model=ReportRead)
async def read_report(report_id: int, db: AsyncSession = Depends(get_db)):
    db_report = await get_report(db, report_id=report_id)
    if db_report is None:
        raise HTTPException(status_code=404, detail="Report not found")
    return db_report

# Transaction endpoints
@app.post("/transactions/", response_model=TransactionRead)
async def create_new_transaction(transaction: TransactionCreate, db: AsyncSession = Depends(get_db)):
    return await create_transaction(db=db, transaction=transaction)

@app.get("/transactions/", response_model=List[TransactionRead])
async def read_transactions(skip: int = 0, limit: int = 10, db: AsyncSession = Depends(get_db)):
    return await get_transactions(db, skip=skip, limit=limit)

@app.get("/transaction/{transaction_id}", response_model=TransactionRead)
async def read_transaction(transaction_id: int, db: AsyncSession = Depends(get_db)):
    db_transaction = await get_transaction(db, transaction_id=transaction_id)
    if db_transaction is None:
        raise HTTPException(status_code=404, detail="Transaction not found")
    return db_transaction

# Notification endpoints
@app.post("/notifications/", response_model=NotificationRead)
async def create_new_notification(notification: NotificationCreate, db: AsyncSession = Depends(get_db)):
    return await create_notification(db=db, notification=notification)

@app.get("/notifications/", response_model=List[NotificationRead])
async def read_notifications(skip: int = 0, limit: int = 10, db: AsyncSession = Depends(get_db)):
    return await get_notifications(db, skip=skip, limit=limit)

@app.get("/notification/{notification_id}", response_model=NotificationRead)
async def read_notification(notification_id: int, db: AsyncSession = Depends(get_db)):
    db_notification = await get_notification(db, notification_id=notification_id)
    if db_notification is None:
        raise HTTPException(status_code=404, detail="Notification not found")
    return db_notification

@app.get("/users/email/{email}", response_model=bool)
def check_user_exists_by_email(email: str, db: Session = Depends(get_db)):
    # Query the database to check if the email already exists
    user = db.query(User).filter(User.email == email).first()
    return user is not None

# Entry point to run the server
if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)