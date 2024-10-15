from passlib.context import CryptContext
import bcrypt

# Initialize the password hashing context with bcrypt
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Hash the plain password
def hash_password(password: str) -> str:
    return pwd_context.hash(password)

def verify_password(plain_password: str, hashed_password: str):
    return pwd_context.verify(plain_password, hashed_password)

def rehash_password(user):
    if not user.password.startswith("$2b$"):
        user.password = hash_password(user.password)