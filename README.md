# Proyecto: Gestor de Libros y Autores

## Descripción
Esta aplicación en Java permite gestionar información sobre libros y autores utilizando una API pública y una base de datos PostgreSQL. Ofrece funcionalidades para realizar búsquedas, almacenar datos y realizar consultas desde una interfaz de línea de comandos.

## Funcionalidades
1. **Buscar libro por nombre:** Realiza una búsqueda en la API y guarda los resultados en la base de datos.
2. **Mostrar libros registrados:** Lista todos los libros almacenados localmente.
3. **Mostrar autores registrados:** Muestra todos los autores registrados en la base de datos.
4. **Autores vivos por años:** Lista autores que estaban vivos en un año específico.
5. **Libros por idiomas:** Filtra libros por el idioma seleccionado.
6. **Estadísticas de libros:** Genera estadísticas sobre descargas.
7. **Top 10 libros más descargados:** Lista los libros con mayor número de descargas.
8. **Buscar autor por nombre:** Busca autores en la base de datos y, si no existen, los consulta en la API y los almacena.
9. **Listar autores por atributos:** Filtra autores por año de nacimiento o fallecimiento.

## Requisitos
- **Java 17 o superior**
- **Spring Boot 3.0 o superior**
- **PostgreSQL 13 o superior**

## Configuración
1. **Clona el repositorio:**
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd <NOMBRE_DEL_PROYECTO>
   ```
2. **Configura PostgreSQL:**
    - Crea una base de datos llamada `literalura`.
    - Ajusta las credenciales en `application.properties`:
      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/literalura
      spring.datasource.username=TU_USUARIO
      spring.datasource.password=TU_CONTRASEÑA
      spring.jpa.hibernate.ddl-auto=update
      ```
3. **Ejecuta el programa:** Ejecuta la clase principal `Principal` desde tu IDE.

## Uso
El programa presenta un menú en la terminal con opciones numeradas para acceder a las distintas funcionalidades. Por ejemplo:
```text
======= Menú Principal =======
1 - Buscar Libro por nombre
2 - Mostrar Libros registrados
3 - Mostrar autores registrados
4 - Mostrar autores vivos por años
5 - Mostrar por idiomas
6 - Generar estadísticas de libros
7 - Mostrar Top 10 libros más descargados
8 - Buscar autor por nombre
9 - Listar autores por año de nacimiento o fallecimiento
0 - Salir
=============================
> 1
```

## Características Técnicas
- **Persistencia:** Usa Spring Data JPA para interactuar con PostgreSQL.
- **API Externa:** Consume datos desde `https://gutendex.com/books/`.
- **Deserialización:** Usa Jackson para manejar JSON.
- **Interfaz:** Basada en línea de comandos.

¡Explora y amplía esta aplicación según tus necesidades! Si tienes preguntas, no dudes en contactarme.

