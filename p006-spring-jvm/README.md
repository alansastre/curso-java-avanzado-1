
## JVM Y HERRAMIENTAS DE MONITORIZACIÓN

* jconsole: 
  * Incluida en el propio JDK
  * C:\Users\Alan\.jdks\temurin-23\bin\jconsole
  * Básica, ligera

* IntelliJ Profiler
  * Incluido en IntelliJ IDEA Ultimate
  * Básica-intemedia 
  * Profiling de memoria, hilos, CPU en desarrollo
  * Integrado en el propio IDE, por lo que es ideal para la fase de desarrollo
  * Te indica memoria y tiempo y consumo de cada método de cada clase con indicadores grises rojos...

* VisualVM
  * Antes era el JVisualVM
  * Más completo que jconsole
  * Hay que descargar e instalarlo: https://visualvm.github.io
  * C:\Users\X\Downloads\visualvm_2110\bin\visualvm

* JMC
  * Usa JFR para capturar datos para hacer el profiling y con JMC se analizan y visualizan
  * https://www.oracle.com/java/technologies/jdk-mission-control.html
  * Interfaz UI basada Eclipse IDE
  * Opción 1: MBeanServer para monitorizar en tiempo real la aplicación
  * Opción 2: Java Flight Recorder graba una sesión y luego JMC la analiza, proporcionando gran nivel de detalle.

* Eclipse MAT
  * Específico para analizar la memoria y hacer head dump y encontrar memory leaks
  * https://eclipse.dev/mat
  * https://github.com/eclipse-mat/mat

* Apache JMeter
  * Ya no es tan específico para la JVM, es más para todo tipo de servicios web
  * Principalmente se usa para probar aplicaciones web / API REST
  * Herramienta para testing de carga y rendimiento
  * Está basado en Java por tanto requiere JDK para ejecutarlo, JAVA_HOME
  * https://jmeter.apache.org/
  * C:\Users\X\Downloads\apache-jmeter-5.6.3\bin\jmeter

* Monitorizar la salud:
  * Opción 1: Spring Boot Actuator + Prometheus (DB) + Grafana (UI) montarlo con docker compose o kubernetes
  * Opción 2: New relic

* Logging:
  * Logstash (ingesta) + Elasticsearch (DB) + Kibana (UI)




## JVM OPTIONS

* Heap size: tamaño total de la memoria utilizada para almacenar objetos y datos en tiempo de ejecución.
  * -Xms tamaño inicial
  * -Xmx tamaño máximo
  * Ejemplo: -Xms1g -Xmx1g
  * Si superamos el heap size lanza: java.lang.OutOfMemoryError: Java Heap Space

* Used Heap
  *  memoria del heap que está en uso

* Metaspace:
  * Memoria separada del Heap (a partir de Java 8) para metadatos de clases, clases cargadas, métodos, etc.
  * Uso: -XX:MaxMetaSpaceSize=512m

* Garbage Collector
  * G1GC (activado por defecto Java 23)
    * grande cantidades de memoria
    * pausas cortas 200ms, ideal para aplicaciones con muchos usuarios concurrentes
    * Equilibrio entre rendimiento y baja latencia
    * Si se quiere reducir la latencia o modificarla: (Puede aumentar el uso de CPU)
      * -XX:MacGCPauseMillis:100

  * ZGC:
    * pausas muy cortas <10ms y heaps de memoria muy grandes de hasta terabytes
    * Tiene consumo de CPU más alto que el G1GC
    * Respuestas en tiempo real, tipo videojuegos, sistemas financieros, manejo de muchos datos en memoria
    * -XX:+UseZGC
  
  * ParallelGC: 
    * alto rendimiento y procesamiento muy intensivo
    * pausas más largas que el G1 y ZG


* < 10GB: G1GC
* > 10GB y < 100GB: Recomendable bajar latencia al G1GC o moverse ZGC
* > 100GB: ideal ZGC

* mvn clean verify package 
* Esto creará un .jar en la carpeta target
* Normal: java -jar app.jar
* Con VM Options:
  * Java: java -Xms10g -Xmx10g -XX:MaxMetaSpaceSize=5g -XX:MaxGCPauseMillis:100 -jar app.jar
  * Maven: ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xms10g -Xmx10g -XX:MaxMetaSpaceSize=5g -XX:MaxGCPauseMillis:100"
  * Gradle: ./gradlew bootRun --jvm-args="-Xms10g -Xmx10g -XX:MaxMetaSpaceSize=5g -XX:MaxGCPauseMillis:100"

## TEST DE RENDIMIENTO PARA COMPARATIVAS DE CONFIGURACIONES

Arrancar Apache JMeter

Abrir el plan.jmx que es un test de rendimiento que ya está creado

Ejecutar spring boot

Iniciar el test de Apache JMeter

Dura 100 segundos, esperar a que acabe y luego pulsar en Display graph.

Esperar los 100 segundos sin cambiar de pestaña en el Apache JMeter para que complete el test antes de ver resultados. 