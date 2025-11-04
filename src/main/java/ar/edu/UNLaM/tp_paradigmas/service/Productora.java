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

    // NUEVO ATRIBUTO: Mapa para llevar la cuenta de las canciones por artista
    private Map<Artista, Integer> cancionesPorArtista;

    public Productora() {
        this.artistasBase = new ArrayList<>();
        this.artistasDisponibles = new ArrayList<>();
        this.recital = new Recital();
        this.lector = new LectorJSON();
        this.cancionesPorArtista = new HashMap<>(); // NUEVO: Inicializamos el mapa
    }

    // --- 1. Carga de Datos ---

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

        // MODIFICADO: Llenamos el mapa de conteo al cargar los datos
        List<Artista> todosLosCandidatos = Stream.concat(this.artistasBase.stream(), this.artistasDisponibles.stream())
                .collect(Collectors.toList());
        for (Artista artista : todosLosCandidatos) {
            this.cancionesPorArtista.put(artista, 0); // Todos empiezan con 0 canciones
        }
    }

    // --- 2. Lógica de Contratación (Núcleo) ---

    public void contratarArtistasParaRecital() {
        // MODIFICADO: Juntamos a todos los artistas
        List<Artista> todosLosCandidatos = new ArrayList<>(this.cancionesPorArtista.keySet());

        // MODIFICADO: El mapa de conteo 'cancionesPorArtista' ya no se crea aquí,
        // usamos el atributo de la clase.

        for (Cancion cancion : this.recital.getCanciones()) {
            // Llamamos a la lógica interna de contratación para esta canción
            this.ejecutarContratacionPara(cancion, todosLosCandidatos);
        }
    }

    // --- 3. Lógica del Menú ---

    /**
     * (Función 3) Contrata artistas solo para una canción específica.
     * ESTA ES LA NUEVA LÓGICA QUE PEDISTE
     */
    public void contratarArtistasParaCancion(String tituloCancion) {
        // 1. Buscar la canción
        Optional<Cancion> cancionOpt = this.recital.getCanciones().stream()
                .filter(c -> c.getTitulo().equalsIgnoreCase(tituloCancion))
                .findFirst();

        if (cancionOpt.isEmpty()) {
            System.out.println("Error: No se encontró la canción '" + tituloCancion + "'.");
            return;
        }

        Cancion cancion = cancionOpt.get();

        // 2. Obtener candidatos
        List<Artista> todosLosCandidatos = new ArrayList<>(this.cancionesPorArtista.keySet());

        // 3. Ejecutar la contratación solo para esa canción
        this.ejecutarContratacionPara(cancion, todosLosCandidatos);

        System.out.println("Proceso de contratación finalizado para '" + tituloCancion + "'.");
        if(cancion.estaCubierta()){
            System.out.println("La canción ahora está: Completa.");
        } else {
            System.out.println("La canción aún tiene roles faltantes.");
        }
    }

    /**
     * MÉTODO PRIVADO: Lógica de asignación reutilizable
     * Este método es llamado por contratarArtistasParaRecital() y contratarArtistasParaCancion()
     */
    private void ejecutarContratacionPara(Cancion cancion, List<Artista> todosLosCandidatos) {

        // --- ARREGLO 3 ---
        // (Creamos un set temporal para esta canción)
        Set<Artista> artistasYaAsignadosEnEstaCancion = new HashSet<>();

        for (Asignacion asignacion : cancion.getAsignacionesSinCubrir()) {

            Artista mejorCandidato = null;
            double costoMinimo = Double.MAX_VALUE;

            for (Artista candidato : todosLosCandidatos) {

                // --- ARREGLO 3 (Continuación) ---
                // (Verificamos que no esté ya asignado EN ESTA CANCIÓN)
                if (artistasYaAsignadosEnEstaCancion.contains(candidato)) {
                    continue; // Saltar a la siguiente iteración del bucle
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

                // --- ARREGLO 3 (Continuación) ---
                // (Lo añadimos al set de esta canción)
                artistasYaAsignadosEnEstaCancion.add(mejorCandidato);

                this.cancionesPorArtista.put(mejorCandidato, this.cancionesPorArtista.get(mejorCandidato) + 1);
            }
        }
    }


    private double calcularCostoContratacion(Artista candidato) {
        if (this.artistasBase.contains(candidato)) {
            return 0; // Artistas de la base no tienen costo
        }

        double costo = candidato.getCostoPorCancion();

        // Verificamos si comparte banda con alguien YA contratado
        for (Artista contratado : this.recital.getArtistasContratados()) {

            // --- ARREGLO 2 ---
            // (Añadimos la condición !candidato.equals(contratado))
            if (!candidato.equals(contratado) && candidato.comparteBandaCon(contratado)) {
                return costo * 0.50; // Aplica descuento
            }
        }

        return costo; // Sin descuento
    }

    // ... (El resto de tus funciones: getRolesFaltantes, entrenarArtista, etc. no cambian) ...

    public Map<String, Long> getRolesFaltantesParaCancion(String tituloCancion) {
        // ... (sin cambios)
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
        // ... (sin cambios)
        return this.recital.getCanciones().stream()
                .flatMap(cancion -> cancion.getAsignacionesSinCubrir().stream())
                .collect(Collectors.groupingBy(Asignacion::getRolRequerido, Collectors.counting()));
    }

    public boolean entrenarArtista(String nombreArtista, String nuevoRol) {
        // ... (sin cambios)
        Optional<Artista> artistaOpt = Stream.concat(
                        this.artistasBase.stream(),
                        this.artistasDisponibles.stream()
                )
                .filter(a -> a.getNombre().equalsIgnoreCase(nombreArtista))
                .findFirst();

        if (artistaOpt.isPresent()) {
            Artista artista = artistaOpt.get();
            if (this.recital.getArtistasContratados().contains(artista)) {
                return false;
            }
            artista.agregarRol(nuevoRol);
            artista.setCostoPorCancion(artista.getCostoPorCancion() * 1.50);
            return true;
        }
        return false;
    }

    public Set<Artista> getArtistasContratados() {
        // ... (sin cambios)
        return this.recital.getArtistasContratados();
    }

    public Map<String, String> getEstadoCanciones() {
        // ... (sin cambios)
        return this.recital.getCanciones().stream()
                .collect(Collectors.toMap(
                        Cancion::getTitulo,
                        cancion -> cancion.estaCubierta() ? "Completa" : "Faltan roles"
                ));
    }

    // ... (Getters para tests sin cambios) ...
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