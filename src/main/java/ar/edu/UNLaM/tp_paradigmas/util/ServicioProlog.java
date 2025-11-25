package ar.edu.UNLaM.tp_paradigmas.util;

import ar.edu.UNLaM.tp_paradigmas.model.Artista;
import ar.edu.UNLaM.tp_paradigmas.model.Cancion;
import org.jpl7.*;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ServicioProlog {

    public ServicioProlog() {
        Query q1 = new Query("consult", new Term[]{new Atom("archivos_fuente/recital.pl")});
        System.out.println("Cargando 'recital.pl' ... " + (q1.hasSolution() ? "OK" : "FALLÓ"));
        limpiarHechosCargados();
    }

    public int getEntrenamientosMinimos(List<Artista> artistasBase, List<Artista> todosLosArtistas, List<Cancion> canciones) {
        limpiarHechosCargados();
        cargarBaseProlog(artistasBase, todosLosArtistas, canciones);

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

    private void cargarBaseProlog(List<Artista> artistasBase, List<Artista> todosLosArtistas, List<Cancion> canciones) {
        for (Artista artista : todosLosArtistas) {
            Term nombre = new Atom(formatearNombre(artista.getNombre()));
            Term roles = Util.termArrayToList(artista.getRoles().stream().map(Atom::new).toArray(Term[]::new));
            assertHecho("artista", nombre, roles);
        }

        for (Artista artistaBase : artistasBase) {
            Term nombre = new Atom(formatearNombre(artistaBase.getNombre()));
            assertHecho("base_member", nombre);
        }

        for (Cancion cancion : canciones) {
            for (String rol : cancion.getRolesRequeridos()) {
                assertHecho("rol_requerido", new Atom(rol));
            }
        }
    }

    private void limpiarHechosCargados() {
        retractAll("base_member", 1);
        retractAll("artista", 2);
        retractAll("rol_requerido", 1);
    }

    private void retractAll(String predicate, int arity) {
        Term[] vars = IntStream.range(0, arity)
                .mapToObj(i -> new Variable("_" + i))
                .toArray(Term[]::new);
        Query q = new Query("retractall", new Term[]{new Compound(predicate, vars)});
        q.hasSolution();
    }

    private void assertHecho(String predicate, Term... args) {
        Query q = new Query("assertz", new Term[]{new Compound(predicate, args)});
        q.hasSolution();
    }

    private String formatearNombre(String nombre) {
        return nombre.toLowerCase().replace(" ", "_");
    }

}