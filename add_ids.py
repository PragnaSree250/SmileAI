
import os

def add_id(file_path, target_text, id_attr):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Simple replacement that adds the ID after the tag start
    # We look for the TextView or ProgressBar that contains the target_text
    # and add the id attribute.
    
    if target_text in content:
        # Find the start of the tag containing target_text
        index = content.find(target_text)
        tag_start = content.rfind('<', 0, index)
        tag_end_bracket = content.find('>', tag_start)
        
        # Check if id already exists
        if 'android:id="' in content[tag_start:tag_end_bracket]:
            print(f"ID already exists for target: {target_text}")
            return
            
        # Add ID after the tag name
        space_index = content.find(' ', tag_start)
        new_content = content[:space_index] + f'\n        android:id="{id_attr}"' + content[space_index:]
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Added ID {id_attr} to {file_path}")
    else:
        print(f"Target text not found: {target_text}")

# Specific for patient reports
ai_analysis_xml = r"c:\Users\Pragna Sree\AndroidStudioProjects\SmileAI\app\src\main\res\layout\activity_patient_view_ai_analysis.xml"
add_id(ai_analysis_xml, 'text="94%"', "@+id/tvConfidence")
add_id(ai_analysis_xml, 'progressDrawable="@drawable/bg_progress_bar_blue"', "@+id/pbConfidence")
add_id(ai_analysis_xml, 'text="Structural Damage Detected"', "@+id/tvFinding1Title")
add_id(ai_analysis_xml, 'text="Significant wear on Upper Right Molar (#3) requiring intervention."', "@+id/tvFinding1Desc")

recommendation_xml = r"c:\Users\Pragna Sree\AndroidStudioProjects\SmileAI\app\src\main\res\layout\activity_patient_ai_recommendation.xml"
add_id(recommendation_xml, 'text="Based on the AI analysis of your bite pressure', "@+id/textExplanation")
