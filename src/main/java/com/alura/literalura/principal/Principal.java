package com.alura.literalura.principal;

import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;

import java.util.Scanner;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {
    }
}
