from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List
from models import Base
from schemas import UserCreate, UserRead, ProfileCreate, ProfileRead, BudgetCreate, BudgetRead, GoalsCreate, GoalsRead, ReportCreate, ReportRead, TransactionCreate, TransactionRead, NotificationCreate, NotificationRead
from crud import (
    get_user, create_user, get_users,
    get_profile, create_profile, get_profiles,
    get_budget, create_budget, get_budgets,
    get_goal, create_goal, get_goals,
    get_report, create_report, get_reports,
    get_transaction, create_transaction, get_transactions,
    get_notification, create_notification, get_notifications,
)
from database import engine, get_db

import uvicorn

Base.metadata.create_all(bind=engine)

app = FastAPI()

if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=True)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)

# User endpoints
@app.post("/users/", response_model=UserRead)
def create_new_user(user: UserCreate, db: Session = Depends(get_db)):
    db_user = create_user(db=db, user=user)
    return db_user

@app.get("/users/", response_model=List[UserRead])
def read_users(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    users = get_users(db, skip=skip, limit=limit)
    return users

@app.get("/users/{user_id}", response_model=UserRead)
def read_user(user_id: int, db: Session = Depends(get_db)):
    db_user = get_user(db, user_id=user_id)
    if db_user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return db_user

# Profile endpoints
@app.post("/profiles/", response_model=ProfileRead)
def create_new_profile(profile: ProfileCreate, db: Session = Depends(get_db)):
    db_profile = create_profile(db=db, profile=profile)
    return db_profile

@app.get("/profiles/", response_model=List[ProfileRead])
def read_profiles(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    profiles = get_profiles(db, skip=skip, limit=limit)
    return profiles

@app.get("/profiles/{profile_id}", response_model=ProfileRead)
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

@app.get("/budgets/{budget_id}", response_model=BudgetRead)
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

@app.get("/goals/{goal_id}", response_model=GoalsRead)
def read_goal(goal_id: int, db: Session = Depends(get_db)):
    db_goal = get_goal(db, goal_id=goal_id)
    if db_goal is None:
        raise HTTPException(status_code=404, detail="Goal not found")
    return db_goal

# Report endpoints
@app.post("/reports/", response_model=ReportRead)
def create_new_report(report: ReportCreate, db: Session = Depends(get_db)):
    db_report = create_report(db=db, report=report)
    return db_report

@app.get("/reports/", response_model=List[ReportRead])
def read_reports(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    reports = get_reports(db, skip=skip, limit=limit)
    return reports

@app.get("/reports/{report_id}", response_model=ReportRead)
def read_report(report_id: int, db: Session = Depends(get_db)):
    db_report = get_report(db, report_id=report_id)
    if db_report is None:
        raise HTTPException(status_code=404, detail="Report not found")
    return db_report

# Transaction endpoints
@app.post("/transactions/", response_model=TransactionRead)
def create_new_transaction(transaction: TransactionCreate, db: Session = Depends(get_db)):
    db_transaction = create_transaction(db=db, transaction=transaction)
    return db_transaction

@app.get("/transactions/", response_model=List[TransactionRead])
def read_transactions(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    transactions = get_transactions(db, skip=skip, limit=limit)
    return transactions

@app.get("/transactions/{transaction_id}", response_model=TransactionRead)
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

@app.get("/notifications/{notification_id}", response_model=NotificationRead)
def read_notification(notification_id: int, db: Session = Depends(get_db)):
    db_notification = get_notification(db, notification_id=notification_id)
    if db_notification is None:
        raise HTTPException(status_code=404, detail="Notification not found")
    return db_notification
