package ar.edu.UNLaM.tp_paradigmas.model;

public class Asignacion {

    private String rolRequerido;
    private Artista artistaAsignado;

    /*
      Crea una nueva Asignación (un puesto a cubrir).
      El artista asignado comienza siendo null.
     */
    public Asignacion(String rolRequerido) {
        this.rolRequerido = rolRequerido;
        this.artistaAsignado = null;
    }

    // --- Getters y Setters ---
    public String getRolRequerido() {
        return rolRequerido;
    }
    public Artista getArtistaAsignado() {
        return artistaAsignado;
    }
    public void setArtistaAsignado(Artista artistaAsignado) {
        this.artistaAsignado = artistaAsignado;
    }

    //Verifica si este puesto ya ha sido cubierto.
    public boolean estaCubierta() {
        return this.artistaAsignado != null;
    }
}