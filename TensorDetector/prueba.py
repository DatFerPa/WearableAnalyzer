import tensorflow as tf
import matplotlib.pyplot as plt

mnist = tf.keras.datasets.mnist
(x_train, y_train), (x_test, y_test) = mnist.load_data()
x_train = tf.keras.utils.normalize(x_train, axis= 1)
x_test = tf.keras.utils.normalize(x_test, axis= 1)
#print(y_train[0])
print(x_train[0])
#plt.imshow(x_train[0],cmap = plt.cm.binary)
#plt.show()

model = tf.keras.models.Sequential()
model.add(tf.keras.layers.Flatten())#un tipo de capa, investigar mas
model.add(tf.keras.layers.Dense(128, activation=tf.nn.relu))#128 neuronas en la capa , funcion de activacion rectificacion linear
model.add(tf.keras.layers.Dense(128, activation=tf.nn.relu))
model.add(tf.keras.layers.Dense(10, activation=tf.nn.softmax))#esta va a ser la capa de salida y va a tener que tener el numero de neuronas, para la salida de la clasificacion, al estar con numeros del 0 al 9, son 10 neuronas

"""
Ahora vamos a añadir parametros para el entrenamiento
"""
model.compile(optimizer='adam',loss='sparse_categorical_crossentropy',metrics=['accuracy'])

model.fit(x_train,y_train,epochs=3)#le pasamos lo que queremos entrenar, recordar que epochs, son las iteraciuones a todo el modelo
val_loss, val_acc = model.evaluate(x_test, y_test)
#print(val_loss,val_acc)
"""
guardar un modelo, cargar modelo
"""

model.save('primer_modelo')

new_model = tf.keras.models.load_model('primer_modelo')

#predicciones
predictions = new_model.predict(x_test)
import numpy as np
#print(np.argmax(predictions[0]))
plt.imshow(x_test[0])
plt.show()
