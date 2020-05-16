import urllib.request
import time
import datetime

currentTime = datetime.datetime.now()
while True:
    print (currentTime.strftime("%H:%M:%S"))
    contents = urllib.request.urlopen("https://servidorhombremuerto.herokuapp.com/").read()
    print(contents)
    time.sleep(1200)
    currentTime = datetime.datetime.now()
