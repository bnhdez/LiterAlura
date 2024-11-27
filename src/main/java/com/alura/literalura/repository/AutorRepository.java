package com.alura.literalura.repository;

import com.alura.literalura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    @Query("SELECT a FROM Autor a WHERE a.birth_day <= :year AND (a.death_day IS NULL OR a.death_day >= :year) ORDER BY a.birth_day ASC")
    List<Autor> findAuthorsAliveInYear(Integer year);

    boolean existsByName(String name);
}
