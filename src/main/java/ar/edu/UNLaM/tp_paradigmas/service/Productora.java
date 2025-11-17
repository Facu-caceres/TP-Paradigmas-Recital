package ar.edu.UNLaM.tp_paradigmas.service;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;

import ar.edu.UNLaM.tp_paradigmas.model.Artista;
import ar.edu.UNLaM.tp_paradigmas.model.Asignacion;
import ar.edu.UNLaM.tp_paradigmas.model.Cancion;
import ar.edu.UNLaM.tp_paradigmas.model.Recital;
import ar.edu.UNLaM.tp_paradigmas.util.LectorJSON;

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
    // ===============================================================
    // BONUS 2: Arrepentimiento - quitar artista del recital
    // ===============================================================
    /**
     * Elimina todas las participaciones de un artista en el recital
     * (de todas las canciones) y ajusta el conteo de cancionesPorArtista
     * y el conjunto de artistas contratados.
     *
     * @param nombreArtista nombre del artista a quitar
     * @return true si se eliminó al menos una participación
     */
    public boolean quitarArtistaDelRecital(String nombreArtista) {
        boolean seQuitoAlgunaParticipacion = false;
        Artista artistaObjetivo = null;
        // Recorremos todas las canciones y asignaciones
        for (Cancion cancion : recital.getCanciones()) {
            for (Asignacion asignacion : cancion.getAsignaciones()) {
                Artista asignado = asignacion.getArtistaAsignado();
                if (asignado != null && asignado.getNombre().equalsIgnoreCase(nombreArtista)) {
                    artistaObjetivo = asignado;
                    asignacion.setArtistaAsignado(null);
                    seQuitoAlgunaParticipacion = true;
                    // Ajustar contador de canciones por artista
                    Integer actual = cancionesPorArtista.getOrDefault(asignado, 0);
                    if (actual > 0) {
                        cancionesPorArtista.put(asignado, actual - 1);
                    }
                }
            }
        }
        if (!seQuitoAlgunaParticipacion || artistaObjetivo == null) {
            return false;
        }
        // Verificamos si aún queda alguna asignación con ese artista
        boolean sigueAsignado = recital.getCanciones().stream()
                .flatMap(c -> c.getAsignaciones().stream())
                .anyMatch(a -> {
                    Artista art = a.getArtistaAsignado();
                    return art != null && art.getNombre().equalsIgnoreCase(nombreArtista);
                });
        if (!sigueAsignado) {
            recital.getArtistasContratados().removeIf(a -> a.getNombre().equalsIgnoreCase(nombreArtista));
        }
        return true;
    }
    // ===============================================================
    // BONUS 3: Historial de colaboraciones (grafo simple)
    // ===============================================================
    /**
     * Construye un grafo simple de colaboraciones entre artistas.
     * Dos artistas colaboran si:
     *  - Comparten una canción en este recital, o
     *  - Comparten al menos una banda en su historial.
     *
     * El resultado es un mapa: nombreArtista -> conjunto de nombres con los que colaboró.
     */
    public Map<String, Set<String>> getGrafoColaboraciones() {
        Map<String, Set<String>> grafo = new TreeMap<>();
        // 1) Colaboraciones por canción compartida
        for (Cancion cancion : recital.getCanciones()) {
            List<Artista> artistasEnCancion = cancion.getAsignaciones().stream()
                    .map(Asignacion::getArtistaAsignado)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            for (int i = 0; i < artistasEnCancion.size(); i++) {
                for (int j = i + 1; j < artistasEnCancion.size(); j++) {
                    Artista a1 = artistasEnCancion.get(i);
                    Artista a2 = artistasEnCancion.get(j);
                    agregarAristaColaboracion(grafo, a1.getNombre(), a2.getNombre());
                }
            }
        }
        // 2) Colaboraciones por banda compartida (historial)
        List<Artista> todos = new ArrayList<>(cancionesPorArtista.keySet());
        for (int i = 0; i < todos.size(); i++) {
            for (int j = i + 1; j < todos.size(); j++) {
                Artista a1 = todos.get(i);
                Artista a2 = todos.get(j);
                if (a1.comparteBandaCon(a2)) {
                    agregarAristaColaboracion(grafo, a1.getNombre(), a2.getNombre());
                }
            }
        }
        return grafo;
    }
    private void agregarAristaColaboracion(Map<String, Set<String>> grafo, String a, String b) {
        grafo.computeIfAbsent(a, k -> new TreeSet<>()).add(b);
        grafo.computeIfAbsent(b, k -> new TreeSet<>()).add(a);
    }
    // ===============================================================
    // BONUS 5: Datos de origen - Guardar / Cargar estado en JSON
    // ===============================================================
    /**
     * Guarda el estado actual del recital en un archivo JSON.
     * Formato:
     * {
     *   "costoTotalRecital": 1234.0,
     *   "canciones": [
     *      {
     *        "titulo": "...",
     *        "completa": true/false,
     *        "costo": 100.0,
     *        "asignaciones": [
     *           {"rol": "...", "artista": "Nombre" | null}
     *        ]
     *      }, ...
     *   ]
     * }
     */
    public boolean guardarEstadoRecitalEnArchivo(String rutaArchivo) {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        List<Map<String, Object>> cancionesResumen = new ArrayList<>();
        double costoTotalRecital = 0.0;
        for (Cancion cancion : recital.getCanciones()) {
            Map<String, Object> datosCancion = new LinkedHashMap<>();
            datosCancion.put("titulo", cancion.getTitulo());
            boolean completa = cancion.estaCubierta();
            datosCancion.put("completa", completa);
            double costoCancion = calcularCostoCancion(cancion);
            datosCancion.put("costo", costoCancion);
            costoTotalRecital += costoCancion;
            List<Map<String, Object>> asignacionesResumen = new ArrayList<>();
            for (Asignacion asignacion : cancion.getAsignaciones()) {
                Map<String, Object> datosAsign = new LinkedHashMap<>();
                datosAsign.put("rol", asignacion.getRolRequerido());
                Artista artista = asignacion.getArtistaAsignado();
                datosAsign.put("artista", artista != null ? artista.getNombre() : null);
                asignacionesResumen.add(datosAsign);
            }
            datosCancion.put("asignaciones", asignacionesResumen);
            cancionesResumen.add(datosCancion);
        }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("costoTotalRecital", costoTotalRecital);
        root.put("cantidadArtistasContratados", recital.getArtistasContratados().size());
        root.put("canciones", cancionesResumen);
        try {
            mapper.writeValue(new File(rutaArchivo), root);
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar el estado del recital: " + e.getMessage());
            return false;
        }
    }
    /**
     * Carga un estado de recital previamente guardado con
     * {@link #guardarEstadoRecitalEnArchivo(String)}.
     *
     * Se reasignan los artistas a las canciones, se recalcula el
     * conjunto de artistas contratados y se reinician los contadores.
     */
    public boolean cargarEstadoRecitalDesdeArchivo(String rutaArchivo) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(new File(rutaArchivo));
            JsonNode cancionesNode = root.get("canciones");
            if (cancionesNode == null || !cancionesNode.isArray()) {
                System.err.println("Formato inválido de archivo de estado (no hay 'canciones').");
                return false;
            }
            // 1) Limpiar estado actual
            for (Cancion cancion : recital.getCanciones()) {
                for (Asignacion asignacion : cancion.getAsignaciones()) {
                    asignacion.setArtistaAsignado(null);
                }
            }
            recital.getArtistasContratados().clear();
            // Reiniciar contadores
            cancionesPorArtista.replaceAll((artista, v) -> 0);
            // Lista de todos los artistas conocidos para resolver nombres
            List<Artista> todosArtistas = new ArrayList<>(cancionesPorArtista.keySet());
            // 2) Reconstruir estado desde el archivo
            for (JsonNode cancionNode : cancionesNode) {
                String titulo = cancionNode.get("titulo").asText();
                Optional<Cancion> cancionOpt = recital.getCanciones().stream()
                        .filter(c -> c.getTitulo().equalsIgnoreCase(titulo))
                        .findFirst();
                if (cancionOpt.isEmpty()) {
                    // Canción no existe en este recital -> la ignoramos
                    continue;
                }
                Cancion cancion = cancionOpt.get();
                JsonNode asignsNode = cancionNode.get("asignaciones");
                if (asignsNode == null || !asignsNode.isArray()) {
                    continue;
                }
                List<Asignacion> asignaciones = cancion.getAsignaciones();
                for (int i = 0; i < asignsNode.size() && i < asignaciones.size(); i++) {
                    JsonNode asignNode = asignsNode.get(i);
                    String nombreArtista = asignNode.get("artista").isNull()
                            ? null
                            : asignNode.get("artista").asText();
                    Asignacion asignacion = asignaciones.get(i);
                    if (nombreArtista == null || nombreArtista.isBlank()) {
                        asignacion.setArtistaAsignado(null);
                    } else {
                        Optional<Artista> artistaOpt = todosArtistas.stream()
                                .filter(a -> a.getNombre().equalsIgnoreCase(nombreArtista))
                                .findFirst();
                        if (artistaOpt.isPresent()) {
                            Artista artista = artistaOpt.get();
                            asignacion.setArtistaAsignado(artista);
                            recital.agregarArtistaContratado(artista);
                            int actual = cancionesPorArtista.getOrDefault(artista, 0);
                            cancionesPorArtista.put(artista, actual + 1);
                        } else {
                            // Artista desconocido en este conjunto de datos: lo dejamos sin asignar
                            asignacion.setArtistaAsignado(null);
                        }
                    }
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error al cargar el estado del recital: " + e.getMessage());
            return false;
        }
    }
}