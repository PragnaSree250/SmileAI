import mysql.connector

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'smile_ai',
    'port': 3307
}

def test_insert():
    try:
        db = mysql.connector.connect(**db_config)
        cursor = db.cursor()
        
        # Exact SQL from app.py
        sql = """INSERT INTO reports (case_id, deficiency_addressed, ai_reasoning, final_recommendation, 
                 risk_analysis, aesthetic_prognosis, placement_strategy, hyperdontia_status, 
                 aesthetic_symmetry, golden_ratio, missing_teeth_status, medications, care_instructions) 
                 VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
        
        # Sample values
        vals = (1, 'Test', 'Test', 'Test', 'Test', 'Test', 'Test', 'Test', 'Test', 'Test', 'Test', 'Test', 'Test')
        
        print("Attempting test insert...")
        cursor.execute(sql, vals)
        db.commit()
        print("Success! Test insert completed.")
        
        # Clean up
        cursor.execute("DELETE FROM reports WHERE case_id = 1 AND deficiency_addressed = 'Test'")
        db.commit()
        
        db.close()
    except Exception as e:
        print(f"Error during test insert: {e}")

if __name__ == "__main__":
    test_insert()
