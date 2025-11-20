package ar.edu.UNLaM.tp_paradigmas.app;

import ar.edu.UNLaM.tp_paradigmas.model.Artista;
import ar.edu.UNLaM.tp_paradigmas.service.Productora;
import ar.edu.UNLaM.tp_paradigmas.util.ServicioProlog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MainGUI extends Application {

    private Productora productora;
    private ServicioProlog servicioProlog;
    private Stage ventanaPrincipal;

    private AudioClip sonidoClick;

    // --- ESTILOS GENERALES ---
    private final String ESTILO_FONDO = "-fx-background-color: #121212;"; // Fondo casi negro
    private final String ESTILO_TITULO = "-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';";
    private final String ESTILO_SUBTITULO = "-fx-text-fill: #cccccc; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;";

    // Estilo base para los botones (dimensiones, texto, bordes)
    // Nota: El background-image se inyecta dinámicamente en el método crearBotonMenu
    // Estilo base para los botones
    private final String ESTILO_BASE_BOTON =
            "-fx-pref-width: 220; -fx-pref-height: 100; " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-radius: 15; " +
                    "-fx-border-color: rgba(255,255,255,0.2); " +
                    "-fx-border-width: 1; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    // "-fx-alignment: CENTER; " +  <--- ¡BORRA ESTA LÍNEA!
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);";

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
        // Carga preventiva del sonido
        try {
            var recurso = getClass().getResource("/sonidos/click.mp3");
            if (recurso != null) {
                sonidoClick = new AudioClip(recurso.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Error cargando audio init: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) {
        this.ventanaPrincipal = stage;
        mostrarMenuPrincipal();
        stage.setTitle("Gestión de Recitales - Visual 🎵");
        stage.show();
    }

    // ========================================================================
    // 🏠 VISTA: MENÚ PRINCIPAL
    // ========================================================================
    private void mostrarMenuPrincipal() {
        VBox layout = new VBox(20);

        // --- LÓGICA DE FONDO (NUEVO BLOQUE) ---
        try {
            // Intentamos buscar la imagen en los recursos
            var recursoFondo = getClass().getResource("/imagenes/fondo.jpg");

            if (recursoFondo != null) {
                // Si existe, obtenemos la URL y reemplazamos espacios por %20 para evitar errores
                String url = recursoFondo.toExternalForm().replace(" ", "%20");

                // Aplicamos el estilo con la imagen de fondo
                layout.setStyle(
                        "-fx-background-image: url(\"" + url + "\"); " +
                                "-fx-background-size: cover; " +
                                "-fx-background-position: center center; " +
                                "-fx-background-repeat: no-repeat;"
                );
            } else {
                // Si devuelve null, usamos el color sólido
                System.out.println("⚠️ No se encontró la imagen de fondo. Usando color base.");
                layout.setStyle(ESTILO_FONDO);
            }
        } catch (Exception e) {
            // Si ocurre cualquier error, usamos el color sólido para no romper la app
            System.err.println("Error cargando fondo: " + e.getMessage());
            layout.setStyle(ESTILO_FONDO);
        }
        // --------------------------------------

        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Gestión de Recitales");
        titulo.setStyle(ESTILO_TITULO);

        // --- GRID DE BOTONES ---
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        // Columna 1: Consultas
        VBox col1 = new VBox(15);
        col1.setAlignment(Pos.TOP_CENTER);
        Label lbl1 = new Label("CONSULTAS");
        lbl1.setStyle(ESTILO_SUBTITULO);

        col1.getChildren().addAll(
                lbl1,
                crearBotonImagen("Roles Faltantes (Canción)", "/iconos/Roles Faltantes (Canción).png", e -> consultaRolesCancion()),
                crearBotonImagen("Roles Faltantes (Total)", "/iconos/Roles Faltantes (Total).png", e -> consultaRolesTotales()),
                crearBotonImagen("Estado Canciones", "/iconos/Estado Canciones.png","EstadoCanciones.wav", e -> verEstadoCancionesTabla()),
                crearBotonImagen("Historial Colaboraciones", "/iconos/Historial Colaboraciones.png", e -> verGrafoColaboraciones())
        );

        // Columna 2: Operaciones
        VBox col2 = new VBox(15);
        col2.setAlignment(Pos.TOP_CENTER);
        Label lbl2 = new Label("OPERACIONES");
        lbl2.setStyle(ESTILO_SUBTITULO);

        col2.getChildren().addAll(
                lbl2,
                crearBotonImagen("Contratar (1 Canción)", "/iconos/Contratar (1 Canción).png", e -> contratarParaCancion()),
                crearBotonImagen("Contratar (Recital)", "/iconos/Contratar (Recital).png", e -> contratarTodoRecital()),
                crearBotonImagen("Entrenar Artista", "/iconos/Entrenar Artista.png", e -> entrenarArtistaDialogo()),
                crearBotonImagen("Ver Contratados", "/iconos/Ver Artistas Contratados.png", e -> verContratadosTabla()),
                crearBotonImagen("Quitar Artista", "/iconos/Quitar Artista.png", e -> quitarArtistaDialogo())
        );

        // Columna 3: Sistema
        VBox col3 = new VBox(15);
        col3.setAlignment(Pos.TOP_CENTER);
        Label lbl3 = new Label("SISTEMA");
        lbl3.setStyle(ESTILO_SUBTITULO);

        col3.getChildren().addAll(
                lbl3,
                crearBotonImagen("Guardar Estado", "/iconos/Guardar Estado.png", e -> guardarEstado()),
                crearBotonImagen("Cargar Estado", "/iconos/Cargar Estado.png", e -> cargarEstado()),
                crearBotonImagen("Consulta Prolog", "/iconos/Consulta Prolog.png", e -> consultaProlog()),
                crearBotonImagen("Salir", "/iconos/Salir.png", "Salir.wav", e -> {
                    // 1. Guardamos
                    productora.guardarEstadoRecitalEnArchivo(RUTA_ESTADO_SALIDA);

                    // 2. Esperamos 1.5 segundos antes de cerrar para dejar sonar el audio
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    javafx.application.Platform.exit();
                                    System.exit(0); // Cierre forzado por si acaso
                                }
                            },
                            1500
                    );
                })
        );

        grid.add(col1, 0, 0);
        grid.add(col2, 1, 0);
        grid.add(col3, 2, 0);

        // ScrollPane por si la pantalla es chica
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        // Hacemos el scrollpane transparente para que se vea el fondo de atrás
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.getStylesheets().add("data:text/css,.scroll-pane > .viewport { -fx-background-color: transparent; }");

        layout.getChildren().addAll(titulo, new Separator(), scroll);

        Scene escena = new Scene(layout, 1100, 750);
        ventanaPrincipal.setScene(escena);
    }

    /**
     * Versión AUTOMÁTICA (3 parámetros):
     * Genera el nombre del sonido y llama a la versión avanzada.
     */
    private Button crearBotonImagen(String texto, String rutaImagen, javafx.event.EventHandler<javafx.event.ActionEvent> accion) {
        String nombreLimpio = texto.replace("\n", " ");
        String nombreSonido = nombreLimpio + ".wav";
        return crearBotonImagen(texto, rutaImagen, nombreSonido, accion);
    }

    /**
     * Versión AVANZADA (4 parámetros) CON CORRECCIÓN DE BLOQUEO:
     * Usa Platform.runLater para que los Dialogs no corten el audio.
     */
    private Button crearBotonImagen(String texto, String rutaImagen, String archivoSonido, javafx.event.EventHandler<javafx.event.ActionEvent> accion) {
        Button btn = new Button(texto);
        btn.setAlignment(Pos.BOTTOM_CENTER);
        btn.setPadding(new Insets(0, 0, 10, 0));

        // --- LÓGICA DE EJECUCIÓN CORREGIDA ---
        btn.setOnAction(event -> {
            // 1. Reproducir sonido PRIMERO
            reproducirSonido(archivoSonido);

            // 2. Ejecutar la acción DESPUÉS (usando runLater)
            // Esto permite que el audio arranque antes de que un Dialog con showAndWait() bloquee el hilo.
            Platform.runLater(() -> {
                try {
                    accion.handle(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        // -------------------------------------

        btn.setWrapText(true);
        btn.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // --- ESTILO VISUAL (IMAGEN Y CSS) ---
        String estiloImagen = "";
        if (rutaImagen != null) {
            try {
                var recurso = getClass().getResource(rutaImagen);
                if (recurso != null) {
                    String url = recurso.toExternalForm().replace(" ", "%20");
                    estiloImagen =
                            "-fx-background-image: url(\"" + url + "\"); " +
                                    "-fx-background-size: cover; " +
                                    "-fx-background-position: center; " +
                                    "-fx-background-repeat: no-repeat; " +
                                    "-fx-background-radius: 15; " +
                                    "-fx-background-insets: 0;";
                } else {
                    estiloImagen = "-fx-background-color: #333;";
                }
            } catch (Exception e) {
                estiloImagen = "-fx-background-color: #333;";
            }
        } else {
            estiloImagen = "-fx-background-color: #333;";
        }

        btn.setStyle(ESTILO_BASE_BOTON + estiloImagen);

        // Recorte (Clip)
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(btn.widthProperty());
        clip.heightProperty().bind(btn.heightProperty());
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        btn.setClip(clip);

        // Efecto Hover
        String finalEstiloImagen = estiloImagen;
        btn.setOnMouseEntered(e -> {
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
            btn.setStyle(ESTILO_BASE_BOTON + finalEstiloImagen + "-fx-border-color: #4CAF50; -fx-border-width: 2;");
        });
        btn.setOnMouseExited(e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
            btn.setStyle(ESTILO_BASE_BOTON + finalEstiloImagen);
        });

        return btn;
    }

    // ========================================================================
    // RESTO DEL CÓDIGO (LÓGICA, TABLAS Y DIÁLOGOS - IGUAL QUE ANTES)
    // ========================================================================

    private void mostrarVistaTabla(String titulo, TableView<?> tabla) {
        VBox layout = new VBox(20);
        layout.setStyle(ESTILO_FONDO);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle(ESTILO_TITULO);

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        Button btnVolver = new Button("Volver al Menú");
        btnVolver.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");
        btnVolver.setPrefWidth(150);
        btnVolver.setOnAction(e -> mostrarMenuPrincipal());

        layout.getChildren().addAll(lblTitulo, tabla, btnVolver);
        ventanaPrincipal.setScene(new Scene(layout, 900, 600));
    }

    private void verEstadoCancionesTabla() {
        TableView<Map.Entry<String, String>> tabla = new TableView<>();
        TableColumn<Map.Entry<String, String>, String> colCancion = new TableColumn<>("Canción");
        colCancion.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
        TableColumn<Map.Entry<String, String>, String> colEstado = new TableColumn<>("Estado / Costo");
        colEstado.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue()));
        tabla.getColumns().addAll(colCancion, colEstado);
        tabla.getItems().addAll(productora.getEstadoCanciones().entrySet());
        mostrarVistaTabla("Estado del Recital", tabla);
    }

    private void verContratadosTabla() {
        TableView<Artista> tabla = new TableView<>();
        TableColumn<Artista, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNombre()));
        TableColumn<Artista, String> colCosto = new TableColumn<>("Costo ($)");
        colCosto.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getCostoPorCancion())));
        TableColumn<Artista, String> colRoles = new TableColumn<>("Roles");
        colRoles.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getRoles().toString()));
        tabla.getColumns().addAll(colNombre, colCosto, colRoles);
        tabla.getItems().addAll(productora.getArtistasContratados());
        mostrarVistaTabla("Artistas Contratados", (TableView) tabla);
    }

    private void contratarParaCancion() {
        TextInputDialog dialog = crearDialogoInput("Contratar", "Ingrese el nombre de la canción:");
        dialog.showAndWait().ifPresent(titulo -> {
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
            verEstadoCancionesTabla();
        } else if (fallidos.get(0).equals("ERROR: CANCION_NO_ENCONTRADA")) {
            mostrarAlertaError("Error", "Canción no encontrada.");
        } else {
            mostrarVistaFalloContratacion(fallidos, esIndividual, titulo);
        }
    }

    private void mostrarVistaFalloContratacion(List<String> fallidos, boolean esIndividual, String tituloCancion) {
        VBox layout = new VBox(20);
        layout.setStyle(ESTILO_FONDO);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label("⚠️ Contratación Incompleta");
        lblTitulo.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 26px; -fx-font-weight: bold;");

        ListView<String> listaRoles = new ListView<>();
        listaRoles.getItems().addAll(fallidos);
        listaRoles.setMaxHeight(200);

        Label lblPregunta = new Label("¿Desea entrenar a un artista para cubrir '" + fallidos.get(0) + "'?");
        lblPregunta.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        HBox boxBotones = new HBox(20);
        boxBotones.setAlignment(Pos.CENTER);

        Button btnEntrenar = new Button("💪 Entrenar Artista");
        btnEntrenar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");

        Button btnVolver = new Button("Cancelar");
        btnVolver.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");

        btnEntrenar.setOnAction(e -> {
            entrenarArtistaDialogo(fallidos.get(0), () -> {
                if(esIndividual) {
                    List<String> nuevos = productora.contratarArtistasParaCancion(tituloCancion);
                    manejarResultadoContratacion(nuevos, true, tituloCancion);
                } else {
                    List<String> nuevos = productora.contratarArtistasParaRecital();
                    manejarResultadoContratacion(nuevos, false, null);
                }
            });
        });

        btnVolver.setOnAction(e -> mostrarMenuPrincipal());
        boxBotones.getChildren().addAll(btnEntrenar, btnVolver);
        layout.getChildren().addAll(lblTitulo, new Label("Faltan roles:"), listaRoles, lblPregunta, boxBotones);
        ventanaPrincipal.setScene(new Scene(layout, 600, 500));
    }

    private void entrenarArtistaDialogo() { entrenarArtistaDialogo("", null); }

    private void entrenarArtistaDialogo(String rolSugerido, Runnable onSuccess) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Entrenar");
        dialog.setHeaderText("Entrenamiento (+50% costo)");
        ButtonType btnOk = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField txtNombre = new TextField(); txtNombre.setPromptText("Artista");
        TextField txtRol = new TextField(); txtRol.setPromptText("Rol");
        if(!rolSugerido.isEmpty()) txtRol.setText(rolSugerido);
        grid.add(new Label("Artista:"), 0, 0); grid.add(txtNombre, 1, 0);
        grid.add(new Label("Rol:"), 0, 1); grid.add(txtRol, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(b -> b == btnOk ? new Pair<>(txtNombre.getText(), txtRol.getText()) : null);
        dialog.showAndWait().ifPresent(datos -> {
            if (productora.entrenarArtista(datos.getKey(), datos.getValue())) {
                mostrarAlertaInfo("Éxito", "Artista entrenado.");
                if (onSuccess != null) onSuccess.run();
            } else mostrarAlertaError("Error", "No se pudo entrenar.");
        });
    }

    private void consultaRolesTotales() {
        Map<String, Long> data = productora.getRolesFaltantesTotales();
        TableView<Map.Entry<String, Long>> tabla = new TableView<>();
        TableColumn<Map.Entry<String, Long>, String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
        TableColumn<Map.Entry<String, Long>, String> colCant = new TableColumn<>("Faltantes");
        colCant.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getValue())));
        tabla.getColumns().addAll(colRol, colCant);
        tabla.getItems().addAll(data.entrySet());
        mostrarVistaTabla("Roles Faltantes Totales", tabla);
    }

    private void consultaRolesCancion() {
        TextInputDialog dialog = crearDialogoInput("Consulta", "Canción:");
        dialog.showAndWait().ifPresent(titulo -> {
            Map<String, Long> data = productora.getRolesFaltantesParaCancion(titulo);
            if(data.isEmpty()) mostrarAlertaInfo("Info", "Completa o no existe.");
            else {
                TableView<Map.Entry<String, Long>> tabla = new TableView<>();
                TableColumn<Map.Entry<String, Long>, String> colRol = new TableColumn<>("Rol");
                colRol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
                TableColumn<Map.Entry<String, Long>, String> colCant = new TableColumn<>("Faltantes");
                colCant.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getValue())));
                tabla.getColumns().addAll(colRol, colCant);
                tabla.getItems().addAll(data.entrySet());
                mostrarVistaTabla("Faltantes: " + titulo, tabla);
            }
        });
    }

    private void consultaProlog() {
        int n = servicioProlog.getEntrenamientosMinimos();
        mostrarAlertaInfo("Prolog", "Entrenamientos mínimos: " + n);
    }

    private void quitarArtistaDialogo() {
        TextInputDialog d = crearDialogoInput("Quitar", "Artista:");
        d.showAndWait().ifPresent(n -> {
            if(productora.quitarArtistaDelRecital(n)) mostrarAlertaInfo("Éxito", "Removido.");
            else mostrarAlertaError("Error", "No encontrado o no contratado.");
        });
    }

    private void verGrafoColaboraciones() {
        TableView<Map.Entry<String, Set<String>>> tabla = new TableView<>();
        TableColumn<Map.Entry<String, Set<String>>, String> colArt = new TableColumn<>("Artista");
        colArt.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
        TableColumn<Map.Entry<String, Set<String>>, String> colColabs = new TableColumn<>("Colaboradores");
        colColabs.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue().toString()));
        tabla.getColumns().addAll(colArt, colColabs);
        tabla.getItems().addAll(productora.getGrafoColaboraciones().entrySet());
        mostrarVistaTabla("Grafo de Colaboraciones", tabla);
    }

    private void guardarEstado() {
        if(productora.guardarEstadoRecitalEnArchivo(RUTA_ESTADO_GUARDADO)) mostrarAlertaInfo("Guardar", "Guardado OK.");
        else mostrarAlertaError("Error", "Falló al guardar.");
    }

    private void cargarEstado() {
        List<String> choices = List.of("Manual", "Salida Automática");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Manual", choices);
        dialog.setTitle("Cargar");
        dialog.setHeaderText("Seleccione origen:");
        dialog.showAndWait().ifPresent(res -> {
            String ruta = res.equals("Manual") ? RUTA_ESTADO_GUARDADO : RUTA_ESTADO_SALIDA;
            if(productora.cargarEstadoRecitalDesdeArchivo(ruta)) {
                mostrarAlertaInfo("Cargar", "Cargado OK.");
                verEstadoCancionesTabla();
            } else mostrarAlertaError("Error", "Falló la carga.");
        });
    }

    // --- UTILIDADES ---
    private TextInputDialog crearDialogoInput(String titulo, String contenido) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);
        dialog.setContentText(contenido);
        return dialog;
    }
    private void mostrarAlertaInfo(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void mostrarAlertaError(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }

    /**
     * Reproduce un archivo de audio ubicado en los recursos.
     * Ideal para efectos cortos (clics, notificaciones).
     */
    private void reproducirSonido(String nombreArchivo) {
        // Si es el sonido de clic estándar y ya está cargado, úsalo directo
        if ("click.mp3".equals(nombreArchivo) && sonidoClick != null) {
            sonidoClick.play();
            return;
        }

        // Para otros sonidos (como el de salir), cárgalos al momento
        try {
            var recurso = getClass().getResource("/sonidos/" + nombreArchivo);
            if (recurso != null) {
                new AudioClip(recurso.toExternalForm()).play();
            } else {
                System.out.println("⚠️ Audio no encontrado: " + nombreArchivo);
            }
        } catch (Exception e) {
            System.err.println("Error audio: " + e.getMessage());
        }
    }

}