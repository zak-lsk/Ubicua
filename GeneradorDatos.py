#librerías
import random
from datetime import datetime, timedelta
import csv

def generar_datos_sensores(): 
    """
    Función para simular los datos de los sensores
    """

    inicio = datetime(2024, 1, 1, 0, 0)             # fecha de inicio de la simulación
    fin = datetime(2024, 1, 31, 23, 59)             # fecha de fin de la simulación
    intervalo_temperatura = timedelta(minutes=20)   # intervalo de tiempo para el sensor de temperatura
    intervalo_gas = timedelta(seconds=5)            # intervalo de tiempo para el sensor de gas 
    intervalo_lluvia = timedelta(minutes=5)         # intervalo de tiempo para el sensor de lluvia
    intervalo_luz = timedelta(minutes=10)           # intervalo de tiempo para el sensor de luz

    #Arrays para guardar temporalmente los datos
    datos_gas = []
    datos_lluvia = []
    datos_luz = []
    datos_temperatura = []

    tiempo = inicio                                 # tiempo de inicio de la simulación
    # Datos del sensor de gas
    while tiempo <= fin: 
        datos_gas.append({
            "hayGas": random.choice([True, False]),
            "sitio":"Casa/Salon", 
            "fecha":tiempo
        })
        tiempo += intervalo_gas


    tiempo = inicio                                 # reiniciar el tiempo
    # Datos del sensor de lluvia 
    while tiempo <= fin: 
        datos_lluvia.append({
            "hayLluvia": random.choice([True, False]),
            "fecha":tiempo
        })     
        tiempo += intervalo_lluvia           

    
    tiempo = inicio                                 # reiniciar el tiempo
    # Datos del sensor de luz 
    while tiempo <= fin: 
        datos_luz.append({
            "hayLuz": random.choice([True, False]),
            "fecha":tiempo
        })     
        tiempo += intervalo_luz
    

    tiempo = inicio                                 # reiniciar el tiempo
    # Datos del sensor de temperatura
    while tiempo <= fin: 
        datos_temperatura.append({
            "zona":"Casa/Salon",
            "valor": round(
                random
                .uniform(15.0, 30.0), 1),           # Rango de temperatura
            "fecha":tiempo
        })     
        tiempo += intervalo_temperatura
    
    # Escribir en los ficheros CSV los datos de los sensores
    escribir_csv("Gas.csv", datos_gas, ["hayGas", "sitio", "fecha"])
    escribir_csv("Lluvia.csv", datos_lluvia, ["hayLluvia", "fecha"])
    escribir_csv("Luz.csv", datos_luz, ["hayLuz", "fecha"])
    escribir_csv("Temperatura.csv", datos_temperatura, ["zona", "valor", "fecha"])

def generar_Datos_Movimiento():
    """
    Función que simula los datos del sensor de movimiento
    """

    datos_movimiento = []                           # Array para guardar los datos del sensor de movimiento   
    inicio = datetime(2024, 1, 1, 0, 0)             # fecha de inicio de la simulación  
    fin = datetime(2024, 1, 31, 23, 59)             # fecha de fin de la simulación 
    intervalo_movimiento = timedelta(seconds=5)     # intervalo de tiempo para el sensor de movimiento    

    tiempo = inicio                                 # tiempo de inicio de la simulación

    while tiempo <= fin: 
        probabilidad = 0.001                        # probabilidad base

        #Suponiendo que la persona suele salir entre las 8-8:30
        #y suele volver entre las 16-17:30
        #la probablidad de que haya movimientos entre esos rangos es muy baja
        #ya que la persona está fuera de casa

        #hora de levantarse
        if (6 <= tiempo.hour <= 8 and  0 <=tiempo.minute <= 30) :
            probabilidad = 0.7                      # probabilidad alta cuando sale de casa
        # hora de volver a casa 
        elif (tiempo.hour >= 16 ):                  # cuando está en casa 
            probabilidad = 0.65
        
        # Durmiendo zzz...
        elif (tiempo.hour >= 23 
              and tiempo.hour < 6):                   
            probabilidad = 0.001

        hayMovimiento = random.random() < probabilidad
        if hayMovimiento : 
            datos_movimiento.append({
                "zona": "Casa/Salon",
                "hayMovimiento": hayMovimiento,
                "fecha": tiempo
            })
        tiempo += intervalo_movimiento

    # Escribir en el fichero CSV los datos del sensor de movimiento
    escribir_csv("Movimiento.csv", datos_movimiento, ["zona", "hayMovimiento", "fecha"])

def escribir_csv(nombre_csv, datos, campos):
    """ 
    Función que escribe en un 
    archivo csv los datos de los sensores
    """

    with open(nombre_csv, mode="w", newline="", encoding="utf-8") as archivo:
        escritor = csv.DictWriter(archivo, fieldnames=campos)
        escritor.writeheader()
        escritor.writerows(datos)
        archivo.close()

#generar_datos_sensores()                            # generar los datos de los sensores de temperatura, gas, lluvia y luz    
generar_Datos_Movimiento()                          # generar los datos del sensor de movimiento