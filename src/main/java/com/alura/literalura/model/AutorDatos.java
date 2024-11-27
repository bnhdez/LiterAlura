package com.alura.literalura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AutorDatos(
        @JsonAlias("name") String name,
        @JsonAlias("birth_year") Integer birthday,
        @JsonAlias("death_year") Integer deathday
) {
}
