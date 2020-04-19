import os
import numpy as np
import random
import tensorflow as tf
# la y va a ser si es caida o no caida

DATADIR = "D:\Git\WearableAnalyzer\TensorDetector"

CATEGORIES = ["caida","nocaida"]
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



model = tf.keras.models.Sequential()
model.add(tf.keras.layers.Flatten(input_shape=(100, 3)))#un tipo de capa, investigar mas
model.add(tf.keras.layers.Dense(128, activation=tf.nn.relu))#128 neuronas en la capa , funcion de activacion rectificacion linear
model.add(tf.keras.layers.Dense(128, activation=tf.nn.relu))
model.add(tf.keras.layers.Dense(2, activation=tf.nn.softmax))#esta va a ser la capa de salida y va a tener que tener el numero de neuronas, para la salida de la clasificacion, al estar con numeros del 0 al 9, son 10 neuronas


model.compile(optimizer='adam',loss='sparse_categorical_crossentropy',metrics=['accuracy'])

model.fit(x,y,epochs=3)#le pasamos lo que queremos entrenar, recordar que epochs, son las iteraciuones a todo el modelo
val_loss, val_acc = model.evaluate(x, y)

lista_accel = []
lista_accel.append(x[0])
lista_otra = np.array(lista_accel)

print(lista_otra)

print("-----------------------------------------")

prediccion = model.predict(lista_otra)
print (prediccion)
print("-----------------------------------------")
print(prediccion[0])
print("-----------------------------------------")
print(np.argmax(prediccion[0]))
