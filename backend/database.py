from fastapi import FastAPI, HTTPException, Depends
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session  # Import Session here

SQLALCHEMY_DATABASE_URL = "mysql+pymysql://root@localhost:3306/personal_finance_db"

engine = create_engine(SQLALCHEMY_DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Dependency to get the database session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

app = FastAPI()

@app.delete("/clear-users/")
def clear_users(db: Session = Depends(get_db)):
    try:
        db.execute("DELETE FROM users;")  # Replace 'users' with your actual table name if needed
        db.commit()
        return {"message": "All users have been deleted."}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))