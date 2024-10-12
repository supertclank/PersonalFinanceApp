# FastAPI imports
from fastapi import FastAPI, Depends, HTTPException, Security, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import OAuth2PasswordRequestForm, OAuth2PasswordBearer

# Database and ORM imports
from sqlalchemy.orm import Session
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker
from models import Base
from database import engine, get_db

# JWT and Password Security imports
from jose import JWTError, jwt
from passlib.context import CryptContext

# Standard libraries
from datetime import datetime, timedelta
from typing import List

# Schema imports (for request/response models)
from schemas import (
    UserCreate, UserRead,
    ProfileCreate, ProfileRead,
    BudgetCreate, BudgetRead,
    GoalsCreate, GoalsRead,
    ReportCreate, ReportRead,
    TransactionCreate, TransactionRead,
    NotificationCreate, NotificationRead,
    LoginRequest,
    UsernameRecoveryRequest  # New import for username recovery
)

# CRUD operations for each entity
from crud import (
    get_user, create_user, get_user_by_username, get_users,
    get_profile, create_profile, get_profiles,
    get_budget, create_budget, get_budgets,
    get_goal, create_goal, get_goals,
    get_report, create_report, get_reports,
    get_transaction, create_transaction, get_transactions,
    get_notification, create_notification, get_notifications,
    get_user_by_email,
)

# Uvicorn server import (for running the application)
import uvicorn

# Import for utils
from utils import hash_password, pwd_context

# JWT Token settings and password hashing context
SECRET_KEY = "your_secret_key"  
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

