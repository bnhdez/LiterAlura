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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
                    1 - Buscar Libro por nombre 
                    2 - Mostrar Libros registrados
                    3 - Mostrar autores registrados
                    4 - Mostrar autores vivos por años
                    5 - Mostrar por idiomas
                                  
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroPorNombre();
                    break;
                case 2:
                    mostrarLibrosRegistrados();
                    break;
                case 3:
                    mostrarAutoresRegistrados();
                    break;
                case 4:
                    mostrarAutoresPorFecha();
                    break;
                case 5:
                    mostrarPorIdiomas();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
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
}
