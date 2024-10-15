from pydantic import BaseModel, EmailStr
from typing import Optional
from datetime import datetime
from decimal import Decimal

# Base model for user data
class UserBase(BaseModel):
    id: int
    username: str
    email: str
    
class UserResponse(BaseModel):
    id: int
    username: str
    email: str

    class Config:
        orm_mode = True

# Model for creating a new user
class UserCreate(BaseModel):
    username: str
    email: EmailStr
    password: str 

    class Config:
        orm_mode = True

# Model for reading user data
class UserRead(UserBase):
    id: int
    username: str
    email: EmailStr

    class Config:
        orm_mode = True

# Model for login requests
class LoginRequest(BaseModel):
    username: str
    password: str
    
class TokenResponse(BaseModel):
    access_token: str
    token_type: str
    user_id: int
    username: str
    email: str

    class Config:
        orm_mode = True
    
# Model for username recovery requests
class UsernameRecoveryRequest(BaseModel):
    email: EmailStr

# Base model for profile data
class ProfileBase(BaseModel):
    firstName: str
    lastName: str
    phoneNumber: str

# Model for creating a new profile
class ProfileCreate(ProfileBase):
    userId: int

# Model for reading profile data
class ProfileRead(ProfileBase):
    profileId: int
    createdAt: datetime

    class Config:
        orm_mode = True

# Base model for budget data
class BudgetBase(BaseModel):
    amount: Decimal
    startDate: datetime
    endDate: datetime

# Model for creating a new budget
class BudgetCreate(BudgetBase):
    userId: int
    budgetCategoryId: int

# Model for reading budget data
class BudgetRead(BudgetBase):
    budgetId: int

    class Config:
        orm_mode = True

# Base model for goal data
class GoalsBase(BaseModel):
    name: str
    targetAmount: Decimal
    currentAmount: Decimal
    deadline: datetime
    description: str

# Model for creating a new goal
class GoalsCreate(GoalsBase):
    userId: int

# Model for reading goal data
class GoalsRead(GoalsBase):
    goalId: int

    class Config:
        orm_mode = True

# Base model for report data
class ReportBase(BaseModel):
    reportTypeId: int
    data: dict

# Model for creating a new report
class ReportCreate(ReportBase):
    userId: int

# Model for reading report data
class ReportRead(ReportBase):
    reportId: int
    generatedAt: datetime

    class Config:
        orm_mode = True

# Base model for transaction data
class TransactionBase(BaseModel):
    amount: Decimal
    date: datetime
    description: str

# Model for creating a new transaction
class TransactionCreate(TransactionBase):
    userId: int
    transactionCategoryId: int

# Model for reading transaction data
class TransactionRead(TransactionBase):
    transactionId: int

    class Config:
        orm_mode = True

# Base model for notification data
class NotificationBase(BaseModel):
    message: str
    isRead: bool

# Model for creating a new notification
class NotificationCreate(NotificationBase):
    userId: int
    notificationTypeId: int

# Model for reading notification data
class NotificationRead(NotificationBase):
    notificationId: int
    createdAt: datetime

    class Config:
        orm_mode = True