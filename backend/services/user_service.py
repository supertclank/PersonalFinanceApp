from sqlalchemy.orm import Session
from fastapi import HTTPException
from models import User
from schemas import UserCreate
from crud import create_user
from utils import hash_password

# Function to create a new user
async def create_new_user(db: Session, user: UserCreate):
    # Check if the email is already registered
    existing_user = db.query(User).filter(User.email == user.email).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="Email already registered")

    # Hash the user's password
    hashed_password = hash_password(user.password)
    
    # Create the user with the hashed password
    db_user = create_user(db=db, user=user.copy(update={"password": hashed_password}))
    return db_user
