import os
import numpy as np

CLINICAL_LABELS = ["Calculus", "Gingivitis", "Healthy", "Hyperdontia", "tooth discoloration", "caries"]

def run_random_smile_ai(case_data):
    import random
    condition = case_data.get("condition")
    if not condition or condition == "General":
        condition = random.choice(CLINICAL_LABELS)
    score = random.randint(70, 98)
    grade = "A" if score >= 90 else ("B" if score >= 80 else "C")
    report_text = f"Automated screening suggests a {condition.lower()} condition. "
    return {
        "ai_deficiency": condition, 
        "ai_report": report_text, 
        "ai_score": score, 
        "ai_grade": grade,
        "ai_recommendation": "Maintain regular checkups." if score > 85 else "Consultation recommended.",
        "suggested_restoration": random.choice(["Composite Filling", "Dental Crown", "N/A"]),
        "suggested_material": random.choice(["Ceramic", "Zirconia", "Composite"]), 
        "caries_status": "No major cavities." if score > 80 else "Monitor potential enamel wear.", 
        "hypodontia_status": "Normal",
        "discoloration_status": "Minimal." if score > 85 else "Moderate staining.", 
        "gum_inflammation_status": "Healthy" if score > 85 else "Mild redness observed.",
        "calculus_status": "Low" if score > 80 else "Moderate", 
        "redness_analysis": "Normal",
        "aesthetic_symmetry": "Symmetric" if score > 85 else "Minor deviation"
    }

def run_smile_ai(case_data, photos=None):
    try:
        # Simulate real engine failure to see fallback
        raise Exception("Simulated Failure")
        # return _run_smile_ai_engine(case_data, photos)
    except Exception as e:
        print(f"Fallback triggered: {e}")
        return run_random_smile_ai(case_data)

if __name__ == "__main__":
    result = run_smile_ai({"condition": "General"})
    print("Result keys:")
    print(list(result.keys()))
    if "hypodontia_status" in result:
        print(f"hypodontia_status: {result['hypodontia_status']}")
    else:
        print("hypodontia_status MISSING!")
