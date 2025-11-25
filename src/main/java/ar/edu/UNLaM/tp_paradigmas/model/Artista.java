package ar.edu.UNLaM.tp_paradigmas.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Artista {

    private String nombre;
    private List<String> roles;
    private List<String> bandas;
    private double costoPorCancion;
    private int maxCanciones;

    @JsonCreator // Constructor anotado para que Jackson sepa como utilizarlo
    public Artista(
            @JsonProperty("nombre") String nombre,
            @JsonProperty("roles") List<String> roles,
            @JsonProperty("bandas") List<String> bandas,
            @JsonProperty("costo") double costoPorCancion,
            @JsonProperty("maxCanciones") int maxCanciones) {

        this.nombre = nombre;
        this.roles = roles;
        this.bandas = bandas;
        this.costoPorCancion = costoPorCancion;
        this.maxCanciones = maxCanciones;
    }

    // --- Getters ---
    public String getNombre() {
        return nombre;
    }
    public List<String> getRoles() {
        return roles;
    }
    public List<String> getBandas() {
        return bandas;
    }
    public double getCostoPorCancion() {
        return costoPorCancion;
    }
    public int getMaxCanciones() {
        return maxCanciones;
    }

    // --- Setters  ---
    public void setCostoPorCancion(double costoPorCancion) {
        this.costoPorCancion = costoPorCancion;
    }
    public void agregarRol(String nuevoRol) {
        if (!this.roles.contains(nuevoRol)) {
            this.roles.add(nuevoRol);
        }
    }

    // --- Métodos de Ayuda ---

    public boolean puedeCubrirRol(String rol) {
        return this.roles.contains(rol);
    }

    public boolean comparteBandaCon(Artista otroArtista) {
        return !Collections.disjoint(this.bandas, otroArtista.getBandas());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artista artista = (Artista) o;
        return Objects.equals(nombre, artista.nombre);
    }

    // Si dos objetos son equals(), deben tener el mismo hashCode().
    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}
