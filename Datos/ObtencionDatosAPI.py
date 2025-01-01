import requests
import os
import xml.etree.ElementTree as ET
import xml.dom.minidom
from datetime import datetime

CIUDAD = "Alcala de Henares,ES"
API_KEY = "685943c417537f5b313fbfda92eca2b0"

# Obtener la ruta absoluta del directorio actual
directorio_actual = os.path.dirname(os.path.abspath(__file__))
ruta_archivo_actual = os.path.join(directorio_actual, "tiempo_actual.xml")
ruta_archivo_pronostico = os.path.join(directorio_actual, "pronostico.xml")

def formatear_xml(xml_string):
    """Formatea el XML para que se vea bonito y legible"""
    dom = xml.dom.minidom.parseString(xml_string)
    return dom.toprettyxml(indent="    ")

def obtener_datos_tiempo():
    # URLs para ambas APIs (modificadas para XML)
    url_actual = f"https://api.openweathermap.org/data/2.5/weather?q={CIUDAD}&appid={API_KEY}&units=metric&lang=es&mode=xml"
    url_pronostico = f"https://api.openweathermap.org/data/2.5/forecast/daily?q={CIUDAD}&cnt=7&appid={API_KEY}&units=metric&lang=es&mode=xml"

    try:
        # Obtener datos actuales
        response_actual = requests.get(url_actual)
        response_actual.raise_for_status()
        
        # Obtener pronóstico
        response_pronostico = requests.get(url_pronostico)
        response_pronostico.raise_for_status()
        
        # Formatear y guardar datos actuales
        xml_actual_formateado = formatear_xml(response_actual.text)
        with open(ruta_archivo_actual, "w", encoding="utf-8") as file:
            file.write(xml_actual_formateado)
            
        # Formatear y guardar pronóstico
        xml_pronostico_formateado = formatear_xml(response_pronostico.text)
        with open(ruta_archivo_pronostico, "w", encoding="utf-8") as file:
            file.write(xml_pronostico_formateado)
        
        # Parsear XML actual
        tree_actual = ET.fromstring(response_actual.text)
        temp_actual = float(tree_actual.find(".//temperature").get("value"))
        
        # Parsear XML pronóstico
        tree_pronostico = ET.fromstring(response_pronostico.text)
        pronosticos = tree_pronostico.findall(".//time")
        
        # Mostrar temperaturas y fechas
        print(f"Temperatura actual en {CIUDAD}: {temp_actual}°C")
        
        for pronostico in pronosticos:
            # Cambio en cómo manejamos la fecha
            fecha_str = pronostico.get("day")
            fecha = datetime.strptime(fecha_str, "%Y-%m-%d")
            temp = float(pronostico.find(".//temperature").get("day"))
            print(f"Fecha: {fecha.strftime('%Y-%m-%d')}, Temperatura: {temp}°C")
            
        print("Archivos XML guardados correctamente")
        
    except requests.RequestException as e:
        print(f"Error al obtener los datos: {e}")
    except IOError as e:
        print(f"Error al escribir los archivos: {e}")
        print(f"Verificar permisos de escritura en: {directorio_actual}")
    except ET.ParseError as e:
        print(f"Error al parsear XML: {e}")
    except Exception as e:
        print(f"Error inesperado: {e}")

if __name__ == "__main__":
    obtener_datos_tiempo()