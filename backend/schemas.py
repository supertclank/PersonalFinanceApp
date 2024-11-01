from pydantic import BaseModel, EmailStr
from datetime import datetime, date
from typing import Optional

# User schemas
class UserResponse(BaseModel):
    id: int
    username: str
    email: str
    
    class Config:
        orm_mode = True

class UserCreate(BaseModel):
    username: str
    email: EmailStr
    password: str
    first_name: str
    last_name: str
    phone_number: str

class UserRead(BaseModel):
    id: int
    username: str
    email: str
    first_name: str
    last_name: str
    phone_number: str

    class Config:
        orm_mode = True

# Login schemas
class LoginRequest(BaseModel):
    username: str
    password: str

class TokenResponse(BaseModel):
    id: int
    access_token: str
    token_type: str = "bearer"
    username: str
    email: str

# Username recovery schemas
class UsernameRecoveryRequest(BaseModel):
    email: str

# Budget schemas
class BudgetCreate(BaseModel):
    userId: int
    budgetCategoryId: int
    amount: float
    startDate: date
    endDate: date

class BudgetRead(BaseModel):
    id: int
    amount: float
    startDate: date
    endDate: date

# Budget Category Schema
class BudgetCategoryRead(BaseModel):
    id: int
    name: str
    description: str

# Goals schemas
class GoalsCreate(BaseModel):
    name: str
    target_amount: float
    current_amount: float
    deadline: date
    description: Optional[str] = None

class GoalsRead(BaseModel):
    id: int
    name: str
    target_amount: float
    current_amount: float
    deadline: date
    description: str

# Report schemas
class ReportCreate(BaseModel):
    userId: int
    reportTypeId: int
    data: dict

class ReportRead(BaseModel):
    reportId: int
    reportTypeId: int
    data: dict
    generatedAt: datetime

# Transaction schemas
class TransactionCreate(BaseModel):
    userId: int
    amount: float
    date: datetime
    description: str
    transactionCategoryId: int

class TransactionRead(BaseModel):
    transactionId: int
    amount: float
    date: datetime
    description: str

# Notification schemas
class NotificationCreate(BaseModel):
    userId: int
    message: str
    isRead: Optional[bool] = False
    date: Optional[datetime] = None

class NotificationRead(BaseModel):
    notificationId: int
    message: str
    isRead: bool
    createdAt: datetime
