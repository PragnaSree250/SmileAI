import mysql.connector

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'smile_ai',
    'port': 3307
}

def migrate():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        # Columns to add to both tables
        columns = [
            ("suggested_restoration", "VARCHAR(255)"),
            ("suggested_material", "VARCHAR(255)"),
            ("hyperdontia_status", "VARCHAR(255)"),
            ("redness_analysis", "VARCHAR(255)"),
            ("aesthetic_symmetry", "VARCHAR(255)")
        ]
        
        tables = ["cases", "reports"]
        
        for table in tables:
            print(f"Checking table: {table}")
            cursor.execute(f"DESCRIBE {table}")
            existing_cols = [row[0] for row in cursor.fetchall()]
            
            for col_name, col_type in columns:
                if col_name not in existing_cols:
                    print(f"Adding {col_name} to {table}...")
                    cursor.execute(f"ALTER TABLE {table} ADD COLUMN {col_name} {col_type}")
                    print(f"Success: {col_name} added to {table}")
                else:
                    print(f"Skipping: {col_name} already exists in {table}")
        
        conn.commit()
        cursor.close()
        conn.close()
        print("\nMigration completed successfully!")
        
    except mysql.connector.Error as err:
        print(f"Error: {err}")

if __name__ == "__main__":
    migrate()
