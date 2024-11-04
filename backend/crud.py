from sqlalchemy.orm import Session
from models import User, Budget, Goal, Report, Transaction, Notification
from schemas import UserCreate, BudgetCreate, GoalsCreate, ReportCreate, TransactionCreate, TransactionRead, NotificationCreate, NotificationRead, UserResponse, GoalsRead, BudgetRead, ReportRead
from passlib.context import CryptContext
from sqlalchemy.exc import IntegrityError
import datetime
from utils import hash_password
from fastapi import HTTPException
import logging

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
logger = logging.getLogger("uvicorn.error")

def get_user(db: Session, user_id: int):
    return db.query(User).filter(User.id == user_id).first()

def get_users(db: Session, skip: int = 0, limit: int = 250):
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

def get_user_by_username(db: Session, username: str):
    return db.query(User).filter(User.username == username).first()

def get_budget(db: Session, budget_id: int):
    return db.query(Budget).filter(Budget.id == budget_id).first()

def get_budgets(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Budget).offset(skip).limit(limit).all()

def create_budget(db: Session, budget: BudgetCreate):
    db_budget = Budget(
        user_id=budget.user_id,
        budget_category_id=budget.budget_category_id,
        amount=budget.amount,
        start_date=budget.start_date,
        end_date=budget.end_date
    )
    db.add(db_budget)
    db.commit()
    db.refresh(db_budget)
    
    return BudgetRead(
        id=db_budget.id,
        amount=db_budget.amount,
        start_date=db_budget.start_date,
        end_date=db_budget.end_date
    )

def get_goal(db: Session, goal_id: int):
    return db.query(Goal).filter(Goal.id == goal_id).first()

def get_goals(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Goal).offset(skip).limit(limit).all()

def create_goal(db: Session, goal: GoalsCreate, user_id: int) -> GoalsRead:
    db_goal = Goal(
        user_id=user_id,
        name=goal.name,
        target_amount=goal.target_amount,
        current_amount=goal.current_amount,
        deadline=goal.deadline,
        description=goal.description
    )
    db.add(db_goal)
    db.commit()
    db.refresh(db_goal)

    return GoalsRead(
        id=db_goal.id,
        name=db_goal.name,
        target_amount=db_goal.target_amount,
        current_amount=db_goal.current_amount,
        deadline=db_goal.deadline,
        description=db_goal.description
    )
    
def get_report(db: Session, report_id: int):
    return db.query(Report).filter(Report.id == report_id).first()

def get_reports(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Report).offset(skip).limit(limit).all()

def create_report(db: Session, report: ReportCreate, user_id: int) -> ReportRead:
    db_report = Report(
        user_id = db_report.user_id,
        report_type_id = db_report.report_type_id,
        generated_at = db_report.generated_at,
        data = db_report.data
    )
    db.add(db_report)
    db.commit()
    db.refresh(db_report)

    return ReportRead(
        id=db_report.id,
        report_type_id = db_report.report_type_id,
        generated_at = db_report.generated_at,
        data = db_report.data,
    )

def get_transaction(db: Session, transaction_id: int):
    return db.query(Transaction).filter(Transaction.id == transaction_id).first()

def get_transactions(db: Session, user_id: int, skip: int = 0, limit: int = 10):
    return db.query(Transaction).filter(Transaction.user_id == user_id).offset(skip).limit(limit).all()

def create_transaction(db: Session, transaction: TransactionCreate, user_id: int) -> TransactionRead:
    db_transaction = Transaction(
        user_id = user_id,
        amount = Transaction.amount,
        transaction_category_id = Transaction.transaction_category_id,
        date = Transaction.date,
        description = Transaction.description
    )
    db.add(db_transaction)
    db.commit()
    db.refresh(db_transaction)
    
    return TransactionRead(
        id = db_transaction.id,
        amount = db_transaction.amount,
        date = db_transaction.date,
        description = db_transaction.description,
        transaction_category_id = db_transaction.transaction_category_id,
    )

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
    
    return NotificationRead(
        id = db_notification.id,
        message = db_notification.message,
        isRead = db_notification.is_read,
        date = db_notification.date,
        notification_type_id = db_notification.notification_type_id
    ) 
