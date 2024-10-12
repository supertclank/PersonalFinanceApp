import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart


#Email function at the ready if app goes live
def send_email(recipient: str, subject: str, message: str):
    
    smtp_server = "smtp.example.com"
    smtp_port = 587  
    sender_email = "your_email@example.com"  
    sender_password = "your_email_password"  

    # Create a multipart email message
    msg = MIMEMultipart()
    msg["From"] = sender_email
    msg["To"] = recipient
    msg["Subject"] = subject

    # Attach the message body to the email
    msg.attach(MIMEText(message, "plain"))

    try:
        # Connect to the SMTP server and send the email
        with smtplib.SMTP(smtp_server, smtp_port) as server:
            server.starttls()  # Upgrade the connection to a secure encrypted SSL/TLS connection
            server.login(sender_email, sender_password)  # Log in to the email account
            server.send_message(msg)  # Send the email
    except Exception as e:
        print(f"Error sending email: {e}")
