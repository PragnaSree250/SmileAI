import mysql.connector

def fix_schema():
    try:
        db = mysql.connector.connect(
            host="localhost",
            user="root",
            password="",
            database="smile_ai",
            port=3307
        )
        cursor = db.cursor()
        
        # Check columns
        cursor.execute("DESCRIBE cases")
        columns = [col[0] for col in cursor.fetchall()]
        print(f"Current columns: {columns}")
        
        missing_columns = {
            "risk_analysis": "TEXT",
            "aesthetic_prognosis": "TEXT",
            "placement_strategy": "TEXT",
            "suggested_restoration": "VARCHAR(255)",
            "suggested_material": "VARCHAR(255)",
            "golden_ratio": "VARCHAR(50)"
        }
        
        for col, dtype in missing_columns.items():
            if col not in columns:
                print(f"Adding column: {col}")
                cursor.execute(f"ALTER TABLE cases ADD COLUMN {col} {dtype}")
                db.commit()
            else:
                print(f"Column {col} already exists.")
                
        db.close()
        print("Schema update complete.")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    fix_schema()
