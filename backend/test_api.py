import urllib.request
import json
import mysql.connector

try:
    db = mysql.connector.connect(host='localhost', user='root', password='', database='dental_ai')
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT email FROM register WHERE role='patient' LIMIT 1")
    row = cursor.fetchone()
    email = row['email'] if row else None
    print(f"Testing with patient email: {email}")
    
    if email:
        # We can't know the password directly since it's hashed, but we can query the reports directly!
        cursor.execute("SELECT r.* FROM reports r JOIN cases c ON r.case_id = c.id WHERE c.patient_id = (SELECT patient_id FROM register WHERE email=%s)", (email,))
        reports = cursor.fetchall()
        print(f"Direct DB reports: {reports}")
        
        # Also let's just query cases directly
        cursor.execute("SELECT * FROM cases LIMIT 1")
        case = cursor.fetchone()
        print(f"\nSample case: {case['id']} - status: {case['status']}")
        
        # Test medications
        cursor.execute("SELECT * FROM medications LIMIT 2")
        print("\nMedications:", cursor.fetchall())

except Exception as e:
    print("Error:", e)
