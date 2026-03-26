import mysql.connector
import re

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'smile_ai',
    'port': 3307
}

def verify():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()

        # Get actual columns in reports
        cursor.execute("DESCRIBE reports")
        db_cols = [col[0] for col in cursor.fetchall()]
        print(f"Database 'reports' columns: {db_cols}")

        # The columns we want to insert in app.py
        target_cols = [
            'case_id', 'deficiency_addressed', 'ai_reasoning', 'final_recommendation', 
            'risk_analysis', 'aesthetic_prognosis', 'placement_strategy', 'hyperdontia_status', 
            'aesthetic_symmetry', 'golden_ratio', 'missing_teeth_status', 'medications', 'care_instructions'
        ]

        missing = [col for col in target_cols if col not in db_cols]
        if missing:
            print(f"ERROR: Missing columns in DB for reports: {missing}")
        else:
            print("SUCCESS: All target columns for reports exist in DB.")

        conn.close()
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    verify()
