from sqlalchemy.orm import Session
from fastapi import HTTPException
from models import User
from schemas import UserCreate
from crud import create_user  # Import your create_user function here
from utils import hash_password  # Assuming you have a utility function for hashing

async def create_new_user(db: Session, user: UserCreate):
    existing_user = db.query(User).filter(User.email == user.email).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="Email already registered")

    hashed_password = hash_password(user.password)  # Ensure this function is defined
    db_user = create_user(db=db, user=user.copy(update={"hashed_password": hashed_password}))
    return db_user
