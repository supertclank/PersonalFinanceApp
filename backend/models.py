from sqlalchemy import Column, Integer, String, ForeignKey, Date, DECIMAL, Boolean, JSON, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.sql import func
from datetime import datetime

Base = declarative_base()

# User table
class User(Base):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True)
    email = Column(String(100), unique=True)
    password = Column(String(255))
    first_name = Column(String(50))
    last_name = Column(String(50))
    phone_number = Column(String(15))
    
    # Preferences fields
    dark_mode = Column(Boolean, default=False)
    font_size = Column(String, default="Normal")

    # Relationships with other tables
    budgets = relationship("Budget", back_populates="user")
    goals = relationship("Goal", back_populates="user")
    reports = relationship("Report", back_populates="user")
    transactions = relationship("Transaction", back_populates="user")
    notifications = relationship("Notification", back_populates="user")

# BudgetCategory table
class BudgetCategory(Base):
    __tablename__ = 'budget_categories'
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255))
    description = Column(String(255))
    budgets = relationship("Budget", back_populates="category")

# Budget table
class Budget(Base):
    __tablename__ = 'budgets'
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    budget_category_id = Column(Integer, ForeignKey('budget_categories.id'))
    amount = Column(DECIMAL(10, 2))
    start_date = Column(Date)
    end_date = Column(Date)
    
    user = relationship("User", back_populates="budgets")
    category = relationship("BudgetCategory", back_populates="budgets")

# Goal table
class Goal(Base):
    __tablename__ = 'goals'
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    name = Column(String(100))
    target_amount = Column(DECIMAL(10, 2))
    current_amount = Column(DECIMAL(10, 2))
    deadline = Column(Date)
    description = Column(String(255))
    
    user = relationship("User", back_populates="goals")

# ReportType table
class ReportType(Base):
    __tablename__ = 'report_types'
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255))
    description = Column(String(255))
    reports = relationship("Report", back_populates="report_type")

# Report table
class Report(Base):
    __tablename__ = 'reports'
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    report_type_id = Column(Integer, ForeignKey('report_types.id'))
    generated_at = Column(Date, default=func.now)
    data = Column(JSON)

    user = relationship("User", back_populates="reports")
    report_type = relationship("ReportType", back_populates="reports")

# TransactionCategory table
class TransactionCategory(Base):
    __tablename__ = 'transaction_categories'
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255))
    description = Column(String(255))
    transactions = relationship("Transaction", back_populates="category")

# Transaction table
class Transaction(Base):
    __tablename__ = 'transactions'
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    amount = Column(DECIMAL(10, 2))
    transaction_category_id = Column(Integer, ForeignKey('transaction_categories.id'))
    date = Column(Date)
    description = Column(String(255))

    user = relationship("User", back_populates="transactions")
    category = relationship("TransactionCategory", back_populates="transactions")

# NotificationType table
class NotificationType(Base):
    __tablename__ = 'notification_types'
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255))
    description = Column(String(255))
    notifications = relationship("Notification", back_populates="notification_type")

# Notification table
class Notification(Base):
    __tablename__ = 'notifications'
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    message = Column(String(255))
    notification_type_id = Column(Integer, ForeignKey('notification_types.id'))
    is_read = Column(Boolean, default=False)
    created_at = Column(Date, default=func.now)

    user = relationship("User", back_populates="notifications")
    notification_type = relationship("NotificationType", back_populates="notifications")