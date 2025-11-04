package ar.edu.UNLaM.tp_paradigmas.util;

// Importa todas las clases de jpl7
import org.jpl7.*;

import java.util.Map;

public class ServicioProlog {

    public ServicioProlog() {
        // 1. Cargamos nuestro archivo .pl
        // Usamos "new Atom(...)" como en el ejemplo Family.java
        Query q1 = new Query("consult", new Term[]{new Atom("recital.pl")});

        // Verificamos si la carga fue exitosa
        System.out.println("Cargando 'recital.pl' ... " + (q1.hasSolution() ? "OK" : "FALLÓ"));
    }

    /**
     * Prueba simple: Pregunta a Prolog qué roles necesitan entrenamiento
     * basándonos en la regla 'necesita_entrenamiento(Rol)'.
     */
    public void consultarRolesSinCobertura() {
        System.out.println("\n--- Consultando roles sin cobertura ---");

        // 1. Definimos la variable 'Rol' (la 'X' de nuestro ejemplo)
        Variable Rol = new Variable("Rol");

        // 2. Creamos la consulta: "necesita_entrenamiento(Rol)."
        Query q = new Query("necesita_entrenamiento", new Term[]{Rol});

        // 3. Iteramos por todas las soluciones (como en el ejemplo)
        //    usando hasMoreSolutions() y nextSolution()
        while (q.hasMoreSolutions()) {
            Map<String, Term> solucion = q.nextSolution();

            // 4. Obtenemos el valor de la variable "Rol"
            System.out.println("Rol que necesita entrenamiento: " + solucion.get(Rol.name));
        }
    }

    /**
     * Prueba simple: Pregunta quiénes pueden cubrir un rol específico.
     */
    public void consultarQuienCubre(String rol) {
        System.out.println("\n--- Consultando quién puede cubrir '" + rol + "' ---");

        Variable Artista = new Variable("Artista");
        Atom rolBuscado = new Atom(rol); // Convertimos el String de Java a un Átomo de Prolog

        Query q = new Query("puede_cubrir", new Term[]{Artista, rolBuscado});

        Map<String, Term>[] soluciones = q.allSolutions(); // Usamos el método allSolutions()

        if (soluciones.length == 0) {
            System.out.println("Nadie puede cubrir ese rol.");
            return;
        }

        for (Map<String, Term> sol : soluciones) {
            System.out.println("Puede cubrirlo: " + sol.get(Artista.name));
        }
    }

    /**
     * Responde a la consigna del TP (Punto 8).
     * Llama al predicado 'entrenamientos_minimos(Cantidad)' y devuelve el número.
     */
    public int getEntrenamientosMinimos() {
        // 1. Definimos la variable que queremos averiguar (Cantidad)
        Variable Cantidad = new Variable("Cantidad");

        // 2. Creamos la consulta: "entrenamientos_minimos(Cantidad)."
        Query q = new Query("entrenamientos_minimos", new Term[]{Cantidad});

        // 3. Esta consulta solo tiene una solución (un número),
        //    así que usamos oneSolution()
        Map<String, Term> solucion = q.oneSolution();

        if (solucion != null) {
            // 4. Extraemos el término (que es un Integer de Prolog)
            //    y lo convertimos a un int de Java.
            return solucion.get(Cantidad.name).intValue();
        } else {
            System.err.println("Error: La consulta 'entrenamientos_minimos' no arrojó solución.");
            return -1; // Devolvemos -1 para indicar un error
        }
    }

}