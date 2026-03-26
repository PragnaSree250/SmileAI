import mysql.connector

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'smile_ai',
    'port': 3307
}

def check_cases_table():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        cursor.execute("DESCRIBE cases")
        columns = cursor.fetchall()
        print("Columns in 'cases' table:")
        for col in columns:
            print(col[0])
        conn.close()
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    check_cases_table()
