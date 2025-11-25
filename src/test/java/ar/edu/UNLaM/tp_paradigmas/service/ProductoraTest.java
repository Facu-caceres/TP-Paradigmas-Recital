package ar.edu.UNLaM.tp_paradigmas.service;

import ar.edu.UNLaM.tp_paradigmas.model.Artista;
import ar.edu.UNLaM.tp_paradigmas.model.Asignacion;
import ar.edu.UNLaM.tp_paradigmas.model.Cancion;
import org.junit.jupiter.api.*; // Importamos todo JUnit 5
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de tests unitarios actualizada para la clase Productora.
 * Con salida por consola mejorada.
 */
public class ProductoraTest {

    private Productora productora;

    // --- Artistas de prueba ---
    private Artista artistaBase_Brian;
    private Artista artistaExterno_Elton;
    private Artista artistaExterno_David;
    private Artista artistaExterno_Annie;

    /**
     * Se ejecuta ANTES de cada test.
     * Ahora recibe TestInfo para imprimir qué se está probando.
     */
    @BeforeEach
    public void setUp(TestInfo testInfo) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("▶ INICIANDO TEST: " + testInfo.getDisplayName());
        System.out.println("╚════════════════════════════════════════════════════════╝");

        productora = new Productora();

        // Creamos listas mutables
        artistaBase_Brian = new Artista("Brian May",
                new ArrayList<>(List.of("guitarra_electrica")),
                new ArrayList<>(List.of("Queen")),
                0, 10);

        artistaExterno_Elton = new Artista("Elton John",
                new ArrayList<>(List.of("piano", "voz_principal")),
                new ArrayList<>(List.of("Elton John Band")),
                1000, 2);

        artistaExterno_David = new Artista("David Bowie",
                new ArrayList<>(List.of("voz_principal")),
                new ArrayList<>(List.of("Tin Machine", "Elton John Band")),
                1500, 1);

        artistaExterno_Annie = new Artista("Annie Lennox",
                new ArrayList<>(List.of("voz_principal")),
                new ArrayList<>(List.of("Eurythmics")),
                2000, 5);

        productora.getArtistasBase().add(artistaBase_Brian);
        productora.getArtistasDisponibles().add(artistaExterno_Elton);
        productora.getArtistasDisponibles().add(artistaExterno_David);
        productora.getArtistasDisponibles().add(artistaExterno_Annie);

