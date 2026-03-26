import mysql.connector

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'smile_ai',
    'port': 3307
}

def analyze_cases():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT status, count(*) as count FROM cases GROUP BY status")
        rows = cursor.fetchall()
        print("Cases grouped by status:")
        for r in rows:
            print(f"- {r['status']}: {r['count']}")
            
        cursor.execute("SELECT id, dentist_id, patient_first_name, status, created_at FROM cases ORDER BY created_at DESC LIMIT 10")
        recent = cursor.fetchall()
        print("\nRecent 10 cases:")
        for r in recent:
            print(f"ID: {r['id']}, Dentist: {r['dentist_id']}, Status: {r['status']}, Name: {r['patient_first_name']}, Date: {r['created_at']}")
            
        conn.close()
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    analyze_cases()
