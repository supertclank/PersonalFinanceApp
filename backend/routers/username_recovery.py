from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from models import User
from schemas import UsernameRecoveryRequest
from database import get_db

router = APIRouter()

@router.post("/username/recover/")
def recover_username(request: UsernameRecoveryRequest, db: Session = Depends(get_db)):
    # Retrieve the user by email
    user = db.query(User).filter(User.email == request.email).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Send an email to the user with their username
    # (assuming a function send_email exists)
    send_email(
        recipient=user.email, 
        subject="Username Recovery",
        message=f"Your username is {user.username}"
    )

    return {"message": "An email with your username has been sent to your email address."}
