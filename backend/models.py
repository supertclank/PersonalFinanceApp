from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, Float, Boolean, JSON, Enum, DECIMAL
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base
import datetime

Base = declarative_base()

class User(Base):
    __tablename__ = 'user'

    userId = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, nullable=False)
    email = Column(String(100), unique=True, nullable=False)
    password = Column(String(255), nullable=False)
    createdAt = Column(DateTime, default=datetime.datetime.utcnow)

    profile = relationship("Profile", back_populates="user")
    budgets = relationship("Budget", back_populates="user")
    goals = relationship("Goals", back_populates="user")
    reports = relationship("Report", back_populates="user")
    transactions = relationship("Transaction", back_populates="user")
    notifications = relationship("Notification", back_populates="user")

class Profile(Base):
    __tablename__ = 'profile'

    profileId = Column(Integer, primary_key=True, index=True)
    userId = Column(Integer, ForeignKey('user.userId'))
    firstName = Column(String(100))
    lastName = Column(String(100))
    phoneNumber = Column(String(15))
    createdAt = Column(DateTime, default=datetime.datetime.utcnow)

    user = relationship("User", back_populates="profile")


class BudgetCategory(Base):
    __tablename__ = 'budgetcategory'

    categoryId = Column(Integer, primary_key=True, index=True)
    categoryName = Column(String(255))
    categoryDescription = Column(String(255))

    budgets = relationship("Budget", back_populates="category")

class Budget(Base):
    __tablename__ = 'budget'

    budgetId = Column(Integer, primary_key=True, index=True)
    userId = Column(Integer, ForeignKey('user.userId'))
    budgetCategoryId = Column(Integer, ForeignKey('budgetcategory.categoryId'))
    amount = Column(DECIMAL(10, 2))
    startDate = Column(DateTime)
    endDate = Column(DateTime)

    user = relationship("User", back_populates="budgets")
    category = relationship("BudgetCategory", back_populates="budgets")

class Goals(Base):
    __tablename__ = 'goals'

    goalId = Column(Integer, primary_key=True, index=True)
    userId = Column(Integer, ForeignKey('user.userId'))
    name = Column(String(100))
    targetAmount = Column(DECIMAL(10, 2))
    currentAmount = Column(DECIMAL(10, 2))
    deadline = Column(DateTime)
    description = Column(String(255))

    user = relationship("User", back_populates="goals")

class ReportType(Base):
    __tablename__ = 'reporttype'

    reportTypeId = Column(Integer, primary_key=True, index=True)
    reportTypeName = Column(String(255))
    reportTypeDescription = Column(String(255))

    reports = relationship("Report", back_populates="reportType")

class Report(Base):
    __tablename__ = 'report'

    reportId = Column(Integer, primary_key=True, index=True)
    userId = Column(Integer, ForeignKey('user.userId'))
    reportTypeId = Column(Integer, ForeignKey('reporttype.reportTypeId'))
    generatedAt = Column(DateTime, default=datetime.datetime.utcnow)
    data = Column(JSON)

    user = relationship("User", back_populates="reports")
    reportType = relationship("ReportType", back_populates="reports")

class TransactionCategory(Base):
    __tablename__ = 'transactioncategory'

    categoryId = Column(Integer, primary_key=True, index=True)
    categoryName = Column(String(255))
    categoryDescription = Column(String(255))

    transactions = relationship("Transaction", back_populates="category")

class Transaction(Base):
    __tablename__ = 'transaction'

    transactionId = Column(Integer, primary_key=True, index=True)
    userId = Column(Integer, ForeignKey('user.userId'))
    amount = Column(DECIMAL(10, 2))
    transactionCategoryId = Column(Integer, ForeignKey('transactioncategory.categoryId'))
    date = Column(DateTime)
    description = Column(String(255))

    user = relationship("User", back_populates="transactions")
    category = relationship("TransactionCategory", back_populates="transactions")

class NotificationType(Base):
    __tablename__ = 'notificationtype'

    notificationTypeId = Column(Integer, primary_key=True, index=True)
    notificationName = Column(String(255))
    notificationTypeDescription = Column(String(255))

    notifications = relationship("Notification", back_populates="notificationType")

class Notification(Base):
    __tablename__ = 'notification'

    notificationId = Column(Integer, primary_key=True, index=True)
    userId = Column(Integer, ForeignKey('user.userId'))
    message = Column(String(255))
    notificationTypeId = Column(Integer, ForeignKey('notificationtype.notificationTypeId'))
    isRead = Column(Boolean, default=False)
    createdAt = Column(DateTime, default=datetime.datetime.utcnow)

    user = relationship("User", back_populates="notifications")
    notificationType = relationship("NotificationType", back_populates="notifications")
