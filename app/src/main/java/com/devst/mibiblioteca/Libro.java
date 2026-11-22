package com.devst.mibiblioteca;

import com.google.firebase.firestore.Exclude;

// Modelo: representa un libro tal como se guarda en Firestore
public class Libro {

    private String id;      // ID del documento en Firestore (no se guarda como campo)
    private String titulo;
    private String autor;
    private String genero;
    private int anio;

    // Firestore necesita un constructor vacío para convertir documentos en objetos
    public Libro() {
    }

    public Libro(String titulo, String autor, String genero, int anio) {
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        this.anio = anio;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    // Así se muestra cada libro en la lista
    @Override
    public String toString() {
        return titulo + " — " + autor + "\n" + genero + " · " + anio;
    }
}