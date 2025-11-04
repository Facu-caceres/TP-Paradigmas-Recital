package ar.edu.UNLaM.tp_paradigmas.model;

public class Asignacion {

    private String rolRequerido;
    private Artista artistaAsignado;

    /**
     * Crea una nueva Asignación (un puesto a cubrir).
     * El artista asignado comienza siendo null.
     */
    public Asignacion(String rolRequerido) {
        this.rolRequerido = rolRequerido;
        this.artistaAsignado = null; // Nadie ha sido asignado a este puesto todavía
    }

    // --- Getters y Setters ---

    public String getRolRequerido() {
        return rolRequerido;
    }

    public Artista getArtistaAsignado() {
        return artistaAsignado;
    }

    /**
     * Asigna un artista para cubrir este puesto.
     */
    public void setArtistaAsignado(Artista artistaAsignado) {
        this.artistaAsignado = artistaAsignado;
    }

    // --- Métodos de Ayuda ---

    /**
     * Verifica si este puesto ya ha sido cubierto.
     */
    public boolean estaCubierta() {
        return this.artistaAsignado != null;
    }
}