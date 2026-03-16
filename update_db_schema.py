import mysql.connector

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'smile_ai',
    'port': 3307
}

def update_schema():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        print("Adding columns to reports table...")
        cols_to_add = [
            "hyperdontia_status",
            "aesthetic_symmetry",
            "golden_ratio",
            "missing_teeth_status"
        ]
        
        for col in cols_to_add:
            try:
                cursor.execute(f"ALTER TABLE reports ADD COLUMN {col} TEXT")
                print(f"Added {col} to reports")
            except mysql.connector.Error as err:
                if err.errno == 1060: # Column already exists
                    print(f"Column {col} already exists in reports")
                else:
                    print(f"Error adding {col}: {err}")

        print("\nUpdating columns in cases table...")
        # Check if caries_status exists to rename it, or just add hyperdontia if missing
        try:
            cursor.execute("ALTER TABLE cases CHANGE COLUMN caries_status hyperdontia_status TEXT")
            print("Renamed caries_status to hyperdontia_status in cases")
        except mysql.connector.Error:
            try:
                cursor.execute("ALTER TABLE cases ADD COLUMN hyperdontia_status TEXT")
                print("Added hyperdontia_status to cases")
            except: pass

        try:
            cursor.execute("ALTER TABLE cases CHANGE COLUMN hypodontia_status missing_teeth_status TEXT")
            print("Renamed hypodontia_status to missing_teeth_status in cases")
        except mysql.connector.Error:
            try:
                cursor.execute("ALTER TABLE cases ADD COLUMN missing_teeth_status TEXT")
                print("Added missing_teeth_status to cases")
            except: pass

        print("\nEnsuring case_files table exists...")
        create_case_files_query = """
        CREATE TABLE IF NOT EXISTS case_files (
            id INT AUTO_INCREMENT PRIMARY KEY,
            case_id INT NOT NULL,
            file_name VARCHAR(255),
            file_path VARCHAR(255),
            file_type ENUM('STL','JSON','PDF','IMAGE') NOT NULL,
            uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_case_files (case_id),
            CONSTRAINT fk_file_case
                FOREIGN KEY (case_id)
                REFERENCES cases(id)
                ON DELETE CASCADE
        )
        """
        try:
            cursor.execute(create_case_files_query)
            print("Verified case_files table")
        except mysql.connector.Error as err:
            print(f"Error creating case_files: {err}")
            
        conn.commit()
        cursor.close()
        conn.close()
        print("\nSchema update completed successfully!")
        
    except mysql.connector.Error as err:
        print(f"Database error: {err}")

if __name__ == "__main__":
    update_schema()