# Utility Functions for Authentication
def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verify a plaintext password against a hashed password."""
    return pwd_context.verify(plain_password, hashed_password)

def authenticate_user(db: Session, username: str, password: str):
    """Authenticate a user by username and password."""
    user = get_user_by_username(db, username=username)
    if not user or not verify_password(password, user.hashed_password):
        return False
    return user

def create_access_token(data: dict, expires_delta: timedelta = None) -> str:
    """Create a JWT token for a given set of data."""
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=15))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    """Retrieve the current user based on the provided JWT token."""
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

    # Create the email message
    msg = MIMEText(body)
    msg["Subject"] = subject
    msg["From"] = from_email
    msg["To"] = to_email

    # Send the email
    with smtplib.SMTP("smtp.example.com", 587) as server:
        server.starttls()
        server.login(from_email, password)
        server.sendmail(from_email, to_email, msg.as_string())

# Username Recovery Endpoint
@app.post("/recover-username/")
async def recover_username(request: UsernameRecoveryRequest, db: Session = Depends(get_db)):
    """Recover username by email."""
    user = get_user_by_email(db, request.email)
    if user is None:
        raise HTTPException(status_code=404, detail="Email not found")

    username = user.username  
    email_subject = "Username Recovery"
    email_body = f"Your username is: {username}"

    send_email(request.email, email_subject, email_body)
    return {"message": "An email with your username has been sent."}

# User endpoints
@app.post("/users/", response_model=UserRead)
def create_new_user(user: UserCreate, db: Session = Depends(get_db)):
    # Hash the password
    hashed_password = hash_password(user.password)
    # Create a new user with the hashed password
    db_user = create_user(db=db, user=user.copy(update={"hashed_password": hashed_password}))
    
    # Return UserRead model instead of the entire user object
    return UserRead(id=db_user.id, username=db_user.username, email=db_user.email)

@app.get("/users/", response_model=List[UserRead])
def read_users(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    users = get_users(db, skip=skip, limit=limit)
    return users

@app.get("/user/{user_id}", response_model=UserRead)
def read_user(user_id: int, db: Session = Depends(get_db)):
    db_user = get_user(db, user_id=user_id)
    if db_user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return UserRead(id=db_user.id, username=db_user.username, email=db_user.email)

# Login endpoint
@app.post("/login/")
async def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    user = authenticate_user(db, username=form_data.username, password=form_data.password)  # Plain password
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(data={"sub": user.username}, expires_delta=access_token_expires)
    return {"access_token": access_token, "token_type": "bearer"}

# Profile endpoints
@app.post("/profiles/", response_model=ProfileRead)
def create_new_profile(profile: ProfileCreate, db: Session = Depends(get_db)):
    db_profile = create_profile(db=db, profile=profile)
    return db_profile

@app.get("/profiles/", response_model=List[ProfileRead])
def read_profiles(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    profiles = get_profiles(db, skip=skip, limit=limit)
    return profiles

@app.get("/profile/{profile_id}", response_model=ProfileRead)
def read_profile(profile_id: int, db: Session = Depends(get_db)):
    db_profile = get_profile(db, profile_id=profile_id)
    if db_profile is None:
        raise HTTPException(status_code=404, detail="Profile not found")
    return db_profile

# Budget endpoints
@app.post("/budgets/", response_model=BudgetRead)
def create_new_budget(budget: BudgetCreate, db: Session = Depends(get_db)):
    db_budget = create_budget(db=db, budget=budget)
    return db_budget

@app.get("/budgets/", response_model=List[BudgetRead])
def read_budgets(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    budgets = get_budgets(db, skip=skip, limit=limit)
    return budgets

@app.get("/budget/{budget_id}", response_model=BudgetRead)
def read_budget(budget_id: int, db: Session = Depends(get_db)):
    db_budget = get_budget(db, budget_id=budget_id)
    if db_budget is None:
        raise HTTPException(status_code=404, detail="Budget not found")
    return db_budget

# Goals endpoints
@app.post("/goals/", response_model=GoalsRead)
def create_new_goal(goal: GoalsCreate, db: Session = Depends(get_db)):
    db_goal = create_goal(db=db, goal=goal)
    return db_goal

@app.get("/goals/", response_model=List[GoalsRead])
def read_goals(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    goals = get_goals(db, skip=skip, limit=limit)
    return goals

@app.get("/goal/{goal_id}", response_model=GoalsRead)
def read_goal(goal_id: int, db: Session = Depends(get_db)):
    db_goal = get_goal(db, goal_id=goal_id)
    if db_goal is None:
        raise HTTPException(status_code=404, detail="Goal not found")
    return db_goal

# Reports endpoints
@app.post("/reports/", response_model=ReportRead)
def create_new_report(report: ReportCreate, db: Session = Depends(get_db)):
    db_report = create_report(db=db, report=report)
    return db_report

@app.get("/reports/", response_model=List[ReportRead])
def read_reports(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    reports = get_reports(db, skip=skip, limit=limit)
    return reports

@app.get("/report/{report_id}", response_model=ReportRead)
def read_report(report_id: int, db: Session = Depends(get_db)):
    db_report = get_report(db, report_id=report_id)
    if db_report is None:
        raise HTTPException(status_code=404, detail="Report not found")
    return db_report

# Transactions endpoints
@app.post("/transactions/", response_model=TransactionRead)
def create_new_transaction(transaction: TransactionCreate, db: Session = Depends(get_db)):
    db_transaction = create_transaction(db=db, transaction=transaction)
    return db_transaction

@app.get("/transactions/", response_model=List[TransactionRead])
def read_transactions(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    transactions = get_transactions(db, skip=skip, limit=limit)
    return transactions

@app.get("/transaction/{transaction_id}", response_model=TransactionRead)
def read_transaction(transaction_id: int, db: Session = Depends(get_db)):
    db_transaction = get_transaction(db, transaction_id=transaction_id)
    if db_transaction is None:
        raise HTTPException(status_code=404, detail="Transaction not found")
    return db_transaction

# Notifications endpoints
@app.post("/notifications/", response_model=NotificationRead)
def create_new_notification(notification: NotificationCreate, db: Session = Depends(get_db)):
    db_notification = create_notification(db=db, notification=notification)
    return db_notification

@app.get("/notifications/", response_model=List[NotificationRead])
def read_notifications(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    notifications = get_notifications(db, skip=skip, limit=limit)
    return notifications

@app.get("/notification/{notification_id}", response_model=NotificationRead)
def read_notification(notification_id: int, db: Session = Depends(get_db)):
    db_notification = get_notification(db, notification_id=notification_id)
    if db_notification is None:
        raise HTTPException(status_code=404, detail="Notification not found")
    return db_notification

# Run the application
if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)
