package com.alura.literalura.model;

import jakarta.persistence.*;

@Entity
@Table(name = "autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private Integer birth_day;
    private Integer death_day;

    public Autor() {
    }

    public Autor(DatosAutor datosAutor) {
        this.name = datosAutor.name();
        this.birth_day = datosAutor.birthday();
        this.death_day = datosAutor.deathday();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBirth_day() {
        return birth_day;
    }

    public void setBirth_day(Integer birth_day) {
        this.birth_day = birth_day;
    }

    public Integer getDeath_day() {
        return death_day;
    }

    public void setDeath_day(Integer death_day) {
        this.death_day = death_day;
    }
}
