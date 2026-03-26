
import tensorflow as tf
import os

model_path = r"c:\Users\Pragna Sree\AndroidStudioProjects\SmileAI\backend\models\smile_aesthetic_model.tflite"

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
