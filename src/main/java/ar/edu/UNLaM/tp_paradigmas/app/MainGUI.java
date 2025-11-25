package ar.edu.UNLaM.tp_paradigmas.app;

import ar.edu.UNLaM.tp_paradigmas.model.Artista;
import ar.edu.UNLaM.tp_paradigmas.model.Cancion;
import ar.edu.UNLaM.tp_paradigmas.service.Productora;
import ar.edu.UNLaM.tp_paradigmas.util.ServicioProlog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MainGUI extends Application {

    private Productora productora;
    private ServicioProlog servicioProlog;
    private Stage ventanaPrincipal;
    private AudioClip sonidoClick;

    // --- ESTILOS GENERALES ---
    private final String ESTILO_FONDO = "-fx-background-color: #121212;";
    private final String ESTILO_TITULO = "-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';";
    private final String ESTILO_SUBTITULO = "-fx-text-fill: #cccccc; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;";
    private final String ESTILO_BASE_BOTON =
            "-fx-pref-width: 220; -fx-pref-height: 100; " +
                    "-fx-background-radius: 15; -fx-border-radius: 15; " +
                    "-fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1; " +
                    "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);";

    // Rutas
    final String RUTA_ARTISTAS = "archivos_prueba/artistas.json";
    final String RUTA_RECITAL = "archivos_prueba/recital.json";
    final String RUTA_ARTISTAS_BASE = "archivos_prueba/artistas-discografica.json";
    final String RUTA_ESTADO_SALIDA = "archivos_salida/recital-out.json";
    final String RUTA_ESTADO_GUARDADO = "archivos_salida/recital-guardado.json";

    @Override
    public void init() {
        productora = new Productora();
        servicioProlog = new ServicioProlog();
        try {
            productora.cargarDatos(RUTA_ARTISTAS, RUTA_RECITAL, RUTA_ARTISTAS_BASE);
        } catch (Exception e) {
            System.err.println("Error iniciando: " + e.getMessage());
        }
        try {
            var recurso = getClass().getResource("/sonidos/click.mp3"); // Opcional si usas wav en todo
            if (recurso != null) sonidoClick = new AudioClip(recurso.toExternalForm());
        } catch (Exception e) { /* Ignorar */ }
    }

    @Override
    public void start(Stage stage) {
        this.ventanaPrincipal = stage;
        mostrarMenuPrincipal();
        stage.setTitle("Gestión de Recitales - Visual 🎵");
        stage.show();
    }

    private void mostrarMenuPrincipal() {
        VBox layout = new VBox(20);

        // Fondo
        try {
            var recursoFondo = getClass().getResource("/imagenes/fondo.jpg");
            if (recursoFondo != null) {
                String url = recursoFondo.toExternalForm().replace(" ", "%20");
                layout.setStyle("-fx-background-image: url(\"" + url + "\"); -fx-background-size: cover; -fx-background-position: center; -fx-background-repeat: no-repeat;");
            } else layout.setStyle(ESTILO_FONDO);
        } catch (Exception e) { layout.setStyle(ESTILO_FONDO); }

        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Gestión de Recitales");
        titulo.setStyle(ESTILO_TITULO);

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(20); grid.setAlignment(Pos.CENTER);

        // Columna 1: Consultas
        VBox col1 = new VBox(15);
        col1.setAlignment(Pos.TOP_CENTER);
        col1.getChildren().addAll(
                crearLabelSubtitulo("CONSULTAS"),
                crearBotonImagen("Roles Faltantes (Canción)", "/iconos/Roles Faltantes (Canción).png", "Roles Faltantes (Canción).wav", e -> consultaRolesCancion()),
                crearBotonImagen("Roles Faltantes (Total)", "/iconos/Roles Faltantes (Total).png", "Roles Faltantes (Total).wav", e -> consultaRolesTotales()),
                crearBotonImagen("Estado Canciones", "/iconos/Estado Canciones.png", "EstadoCanciones.wav", e -> verEstadoCancionesTabla()),
                crearBotonImagen("Historial Colaboraciones", "/iconos/Historial Colaboraciones.png", "Historial Colaboraciones.wav", e -> verGrafoColaboraciones())
        );

        // Columna 2: Operaciones
        VBox col2 = new VBox(15);
        col2.setAlignment(Pos.TOP_CENTER);
        col2.getChildren().addAll(
                crearLabelSubtitulo("OPERACIONES"),
                crearBotonImagen("Contratar (1 Canción)", "/iconos/Contratar(1 Canción).png", "Contratar (1 Canción).wav", e -> contratarParaCancion()),
                crearBotonImagen("Contratar (Recital)", "/iconos/Contratar (Recital).png", "Contratar (Recital).wav", e -> contratarTodoRecital()),
                crearBotonImagen("Entrenar Artista", "/iconos/Entrenar Artista.png", "Entrenar Artista.wav", e -> entrenarArtistaFlujoCompleto()),
                crearBotonImagen("Ver Contratados", "/iconos/Ver Artistas Contratados.png", "Ver Contratados.wav", e -> verContratadosTabla()),
                crearBotonImagen("Salir", "/iconos/Salir.png", "Salir.wav", e -> salirDelPrograma())
        );

        // Columna 3: Sistema
        VBox col3 = new VBox(15);
        col3.setAlignment(Pos.TOP_CENTER);
        col3.getChildren().addAll(
                crearLabelSubtitulo("SISTEMA"),
                crearBotonImagen("Guardar Estado", "/iconos/Guardar Estado.png", "Guardar Estado.wav", e -> guardarEstado()),
                crearBotonImagen("Cargar Estado", "/iconos/Cargar Estado.png", "Cargar Estado.wav", e -> cargarEstado()),
                crearBotonImagen("Consulta Prolog", "/iconos/Consulta Prolog.png", "Consulta Prolog.wav", e -> consultaProlog()),
                crearBotonImagen("Quitar Artista", "/iconos/Quitar Artista.png", "Quitar Artista.wav", e -> quitarArtistaDialogo())
        );

        grid.add(col1, 0, 0); grid.add(col2, 1, 0); grid.add(col3, 2, 0);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.getStylesheets().add("data:text/css,.scroll-pane > .viewport { -fx-background-color: transparent; }");

        layout.getChildren().addAll(titulo, new Separator(), scroll);
        ventanaPrincipal.setScene(new Scene(layout, 1100, 750));
    }

    // ========================================================================
    // ⚙️ LÓGICA DE NEGOCIO (ACTUALIZADA CON SELECTORES)
    // ========================================================================

    // --- 1. CONSULTAS ---
    private void consultaRolesCancion() {
        seleccionarCancion("Consultar Roles").ifPresent(titulo -> {
            Map<String, Long> data = productora.getRolesFaltantesParaCancion(titulo);
            if (data.isEmpty()) mostrarAlertaInfo("Info", "La canción '" + titulo + "' está completa o no existe.");
            else {
                long total = data.values().stream().mapToLong(Long::longValue).sum();
                mostrarTablaSimple("Roles faltantes: " + titulo + " (Total: " + total + ")", data, "Rol", "Faltantes");
            }
        });
    }

    private void consultaRolesTotales() {
        Map<String, Long> data = productora.getRolesFaltantesTotales();
        long total = data.values().stream().mapToLong(Long::longValue).sum();
        mostrarTablaSimple("Roles Faltantes Totales (Total: " + total + ")", data, "Rol", "Cantidad");
    }

    private void verEstadoCancionesTabla() {
        // Usamos el nuevo método detallado de Productora para mostrar info rica
        Map<String, String> estado = productora.getEstadoCancionesDetallado();

        TextArea area = new TextArea();
        area.setEditable(false);
        area.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");

        estado.forEach((titulo, detalle) -> {
            area.appendText("🎵 " + titulo + ":\n" + detalle + "\n-----------------------------------\n");
        });

        mostrarVistaGenerica("Estado Detallado del Recital", area);
    }

    // --- 2. CONTRATACIÓN (Con Selectores) ---
    private void contratarParaCancion() {
        seleccionarCancion("Contratar para Canción").ifPresent(titulo -> {
            List<String> fallidos = productora.contratarArtistasParaCancion(titulo);
            manejarResultadoContratacion(fallidos, true, titulo);
        });
    }

    private void contratarTodoRecital() {
        List<String> fallidos = productora.contratarArtistasParaRecital();
        manejarResultadoContratacion(fallidos, false, null);
    }

    private void manejarResultadoContratacion(List<String> fallidos, boolean esIndividual, String titulo) {
        if (fallidos.isEmpty()) {
            mostrarAlertaInfo("Éxito", "¡Contratación completada exitosamente!");
            verContratadosTabla();
        } else if (fallidos.get(0).equals("ERROR: CANCION_NO_ENCONTRADA")) {
            mostrarAlertaError("Error", "Error interno: Canción no encontrada.");
        } else {
            // Fallo: Ofrecer entrenar con selector
            String rolFaltante = fallidos.get(0);
            boolean entrenar = mostrarConfirmacion("Faltan Roles",
                    "No hay artistas para: " + fallidos + "\n¿Desea entrenar a alguien para el rol '" + rolFaltante + "'?");

            if (entrenar) {
                // Usamos el selector de artistas disponibles (lógica actualizada del Main)
                seleccionarArtistaDisponible("Entrenar para: " + rolFaltante).ifPresent(artistaNombre -> {
                    if (productora.entrenarArtista(artistaNombre, rolFaltante)) {
                        mostrarAlertaInfo("Entrenamiento", "¡" + artistaNombre + " entrenado! Reintentando contratación...");
                        // Recursión para reintentar
                        if (esIndividual) {
                            List<String> nuevos = productora.contratarArtistasParaCancion(titulo);
                            manejarResultadoContratacion(nuevos, true, titulo);
                        } else {
                            List<String> nuevos = productora.contratarArtistasParaRecital();
                            manejarResultadoContratacion(nuevos, false, null);
                        }
                    } else {
                        mostrarAlertaError("Error", "No se pudo entrenar (Artista base o ya sabe el rol).");
                    }
                });
            }
        }
    }

    // --- 3. ENTRENAMIENTO (Flujo manual completo) ---
    private void entrenarArtistaFlujoCompleto() {
        // Paso 1: Elegir Artista
        seleccionarArtistaDisponible("Paso 1: Elegir Artista").ifPresent(artista -> {
            // Paso 2: Elegir Rol
            seleccionarRol("Paso 2: Elegir Rol a enseñar").ifPresent(rol -> {
                if (productora.entrenarArtista(artista, rol)) {
                    mostrarAlertaInfo("Éxito", "¡" + artista + " ahora sabe " + rol + "!");
                } else {
                    mostrarAlertaError("Error", "No se pudo entrenar (Ya sabe el rol o restricción).");
                }
            });
        });
    }

    // --- 4. LISTADOS Y BONUS ---
    private void verContratadosTabla() {
        TableView<Artista> tabla = new TableView<>();

        TableColumn<Artista, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNombre()));

        TableColumn<Artista, String> colCosto = new TableColumn<>("Costo ($)");
        colCosto.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getCostoPorCancion())));

        // Columna especial para mostrar qué tocan (Nueva lógica Main.java)
        TableColumn<Artista, String> colTemas = new TableColumn<>("Participaciones");
        Map<Artista, List<String>> participaciones = productora.getParticipacionesPorArtista();
        colTemas.setCellValueFactory(p -> {
            List<String> temas = participaciones.getOrDefault(p.getValue(), List.of());
            return new SimpleStringProperty(temas.isEmpty() ? "-" : String.join(", ", temas));
        });

        tabla.getColumns().addAll(colNombre, colCosto, colTemas);
        tabla.getItems().addAll(productora.getArtistasContratados());

        mostrarVistaGenerica("Artistas Contratados", tabla);
    }

    private void quitarArtistaDialogo() {
        seleccionarArtistaContratado("Quitar Artista").ifPresent(nombre -> {
            if (productora.quitarArtistaDelRecital(nombre)) {
                mostrarAlertaInfo("Éxito", "Se han removido las participaciones de " + nombre);
                verContratadosTabla();
            } else {
                mostrarAlertaError("Error", "No se encontraron participaciones activas.");
            }
        });
    }

    private void verGrafoColaboraciones() {
        Map<String, Set<String>> grafo = productora.getGrafoColaboraciones();
        // Convertimos a formato compatible para tabla simple
        Map<String, String> dataView = grafo.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(", ", e.getValue())));
        mostrarTablaSimple("Historial de Colaboraciones", dataView, "Artista", "Colaboradores");
    }

    private void consultaProlog() {
        int n = servicioProlog.getEntrenamientosMinimos(productora.getArtistasBase(), productora.getRecital().getCanciones());
        mostrarAlertaInfo("Prolog", "Entrenamientos mínimos necesarios: " + n);
    }

    private void guardarEstado() {
        if(productora.guardarEstadoRecitalEnArchivo(RUTA_ESTADO_GUARDADO)) mostrarAlertaInfo("Guardar", "Guardado OK.");
        else mostrarAlertaError("Error", "Falló al guardar.");
    }

    private void cargarEstado() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Manual", "Manual", "Salida Automática");
        dialog.setTitle("Cargar Estado");
        dialog.setHeaderText("Seleccione origen de datos:");
        dialog.showAndWait().ifPresent(res -> {
            String ruta = res.equals("Manual") ? RUTA_ESTADO_GUARDADO : RUTA_ESTADO_SALIDA;
            if (productora.cargarEstadoRecitalDesdeArchivo(ruta)) {
                mostrarAlertaInfo("Cargar", "Datos cargados correctamente.");
                verEstadoCancionesTabla();
            } else mostrarAlertaError("Error", "Falló la carga del archivo.");
        });
    }

    private void salirDelPrograma() {
        productora.guardarEstadoRecitalEnArchivo(RUTA_ESTADO_SALIDA);
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override public void run() { Platform.exit(); System.exit(0); }
        }, 1000);
    }

    // ========================================================================
    // 🛠️ UTILIDADES DE SELECCIÓN (CHOICE DIALOGS)
    // ========================================================================

    private Optional<String> seleccionarCancion(String tituloVentana) {
        List<String> canciones = productora.getRecital().getCanciones().stream()
                .map(Cancion::getTitulo).toList();
        if (canciones.isEmpty()) { mostrarAlertaInfo("Vacío", "No hay canciones."); return Optional.empty(); }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(canciones.get(0), canciones);
        dialog.setTitle(tituloVentana);
        dialog.setHeaderText("Seleccione una canción:");
        return dialog.showAndWait();
    }

    private Optional<String> seleccionarArtistaDisponible(String tituloVentana) {
        List<String> disponibles = productora.getArtistasDisponibles().stream()
                .filter(a -> !productora.getRecital().getArtistasContratados().contains(a)) // Filtrar ya contratados
                .map(a -> a.getNombre() + " ($" + a.getCostoPorCancion() + ")")
                .toList();

        if (disponibles.isEmpty()) { mostrarAlertaInfo("Vacío", "No hay artistas disponibles."); return Optional.empty(); }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(disponibles.get(0), disponibles);
        dialog.setTitle(tituloVentana);
        dialog.setHeaderText("Seleccione un artista:");
        // Devolvemos solo el nombre (quitamos el precio del string)
        return dialog.showAndWait().map(s -> s.split(" \\(")[0]);
    }

    private Optional<String> seleccionarArtistaContratado(String tituloVentana) {
        List<String> contratados = productora.getRecital().getArtistasContratados().stream()
                .map(Artista::getNombre).toList();
        if (contratados.isEmpty()) { mostrarAlertaInfo("Vacío", "No hay artistas contratados."); return Optional.empty(); }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(contratados.get(0), contratados);
        dialog.setTitle(tituloVentana);
        dialog.setHeaderText("Seleccione artista a remover:");
        return dialog.showAndWait();
    }

    private Optional<String> seleccionarRol(String tituloVentana) {
        List<String> roles = productora.getRolesRequeridosDelRecital();
        if (roles.isEmpty()) { mostrarAlertaInfo("Vacío", "No hay roles definidos."); return Optional.empty(); }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(roles.get(0), roles);
        dialog.setTitle(tituloVentana);
        dialog.setHeaderText("Seleccione el rol:");
        return dialog.showAndWait();
    }

    // --- UTILIDADES VISUALES ---

    private Label crearLabelSubtitulo(String texto) {
        Label l = new Label(texto);
        l.setStyle(ESTILO_SUBTITULO);
        return l;
    }

    private <T> void mostrarTablaSimple(String titulo, Map<String, T> data, String col1Name, String col2Name) {
        TableView<Map.Entry<String, T>> tabla = new TableView<>();
        TableColumn<Map.Entry<String, T>, String> c1 = new TableColumn<>(col1Name);
        c1.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
        TableColumn<Map.Entry<String, T>, String> c2 = new TableColumn<>(col2Name);
        c2.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getValue())));
        tabla.getColumns().addAll(c1, c2);
        tabla.getItems().addAll(data.entrySet());
        mostrarVistaGenerica(titulo, tabla);
    }

    private void mostrarVistaGenerica(String titulo, javafx.scene.Node contenido) {
        VBox layout = new VBox(20);
        layout.setStyle(ESTILO_FONDO);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        Label lbl = new Label(titulo);
        lbl.setStyle(ESTILO_TITULO);

        if (contenido instanceof Control) {
            ((Control) contenido).setPrefWidth(800);
            ((Control) contenido).setPrefHeight(500);
        }
        VBox.setVgrow(contenido, Priority.ALWAYS);

        Button btnVolver = new Button("Volver al Menú");
        btnVolver.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");
        btnVolver.setOnAction(e -> mostrarMenuPrincipal());

        layout.getChildren().addAll(lbl, contenido, btnVolver);
        ventanaPrincipal.setScene(new Scene(layout, 1000, 700));
    }

    private Button crearBotonImagen(String texto, String rutaImagen, String archivoSonido, javafx.event.EventHandler<javafx.event.ActionEvent> accion) {
        Button btn = new Button(texto);
        btn.setAlignment(Pos.BOTTOM_CENTER);
        btn.setPadding(new Insets(0, 0, 10, 0));
        btn.setWrapText(true);
        btn.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        btn.setOnAction(event -> {
            reproducirSonido(archivoSonido);
            Platform.runLater(() -> { try { accion.handle(event); } catch (Exception e) { e.printStackTrace(); } });
        });

        String estiloImagen = "-fx-background-color: #333;";
        if (rutaImagen != null) {
            try {
                var recurso = getClass().getResource(rutaImagen);
                if (recurso != null) {
                    estiloImagen = "-fx-background-image: url(\"" + recurso.toExternalForm().replace(" ", "%20") + "\"); " +
                            "-fx-background-size: cover; -fx-background-position: center; -fx-background-repeat: no-repeat; " +
                            "-fx-background-radius: 15; -fx-background-insets: 0;";
                }
            } catch (Exception e) { /* Fallback */ }
        }
        btn.setStyle(ESTILO_BASE_BOTON + estiloImagen);

        // Efecto Hover
        String finalEstilo = estiloImagen;
        btn.setOnMouseEntered(e -> {
            btn.setScaleX(1.05); btn.setScaleY(1.05);
            btn.setStyle(ESTILO_BASE_BOTON + finalEstilo + "-fx-border-color: #4CAF50; -fx-border-width: 2;");
        });
        btn.setOnMouseExited(e -> {
            btn.setScaleX(1.0); btn.setScaleY(1.0);
            btn.setStyle(ESTILO_BASE_BOTON + finalEstilo);
        });

        // Recorte
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(btn.widthProperty());
        clip.heightProperty().bind(btn.heightProperty());
        clip.setArcWidth(30); clip.setArcHeight(30);
        btn.setClip(clip);

        return btn;
    }

    private void reproducirSonido(String nombreArchivo) {
        if ("click.mp3".equals(nombreArchivo) && sonidoClick != null) { sonidoClick.play(); return; }
        try {
            var recurso = getClass().getResource("/sonidos/" + nombreArchivo);
            if (recurso != null) new AudioClip(recurso.toExternalForm()).play();
        } catch (Exception e) { /* Ignorar error audio */ }
    }

    private void mostrarAlertaInfo(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
    private void mostrarAlertaError(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
    private boolean mostrarConfirmacion(String t, String m) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(m);
        return a.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    public static void main(String[] args) { launch(args); }
}