        Map<Artista, Integer> mapaConteo = productora.getCancionesPorArtista();
        mapaConteo.put(artistaBase_Brian, 0);
        mapaConteo.put(artistaExterno_Elton, 0);
        mapaConteo.put(artistaExterno_David, 0);
        mapaConteo.put(artistaExterno_Annie, 0);
    }

    /**
     * Se ejecuta DESPUÉS de cada test si no hubo errores (Asserts fallidos).
     */
    @AfterEach
    public void tearDown(TestInfo testInfo) {
        System.out.println("✅ PRUEBA EXITOSA: " + testInfo.getDisplayName());
        System.out.println("----------------------------------------------------------");
    }

    // --- TESTS CORE (Lógica Base) ---

    @Test
    @DisplayName("Prioriza artistas base (costo 0)")
    public void testPrioridadArtistaBase() {
        Cancion cancion = new Cancion("Song A", List.of("guitarra_electrica"));
        productora.getRecital().setCanciones(List.of(cancion));

        productora.contratarArtistasParaRecital();

        assertTrue(cancion.estaCubierta());
        assertEquals("Brian May", cancion.getAsignaciones().get(0).getArtistaAsignado().getNombre());
    }

    @Test
    @DisplayName("Elige al artista externo más barato y aplica descuento por banda compartida")
    public void testDescuentoBandaCompartida() {
        Cancion cancion = new Cancion("Duet Song", List.of("voz_principal", "voz_principal"));
        productora.getRecital().setCanciones(List.of(cancion));

        productora.contratarArtistasParaRecital();

        List<Asignacion> asigs = cancion.getAsignaciones();
        boolean estaElton = asigs.stream().anyMatch(a -> a.getArtistaAsignado().getNombre().equals("Elton John"));
        boolean estaDavid = asigs.stream().anyMatch(a -> a.getArtistaAsignado().getNombre().equals("David Bowie"));

        assertTrue(estaElton, "Elton debería ser contratado (más barato inicial)");
        assertTrue(estaDavid, "David debería ser contratado (descuento aplicado)");
    }

    @Test
    @DisplayName("Respeta maxCanciones")
    public void testMaxCanciones() {
        productora.getCancionesPorArtista().remove(artistaExterno_Elton);
        productora.getCancionesPorArtista().remove(artistaExterno_Annie);

        Cancion c1 = new Cancion("Song 1", List.of("voz_principal"));
        Cancion c2 = new Cancion("Song 2", List.of("voz_principal"));
        productora.getRecital().setCanciones(List.of(c1, c2));

        productora.contratarArtistasParaRecital();

        long cubiertas = Stream.of(c1, c2).filter(Cancion::estaCubierta).count();
        assertEquals(1, cubiertas);
    }

    // --- TESTS ENTRENAMIENTO ---

    @Test
    @DisplayName("Entrenar sube costo y agrega rol")
    public void testEntrenarExitoso() {
        double costoOriginal = artistaExterno_Annie.getCostoPorCancion();
        assertFalse(artistaExterno_Annie.puedeCubrirRol("bateria"));

        boolean resultado = productora.entrenarArtista("Annie Lennox", "bateria");

        assertTrue(resultado);
        assertTrue(artistaExterno_Annie.puedeCubrirRol("bateria"));
        assertEquals(costoOriginal * 1.5, artistaExterno_Annie.getCostoPorCancion());
    }

    @Test
    @DisplayName("No permite entrenar rol ya conocido")
    public void testEntrenarRolConocido() {
        boolean resultado = productora.entrenarArtista("Elton John", "piano");
        assertFalse(resultado, "No debería permitir entrenar un rol que ya tiene");
    }

    @Test
    @DisplayName("No permite entrenar artista ya contratado")
    public void testEntrenarYaContratado() {
        productora.getRecital().agregarArtistaContratado(artistaExterno_Elton);
        boolean resultado = productora.entrenarArtista("Elton John", "bateria");
        assertFalse(resultado);
    }

    // --- TESTS BONUS: ARREPENTIMIENTO ---

    @Test
    @DisplayName("Quitar artista del recital libera asignaciones")
    public void testQuitarArtista() {
        Cancion cancion = new Cancion("Rocket Man", List.of("piano"));
        productora.getRecital().setCanciones(List.of(cancion));
        productora.contratarArtistasParaRecital();

        assertTrue(cancion.estaCubierta());
        assertTrue(productora.getArtistasContratados().contains(artistaExterno_Elton));

        boolean resultado = productora.quitarArtistaDelRecital("Elton John");

        assertTrue(resultado, "La operación debe ser exitosa");
        assertFalse(cancion.estaCubierta(), "La canción debe quedar incompleta");
        assertNull(cancion.getAsignaciones().get(0).getArtistaAsignado(), "La asignación debe ser null");
        assertFalse(productora.getArtistasContratados().contains(artistaExterno_Elton));
    }

    @Test
    @DisplayName("Quitar artista solo borra si estaba contratado")
    public void testQuitarArtistaNoContratado() {
        boolean resultado = productora.quitarArtistaDelRecital("Freddie Mercury");
        assertFalse(resultado);
    }

    // --- TESTS BONUS: GRAFO COLABORACIONES ---

    @Test
    @DisplayName("Genera grafo de colaboraciones por banda compartida")
    public void testGrafoColaboracionesHistoricas() {
        Map<String, Set<String>> grafo = productora.getGrafoColaboraciones();

        assertTrue(grafo.containsKey("David Bowie"));
        assertTrue(grafo.get("David Bowie").contains("Elton John"));
        assertTrue(grafo.containsKey("Elton John"));
        assertTrue(grafo.get("Elton John").contains("David Bowie"));
    }

    @Test
    @DisplayName("Genera grafo de colaboraciones por canción compartida en el recital")
    public void testGrafoColaboracionesRecital() {
        Cancion cancion = new Cancion("Sweet Dreams", List.of("piano", "voz_principal"));
        Asignacion asig1 = cancion.getAsignaciones().get(0);
        Asignacion asig2 = cancion.getAsignaciones().get(1);
        asig1.setArtistaAsignado(artistaExterno_Elton);
        asig2.setArtistaAsignado(artistaExterno_Annie);
        productora.getRecital().setCanciones(List.of(cancion));

        Map<String, Set<String>> grafo = productora.getGrafoColaboraciones();

        assertTrue(grafo.get("Elton John").contains("Annie Lennox"));
        assertTrue(grafo.get("Annie Lennox").contains("Elton John"));
    }

    // --- TESTS BONUS: PERSISTENCIA (JSON) ---

    @Test
    @DisplayName("Guarda y Carga estado del recital correctamente")
    public void testPersistenciaEstado(@TempDir Path tempDir) {
        Cancion cancion = new Cancion("Tiny Dancer", List.of("piano"));
        productora.getRecital().setCanciones(List.of(cancion));
        productora.contratarArtistasParaRecital();

        assertTrue(cancion.estaCubierta());

        File archivoTemp = tempDir.resolve("recital-test.json").toFile();
        boolean guardado = productora.guardarEstadoRecitalEnArchivo(archivoTemp.getAbsolutePath());
        assertTrue(guardado, "Debe guardar el archivo JSON");

        productora.quitarArtistaDelRecital("Elton John");
        assertFalse(cancion.estaCubierta());

        boolean cargado = productora.cargarEstadoRecitalDesdeArchivo(archivoTemp.getAbsolutePath());
        assertTrue(cargado, "Debe cargar el archivo JSON");

        assertTrue(cancion.estaCubierta(), "La canción debe volver a estar cubierta tras la carga");
        assertEquals("Elton John", cancion.getAsignaciones().get(0).getArtistaAsignado().getNombre());
        assertTrue(productora.getArtistasContratados().contains(artistaExterno_Elton));
    }
}