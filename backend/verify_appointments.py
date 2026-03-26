
import requests
import json

base_url = "http://localhost:5000"

# Mock IDs
case_id = 1
patient_id = "P0001" # Ensure this patient exists in your DB for notification test
token = "YOUR_TOKEN_HERE" # Need a dentist token

# 1. Create Appointment
def test_create_appointment():
    headers = {"Authorization": f"Bearer {token}"}
    payload = {
        "case_id": case_id,
        "patient_id": patient_id,
        "appointment_date": "2026-03-20",
        "appointment_day": "Friday"
    }
    
    response = requests.post(f"{base_url}/appointments", json=payload, headers=headers)
    print(f"Status Code: {response.status_code}")
    print(f"Response: {response.json()}")

if __name__ == "__main__":
    # This is a manual test placeholder. 
    # To run effectively, you should use a valid JWT from a dentist login.
    print("Verification script ready. Replace token with a valid JWT to test.")
