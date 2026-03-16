import cv2
import numpy as np
import os

def analyze_gum_redness(image_path):
    """
    Analyzes an intraoral image for gum redness levels.
    """
    if not os.path.exists(image_path):
        print(f"Error: File {image_path} not found.")
        return None

    # 1. Load Image
    img = cv2.imread(image_path)
    if img is None:
        print("Error: Could not decode image.")
        return None

    # 2. Convert to HSV (Better for color segmentation)
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

    # 3. Define range for Gum Red/Pink color
    # These values might need tuning depending on lighting
    lower_red1 = np.array([0, 50, 50])
    upper_red1 = np.array([10, 255, 255])
    lower_red2 = np.array([160, 50, 50])
    upper_red2 = np.array([180, 255, 255])

    # 4. Create Mask
    mask1 = cv2.inRange(hsv, lower_red1, upper_red1)
    mask2 = cv2.inRange(hsv, lower_red2, upper_red2)
    red_mask = cv2.addWeighted(mask1, 1.0, mask2, 1.0, 0)

    # 5. Calculate Redness Percentage
    total_pixels = img.shape[0] * img.shape[1]
    red_pixels = cv2.countNonZero(red_mask)
    redness_percentage = (red_pixels / total_pixels) * 100

    # 6. Generate Heatmap (Visualization)
    heatmap = cv2.applyColorMap(red_mask, cv2.COLORMAP_JET)
    output_img = cv2.addWeighted(img, 0.7, heatmap, 0.3, 0)

    return redness_percentage, output_img

if __name__ == "__main__":
    # Example usage for testing
    test_image = "test_intraoral.jpg" # Make sure to put a sample image here
    if os.path.exists(test_image):
        percentage, result = analyze_gum_redness(test_image)
        print(f"Gum Redness detected: {percentage:.2f}%")
        cv2.imwrite("redness_heatmap.jpg", result)
        print("Heatmap saved as redness_heatmap.jpg")
    else:
        print(f"To test, please place an image named '{test_image}' in this folder.")
