from __future__ import absolute_import, division, print_function, unicode_literals
import tensorflow as tf
from tensorflow import keras
import numpy as np
#print(keras.__version__)

"""
Vamos a crear el modelo secuencial con las capas

"""

from tensorflow.keras import layers

model = tf.keras.Sequential()
# Adds a densely-connected layer with 64 units to the model:
layers.Dense(64, activation='relu', input_shape=(32,)),
# Add another:
model.add(layers.Dense(64, activation='relu'))
# Add a softmax layer with 10 output units:
model.add(layers.Dense(10, activation='softmax'))

model.compile(optimizer=tf.keras.optimizers.Adam(0.01),
              loss='categorical_crossentropy',
              metrics=['accuracy'])


data = np.random.random((1000, 32))
labels = np.random.random((1000, 10))

model.fit(data, labels, epochs=10, batch_size=32)
#evaluate
data = np.random.random((1000, 32))
labels = np.random.random((1000, 10))
print("evaluate")
model.evaluate(data, labels, batch_size=32)
print("predict")
result = model.predict(data, batch_size=32)
print(result.shape)
