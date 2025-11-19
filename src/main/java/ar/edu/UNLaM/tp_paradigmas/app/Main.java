package ar.edu.UNLaM.tp_paradigmas.app;

import ar.edu.UNLaM.tp_paradigmas.model.Artista;
import ar.edu.UNLaM.tp_paradigmas.service.Productora;
import ar.edu.UNLaM.tp_paradigmas.util.ServicioProlog;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Productora productora = new Productora();
        ServicioProlog servicioProlog = new ServicioProlog();

        ///////////////////////// ¡Importante! Rutas relativas a la raíz del proyecto///////////////////////////////
        final String RUTA_ARTISTAS = "archivos_prueba/artistas.json";
        final String RUTA_RECITAL = "archivos_prueba/recital.json";
        final String RUTA_ARTISTAS_BASE = "archivos_prueba/artistas-discografica.json";

        // BONUS: archivos de salida
        final String RUTA_ESTADO_SALIDA = "archivos_salida/recital-out.json";
        // RUTA FIJA para guardar/cargar
        final String RUTA_ESTADO_GUARDADO = "archivos_salida/recital-guardado.json";


        productora.cargarDatos(RUTA_ARTISTAS, RUTA_RECITAL, RUTA_ARTISTAS_BASE);
        System.out.println("¡Datos cargados correctamente!");

        int opcion = -1;
        do {
            mostrarMenu();
            try {
                opcion = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor, ingrese un número válido.");
                continue;
            }

            switch (opcion) {
                case 1:
                    // Ver roles faltantes de una canción
                    System.out.print("Ingrese el título de la canción: ");
                    String titulo = scanner.nextLine();
                    Map<String, Long> rolesFaltantes = productora.getRolesFaltantesParaCancion(titulo);
                    if (rolesFaltantes.isEmpty()) {
                        System.out.println("La canción no se encontró o ya está completa.");
                    } else {
                        System.out.println("Roles faltantes para '" + titulo + "': " + rolesFaltantes);
                    }
                    break;
                case 2:
                    // Ver roles faltantes totales
                    Map<String, Long> rolesTotales = productora.getRolesFaltantesTotales();
                    System.out.println("Roles faltantes en todo el recital: " + rolesTotales);
                    break;
                case 3:
                    //Contratar artistas para una canción X del recital
                    System.out.print("Ingrese el título de la canción a contratar: ");
                    String tituloContratar = scanner.nextLine();
                    boolean reintentarContratacionCancion = true;
                    do {
                        List<String> rolesFallidosCancion = productora.contratarArtistasParaCancion(tituloContratar);

                        if (rolesFallidosCancion.isEmpty()) {
                            System.out.println("¡Contratación exitosa para '" + tituloContratar + "'!");
                            reintentarContratacionCancion = false;
                        } else if (rolesFallidosCancion.get(0).equals("ERROR: CANCION_NO_ENCONTRADA")) {
                            reintentarContratacionCancion = false;
                        } else {
                            String rolFallido = rolesFallidosCancion.get(0); // Tomamos solo el primer rol que falló
                            System.out.println("Error: No se encontró artista disponible para el rol: '" + rolFallido + "'");
                            System.out.print("¿Desea entrenar un artista para este rol? (S/N): ");
                            String decision = scanner.nextLine();

                            if (decision.equalsIgnoreCase("S")) {
                                System.out.print("Nombre del artista a entrenar: ");
                                String nombreEntrenar = scanner.nextLine();

                                if (productora.entrenarArtista(nombreEntrenar, rolFallido)) {
                                    System.out.println("¡Artista entrenado! (Su costo aumentó 50%). Reintentando contratación...");
                                    // reintentarContratacionCancion sigue en true, el bucle se repite
                                } else {
                                    System.out.println("Error: No se pudo entrenar a ese artista (no existe, es base o ya está contratado).");
                                    reintentarContratacionCancion = false; // Termina el bucle
                                }
                            } else {
                                System.out.println("Contratación abortada para esta canción.");
                                reintentarContratacionCancion = false; // Termina el bucle
                            }
                        }
                    } while (reintentarContratacionCancion);
                    break;
                case 4:
                    // Contratar artistas para todas las canciones a la vez
                    System.out.println("Contratando artistas para el recital...");
                    boolean reintentarContratacionRecital = true;
                    do {
                        List<String> rolesFallidosRecital = productora.contratarArtistasParaRecital();
                        if (rolesFallidosRecital.isEmpty()) {
                            System.out.println("¡Contratación exitosa para todo el recital!");
                            reintentarContratacionRecital = false;
                        } else {
                            // Unimos los roles fallidos en un String para mostrarlos
                            String rolesStr = rolesFallidosRecital.stream().collect(Collectors.joining(", "));
                            System.out.println("Error: No se encontraron artistas para los siguientes roles: " + rolesStr);
                            System.out.print("¿Desea entrenar un artista para el primer rol ('" + rolesFallidosRecital.get(0) + "')? (S/N): ");
                            String decision = scanner.nextLine();

                            if (decision.equalsIgnoreCase("S")) {
                                String rolAEntrenar = rolesFallidosRecital.get(0);
                                System.out.print("Nombre del artista a entrenar: ");
                                String nombreEntrenar = scanner.nextLine();

                                if (productora.entrenarArtista(nombreEntrenar, rolAEntrenar)) {
                                    System.out.println("¡Artista entrenado! (Su costo aumentó 50%). Reintentando contratación para todo el recital...");
                                    // reintentarContratacionRecital sigue en true, el bucle se repite
                                } else {
                                    System.out.println("Error: No se pudo entrenar a ese artista (no existe, es base o ya está contratado).");
                                    reintentarContratacionRecital = false; // Termina el bucle
                                }
                            } else {
                                System.out.println("Contratación finalizada (con roles faltantes).");
                                reintentarContratacionRecital = false; // Termina el bucle
                            }
                        }
                    } while (reintentarContratacionRecital);
                    break;
                case 5:
                    // Entrenar artista
                    System.out.print("Nombre del artista a entrenar: ");
                    String nombreArtista = scanner.nextLine();
                    System.out.print("Nuevo rol a enseñar: ");
                    String nuevoRol = scanner.nextLine();
                    if (productora.entrenarArtista(nombreArtista, nuevoRol)) {
                        System.out.println("¡Artista entrenado! (Su costo aumentó 50%)");
                    } else {
                        System.out.println("Error: No se pudo entrenar al artista (no existe, es base o ya fue contratado).");
                    }
                    break;
                case 6:
                    // Listar artistas contratados
                    Set<Artista> contratados = productora.getArtistasContratados();
                    if(contratados.isEmpty()){
                        System.out.println("Aún no se ha contratado a ningún artista.");
                    } else {
                        System.out.println("Artistas contratados (" + contratados.size() + "):");
                        for (Artista a : contratados) {
                            System.out.println("- " + a.getNombre() + " (Costo: $" + a.getCostoPorCancion() + ")");
                        }
                    }
                    break;
                case 7:
                    // Ver estado de canciones
                    Map<String, String> estado = productora.getEstadoCanciones();
                    System.out.println("Estado de las canciones:");
                    estado.forEach((tituloCancion, estadoStr) ->
                            System.out.println("- " + tituloCancion + ": " + estadoStr)
                    );
                    break;
                case 8:
                    // Resolver la consigna de Prolog
                    int cantidadEntrenamientos = servicioProlog.getEntrenamientosMinimos();
                    System.out.println("--- Respuesta de Prolog ---");
                    System.out.println("Según los miembros base y los roles del recital, se necesitarían...");
                    System.out.println("Entrenamientos mínimos: " + cantidadEntrenamientos);
                    break;
                case 9:
                    // MENÚ BONUS
                    String subOpcion = "";
                    do {
                        mostrarMenuBonus();
                        subOpcion = scanner.nextLine();
                        switch (subOpcion) {
                            case "1":
                                // BONUS: Guardar estado
                                if (productora.guardarEstadoRecitalEnArchivo(RUTA_ESTADO_GUARDADO)) {
                                    System.out.println("Estado guardado correctamente en '" + RUTA_ESTADO_GUARDADO + "'");
                                } else {
                                    System.out.println("No se pudo guardar el estado del recital.");
                                }
                                break;
                            case "2":
                                // BONUS 5: Cargar estado (Selección de archivo)
                                System.out.println("¿Desde qué archivo desea cargar el estado?");
                                System.out.println("  [1] Archivo guardado manualmente ('recital-guardado.json')");
                                System.out.println("  [2] Archivo de salida automática ('recital-out.json')");
                                System.out.print("Seleccione una opción (1 o 2): ");
                                String opcionCarga = scanner.nextLine();

                                String rutaSeleccionada = null;

                                if (opcionCarga.equals("1")) {
                                    rutaSeleccionada = RUTA_ESTADO_GUARDADO;
                                } else if (opcionCarga.equals("2")) {
                                    rutaSeleccionada = RUTA_ESTADO_SALIDA;
                                } else {
                                    System.out.println("Opción no válida. Cancelando carga.");
                                }

                                if (rutaSeleccionada != null) {
                                    if (productora.cargarEstadoRecitalDesdeArchivo(rutaSeleccionada)) {
                                        System.out.println("Estado cargado correctamente desde '" + rutaSeleccionada + "'");
                                    } else {
                                        System.out.println("No se pudo cargar el estado. Verifique que el archivo exista y tenga el formato correcto.");
                                    }
                                }
                                break;
                            case "3":
                                // BONUS: Arrepentimiento - quitar artista del recital
                                System.out.print("Nombre del artista a quitar del recital: ");
                                String nombreQuitar = scanner.nextLine();
                                if (productora.quitarArtistaDelRecital(nombreQuitar)) {
                                    System.out.println("Se quitaron las participaciones de '" + nombreQuitar + "' del recital.");
                                } else {
                                    System.out.println("No se encontraron participaciones de ese artista (quizás no estaba contratado).");
                                }
                                break;
                            case "4":
                                // BONUS: Historial de colaboraciones
                                Map<String, Set<String>> grafo = productora.getGrafoColaboraciones();
                                if (grafo.isEmpty()) {
                                    System.out.println("Aún no hay colaboraciones registradas (no hay canciones cubiertas).");
                                } else {
                                    System.out.println("Historial de colaboraciones (grafo simple):");
                                    grafo.forEach((artista, colaboradores) -> {
                                        String lista = String.join(", ", colaboradores);
                                        System.out.println(" - " + artista + " ↔ { " + lista + " }");
                                    });
                                }
                                break;
                            case "5":
                                System.out.println("Volviendo al menú principal...");
                                break;
                            default:
                                System.out.println("Sub-opción no válida.");
                        }
                        if (!subOpcion.equals("5")) {
                            System.out.println("--- Presione Enter para continuar en el menú Bonus ---");
                            scanner.nextLine();
                        }
                    } while (!subOpcion.equals("5"));
                    break;
                case 10:
                    // Al salir, guardar estado automático
                    System.out.println("Generando archivo con el estado final del recital...");
                    if (productora.guardarEstadoRecitalEnArchivo(RUTA_ESTADO_SALIDA)) {
                        System.out.println("Estado final guardado en '" + RUTA_ESTADO_SALIDA + "'");
                    } else {
                        System.out.println("No se pudo generar el archivo de estado final.");
                    }
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
            if (opcion != 10 && opcion != 9) {
                System.out.println("--- Presione Enter para continuar ---");
                scanner.nextLine(); // Pausa
            }

        } while (opcion != 10);

        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("\n===== GESTIÓN DE RECITAL =====");
        System.out.println("1. ¿Qué roles me faltan para tocar una canción especifica?");
        System.out.println("2. ¿Qué roles me faltan para tocar todas las canciones?");
        System.out.println("3. Contratar artistas para una canción especifica");
        System.out.println("4. Contratar artistas para todas las canciones del recital");
        System.out.println("5. Entrenar artista");
        System.out.println("6. Listar artistas contratados y su costo");
        System.out.println("7. Listar canciones con su estado (completa/incompleta)");
        System.out.println("8. Calcular entrenamientos mínimos (Prolog)");
        System.out.println("9. BONUSES (guardar/cargar estado, arrepentimiento, colaboraciones)");
        System.out.println("10. Salir y Guardar Estado Final");
        System.out.print("Seleccione una opción: ");
    }

    // <-- MENÚ BONUS
    private static void mostrarMenuBonus() {
        System.out.println("\n--- MENÚ BONUS ---");
        System.out.println("1. Guardar estado actual del recital (en 'recital-guardado.json')");
        System.out.println("2. Cargar estado del recital ");
        System.out.println("3. Quitar artista del recital ");
        System.out.println("4. Mostrar historial de colaboraciones ");
        System.out.println("5. Volver al menú principal");
        System.out.print("Seleccione una opción: ");
    }
}