import os
import numpy as np
import random
import tensorflow as tf
DATADIR = "D:\Git\WearableAnalyzer\TensorDetector"

CATEGORIA = ["si movimiento","no movimiento"]

path = os.path.join(DATADIR,"datosEjecucion")
executingData = []
for dataThing in os.listdir(path):
    print(dataThing)
    try:
        #print(os.path.join(path,dataThing))
        f = open(os.path.join(path,dataThing),'r')
        f1 = f.read().splitlines()
        #print(f1)
        array_caidas = []
        for x in f1:
            lista = list(map(float,x.split(";")))
            #print(lista)
            array_caidas.append(lista)
        #print(array_caidas)
        executingData.append(array_caidas)
    except Exception as e:
        print(e)

x = []
for numbers in executingData:
    x.append(numbers)

x = np.array(x)

modelo = tf.keras.models.load_model('modelo_movimientos')
predicciones = modelo.predict(x)
for prediccion in predicciones:
    if np.argmax(prediccion) == 0:
        print(CATEGORIA[0])
    else:
        print(CATEGORIA[1])
