from pydantic import BaseModel, EmailStr
from typing import Optional
from datetime import datetime
from decimal import Decimal

class UserBase(BaseModel):
    username: str
    email: str

class UserCreate(BaseModel):
    username: str
    email: EmailStr
    password: str 

    class Config:
        orm_mode = True

class UserRead(UserBase):
    id: int

    class Config:
        orm_mode = True


class LoginRequest(BaseModel):
    username: str
    hashed_password: str
    
class UsernameRecoveryRequest(BaseModel):
    email: EmailStr

class ProfileBase(BaseModel):
    firstName: str
    lastName: str
    phoneNumber: str

class ProfileCreate(ProfileBase):
    userId: int

class ProfileRead(ProfileBase):
    profileId: int
    createdAt: datetime

    class Config:
        orm_mode = True

class BudgetBase(BaseModel):
    amount: Decimal
    startDate: datetime
    endDate: datetime

class BudgetCreate(BudgetBase):
    userId: int
    budgetCategoryId: int

class BudgetRead(BudgetBase):
    budgetId: int

    class Config:
        orm_mode = True

class GoalsBase(BaseModel):
    name: str
    targetAmount: Decimal
    currentAmount: Decimal
    deadline: datetime
    description: str

class GoalsCreate(GoalsBase):
    userId: int

class GoalsRead(GoalsBase):
    goalId: int

    class Config:
        orm_mode = True

class ReportBase(BaseModel):
    reportTypeId: int
    data: dict

class ReportCreate(ReportBase):
    userId: int

class ReportRead(ReportBase):
    reportId: int
    generatedAt: datetime

    class Config:
        orm_mode = True

class TransactionBase(BaseModel):
    amount: Decimal
    date: datetime
    description: str

class TransactionCreate(TransactionBase):
    userId: int
    transactionCategoryId: int

class TransactionRead(TransactionBase):
    transactionId: int

    class Config:
        orm_mode = True

class NotificationBase(BaseModel):
    message: str
    isRead: bool

class NotificationCreate(NotificationBase):
    userId: int
    notificationTypeId: int

class NotificationRead(NotificationBase):
    notificationId: int
    createdAt: datetime

    class Config:
        orm_mode = True
