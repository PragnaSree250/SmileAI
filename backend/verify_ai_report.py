
import requests
import os

base_url = "http://localhost:5000"
# Need a valid dentist token from a login response
token = "YOUR_TOKEN_HERE" 

def test_ai_analysis(case_id, image_path):
    headers = {"Authorization": f"Bearer {token}"}
    
    # Upload image
    with open(image_path, 'rb') as f:
        files = {'file': f}
        upload_res = requests.post(f"{base_url}/cases/{case_id}/upload", files=files, headers=headers)
        print("Upload Response:", upload_res.json())
        
    # Trigger Analysis
    analysis_res = requests.get(f"{base_url}/cases/{case_id}/analyze", headers=headers)
    print("Analysis Result:", analysis_res.json())
    
    # Finalize Report (should auto-fill meds based on AI)
    report_payload = {
        "case_id": case_id,
        "deficiency_addressed": "AI Suggested Treatment",
        "final_recommendation": "Follow AI suggestions",
        # meds and tips omitted to test auto-fill
    }
    report_res = requests.post(f"{base_url}/reports", json=report_payload, headers=headers)
    print("Report Response:", report_res.json())

if __name__ == "__main__":
    print("AI Integration verification script ready. Replace token with a valid dentist JWT.")
