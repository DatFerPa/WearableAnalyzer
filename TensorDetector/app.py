import numpy as np
import tensorflow as tf
accel = "1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1:1.1;1.1;1.1"
accel = "0.0;0.0;0.0:0.0;0.0;0.0:0.0;0.0;0.0:0.0;0.0;0.0:0.0;0.0;0.0:0.0;0.0;0.0:0.0;0.0;0.0:0.0;0.0;0.0:0.0;0.0;0.0:0.0;0.0;0.0"
corte_1 = accel.split(":")
lista_accel = []
for x in corte_1:
    lista = list(map(float,x.split(";")))
    lista_accel.append(lista)

#todavia hay que cercionarse de esto
lista_previa = []
lista_previa.append(lista_accel)
print(lista_previa)
numpy_lista = np.array(lista_previa)
print("-----------------------")
print(numpy_lista)
modelo = tf.keras.models.load_model('modelo_caidas')
prediccion = modelo.predict(numpy_lista)
print(np.argmax(prediccion[0]))
