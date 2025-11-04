package ar.edu.UNLaM.tp_paradigmas.app;

import ar.edu.UNLaM.tp_paradigmas.model.Artista;
import ar.edu.UNLaM.tp_paradigmas.service.Productora;
import ar.edu.UNLaM.tp_paradigmas.util.ServicioProlog;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        // --- 1. Inicialización ---
        Scanner scanner = new Scanner(System.in);
        Productora productora = new Productora();
        ServicioProlog servicioProlog = new ServicioProlog();

        // ¡Importante! Rutas relativas a la raíz del proyecto
        final String RUTA_ARTISTAS = "artistas.json";
        final String RUTA_RECITAL = "recital.json";
        final String RUTA_ARTISTAS_BASE = "artistas-discografica.json";

        // Cargamos todos los datos al iniciar
        productora.cargarDatos(RUTA_ARTISTAS, RUTA_RECITAL, RUTA_ARTISTAS_BASE);
        System.out.println("¡Datos cargados correctamente!");

        // --- 2. Bucle del Menú ---
        int opcion = -1;
        do {
            mostrarMenu();
            try {
                opcion = Integer.parseInt(scanner.nextLine()); // Leemos línea completa para evitar errores
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
                    // Contratar artistas para una canción X
                    System.out.print("Ingrese el título de la canción a contratar: ");
                    String tituloContratar = scanner.nextLine();
                    productora.contratarArtistasParaCancion(tituloContratar);
                    break;
                case 4:
                    // Contratar artistas para todo el recital
                    System.out.println("Contratando artistas para el recital...");
                    productora.contratarArtistasParaRecital();
                    System.out.println("¡Proceso de contratación finalizado!");
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
                        System.out.println("Error: No se pudo entrenar al artista (no existe o ya fue contratado).");
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
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
            System.out.println("--- Presione Enter para continuar ---");
            scanner.nextLine(); // Pausa

        } while (opcion != 9);

        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("\n===== GESTIÓN DE RECITAL =====");
        System.out.println("1. ¿Qué roles me faltan para tocar una canción X?");
        System.out.println("2. ¿Qué roles me faltan para tocar todas las canciones?");
        System.out.println("3. Contratar artistas para una canción X (Función no implementada)");
        System.out.println("4. Contratar artistas para todas las canciones del recital");
        System.out.println("5. Entrenar artista");
        System.out.println("6. Listar artistas contratados y su costo");
        System.out.println("7. Listar canciones con su estado (completa/incompleta)");
        System.out.println("8. Calcular entrenamientos mínimos (Prolog)");
        System.out.println("9. Salir");
        System.out.print("Seleccione una opción: ");
    }
}