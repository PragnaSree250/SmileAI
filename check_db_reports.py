import mysql.connector

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'smile_ai',
    'port': 3307
}

def check_reports_table():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        cursor.execute("DESCRIBE reports")
        columns = cursor.fetchall()
        print("Columns in 'reports' table (Full Detail):")
        for col in columns:
            print(f"Field: {col['Field']}, Type: {col['Type']}")
        conn.close()
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    check_reports_table()
