package ar.edu.UNLaM.tp_paradigmas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cancion {

    private String titulo;
    private List<Asignacion> asignaciones;

    /**
     * Constructor para crear una Canción.
     * Recibe los roles como Strings (ej: ["voz", "guitarra", "voz"])
     * y los convierte internamente en objetos Asignacion.
     */
    public Cancion(String titulo, List<String> rolesRequeridos) {
        this.titulo = titulo;
        this.asignaciones = new ArrayList<>();

        // Bucle para convertir cada String "rol" en un objeto "Asignacion"
        for (String rol : rolesRequeridos) {
            this.asignaciones.add(new Asignacion(rol));
        }
    }

    // Constructor vacío necesario para algunas librerías JSON (como Jackson)
    // No lo uses directamente, pero es bueno tenerlo.
    public Cancion() {
        this.asignaciones = new ArrayList<>();
    }


    // --- Getters ---

    public String getTitulo() {
        return titulo;
    }

    public List<Asignacion> getAsignaciones() {
        return asignaciones;
    }

    // Setters (necesarios para que el lector JSON Jackson funcione correctamente)

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Setter especial para Jackson. Convierte la lista de roles (Strings)
     * del JSON en la lista de objetos Asignacion.
     */
    public void setRolesRequeridos(List<String> rolesRequeridos) {
        for (String rol : rolesRequeridos) {
            this.asignaciones.add(new Asignacion(rol));
        }
    }


    // --- Métodos de Ayuda ---

    /**
     * Verifica si todos los puestos de la canción ya fueron cubiertos.
     */
    public boolean estaCubierta() {
        // Devuelve true si TODAS las asignaciones de la lista están cubiertas
        return this.asignaciones.stream().allMatch(Asignacion::estaCubierta);
    }

    /**
     * Devuelve una lista de los puestos que aún no tienen un artista asignado.
     */
    public List<Asignacion> getAsignacionesSinCubrir() {
        // Filtra la lista y devuelve solo las asignaciones NO cubiertas
        return this.asignaciones.stream()
                .filter(asignacion -> !asignacion.estaCubierta())
                .collect(Collectors.toList());
    }
}