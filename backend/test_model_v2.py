
import tensorflow as tf
import os

model_path = r"c:\Users\Pragna Sree\AndroidStudioProjects\SmileAI\backend\models\clinical_diagnostic_model.tflite"

if not os.path.exists(model_path):
    print(f"File not found: {model_path}")
else:
    try:
        interpreter = tf.lite.Interpreter(model_path=model_path)
        interpreter.allocate_tensors()
        print("Model loaded successfully!")
        print("Input Details:", interpreter.get_input_details())
        print("Output Details:", interpreter.get_output_details())
    except Exception as e:
        print(f"Error loading model: {e}")

# Test with all sample images in uploads
import numpy as np
from PIL import Image
uploads_dir = os.path.join(os.path.dirname(model_path), "..", "uploads")
if os.path.exists(uploads_dir):
    print(f"Testing on all images in {uploads_dir}...")
    for filename in os.listdir(uploads_dir):
        if filename.endswith(".jpg") or filename.endswith(".png"):
            image_path = os.path.join(uploads_dir, filename)
            try:
                img = Image.open(image_path).convert('RGB').resize((224, 224))
                img_array = np.array(img, dtype=np.float32) / 255.0
                input_data = np.expand_dims(img_array, axis=0)
                
                input_details = interpreter.get_input_details()
                output_details = interpreter.get_output_details()
                
                interpreter.set_tensor(input_details[0]['index'], input_data)
                interpreter.invoke()
                output_data = interpreter.get_tensor(output_details[0]['index'])[0]
                print(f"Image: {filename} -> Prediction: {output_data} -> Index: {np.argmax(output_data)}")
            except Exception as e:
                print(f"Error processing {filename}: {e}")
else:
    print(f"Uploads dir not found: {uploads_dir}")
