import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D
from tensorflow.keras.models import Model
import os

# 1. Point to your sorted folder
train_dir = '.' # Subfolders should be: hyperdontia, calculus, gingivitis, healthy

# 2. Image Data Augmentation
train_datagen = ImageDataGenerator(
    rescale=1./255,
    rotation_range=30,
    width_shift_range=0.2,
    height_shift_range=0.2,
    horizontal_flip=True,
    fill_mode='nearest',
    validation_split=0.2
)

train_generator = train_datagen.flow_from_directory(
    train_dir,
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    subset='training'
)

val_generator = train_datagen.flow_from_directory(
    train_dir,
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    subset='validation'
)

# 3. Base Model (Transfer Learning)
# Get the number of classes from the generator
num_classes = len(train_generator.class_indices)
print(f"Detected {num_classes} classes: {list(train_generator.class_indices.keys())}")

base_model = MobileNetV2(weights='imagenet', include_top=False, input_shape=(224, 224, 3))
x = base_model.output
x = GlobalAveragePooling2D()(x)
x = Dense(512, activation='relu')(x)
predictions = Dense(num_classes, activation='softmax')(x) 

model = Model(inputs=base_model.input, outputs=predictions)

import numpy as np
from sklearn.utils.class_weight import compute_class_weight

# 4. Handle Imbalanced Data (Important for Hyperdontia balance)
# This tells the AI to "pay more attention" to the smaller folder
class_labels = np.unique(train_generator.classes)
class_weights = compute_class_weight(
    class_weight='balanced',
    classes=class_labels,
    y=train_generator.classes
)
class_weights_dict = dict(zip(class_labels, class_weights))

# 5. Compile & Train
model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
model.fit(
    train_generator, 
    validation_data=val_generator, 
    epochs=15, 
    class_weight=class_weights_dict
)

# 6. Save the result
model.save('clinical_diagnostic_model.h5')
print("Model trained and saved as clinical_diagnostic_model.h5")
