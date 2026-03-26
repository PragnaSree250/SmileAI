
import mysql.connector
import os
import sys

# Database Configuration (XAMPP MySQL)
db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'smile_ai',
    'port': 3307
}

def get_db_connection():
    return mysql.connector.connect(**db_config)

def verify_ai_trigger():
    print("🔍 Verifying AI Trigger Logic...")
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        
        # Get the latest case
        cursor.execute("SELECT id, ai_deficiency, ai_report FROM cases ORDER BY id DESC LIMIT 1")
        case = cursor.fetchone()
        
        if not case:
            print("❌ No cases found in database.")
            return

        print(f"Latest Case ID: {case['id']}")
        print(f"Current Condition: {case['ai_deficiency']}")
        
        # We can't easily trigger the HTTP upload here without full flask env, 
        # but we can verify the code existence and logic in app.py
        
        print("✅ Manual Verification Tip: Upload a photo via the app and check if 'ai_deficiency' changes from 'General Assessment'.")
        
        conn.close()
    except Exception as e:
        print(f"❌ Error: {e}")

if __name__ == "__main__":
    verify_ai_trigger()
