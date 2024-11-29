# Instrucciones para la creación de la base de datos. 
## Instrucciones desde la máquina virtual 
* Crear la base de datos: ```CREATE DATABASE ubicuabd```;
* Crear un usuario remoto: ```CREATE USER 'ubicua'@'%' IDENTIFIED BY 'ubicua'```;
* Darle todos los permisos necesarios: ```GRANT ALL PRIVILEGES ON *.* TO 'ubicua'@'%'```;
* Actualizar los permisos: ```FLUSH PRIVILEGES```;


## Instrucciones desde MySQL Workbench: 
 Crear una nueva conexión: en los campos: 
 * nombre de la conexión: Ubicua (podría ser cualquier nombre)
 * hostname: poner la IP de la máquina virtual, que nos sale al introducir el comando ```ip address```
 * User: ubicua (el que se ha creado antes) y la contraseña: ubicua (original eh)
 * Default schema : ponemos el nombre de la base de datos que hemos creado: ubicuabd

Y poquito más
                                           
  
