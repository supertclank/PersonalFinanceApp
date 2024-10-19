from sqlalchemy.orm import Session
from models import User, Budget, Goal, Report, Transaction, Notification
from schemas import UserCreate, BudgetCreate, GoalsCreate, ReportCreate, TransactionCreate, NotificationCreate, UserResponse
from passlib.context import CryptContext
from sqlalchemy.exc import IntegrityError
import datetime
from utils import hash_password
from fastapi import HTTPException, logger
import logging

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
logger = logging.getLogger("uvicorn.error")

def get_user(db: Session, username: str):
    return db.query(User).filter(User.username == username).first()

def get_users(db: Session, skip: int = 0, limit: int = 255):
    return db.query(User).offset(skip).limit(limit).all()

def create_user(db: Session, user: UserCreate) -> UserResponse:
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

def get_user_by_email(db: Session, email: str):
    return db.query(User).filter(User.email == email).first()

def get_budget(db: Session, budget_id: int):
    return db.query(Budget).filter(Budget.id == budget_id).first()

def get_budgets(db: Session, user_id: int, skip: int = 0, limit: int = 10):
    return db.query(Budget).filter(Budget.user_id == user_id).offset(skip).limit(limit).all()

def create_budget(db: Session, budget: BudgetCreate):
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

def get_goal(db: Session, goal_id: int):
    return db.query(Goal).filter(Goal.id == goal_id).first()

def get_goals(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Goal).offset(skip).limit(limit).all()

def create_goal(db: Session, goal: GoalsCreate):
    db_goal = Goal(
        user_id=goal.userId,
        name=goal.name,
        target_amount=goal.targetAmount,
        current_amount=goal.currentAmount,
        deadline=goal.deadline,
        description=goal.description
    )
    db.add(db_goal)
    db.commit()
    db.refresh(db_goal)
    return db_goal

def get_report(db: Session, report_id: int):
    return db.query(Report).filter(Report.id == report_id).first()

def get_reports(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Report).offset(skip).limit(limit).all()

def create_report(db: Session, report: ReportCreate):
    db_report = Report(
        user_id=report.userId,
        report_type_id=report.reportTypeId,
        data=report.data
    )
    db.add(db_report)
    db.commit()
    db.refresh(db_report)
    return db_report

def get_transaction(db: Session, transaction_id: int):
    return db.query(Transaction).filter(Transaction.id == transaction_id).first()

def get_transactions(db: Session, user_id: int, skip: int = 0, limit: int = 10):
    return db.query(Transaction).filter(Transaction.user_id == user_id).offset(skip).limit(limit).all()

def create_transaction(db: Session, transaction: TransactionCreate):
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

def get_notification(db: Session, notification_id: int):
    return db.query(Notification).filter(Notification.id == notification_id).first()

def get_notifications(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Notification).offset(skip).limit(limit).all()

def create_notification(db: Session, notification: NotificationCreate):
    db_notification = Notification(
        user_id=notification.userId,
        message=notification.message,
        created_at=notification.date or datetime.datetime.utcnow()
    )
    db.add(db_notification)
    db.commit()
    db.refresh(db_notification)
    return db_notification
