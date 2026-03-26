import mysql.connector

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'smile_ai',
    'port': 3307
}

try:
    db = mysql.connector.connect(**db_config)
    cursor = db.cursor(dictionary=True)
    
    cursor.execute("SELECT id FROM cases ORDER BY id DESC LIMIT 5")
    case_ids = [row['id'] for row in cursor.fetchall()]
    
    for cid in case_ids:
        print(f"\n--- Checking Case {cid} ---")
        cursor.execute("SELECT * FROM reports WHERE case_id = %s", (cid,))
        report = cursor.fetchone()
        if report:
            print(f"Report ID: {report['id']}")
            print(f"Deficiency: {report['deficiency_addressed']}")
            print(f"Meds: {report['medications']}")
            print(f"Tips: {report['care_instructions']}")
        else:
            print("No report found in 'reports' table.")
            
        cursor.execute("SELECT * FROM medications WHERE case_id = %s", (cid,))
        meds = cursor.fetchall()
        print(f"Medications in 'medications' table: {len(meds)}")
        for m in meds:
            print(f"  - {m['name']}")

    db.close()
except Exception as e:
    print("Error:", e)
