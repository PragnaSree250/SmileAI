import mysql.connector

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'smile_ai',
    'port': 3307
}

def update_db():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()

        # --- UPDATE REPORTS TABLE ---
        print("Checking 'reports' table...")
        cursor.execute("DESCRIBE reports")
        report_cols = [col[0] for col in cursor.fetchall()]
        
        report_missing = {
            "risk_analysis": "TEXT",
            "aesthetic_prognosis": "TEXT",
            "placement_strategy": "TEXT",
            "hyperdontia_status": "VARCHAR(50)",
            "aesthetic_symmetry": "VARCHAR(50)",
            "golden_ratio": "VARCHAR(50)",
            "missing_teeth_status": "VARCHAR(50)",
            "medications": "TEXT",
            "care_instructions": "TEXT"
        }
        
        for col, dtype in report_missing.items():
            if col not in report_cols:
                print(f"Adding column '{col}' to 'reports' table...")
                cursor.execute(f"ALTER TABLE reports ADD COLUMN {col} {dtype}")
                conn.commit()
            else:
                print(f"Column '{col}' already exists in 'reports'.")

        # --- UPDATE CASES TABLE ---
        print("\nChecking 'cases' table...")
        cursor.execute("DESCRIBE cases")
        case_cols = [col[0] for col in cursor.fetchall()]
        
        case_missing = {
            "risk_analysis": "TEXT",
            "aesthetic_prognosis": "TEXT",
            "placement_strategy": "TEXT",
            "golden_ratio": "VARCHAR(50)",
            "suggested_restoration": "VARCHAR(255)",
            "suggested_material": "VARCHAR(255)"
        }
        
        for col, dtype in case_missing.items():
            if col not in case_cols:
                print(f"Adding column '{col}' to 'cases' table...")
                cursor.execute(f"ALTER TABLE cases ADD COLUMN {col} {dtype}")
                conn.commit()
            else:
                print(f"Column '{col}' already exists in 'cases'.")

        conn.close()
        print("\nDatabase update completed successfully!")
    except Exception as e:
        print(f"\nError updating database: {e}")

if __name__ == "__main__":
    update_db()
