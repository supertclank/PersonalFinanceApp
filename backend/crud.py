from sqlalchemy.ext.asyncio import AsyncSession
from models import User, Budget, Goal, Report, Transaction, Notification
from schemas import UserCreate, BudgetCreate, GoalsCreate, ReportCreate, TransactionCreate, NotificationCreate, UserResponse
from passlib.context import CryptContext
from sqlalchemy.exc import IntegrityError
import datetime
from utils import hash_password
from fastapi import HTTPException
import logging


pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
logger = logging.getLogger("uvicorn.error")

def get_user(db: AsyncSession, username: str):
    return db.query(User).filter(User.username == username).first()

def get_users(db: AsyncSession, skip: int = 0, limit: int = 255):
    return db.query(User).offset(skip).limit(limit).all()

def create_user(db: AsyncSession, user: UserCreate) -> UserResponse:
    try:
        logger.info(f"Creating user with username: {user.username} and email: {user.email}")
        hashed_password = hash_password(user.password)
        logger.info(f"Hashed password for user: {user.username}")

        db_user = User(
            username=user.username,
            email=user.email,
            password=hashed_password,
            first_name=user.first_name,
            last_name=user.last_name,
            phone_number=user.phone_number
        )
        db.add(db_user)
        db.commit()
        logger.info("User created successfully, committing to database.")
        db.refresh(db_user)

        return UserResponse(id=db_user.id, username=db_user.username, email=db_user.email)

    except IntegrityError as e:
        logger.error(f"Integrity error occurred: {e}")
        db.rollback()
        raise HTTPException(status_code=400, detail="User already exists or invalid data")
    except Exception as e:
        logger.error(f"Unexpected error occurred while creating user: {e}", exc_info=True)
        db.rollback()
        raise HTTPException(status_code=500, detail="Database error occurred")

def get_user_by_email(db: AsyncSession, email: str):
    return db.query(User).filter(User.email == email).first()

def get_user_by_username(db: AsyncSession, username:str):
    return db.query(User).filter(User.username == username).first()

def get_budget(db: AsyncSession, budget_id: int):
    return db.query(Budget).filter(Budget.id == budget_id).first()

def get_budgets(db: AsyncSession, user_id: int, skip: int = 0, limit: int = 10):
    return db.query(Budget).filter(Budget.user_id == user_id).offset(skip).limit(limit).all()

def create_budget(db: AsyncSession, budget: BudgetCreate):
    db_budget = Budget(
        user_id=budget.userId,
        budget_category_id=budget.budgetCategoryId,
        amount=budget.amount,
        start_date=budget.startDate,
        end_date=budget.endDate
    )
    db.add(db_budget)
    db.commit()
    db.refresh(db_budget)
    return db_budget

def get_goal(db: AsyncSession, goal_id: int):
    return db.query(Goal).filter(Goal.id == goal_id).first()

def get_goals(db: AsyncSession, skip: int = 0, limit: int = 10):
    return db.query(Goal).offset(skip).limit(limit).all()

async def create_goal(db: AsyncSession, goal: GoalsCreate, user_id: int):
    db_goal = Goal(
        user_id=user_id,
        name=goal.name,
        target_amount=goal.target_amount,
        current_amount=goal.current_amount,
        deadline=goal.deadline,
        description=goal.description
    )
    db.add(db_goal)
    await db.commit()  # Use await for async commit
    await db.refresh(db_goal)  # Use await for async refresh
    return db_goal


def get_report(db: AsyncSession, report_id: int):
    return db.query(Report).filter(Report.id == report_id).first()

def get_reports(db: AsyncSession, skip: int = 0, limit: int = 10):
    return db.query(Report).offset(skip).limit(limit).all()

def create_report(db: AsyncSession, report: ReportCreate):
    db_report = Report(
        user_id=report.userId,
        report_type_id=report.reportTypeId,
        data=report.data
    )
    db.add(db_report)
    db.commit()
    db.refresh(db_report)
    return db_report

def get_transaction(db: AsyncSession, transaction_id: int):
    return db.query(Transaction).filter(Transaction.id == transaction_id).first()

def get_transactions(db: AsyncSession, user_id: int, skip: int = 0, limit: int = 10):
    return db.query(Transaction).filter(Transaction.user_id == user_id).offset(skip).limit(limit).all()

def create_transaction(db: AsyncSession, transaction: TransactionCreate):
    db_transaction = Transaction(
        user_id=transaction.userId,
        amount=transaction.amount,
        transaction_category_id=transaction.categoryId,
        date=transaction.transactionDate,
        description=transaction.description
    )
    db.add(db_transaction)
    db.commit()
    db.refresh(db_transaction)
    return db_transaction

def get_notification(db: AsyncSession, notification_id: int):
    return db.query(Notification).filter(Notification.id == notification_id).first()

def get_notifications(db: AsyncSession, skip: int = 0, limit: int = 10):
    return db.query(Notification).offset(skip).limit(limit).all()

def create_notification(db: AsyncSession, notification: NotificationCreate):
    db_notification = Notification(
        user_id=notification.userId,
        message=notification.message,
        created_at=notification.date or datetime.datetime.utcnow()
    )
    db.add(db_notification)
    db.commit()
    db.refresh(db_notification)
    return db_notification
