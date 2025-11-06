package ar.edu.UNLaM.tp_paradigmas.util;

import org.jpl7.*;
import java.util.Map;

public class ServicioProlog {

    public ServicioProlog() {
        // Cargamos nuestro archivo .pl
        Query q1 = new Query("consult", new Term[]{new Atom("archivos_fuente/recital.pl")});
        // Verificamos si la carga fue exitosa
        System.out.println("Cargando 'recital.pl' ... " + (q1.hasSolution() ? "OK" : "FALLÓ"));
    }

    /*
       Prueba simple: Pregunta a Prolog qué roles necesitan entrenamiento
       basándonos en la regla 'necesita_entrenamiento(Rol)'.
     */
    public void consultarRolesSinCobertura() {
        System.out.println("\n--- Consultando roles sin cobertura ---");

        Variable Rol = new Variable("Rol");

        Query q = new Query("necesita_entrenamiento", new Term[]{Rol});

        // Iteramos por todas las soluciones
        while (q.hasMoreSolutions()) {
            Map<String, Term> solucion = q.nextSolution();
            System.out.println("Rol que necesita entrenamiento: " + solucion.get(Rol.name));
        }
    }

    public void consultarQuienCubre(String rol) {
        System.out.println("\n--- Consultando quién puede cubrir '" + rol + "' ---");

        Variable Artista = new Variable("Artista");
        Atom rolBuscado = new Atom(rol); // Convertimos el String de Java a un Átomo de Prolog

        Query q = new Query("puede_cubrir", new Term[]{Artista, rolBuscado});

        Map<String, Term>[] soluciones = q.allSolutions(); // Usamos el metodo allSolutions()

        if (soluciones.length == 0) {
            System.out.println("Nadie puede cubrir ese rol.");
            return;
        }

        for (Map<String, Term> sol : soluciones) {
            System.out.println("Puede cubrirlo: " + sol.get(Artista.name));
        }
    }

    // Logica para el punto 8
    public int getEntrenamientosMinimos() {

        Variable Cantidad = new Variable("Cantidad");

        Query q = new Query("entrenamientos_minimos", new Term[]{Cantidad});

        Map<String, Term> solucion = q.oneSolution();

        if (solucion != null) {
            return solucion.get(Cantidad.name).intValue();
        } else {
            System.err.println("Error: La consulta 'entrenamientos_minimos' no arrojó solución.");
            return -1;
        }
    }

}