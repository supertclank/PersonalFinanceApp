from sqlalchemy.orm import Session
from models import User, Budget, Goal, Report, Transaction, Notification
from schemas import UserCreate, BudgetCreate, GoalsCreate, ReportCreate, TransactionCreate, NotificationCreate, UserResponse
from passlib.context import CryptContext
from sqlalchemy.exc import IntegrityError
import datetime
from utils import hash_password
from fastapi import HTTPException, logger
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
import logging

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

logger = logging.getLogger("uvicorn.error")

async def get_user(db: AsyncSession, username: str):
    result = await db.execute(select(User).filter(User.username == username))
    return result.scalar_one_or_none()

def get_user_by_username(db: Session, username: str):
    return db.query(User).filter(User.username == username).first()

async def get_users(db: AsyncSession, skip: int = 0, limit: int = 255):
    result = await db.execute(select(User).offset(skip).limit(limit))
    return result.scalars().all()

from sqlalchemy.exc import IntegrityError, SQLAlchemyError

def create_user(db: Session, user: UserCreate) -> UserResponse:
    try:
        logger.info(f"Creating user with username: {user.username} and email: {user.email}")

        # Hash the password before storing it in the database
        hashed_password = hash_password(user.password)
        logger.info(f"Hashed password for user: {user.username}")

        # Create a new User instance with the hashed password and profile fields
        db_user = User(
            username=user.username,
            email=user.email,
            password=hashed_password,
            first_name=user.first_name,
            last_name=user.last_name,
            phone_number=user.phone_number
        )
        
        # Add the new user to the session and commit the transaction
        db.add(db_user)
        db.commit()
        logger.info("User created successfully, committing to database.")

        # Refresh the instance to get the newly assigned ID and other defaults
        db.refresh(db_user)
        
        # Return the created user information as a UserResponse
        return UserResponse(id=db_user.id, username=db_user.username, email=db_user.email)

    except IntegrityError as e:
        logger.error(f"Integrity error occurred: {e}")  # Log specific integrity error
        db.rollback()  # Rollback the transaction if there was an error
        raise HTTPException(status_code=400, detail="User already exists or invalid data")
    except SQLAlchemyError as e:
        logger.error(f"SQLAlchemy error occurred: {e}")  # Log SQLAlchemy error
        db.rollback()  # Rollback the transaction if there was an unexpected error
        raise HTTPException(status_code=500, detail="Database error occurred")
    except Exception as e:
        logger.error(f"Unexpected error occurred while creating user: {e}", exc_info=True)  # Log the complete traceback
        db.rollback()  # Rollback the transaction if there was an unexpected error
        raise HTTPException(status_code=500, detail="Database error occurred")

def get_user_by_email(db: Session, email: str):
    return db.query(User).filter(User.email == email).first()

async def get_budget(db: AsyncSession, budget_id: int):
    result = await db.execute(select(Budget).filter(Budget.id == budget_id))
    return result.scalar_one_or_none()

async def get_budgets(db: AsyncSession, skip: int = 0, limit: int = 10):
    result = await db.execute(select(Budget).offset(skip).limit(limit))
    return result.scalars().all()

async def create_budget(db: AsyncSession, budget: BudgetCreate):
    db_budget = Budget(
        user_id=budget.userId,
        budget_category_id=budget.budgetCategoryId,
        amount=budget.amount,
        start_date=budget.startDate,
        end_date=budget.endDate
    )
    db.add(db_budget)
    await db.commit()
    await db.refresh(db_budget)
    return db_budget

async def get_goal(db: AsyncSession, goal_id: int):
    result = await db.execute(select(Goal).filter(Goal.id == goal_id))
    return result.scalar_one_or_none()

async def get_goals(db: AsyncSession, skip: int = 0, limit: int = 10):
    result = await db.execute(select(Goal).offset(skip).limit(limit))
    return result.scalars().all()

async def create_goal(db: AsyncSession, goal: GoalsCreate):
    db_goal = Goal(
        user_id=goal.userId,
        name=goal.name,
        target_amount=goal.targetAmount,
        current_amount=goal.currentAmount,
        deadline=goal.deadline,
        description=goal.description
    )
    db.add(db_goal)
    await db.commit()
    await db.refresh(db_goal)
    return db_goal

async def get_report(db: AsyncSession, report_id: int):
    result = await db.execute(select(Report).filter(Report.id == report_id))
    return result.scalar_one_or_none()

async def get_reports(db: AsyncSession, skip: int = 0, limit: int = 10):
    result = await db.execute(select(Report).offset(skip).limit(limit))
    return result.scalars().all()

async def create_report(db: AsyncSession, report: ReportCreate):
    db_report = Report(
        user_id=report.userId,
        report_type_id=report.reportTypeId,
        data=report.data
    )
    db.add(db_report)
    await db.commit()
    await db.refresh(db_report)
    return db_report

async def get_transaction(db: AsyncSession, transaction_id: int):
    result = await db.execute(select(Transaction).filter(Transaction.id == transaction_id))
    return result.scalar_one_or_none()

async def get_transactions(db: AsyncSession, skip: int = 0, limit: int = 10):
    result = await db.execute(select(Transaction).offset(skip).limit(limit))
    return result.scalars().all()

async def create_transaction(db: AsyncSession, transaction: TransactionCreate):
    db_transaction = Transaction(
        user_id=transaction.userId,
        amount=transaction.amount,
        transaction_category_id=transaction.categoryId,
        date=transaction.transactionDate,
        description=transaction.description
    )
    db.add(db_transaction)
    await db.commit()
    await db.refresh(db_transaction)
    return db_transaction

async def get_notification(db: AsyncSession, notification_id: int):
    result = await db.execute(select(Notification).filter(Notification.id == notification_id))
    return result.scalar_one_or_none()

async def get_notifications(db: AsyncSession, skip: int = 0, limit: int = 10):
    result = await db.execute(select(Notification).offset(skip).limit(limit))
    return result.scalars().all()

async def create_notification(db: AsyncSession, notification: NotificationCreate):
    db_notification = Notification(
        user_id=notification.userId,
        message=notification.message,
        created_at=notification.date or datetime.datetime.utcnow()
    )
    db.add(db_notification)
    await db.commit()
    await db.refresh(db_notification)
    return db_notification
