import tensorflow as tf

def convert_h5_to_tflite(h5_model_path, tflite_model_path):
    """
    Converts a Keras .h5 model to a compressed .tflite model for mobile deployment.
    """
    print(f"Loading model from {h5_model_path}...")
    model = tf.keras.models.load_model(h5_model_path)

    print("Converting to TFLite format...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # Optimization: Reduces size and increases speed on mobile
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    
    tflite_model = converter.convert()

    print(f"Saving TFLite model to {tflite_model_path}...")
    with open(tflite_model_path, 'wb') as f:
        f.write(tflite_model)
    
    print("--- SUCCESS! Your model is now mobile-ready. ---")

if __name__ == "__main__":
    # This will run after your training finishes
    convert_h5_to_tflite('clinical_diagnostic_model.h5', 'clinical_diagnostic_model.tflite')
