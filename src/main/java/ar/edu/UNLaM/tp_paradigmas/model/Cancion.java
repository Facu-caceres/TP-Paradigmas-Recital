package ar.edu.UNLaM.tp_paradigmas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cancion {

    private String titulo;
    private List<Asignacion> asignaciones;

    public Cancion(String titulo, List<String> rolesRequeridos) {
        this.titulo = titulo;
        this.asignaciones = new ArrayList<>();
        for (String rol : rolesRequeridos) {
            this.asignaciones.add(new Asignacion(rol));
        }
    }

    // Constructor vacío necesario para algunas librerías JSON (como Jackson)
    public Cancion() {
        this.asignaciones = new ArrayList<>();
    }

    // Getters
    public String getTitulo() {
        return titulo;
    }
    public List<Asignacion> getAsignaciones() {
        return asignaciones;
    }
    // Setters
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    /*
       Setter especial para Jackson. Convierte la lista de roles (Strings)
       del JSON en la lista de objetos Asignacion.
     */
    public void setRolesRequeridos(List<String> rolesRequeridos) {
        for (String rol : rolesRequeridos) {
            this.asignaciones.add(new Asignacion(rol));
        }
    }

    /*
      Verifica si todos los puestos de la canción ya fueron cubiertos.
      Devuelve true si TODAS las asignaciones de la lista están cubiertas
     */
    public boolean estaCubierta() {

        return this.asignaciones.stream().allMatch(Asignacion::estaCubierta);
    }


    // Devuelve una lista de los puestos que aún no tienen un artista asignado.

    public List<Asignacion> getAsignacionesSinCubrir() {
        return this.asignaciones.stream()
                .filter(asignacion -> !asignacion.estaCubierta())
                .collect(Collectors.toList());
    }
}