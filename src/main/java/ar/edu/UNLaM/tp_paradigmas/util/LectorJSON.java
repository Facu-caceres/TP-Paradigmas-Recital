package ar.edu.UNLaM.tp_paradigmas.util;

import ar.edu.UNLaM.tp_paradigmas.model.Artista;
import ar.edu.UNLaM.tp_paradigmas.model.Cancion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Clase de utilidad responsable de leer los archivos .json
 * y convertirlos en objetos Java (POJOs) usando la librería Jackson.
 */
public class LectorJSON {

    private ObjectMapper mapper;

    public LectorJSON() {
        // ObjectMapper es el objeto principal de Jackson para la conversión
        this.mapper = new ObjectMapper();
    }

    /**
     * Carga el archivo artistas.json (o similar) y lo convierte en una Lista de Artistas.
     */
    public List<Artista> cargarArtistas(String rutaArchivo) {
        try {
            // Lee el archivo y usa TypeReference para indicarle a Jackson
            // que queremos una Lista de Artistas (List<Artista>)
            return mapper.readValue(new File(rutaArchivo), new TypeReference<List<Artista>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de artistas: " + e.getMessage());
            e.printStackTrace();
            // Devuelve una lista vacía en caso de error para evitar que el programa falle
            return Collections.emptyList();
        }
    }

    /**
     * Carga el archivo recital.json (o similar) y lo convierte en una Lista de Canciones.
     * Jackson usará los setters de la clase Cancion (setTitulo y setRolesRequeridos)
     * para construir los objetos.
     */
    public List<Cancion> cargarCanciones(String rutaArchivo) {
        try {
            return mapper.readValue(new File(rutaArchivo), new TypeReference<List<Cancion>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer el archivo del recital: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Carga el archivo artistas-discografica.json (o similar) y lo convierte en una
     * simple Lista de Strings (nombres).
     */
    public List<String> cargarArtistasBase(String rutaArchivo) {
        try {
            return mapper.readValue(new File(rutaArchivo), new TypeReference<List<String>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de artistas base: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}