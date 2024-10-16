from pydantic import BaseModel, EmailStr
from datetime import datetime
from typing import Optional

# User schemas
class UserCreate(BaseModel):
    username: str
    email: EmailStr
    password: str

class UserResponse(BaseModel):
    id: int
    username: str
    email: str
    
    class Config:
        orm_mode = True

class UserRead(BaseModel):
    id: int
    username: str
    email: str
    created_at: datetime

    class Config:
        orm_mode = True

# Login schemas
class LoginRequest(BaseModel):
    username: str
    password: str

class TokenResponse(BaseModel):
    access_token: str
    token_type: str
    id: int
    username: str
    email: str

# Username recovery schemas
class UsernameRecoveryRequest(BaseModel):
    email: str

# Profile schemas
class ProfileResponse(BaseModel):
    profileId: int
    firstName: str
    lastName: str
    phoneNumber: str
    createdAt: datetime
    
class ProfileCreate(BaseModel):
    user_id: int
    first_name: str
    last_name: str
    phone_number: str

class ProfileRead(BaseModel):
    profileId: int
    firstName: str
    lastName: str
    phoneNumber: str
    createdAt: datetime

    class Config:
        orm_mode = True

# Budget schemas
class BudgetCreate(BaseModel):
    userId: int
    budgetCategoryId: int
    amount: float
    startDate: datetime
    endDate: datetime

class BudgetRead(BaseModel):
    budgetId: int
    amount: float
    startDate: datetime
    endDate: datetime

# Goals schemas
class GoalsCreate(BaseModel):
    userId: int
    name: str
    targetAmount: float
    currentAmount: float
    deadline: datetime
    description: str

class GoalsRead(BaseModel):
    goalId: int
    name: str
    targetAmount: float
    currentAmount: float
    deadline: datetime
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
