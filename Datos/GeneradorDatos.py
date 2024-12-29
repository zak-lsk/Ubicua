# Librerías
import os
import random
from datetime import datetime, timedelta
import csv

# Datos simulados de un mes
INICIO = datetime(2024, 1, 1, 0, 0) 
FIN = datetime(2024, 1, 31, 23, 59)

def generar_datos_gas():
    """
    Función que simula los datos del sensor de gas
    """

    intervalo_gas = timedelta(seconds=5)                # Intervalo de tiempo entre mediciones

    datos_gas = []
    tiempo = INICIO                                     # Tiempo de inicio de la simulación          

    while tiempo <= FIN:                                
        probabilidad_gas = 0.0                          # Probabilidad base de que haya gas en el ambiente
        if 6 <= tiempo.hour < 8:                        # preparación de desayuno
            probabilidad_gas = 0.3                      
        elif 8 <= tiempo.hour < 16:                     # Está fuera de casa
            probabilidad_gas = 0.05
        elif 16 <= tiempo.hour < 19:                    # posibilidad de estar cocinando
            probabilidad_gas = 0.8
        elif 19 <= tiempo.hour < 21:                    # preparación de cena
            probabilidad_gas = 0.9
        elif 21 <= tiempo.hour < 23:                    # calentar algo 
            probabilidad_gas = 0.1
        elif tiempo.hour <= 23 or tiempo.hour < 6:      # Durmiendo
            probabilidad_gas = 0.0

        hay_gas = random.random() < probabilidad_gas
        datos_gas.append({
            "zona": "Casa/Salon",
            "hayGas": int(hay_gas),
            "fecha": tiempo
        })
        tiempo += intervalo_gas

    escribir_csv("Gas.csv", datos_gas, ["zona", "hayGas", "fecha"])

def generar_datos_lluvia():
    """
    Función que simula los datos del sensor de lluvia
    """

    intervalo_lluvia = timedelta(minutes=5)               # Intervalo de tiempo entre mediciones

    datos_lluvia = []
    tiempo = INICIO
    estado_lluvia = False
    tiempo_proximo_cambio = tiempo + timedelta(minutes=random.randint(30, 480)) # Tiempo del cambio del estado de la lluvia

    while tiempo <= FIN:
        if tiempo >= tiempo_proximo_cambio:                 
            estado_lluvia = not estado_lluvia               # Cambio del estado de la lluvia
            duracion = random.randint(15, 120) if estado_lluvia else random.randint(30, 480)
            tiempo_proximo_cambio = tiempo + timedelta(minutes=duracion)    # Tiempo del próximo cambio de estado

        datos_lluvia.append({
            "hayLluvia": int(estado_lluvia),
            "fecha": tiempo
        })
        tiempo += intervalo_lluvia

    escribir_csv("Lluvia.csv", datos_lluvia, ["hayLluvia", "fecha"])

def generar_datos_luz():
    """
    Función que simula los datos del sensor de luz
    """

    intervalo_luz = timedelta(minutes=10)

    datos_luz = []
    tiempo = INICIO

    while tiempo <= FIN:
        # dia de simulación que se irá cambiando
        dia = tiempo.day    
        salida_sol_inicio = datetime(2024, 1, dia, 8, 6)            # hora del comienzo de salida del sol
        salida_sol = datetime(2024, 1, dia, 8, 36)                  # luz plena del sol
        puesta_sol = datetime(2024, 1, dia, 17, 53)                 # hora del comienzdo de puesta del sol
        puesta_sol_fin = datetime(2024, 1, dia, 18, 23)             # de noche

        if salida_sol_inicio <= tiempo <= salida_sol:               # está saliendo el sol
            hay_luz = True
        elif puesta_sol <= tiempo <= puesta_sol_fin:                # está oscureciendo
            hay_luz = False
        elif salida_sol < tiempo <= puesta_sol:                     # luz plena del sol
            hay_luz = True
        else:                                                       # de noche         
            hay_luz = False

        datos_luz.append({
            "hayLuz": int(hay_luz),
            "fecha": tiempo
        })
        tiempo += intervalo_luz

    escribir_csv("Luz.csv", datos_luz, ["hayLuz", "fecha"])

def generar_datos_temperatura():
    """
    Función que simula los datos del sensor de temperatura
    """
    
    intervalo_temperatura = timedelta(minutes=20)

    datos_temperatura = []
    tiempo = INICIO

    while tiempo <= FIN:
        datos_temperatura.append({
            "zona": "Casa/Salon",
            "valor": round(random.uniform(15.0, 30.0), 1),
            "fecha": tiempo
        })
        tiempo += intervalo_temperatura

    escribir_csv("Temperatura.csv", datos_temperatura, ["zona", "valor", "fecha"])

def generar_datos_movimiento():
    """
    Función que simula los datos del sensor de movimiento
    """

    intervalo_movimiento = timedelta(seconds=5)

    datos_movimiento = []
    tiempo = INICIO

    while tiempo <= FIN:
        probabilidad = 0.001                                    # Probabilidad base de que haya movimiento

        if 6 <= tiempo.hour <= 8 and tiempo.minute <= 30:       # Despertar y preparación para salir de casa
            probabilidad = 0.7  
        elif tiempo.hour >= 16 and tiempo.hour <= 23:           # De vuelta a casa
            probabilidad = 0.65
        elif tiempo.hour >= 23 or tiempo.hour < 6:              # Durmiendo 
            probabilidad = 0.0005

        hay_movimiento = random.random() < probabilidad
        if hay_movimiento:
            datos_movimiento.append({
                "zona": "Casa/Salon",
                "hayMovimiento": int(hay_movimiento),
                "fecha": tiempo
            })
        tiempo += intervalo_movimiento

    escribir_csv("Movimiento.csv", datos_movimiento, ["zona", "hayMovimiento", "fecha"])

def escribir_csv(nombre_csv, datos, campos):
    """
    Función que escribe en un archivo CSV los datos de los sensores
    """
    with open(os.path.join(os.path.dirname(__file__), nombre_csv), mode="w", newline="", encoding="utf-8") as archivo:
        escritor = csv.DictWriter(archivo, fieldnames=campos)
        escritor.writeheader()
        escritor.writerows(datos)

# Generar los datos de cada sensors
generar_datos_gas()

