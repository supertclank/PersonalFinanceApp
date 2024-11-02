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
    amount: float
    start_date: date
    end_date: date
    user_id: int
    budget_category_id: int

class BudgetRead(BaseModel):
    id: int
    amount: float
    start_date: date
    end_date: date
    user_id: int
    budget_category_id: int

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
    user_id: int

# Report schemas
class ReportCreate(BaseModel):
    user_id: int
    report_type_id: int
    generated_at: date
    data: dict

class ReportRead(BaseModel):
    id: int
    user_id: int
    report_type_id: int
    data: dict
    generated_at: datetime
    
class ReportType(BaseModel):
    id: int
    name: str
    description: str

# Transaction schemas
class TransactionCreate(BaseModel):
    user_id: int
    amount: float
    date: datetime
    description: str
    transaction_category_id: int

class TransactionRead(BaseModel):
    id: int
    user_id: int
    amount: float
    date: datetime
    description: str
    transaction_category_id: int
    
class TransactionCategory(BaseModel):
    id: int
    name: str
    description: str
    
# Notification schemas
class NotificationCreate(BaseModel):
    user_id: int
    message: str
    isRead: Optional[bool] = False
    created_at: Optional[datetime] = None
    notification_type_id: int

class NotificationRead(BaseModel):
    id: int
    user_id: int
    message: str
    isRead: Optional[bool] = False
    created_at: Optional[datetime] = None
    notification_type_id: int
    
class NotificationType(BaseModel):
    id: int
    name: str
    description: str