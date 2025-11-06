package ar.edu.UNLaM.tp_paradigmas.service;

import ar.edu.UNLaM.tp_paradigmas.model.*;
import ar.edu.UNLaM.tp_paradigmas.util.LectorJSON;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Productora {

    private List<Artista> artistasBase;
    private List<Artista> artistasDisponibles;
    private Recital recital;
    private LectorJSON lector;

    // Mapa para llevar la cuenta de las canciones por artista
    private Map<Artista, Integer> cancionesPorArtista;

    public Productora() {
        this.artistasBase = new ArrayList<>();
        this.artistasDisponibles = new ArrayList<>();
        this.recital = new Recital();
        this.lector = new LectorJSON();
        this.cancionesPorArtista = new HashMap<>();
    }

    // --- Carga de Datos ---
    public void cargarDatos(String rutaArtistas, String rutaRecital, String rutaArtistasDiscografica) {
        this.artistasDisponibles = lector.cargarArtistas(rutaArtistas);
        List<String> nombresBase = lector.cargarArtistasBase(rutaArtistasDiscografica);

        for (String nombreBase : nombresBase) {
            Optional<Artista> artistaOpt = this.artistasDisponibles.stream()
                    .filter(a -> a.getNombre().equalsIgnoreCase(nombreBase))
                    .findFirst();

            if (artistaOpt.isPresent()) {
                Artista artista = artistaOpt.get();
                artista.setCostoPorCancion(0);
                this.artistasBase.add(artista);
                this.artistasDisponibles.remove(artista);
            }
        }

        List<Cancion> canciones = lector.cargarCanciones(rutaRecital);
        this.recital.setCanciones(canciones);

        // Llenamos el mapa de conteo al cargar los datos
        List<Artista> todosLosCandidatos = Stream.concat(this.artistasBase.stream(), this.artistasDisponibles.stream())
                .collect(Collectors.toList());
        for (Artista artista : todosLosCandidatos) {
            this.cancionesPorArtista.put(artista, 0); // Todos empiezan con 0 canciones
        }
    }

    // --- Lógica de Contratación ---
    public List<String> contratarArtistasParaRecital() {
        List<Artista> todosLosCandidatos = new ArrayList<>(this.cancionesPorArtista.keySet());

        List<String> rolesFallidosTotales = new ArrayList<>(); // lista con los roles que fallen

        for (Cancion cancion : this.recital.getCanciones()) {
            List<String> fallidosEstaCancion = this.ejecutarContratacionPara(cancion, todosLosCandidatos);
            rolesFallidosTotales.addAll(fallidosEstaCancion);
        }
        // Devolvemos una lista única de roles que fallaron en todo el recital
        return rolesFallidosTotales.stream().distinct().collect(Collectors.toList());
    }

    public List<String> contratarArtistasParaCancion(String tituloCancion) {
        Optional<Cancion> cancionOpt = this.recital.getCanciones().stream()
                .filter(c -> c.getTitulo().equalsIgnoreCase(tituloCancion))
                .findFirst();

        if (cancionOpt.isEmpty()) {
            System.out.println("Error: No se encontró la canción '" + tituloCancion + "'.");
            // Devolvemos una lista indicando el error
            return Collections.singletonList("ERROR: CANCION_NO_ENCONTRADA");
        }
        Cancion cancion = cancionOpt.get();
        List<Artista> todosLosCandidatos = new ArrayList<>(this.cancionesPorArtista.keySet());
        return this.ejecutarContratacionPara(cancion, todosLosCandidatos);
    }

    /*
       Lógica de asignación reutilizable
       Este metodo es llamado por contratarArtistasParaRecital() y contratarArtistasParaCancion()
       retorna una lista de los roles que no pudieron ser cubiertos.
     */
    private List<String> ejecutarContratacionPara(Cancion cancion, List<Artista> todosLosCandidatos) {

        Set<Artista> artistasYaAsignadosEnEstaCancion = new HashSet<>();
        List<String> rolesFallidos = new ArrayList<>();

        for (Asignacion asignacion : cancion.getAsignacionesSinCubrir()) {

            Artista mejorCandidato = null;
            double costoMinimo = Double.MAX_VALUE;

            for (Artista candidato : todosLosCandidatos) {

                if (artistasYaAsignadosEnEstaCancion.contains(candidato)) {
                    continue;
                }

                boolean puedeCubrirRol = candidato.puedeCubrirRol(asignacion.getRolRequerido());
                boolean tieneCupo = this.cancionesPorArtista.get(candidato) < candidato.getMaxCanciones();

                if (puedeCubrirRol && tieneCupo) {
                    double costoActual = calcularCostoContratacion(candidato);

                    if (costoActual < costoMinimo) {
                        costoMinimo = costoActual;
                        mejorCandidato = candidato;
                    }
                }
            }
            if (mejorCandidato != null) {
                asignacion.setArtistaAsignado(mejorCandidato);
                this.recital.agregarArtistaContratado(mejorCandidato);
                artistasYaAsignadosEnEstaCancion.add(mejorCandidato);
                this.cancionesPorArtista.put(mejorCandidato, this.cancionesPorArtista.get(mejorCandidato) + 1);
            } else {
                rolesFallidos.add(asignacion.getRolRequerido());
            }
        }
        // Devolvemos la lista de roles que no pudimos cubrir
        return rolesFallidos;
    }


    private double calcularCostoContratacion(Artista candidato) {
        if (this.artistasBase.contains(candidato)) {
            return 0;
        }

        double costo = candidato.getCostoPorCancion();

        // Verificamos si comparte banda con alguien ya contratado
        for (Artista contratado : this.recital.getArtistasContratados()) {

            if (!candidato.equals(contratado) && candidato.comparteBandaCon(contratado)) {
                return costo * 0.50; // Aplica descuento
            }
        }
        return costo; // Sin descuento
    }

    private double calcularCostoCancion(Cancion cancion) {
        double costoTotal = 0;

        for (Asignacion asignacion : cancion.getAsignaciones()) {
            Artista artista = asignacion.getArtistaAsignado();

            if (artista != null) {
                costoTotal += this.calcularCostoContratacion(artista);
            }
        }
        return costoTotal;
    }


    public Map<String, Long> getRolesFaltantesParaCancion(String tituloCancion) {
        Optional<Cancion> cancionOpt = this.recital.getCanciones().stream()
                .filter(c -> c.getTitulo().equalsIgnoreCase(tituloCancion))
                .findFirst();

        if (cancionOpt.isPresent()) {
            return cancionOpt.get().getAsignacionesSinCubrir().stream()
                    .collect(Collectors.groupingBy(Asignacion::getRolRequerido, Collectors.counting()));
        }
        return new HashMap<>();
    }

    public Map<String, Long> getRolesFaltantesTotales() {
        return this.recital.getCanciones().stream()
                .flatMap(cancion -> cancion.getAsignacionesSinCubrir().stream())
                .collect(Collectors.groupingBy(Asignacion::getRolRequerido, Collectors.counting()));
    }

    public boolean entrenarArtista(String nombreArtista, String nuevoRol) {
        Optional<Artista> artistaOpt = Stream.concat(
                        this.artistasBase.stream(),
                        this.artistasDisponibles.stream()
                )
                .filter(a -> a.getNombre().equalsIgnoreCase(nombreArtista))
                .findFirst();

        if (artistaOpt.isPresent()) {
            Artista artista = artistaOpt.get();
            // Verificamos si el artista está en la lista base o si ya fue contratado.
            if (this.artistasBase.contains(artista) || this.recital.getArtistasContratados().contains(artista)) {
                return false; // Error: No se puede entrenar un artista base o uno ya contratado
            }
            artista.agregarRol(nuevoRol);
            artista.setCostoPorCancion(artista.getCostoPorCancion() * 1.50);
            return true;
        }
        return false; // El artista no existe
    }

    public Set<Artista> getArtistasContratados() {
        return this.recital.getArtistasContratados();
    }

    public Map<String, String> getEstadoCanciones() {
        return this.recital.getCanciones().stream()
                .collect(Collectors.toMap(
                        Cancion::getTitulo,
                        cancion -> {
                            if (cancion.estaCubierta()) {
                                double costo = this.calcularCostoCancion(cancion);
                                return "Completa (Costo: $" + costo + ")";
                            } else {
                                return "Faltan roles";
                            }
                        }
                ));
    }

    public Recital getRecital() {
        return recital;
    }
    public List<Artista> getArtistasDisponibles() {
        return artistasDisponibles;
    }
    public List<Artista> getArtistasBase() {
        return artistasBase;
    }
    public Map<Artista, Integer> getCancionesPorArtista() {
        return cancionesPorArtista;
    }
}