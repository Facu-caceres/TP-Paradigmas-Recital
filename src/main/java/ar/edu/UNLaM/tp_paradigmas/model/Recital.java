package ar.edu.UNLaM.tp_paradigmas.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Recital {

    private List<Cancion> canciones;
    private Set<Artista> artistasContratados; // Usamos Set para evitar duplicados

    public Recital() {
        this.canciones = new ArrayList<>();
        this.artistasContratados = new HashSet<>();
    }

    // --- Getters ---
    public List<Cancion> getCanciones() {
        return canciones;
    }
    public Set<Artista> getArtistasContratados() {
        return artistasContratados;
    }
    // --- Métodos para modificar el estado ---

    // Establece la lista completa de canciones del recital
    public void setCanciones(List<Cancion> canciones) {
        this.canciones = canciones;
    }

    // Agrega un artista al conjunto de artistas contratados para el evento.
    public void agregarArtistaContratado(Artista artista) {
        this.artistasContratados.add(artista);
    }
}