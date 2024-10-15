from sqlalchemy.orm import Session
from models import User, Profile, Budget, Goals, Report, Transaction, Notification
from schemas import UserCreate, ProfileCreate, BudgetCreate, GoalsCreate, ReportCreate, TransactionCreate, NotificationCreate, UserResponse
from passlib.context import CryptContext
from sqlalchemy.exc import IntegrityError
import datetime
from utils import hash_password
from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

async def get_user(db: AsyncSession, username: str):
    return await db.execute(select(User).filter(User.username == username)).scalar_one_or_none()


def get_users(db: Session, skip: int = 0, limit: int = 255):
    return db.query(User).offset(skip).limit(limit).all()

async def create_user(db: AsyncSession, user: UserCreate) -> UserResponse:
    try:
        hashed_password = hash_password(user.password)
        db_user = User(username=user.username, email=user.email, password=hashed_password)
        db.add(db_user)
        await db.commit()
        await db.refresh(db_user)
        return UserResponse(id=db_user.userId, username=db_user.username, email=db_user.email)
    except IntegrityError:
        await db.rollback()
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Username or email already exists")


def get_user_by_username(db: Session, username: str):
    return db.query(User).filter(User.username == username).first()

def get_user_by_email(db: Session, email: str):
    return db.query(User).filter(User.email == email).first()

def get_profile(db: Session, profile_id: int):
    return db.query(Profile).filter(Profile.profileId == profile_id).first()

def get_profiles(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Profile).offset(skip).limit(limit).all()

def create_profile(db: Session, profile: ProfileCreate):
    db_profile = Profile(
        userId=profile.userId,
        firstName=profile.firstName,
        lastName=profile.lastName, 
        phoneNumber=profile.phoneNumber
    )
    db.add(db_profile)
    db.commit()
    db.refresh(db_profile)
    return db_profile

def get_budget(db: Session, budget_id: int):
    return db.query(Budget).filter(Budget.budgetId == budget_id).first()

def get_budgets(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Budget).offset(skip).limit(limit).all()

def create_budget(db: Session, budget: BudgetCreate):
    db_budget = Budget(
        userId=budget.userId,
        budgetCategoryId=budget.budgetCategoryId,
        amount=budget.amount,
        startDate=budget.startDate,
        endDate=budget.endDate
    )
    db.add(db_budget)
    db.commit()
    db.refresh(db_budget)
    return db_budget

def get_goal(db: Session, goal_id: int):
    return db.query(Goals).filter(Goals.goalId == goal_id).first()

def get_goals(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Goals).offset(skip).limit(limit).all()

def create_goal(db: Session, goal: GoalsCreate):
    db_goal = Goals(
        userId=goal.userId,
        name=goal.name,
        targetAmount=goal.targetAmount,
        currentAmount=goal.currentAmount,
        deadline=goal.deadline,
        description=goal.description
    )
    db.add(db_goal)
    db.commit()
    db.refresh(db_goal)
    return db_goal

def get_report(db: Session, report_id: int):
    return db.query(Report).filter(Report.reportId == report_id).first()

def get_reports(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Report).offset(skip).limit(limit).all()

def create_report(db: Session, report: ReportCreate):
    db_report = Report(
        userId=report.userId,
        reportTypeId=report.reportTypeId,
        data=report.data
    )
    db.add(db_report)
    db.commit()
    db.refresh(db_report)
    return db_report

def get_transaction(db: Session, transaction_id: int):
    return db.query(Transaction).filter(Transaction.transactionId == transaction_id).first()

def get_transactions(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Transaction).offset(skip).limit(limit).all()

def create_transaction(db: Session, transaction: TransactionCreate):
    db_transaction = Transaction(
        userId=transaction.userId,
        amount=transaction.amount,
        transactionDate=transaction.transactionDate,
        description=transaction.description,
        categoryId=transaction.categoryId
    )
    db.add(db_transaction)
    db.commit()
    db.refresh(db_transaction)
    return db_transaction

def get_notification(db: Session, notification_id: int):
    return db.query(Notification).filter(Notification.notificationId == notification_id).first()

def get_notifications(db: Session, skip: int = 0, limit: int = 10):
    return db.query(Notification).offset(skip).limit(limit).all()

def create_notification(db: Session, notification: NotificationCreate):
    db_notification = Notification(
        userId=notification.userId,
        message=notification.message,
        date=notification.date or datetime.datetime.now()
    )
    db.add(db_notification)
    db.commit()
    db.refresh(db_notification)
    return db_notification
