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

    /**
     * Constructor para crear un Artista.
     * Anotado para que Jackson sepa cómo usarlo.
     */
    @JsonCreator // <-- 1. Le dice a Jackson que use ESTE constructor
    public Artista(
            @JsonProperty("nombre") String nombre,
            @JsonProperty("roles") List<String> roles,
            @JsonProperty("bandas") List<String> bandas,
            @JsonProperty("costo") double costoPorCancion, // <-- 2. Mapea "costo" del JSON a la variable
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

    // --- Setters (para lógica de negocio) ---

    public void setCostoPorCancion(double costoPorCancion) {
        this.costoPorCancion = costoPorCancion;
    }

    public void agregarRol(String nuevoRol) {
        if (!this.roles.contains(nuevoRol)) {
            this.roles.add(nuevoRol);
        }
    }

    // --- Métodos de Ayuda (Lógica) ---

    /**
     * Verifica si el artista puede desempeñar un rol específico.
     */
    public boolean puedeCubrirRol(String rol) {
        return this.roles.contains(rol);
    }

    /**
     * Verifica si este artista comparte al menos una banda con otro artista.
     * Esto es clave para calcular el descuento del 50%.
     */
    public boolean comparteBandaCon(Artista otroArtista) {
        // Collections.disjoint devuelve 'true' si las listas NO tienen elementos en común.
        // Por lo tanto, devolvemos lo contrario (negación).
        return !Collections.disjoint(this.bandas, otroArtista.getBandas());
    }

    // --- Métodos OBLIGATORIOS para Colecciones (Set, Map) ---

    /**
     * Define que dos artistas son "iguales" si tienen el mismo nombre.
     * Esto es crucial para que Set<Artista> funcione y no haya duplicados.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artista artista = (Artista) o;
        return Objects.equals(nombre, artista.nombre);
    }

    /**
     * Acompaña a equals(). Si dos objetos son equals(), deben tener el mismo hashCode().
     */
    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}
