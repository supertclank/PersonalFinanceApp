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
    startDate: date  # Use date here if no time component is needed
    endDate: date

class BudgetRead(BaseModel):
    budgetId: int
    amount: float
    startDate: date
    endDate: date

# Goals schemas
class GoalsCreate(BaseModel):
    user_id: int
    name: str
    target_amount: float
    current_amount: float
    deadline: date  # Use date for deadline as it's date-specific
    description: Optional[str] = None

class GoalsRead(BaseModel):
    goalId: int
    name: str
    targetAmount: float
    currentAmount: float
    deadline: date  # Ensure consistency with the model
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
    generatedAt: datetime  # Typically includes both date and time

# Transaction schemas
class TransactionCreate(BaseModel):
    userId: int
    amount: float
    date: datetime  # This should include both date and time
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
    date: Optional[datetime] = None  # If timestamp is optional

class NotificationRead(BaseModel):
    notificationId: int
    message: str
    isRead: bool
    createdAt: datetime  # Date and time when the notification was created
