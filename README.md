# POD-TPE1

Trabajo practico numero 1 de Programacion de Objetos Distribuidos: Despegues

## Integrantes

- [Brandy Tobias](https://github.com/tobiasbrandy)
- [Pannunzio Faustino](https://github.com/Fpannunzio)
- [Sagues Ignacio](https://github.com/isagues)
- [Vazquez Ignacio](https://github.com/igvazquez)

## Compilacion

Para la compilacion del proyecto basta con correr `mvn clean install` en la raiz del proyecto. Esto genera los 2 archivos necesarios para la ejecucion. Esto son 2 archivos `.tar.gz` ubicados en `server/target` y `client/target`.

## Ejecucion

Habiendo decomprimidos los archivos generados en una carpeta a eleccion, se puede proceder a la ejecucion.

Dentro de cada una de las carpetas descomprimidas estan los archviso `.sh` necesarios para la ejecucion.

### Paso 1

El primer paso es prender el rmi-registry con `run-registry.sh` encontrado en la carpeta del servidor.

### Paso 2

Lo siguiente es prender el servidor para que exponga los objetos remotos deseados. Para esto es necesario correr `run-server.sh`, tambien encontrado en la carpeta del servidor.

Este ejecutable tambien permite especificar la direccion del registry: `run-server.sh -DregistryAddress=xx.xx.xx.xx:yyyy`

### Paso 3

Una vez prendido los 2 servicios necesario, ya se puede utilizar el sistema. Existen 4 clientes para hacer uso de la aplicacion

#### Cliente de Administración

```sh
./run-management.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -Drunway=runwayName | -Dcategory=**minCategory** ]
```

- **xx.xx.xx.xx:yyyy** es la dirección IP y el puerto donde está publicado el servicio de administración de los despegues.
- **actionName**: es el nombre de la acción a realizar.
  - add: Agrega una pista de categoría **minCategory** con el nombre **runwayName**. Deberá imprimir en pantalla el estado de la pista luego de agregarla o el error correspondiente.
  - open: Abre la pista **runwayName**. Deberá imprimir en pantalla el estado de la pista luego de invocar a la acción o el error correspondiente.
  - close: Cierra la pista **runwayName**. Deberá imprimir en pantalla el estado de la pista luego de invocar a la acción o el error correspondiente.
  - status: Consulta el estado de la pista **runwayName**. Deberá imprimir en pantalla el estado de la pista al momento de la consulta.
  - takeOff:Emiteunaordendedespegueenlaspistasabiertas.Deberáimprimiren pantalla la finalización de la acción.
  - reorder: Emite una orden de reordenamiento en las pistas. Deberá imprimir en pantalla la cantidad de vuelos que obtuvieron una pista y detallar aquellos que no.

#### Cliente de Solicitud de Pista

```sh
./run-runway.sh -DserverAddress=xx.xx.xx.xx:yyyy -DinPath=fileName
```

- **xx.xx.xx.xx:yyyy** es la dirección IP y el puerto donde está publicado el servicio de solicitud de pista.
- **fileName**: es el path del archivo de entrada con las solicitudes de pista

Input de ejemplo:

```csv
FlightCode;DestinyAirport;AirlineName;MinimumCategory
5382;SCL;Air Canada;F
926;COR;Aerolíneas Argentinas;C
927;COR;Aerolíneas Argentinas;C
```

#### Cliente de Seguimiento de Vuelo

```sh
./run-airline.sh -DserverAddress=xx.xx.xx.xx:yyyy -Dairline=airlineName -DflightCode=flightCode
```

- **xx.xx.xx.xx:yyyy** es la dirección IP y el puerto donde está publicado el servicio de
seguimiento de vuelo.
- **airlineName**: el nombre de la aerolínea
- **flightCode**: el código identificador de un vuelo de la aerolínea **airlineName** que esté esperando despegar.

#### Cliente de Consulta

```sh
./run-query -DserverAddress=xx.xx.xx.xx:yyyy [ -Dairline=airlineName | -Drunway=runwayName ] -DoutPath=fileName
```

- **xx.xx.xx.xx:yyyy** es la dirección IP y el puerto donde está publicado el servicio de consulta de los despegues.
- Si no se indica **-Dairline** ni **-Drunway** se resuelve la consulta 1.
- Si se indica **-Dairline**, **airlineName** es el nombre de la aerolínea elegida para resolver la consulta 2.
- Si se indica **-Drunway**, **runwayName** es el nombre de la pista elegida para resolver la consulta 3.
- Si se indican ambos **-Dairline** y **-Drunway** la consulta falla y se indica el error de parámetros en pantalla.
- **fileName** es el path del archivo de salida con los resultados de la consulta elegida.