f = open("ficherico.txt","w+")

texto = "hola caracola, me voy para no volver, me salgo y me voy, estoy para no estar"
texto_split = texto.split(",")

for i in texto_split:
    f.write(i+"\n")
f.close()
