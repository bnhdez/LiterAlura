package com.alura.literalura.model;

import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String title;
    private String author;
    private String language;
    private Integer download_count;

    public Libro(){}

    public Libro(DatosLibro datosLibro) {
        this.title = datosLibro.title() != null ? datosLibro.title() : "Título desconocido";
        this.author = (datosLibro.authors() != null && !datosLibro.authors().isEmpty())
                ? datosLibro.authors().get(0).name()
                : "Autor desconocido";
        this.language = (datosLibro.languages() != null && !datosLibro.languages().isEmpty())
                ? datosLibro.languages().get(0)
                : "Idioma desconocido";
        this.download_count = datosLibro.download_count() != null ? datosLibro.download_count() : 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getDownload_count() {
        return download_count;
    }

    public void setDownload_count(Integer download_count) {
        this.download_count = download_count;
    }
}
