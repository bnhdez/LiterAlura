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
    }

    private void mostrarAutoresPorFecha() {
    }

    private void mostrarPorIdiomas() {
    }
}
