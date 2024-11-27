package com.alura.literalura.principal;

import com.alura.literalura.model.Autor;
import com.alura.literalura.model.DatosAutor;
import com.alura.literalura.model.DatosLibro;
import com.alura.literalura.model.Libro;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private final String url = "https://gutendex.com/books/";
    private List<DatosLibro> datosLibros = new ArrayList<>();
    private List<DatosAutor> datosAutores = new ArrayList<>();
    private List<Libro> libros;
    private List<Autor> autores;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
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
                """;
            System.out.println(menu);
            System.out.print("> ");
            opcion = teclado.nextInt();
            teclado.nextLine(); // Limpiar el buffer

            switch (opcion) {
                case 1 -> buscarLibroPorNombre();
                case 2 -> mostrarLibrosRegistrados();
                case 3 -> mostrarAutoresRegistrados();
                case 4 -> mostrarAutoresPorFecha();
                case 5 -> mostrarPorIdiomas();
                case 6 -> mostrarEstadisticasLibros();
                case 7 -> mostrarTopLibrosMasDescargados();
                case 8 -> buscarAutorPorNombre();
                case 9 -> listarAutoresPorAtributo();
                case 0 -> System.out.println("Cerrando la aplicación...");
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    private void buscarLibroPorNombre() {
        System.out.println("Escribe el nombre del libro que desea buscar");
        var nombreLibro = teclado.nextLine();

        String urlFinal = url + "?search=" + URLEncoder.encode(nombreLibro, StandardCharsets.UTF_8);
        System.out.println("URL construida: " + urlFinal);

        var json = consumoAPI.obtenerDatos(urlFinal);
        System.out.println("Respuesta JSON: " + json);

        try {
            // Deserializar la clave `results` como una lista de DatosLibro
            var rootNode = conversor.obtenerDatos(json, Map.class);
            var results = (List<Map<String, Object>>) rootNode.get("results");

            if (results != null && !results.isEmpty()) {
                for (var result : results) {
                    // Convertir cada resultado individualmente a DatosLibro
                    var datosLibroJson = conversor.obtenerDatos(new ObjectMapper().writeValueAsString(result), DatosLibro.class);
                    Libro libro = new Libro(datosLibroJson);

                    // Guardar los autores asociados al libro
                    for (DatosAutor datosAutor : datosLibroJson.authors()) {
                        Autor autor = new Autor(datosAutor);
                        try {
                            // Verificar si el autor ya existe
                            if (!autorRepository.existsByName(autor.getName())) {
                                autorRepository.save(autor);
                                System.out.println("Autor guardado correctamente: " + autor.getName());
                            } else {
                                System.out.println("El autor ya existe en la base de datos: " + autor.getName());
                            }
                        } catch (Exception e) {
                            System.out.println("Error al guardar el autor: " + e.getMessage());
                        }
                    }

                    // Guardar el libro en la base de datos
                    try {
                        libroRepository.save(libro);
                        System.out.println("Libro guardado correctamente: " + libro.getTitle());
                    } catch (Exception e) {
                        System.out.println("Error al guardar el libro: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("No se encontraron libros para el término de búsqueda proporcionado.");
            }
        } catch (Exception e) {
            System.out.println("Error al procesar los datos del JSON: " + e.getMessage());
        }
    }

    private void mostrarLibrosRegistrados() {
        try {
            libros = libroRepository.findAll();
            if (libros.isEmpty()) {
                System.out.println("No hay libros registrados...");
                System.out.println("Presione Enter para continuar...");
                teclado.nextLine();
            } else {
                libros.forEach(l -> {
                    System.out.printf("""
                            Libro: %s
                            Autor: %s
                            Idioma: %s
                            Descargas: %s
                            %n""", l.getTitle(), l.getAuthor(), l.getLanguage(), l.getDownload_count().toString());
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mostrarAutoresRegistrados() {
        try {
            autores = autorRepository.findAll();
            if (autores.isEmpty()) {
                System.out.println("No hay libros registrados...");
                System.out.println("Presione Enter para continuar...");
                teclado.nextLine();
            } else {
                autores.forEach(a -> {
                    System.out.printf("""
                            Autor: %s
                            Nacimiento: %s
                            Fallecimiento: %s
                            %n""",
                            a.getName(), a.getBirth_day() != null ? a.getBirth_day().toString() : "No se encuentra fecha de nacimiento",
                            a.getDeath_day() != null ? a.getDeath_day().toString() : "En la actualidad");
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mostrarAutoresPorFecha() {
        System.out.println("Ingrese el año para buscar autores que estaban vivos en ese periodo:");
        System.out.print("> ");
        int anio = teclado.nextInt();
        teclado.nextLine(); // Limpiar el buffer

        try {
            // Llamar al repositorio para obtener los autores vivos en el año especificado
            List<Autor> autoresVivos = autorRepository.findAuthorsAliveInYear(anio);

            // Mostrar resultados
            if (autoresVivos.isEmpty()) {
                System.out.println("No se encontraron autores vivos en el año " + anio);
            } else {
                System.out.println("Autores vivos en el año " + anio + ":");
                autoresVivos.forEach(a -> {
                    System.out.printf("""
                        Autor: %s
                        Nacimiento: %s
                        Fallecimiento: %s
                        %n""",
                            a.getName(),
                            a.getBirth_day() != null ? a.getBirth_day() : "Desconocido",
                            a.getDeath_day() != null ? a.getDeath_day() : "Aún vivo");
                });
            }
        } catch (Exception e) {
            System.out.println("Ocurrió un error al consultar los autores: " + e.getMessage());
        }
    }

    private void mostrarPorIdiomas() {
        System.out.println("""
            Seleccione un idioma para mostrar los libros registrados:
            1: Inglés (en)
            2: Español (es)
            3: Francés (fr)
            4: Alemán (de)
            5: Italiano (it)
            6: Otro idioma
            """);
        System.out.print("> ");
        int opcion = teclado.nextInt();
        teclado.nextLine(); // Limpiar el buffer

        String idioma = null;

        // Mapear la opción seleccionada a un código de idioma
        switch (opcion) {
            case 1 -> idioma = "en";
            case 2 -> idioma = "es";
            case 3 -> idioma = "fr";
            case 4 -> idioma = "de";
            case 5 -> idioma = "it";
            case 6 -> {
                System.out.println("Ingrese el código del idioma (ISO 639-1, por ejemplo: en, es, fr):");
                System.out.print("> ");
                idioma = teclado.nextLine();
            }
            default -> {
                System.out.println("Opción inválida. Volviendo al menú principal...");
                return;
            }
        }

        try {
            // Consultar libros por idioma en la base de datos
            List<Libro> librosPorIdioma = libroRepository.findByLanguage(idioma);

            // Mostrar resultados
            if (librosPorIdioma.isEmpty()) {
                System.out.println("No se encontraron libros registrados en el idioma seleccionado (" + idioma + ").");
            } else {
                System.out.println("Libros disponibles en el idioma seleccionado (" + idioma + "):");
                librosPorIdioma.forEach(libro -> System.out.printf("""
                    Título: %s
                    Autor: %s
                    Idioma: %s
                    Descargas: %d
                    %n""",
                        libro.getTitle(),
                        libro.getAuthor(),
                        libro.getLanguage(),
                        libro.getDownload_count()));
            }
        } catch (Exception e) {
            System.out.println("Ocurrió un error al consultar los libros: " + e.getMessage());
        }
    }

    private void mostrarEstadisticasLibros() {
        try {
            // Obtener todos los libros de la base de datos
            List<Libro> libros = libroRepository.findAll();

            if (libros.isEmpty()) {
                System.out.println("No hay libros registrados para generar estadísticas.");
                return;
            }

            // Generar estadísticas con streams
            DoubleSummaryStatistics stats = libros.stream()
                    .mapToDouble(Libro::getDownload_count)
                    .summaryStatistics();

            // Mostrar estadísticas
            System.out.println("Estadísticas de descargas de libros:");
            System.out.printf("Número total de descargas: %.0f%n", stats.getSum());
            System.out.printf("Promedio de descargas: %.2f%n", stats.getAverage());
            System.out.printf("Máximo número de descargas: %.0f%n", stats.getMax());
            System.out.printf("Mínimo número de descargas: %.0f%n", stats.getMin());
        } catch (Exception e) {
            System.out.println("Ocurrió un error al generar las estadísticas: " + e.getMessage());
        }
    }

    private void mostrarTopLibrosMasDescargados() {
        try {
            // Consultar los 10 libros más descargados
            List<Libro> topLibros = libroRepository.findTop10ByOrderByDownloadCountDesc(PageRequest.of(0, 10));

            if (topLibros.isEmpty()) {
                System.out.println("No hay libros registrados.");
                return;
            }

            // Mostrar el top 10
            System.out.println("Top 10 libros más descargados:");
            topLibros.forEach(libro -> System.out.printf("""
                Título: %s
                Autor: %s
                Descargas: %d
                %n""",
                    libro.getTitle(),
                    libro.getAuthor(),
                    libro.getDownload_count()));
        } catch (Exception e) {
            System.out.println("Ocurrió un error al obtener el top 10 de libros: " + e.getMessage());
        }
    }

    private void buscarAutorPorNombre() {
        System.out.println("Ingrese el nombre del autor a buscar:");
        System.out.print("> ");
        String nombreAutor = teclado.nextLine();

        try {
            // Consultar autores por nombre en la base de datos
            List<Autor> autores = autorRepository.findByName(nombreAutor);

            if (autores.isEmpty()) {
                System.out.println("El autor no se encuentra en la base de datos. Buscando en la API...");

                // Consultar la API
                String urlFinal = url + "?search=" + URLEncoder.encode(nombreAutor, StandardCharsets.UTF_8);
                var json = consumoAPI.obtenerDatos(urlFinal);

                // Procesar el JSON y buscar autores
                var rootNode = conversor.obtenerDatos(json, Map.class);
                var results = (List<Map<String, Object>>) rootNode.get("results");

                if (results == null || results.isEmpty()) {
                    System.out.println("No se encontraron autores en la API con el nombre: " + nombreAutor);
                    return;
                }

                // Extraer los datos de los autores y almacenarlos en la base de datos
                for (var result : results) {
                    var datosAutorJson = conversor.obtenerDatos(new ObjectMapper().writeValueAsString(result.get("authors")), DatosAutor[].class);

                    for (DatosAutor datosAutor : datosAutorJson) {
                        Autor autor = new Autor(datosAutor);

                        if (!autorRepository.existsByName(autor.getName())) {
                            autorRepository.save(autor);
                            System.out.println("Autor guardado correctamente: " + autor.getName());
                        } else {
                            System.out.println("El autor ya existe en la base de datos: " + autor.getName());
                        }
                    }
                }

                // Volver a consultar en la base de datos
                autores = autorRepository.findByName(nombreAutor);
            }

            // Mostrar los autores encontrados
            if (autores.isEmpty()) {
                System.out.println("No se encontraron autores con el nombre: " + nombreAutor);
            } else {
                System.out.println("Autores encontrados:");
                autores.forEach(a -> System.out.printf("""
                    Nombre: %s
                    Año de nacimiento: %s
                    Año de fallecimiento: %s
                    %n""",
                        a.getName(),
                        a.getBirth_day() != null ? a.getBirth_day() : "Desconocido",
                        a.getDeath_day() != null ? a.getDeath_day() : "Aún vivo"));
            }
        } catch (Exception e) {
            System.out.println("Ocurrió un error al buscar el autor: " + e.getMessage());
        }
    }

    private void listarAutoresPorAtributo() {
        System.out.println("""
            ¿Qué atributo deseas consultar?
            1: Año de nacimiento
            2: Año de fallecimiento
            """);
        System.out.print("> ");
        int opcion = teclado.nextInt();
        teclado.nextLine(); // Limpiar el buffer

        System.out.println("Ingrese el año a consultar:");
        System.out.print("> ");
        int anio = teclado.nextInt();
        teclado.nextLine(); // Limpiar el buffer

        try {
            List<Autor> autores = switch (opcion) {
                case 1 -> autorRepository.findByBirthYear(anio);
                case 2 -> autorRepository.findByDeathYear(anio);
                default -> {
                    System.out.println("Opción inválida. Volviendo al menú principal...");
                    yield new ArrayList<>();
                }
            };

            if (autores.isEmpty()) {
                System.out.println("No se encontraron autores en el año especificado.");
            } else {
                System.out.println("Autores encontrados:");
                autores.forEach(a -> System.out.printf("""
                    Nombre: %s
                    Año de nacimiento: %s
                    Año de fallecimiento: %s
                    %n""",
                        a.getName(),
                        a.getBirth_day() != null ? a.getBirth_day() : "Desconocido",
                        a.getDeath_day() != null ? a.getDeath_day() : "Aún vivo"));
            }
        } catch (Exception e) {
            System.out.println("Ocurrió un error al consultar los autores: " + e.getMessage());
        }
    }
}
