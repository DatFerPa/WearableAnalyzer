import os
import numpy as np
import random
import tensorflow as tf
# la y va a ser si es caida o no caida

DATADIR = "D:\Git\WearableAnalyzer\TensorDetector"

CATEGORIES = ["simovimineto","nomovimiento"]
training_data = []

for category in CATEGORIES:
    path = os.path.join(DATADIR,category)
    #print(path)
    caida_tipo = CATEGORIES.index(category)
    for dataThing in os.listdir(path):
        #print(dataThing)
        try:
            #print(os.path.join(path,dataThing))
            f = open(os.path.join(path,dataThing),'r')
            f1 = f.read().splitlines()
            #print(f1)
            array_caidas = []
            for x in f1:
                lista = list(map(float,x.split(";")))
                print(lista)
                array_caidas.append(lista)
            #print(array_caidas)
            training_data.append([array_caidas,caida_tipo])
        except Exception as e:
            print(e)
#print(len(training_data))
random.shuffle(training_data)
x = []
y = []
for numbers,labels in training_data:
    x.append(numbers)
    y.append(labels)
x = np.array(x)
y = np.array(y)
print(x)
print(y)


model = tf.keras.models.Sequential()
model.add(tf.keras.layers.Flatten(input_shape=(1000, 3)))#un tipo de capa, investigar mas
model.add(tf.keras.layers.Dense(128, activation=tf.nn.relu))#128 neuronas en la capa , funcion de activacion rectificacion linear
model.add(tf.keras.layers.Dense(128, activation=tf.nn.relu))
model.add(tf.keras.layers.Dense(2, activation=tf.nn.softmax))#esta va a ser la capa de salida y va a tener que tener el numero de neuronas, para la salida de la clasificacion, al estar con numeros del 0 al 9, son 10 neuronas


model.compile(optimizer='adam',loss='sparse_categorical_crossentropy',metrics=['accuracy'])

model.fit(x,y,epochs=3)#le pasamos lo que queremos entrenar, recordar que epochs, son las iteraciuones a todo el modelo
val_loss, val_acc = model.evaluate(x, y)
model.save('modelo_movimientos')




# Save tf.keras model in HDF5 format.
keras_file_caidas = "keras_model_modelo_caidas.h5"
tf.keras.models.save_model(model, keras_file_caidas)


# Transformar modelo para android
converter = tf.lite.TFLiteConverter.from_keras_model_file(keras_file_caidas)
print("fin")
tflite_model = converter.convert()
open("converted_model.tflite", "wb").write(tflite_model)
