import mysql.connector
import os

# Database configuration
DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": "", 
    "database": "smile_ai",
    "port": 3307
}

def generate_patient_id(numeric_id):
    return f"P{numeric_id:04d}"

def migrate():
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)
        
        # 1. Get all patients
        cursor.execute("SELECT id, full_name, patient_id FROM register WHERE role = 'patient'")
        patients = cursor.fetchall()
        
        if not patients:
            print("No patients found.")
            return

        print(f"Found {len(patients)} patients. Updating to sequential IDs...")
        for p in patients:
            old_id = p['patient_id']
            new_id = generate_patient_id(p['id'])
            
            if old_id == new_id:
                print(f"ID already correct for {p['full_name']} ({new_id})")
                continue
                
            # Update register
            cursor.execute("UPDATE register SET patient_id = %s WHERE id = %s", (new_id, p['id']))
            print(f"Updated {p['full_name']}: {old_id} -> {new_id}")
            
            # Sync cases
            if old_id:
                cursor.execute("UPDATE cases SET patient_id = %s WHERE patient_id = %s", (new_id, old_id))
                affected_cases = cursor.rowcount
                if affected_cases > 0:
                    print(f"  -> Linked {affected_cases} cases to new ID")
            
        conn.commit()
        conn.close()
        print("Migration complete!")
    except mysql.connector.Error as err:
        print(f"Error: {err}")

if __name__ == "__main__":
    migrate()
