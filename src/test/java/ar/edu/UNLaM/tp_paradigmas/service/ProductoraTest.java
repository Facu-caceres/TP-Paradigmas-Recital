package ar.edu.UNLaM.tp_paradigmas.service;

import ar.edu.UNLaM.tp_paradigmas.model.Artista;
import ar.edu.UNLaM.tp_paradigmas.model.Asignacion;
import ar.edu.UNLaM.tp_paradigmas.model.Cancion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de tests unitarios para la clase Productora.
 * Esta clase NO USA JSON. Prueba la lógica de negocio en aislamiento
 * creando datos "falsos" (mocks) en el método setUp.
 */
public class ProductoraTest {

    private Productora productora;

    // --- Artistas de prueba ---
    private Artista artistaBase_Brian; // Costo 0, guitarra
    private Artista artistaBase_Roger; // Costo 0, bateria

    private Artista artistaExterno_Elton;  // Costo 1000, piano/voz, max 2
    private Artista artistaExterno_David;  // Costo 1500, voz, max 1, comparte banda
    private Artista artistaExterno_Annie;  // Costo 2000, voz, max 5 (la más cara)

    /**
     * Este método se ejecuta ANTES de CADA test (@Test).
     * Asegura que cada prueba comience con una instancia "limpia"
     * de la productora y los artistas.
     */
    @BeforeEach
    public void setUp() {
        // 1. Instancia limpia
        productora = new Productora();

        // 2. Creamos los artistas de prueba
        // --- ARREGLO FINAL ---
        // (Envolvemos List.of() en new ArrayList<>() para crear listas MUTABLES)

        artistaBase_Brian = new Artista("Brian May",
                new ArrayList<>(List.of("guitarra_electrica")), // <-- mutable
                new ArrayList<>(List.of("Queen")), // <-- mutable
                0, 10);

        artistaBase_Roger = new Artista("Roger Taylor",
                new ArrayList<>(List.of("bateria")), // <-- mutable
                new ArrayList<>(List.of("Queen")), // <-- mutable
                0, 10);

        artistaExterno_Elton = new Artista("Elton John",
                new ArrayList<>(List.of("piano", "voz_principal")), // <-- mutable
                new ArrayList<>(List.of("Elton John Band")), // <-- mutable
                1000, 2);

        artistaExterno_David = new Artista("David Bowie",
                new ArrayList<>(List.of("voz_principal")), // <-- mutable
                new ArrayList<>(List.of("Tin Machine", "Elton John Band")), // <-- mutable
                1500, 1);

        artistaExterno_Annie = new Artista("Annie Lennox",
                new ArrayList<>(List.of("voz_principal")), // <-- mutable
                new ArrayList<>(List.of("Eurythmics")), // <-- mutable
                2000, 5);

        // 3. Simulamos la "carga de datos"
        productora.getArtistasBase().add(artistaBase_Brian);
        productora.getArtistasBase().add(artistaBase_Roger);
        productora.getArtistasDisponibles().add(artistaExterno_Elton);
        productora.getArtistasDisponibles().add(artistaExterno_David);
        productora.getArtistasDisponibles().add(artistaExterno_Annie);

        Map<Artista, Integer> mapaConteo = productora.getCancionesPorArtista();
        mapaConteo.put(artistaBase_Brian, 0);
        mapaConteo.put(artistaBase_Roger, 0);
        mapaConteo.put(artistaExterno_Elton, 0);
        mapaConteo.put(artistaExterno_David, 0);
        mapaConteo.put(artistaExterno_Annie, 0);
    }

    // --- Tests de Lógica de Contratación (Core) ---

    @Test
    @DisplayName("Prioriza artistas base (costo 0) sobre externos")
    public void queAlContratarPriorizaArtistaBaseCostoCero() {
        // 1. Preparación: Una canción que necesita batería
        Cancion cancion = new Cancion("We Will Rock You", List.of("bateria"));
        productora.getRecital().setCanciones(List.of(cancion));

        // 2. Ejecución
        productora.contratarArtistasParaRecital();

        // 3. Verificación
        Artista asignado = cancion.getAsignaciones().get(0).getArtistaAsignado();
        assertTrue(cancion.estaCubierta());
        assertNotNull(asignado);
        assertEquals("Roger Taylor", asignado.getNombre());
    }

    @Test
    @DisplayName("Elige al artista externo más barato")
    public void queAlContratarEligeArtistaExternoMasBarato() {
        // 1. Preparación: Una canción que necesita voz principal.
        // Opciones: Elton (1000), David (1500), Annie (2000)
        Cancion cancion = new Cancion("Song 1", List.of("voz_principal"));
        productora.getRecital().setCanciones(List.of(cancion));

        // 2. Ejecución
        productora.contratarArtistasParaRecital();

        // 3. Verificación
        Artista asignado = cancion.getAsignaciones().get(0).getArtistaAsignado();
        assertTrue(cancion.estaCubierta());
        assertEquals("Elton John", asignado.getNombre()); // Elton (1000) es el más barato
    }

    @Test
    @DisplayName("Aplica descuento del 50% por banda compartida")
    public void queAlContratarAplicaDescuentoPorBanda() {
        // 1. Preparación: Una canción con 2 voces.
        // Opciones: Elton (1000), David (1500), Annie (2000)
        // David comparte banda con Elton ("Elton John Band")
        Cancion cancion = new Cancion("Under Pressure", List.of("voz_principal", "voz_principal"));
        productora.getRecital().setCanciones(List.of(cancion));

        // 2. Ejecución
        productora.contratarArtistasParaRecital();

        // 3. Verificación
        // Asignación 1: Debería ser Elton (costo 1000, el más barato)
        Artista artista1 = cancion.getAsignaciones().get(0).getArtistaAsignado();
        assertEquals("Elton John", artista1.getNombre());

        // Asignación 2: Opciones restantes
        // - David: 1500 * 0.5 (descuento) = 750
        // - Annie: 2000
        // Debería ser David.
        Artista artista2 = cancion.getAsignaciones().get(1).getArtistaAsignado();
        assertEquals("David Bowie", artista2.getNombre());
    }

    @Test
    @DisplayName("Respeta el límite (maxCanciones) de un artista")
    public void queAlContratarRespetaMaxCanciones() {
        // 1. Preparación: 2 canciones, 1 artista (David) con maxCanciones = 1
        // Modificamos a David para que sea el único que puede tocar "voz_principal"
        productora.getCancionesPorArtista().remove(artistaExterno_Elton);
        productora.getCancionesPorArtista().remove(artistaExterno_Annie);
        // Ahora David (max 1) es la única opción.

        Cancion cancion1 = new Cancion("Song 1", List.of("voz_principal"));
        Cancion cancion2 = new Cancion("Song 2", List.of("voz_principal"));
        productora.getRecital().setCanciones(List.of(cancion1, cancion2));

        // 2. Ejecución
        productora.contratarArtistasParaRecital();

        // 3. Verificación
        // Debería cubrir la primera canción
        assertTrue(cancion1.estaCubierta());
        assertEquals("David Bowie", cancion1.getAsignaciones().get(0).getArtistaAsignado().getNombre());

        // Debería fallar en cubrir la segunda (cupo lleno)
        assertFalse(cancion2.estaCubierta());
        assertNull(cancion2.getAsignaciones().get(0).getArtistaAsignado());
    }

    // --- Tests de Lógica de Entrenamiento ---

    @Test
    @DisplayName("Entrenar artista sube costo 50% y agrega rol")
    public void queAlEntrenarArtistaSubeCostoYAgregaRol() {
        // 1. Preparación
        double costoOriginal = artistaExterno_Annie.getCostoPorCancion(); // 2000
        assertFalse(artistaExterno_Annie.puedeCubrirRol("bateria")); // Verifica estado inicial

        // 2. Ejecución
        boolean resultado = productora.entrenarArtista("Annie Lennox", "bateria");

        // 3. Verificación
        assertTrue(resultado);
        assertEquals(costoOriginal * 1.5, artistaExterno_Annie.getCostoPorCancion()); // 2000 * 1.5 = 3000
        assertTrue(artistaExterno_Annie.puedeCubrirRol("bateria"));
    }

    @Test
    @DisplayName("Falla al entrenar artista si ya está contratado")
    public void queAlEntrenarFallaSiArtistaYaFueContratado() {
        // 1. Preparación: Contratamos a Elton
        Cancion cancion = new Cancion("Song 1", List.of("piano"));
        productora.getRecital().setCanciones(List.of(cancion));
        productora.contratarArtistasParaRecital();

        // Verificación previa
        assertTrue(productora.getArtistasContratados().contains(artistaExterno_Elton));

        // 2. Ejecución
        boolean resultado = productora.entrenarArtista("Elton John", "bateria");

        // 3. Verificación
        assertFalse(resultado);
    }

    @Test
    @DisplayName("Falla al entrenar artista si no existe")
    public void queAlEntrenarFallaSiArtistaNoExiste() {
        boolean resultado = productora.entrenarArtista("Freddie Mercury", "voz");
        assertFalse(resultado);
    }

    // --- Tests de Lógica de Estado y Consultas ---

    @Test
    @DisplayName("Opción 3 (Canción) y 4 (Recital) comparten estado")
    public void queOpcion3YOpcion4CompartenEstadoDeConteo() {
        // 1. Preparación: Elton tiene max 2. David tiene max 1.
        // Creamos 3 canciones que solo ellos pueden tocar.
        Cancion cancion1 = new Cancion("Song 1 (Piano)", List.of("piano")); // Elton
        Cancion cancion2 = new Cancion("Song 2 (Voz)", List.of("voz_principal")); // David (más barato)
        Cancion cancion3 = new Cancion("Song 3 (Piano)", List.of("piano")); // Elton

        productora.getRecital().setCanciones(List.of(cancion1, cancion2, cancion3));

        // 2. Ejecución (Opción 3)
        // Contratamos solo para Song 1. Elton es asignado (count = 1).
        productora.contratarArtistasParaCancion("Song 1 (Piano)");

        // 3. Verificación Intermedia
        assertTrue(cancion1.estaCubierta());
        assertEquals("Elton John", cancion1.getAsignaciones().get(0).getArtistaAsignado().getNombre());
        assertFalse(cancion2.estaCubierta());
        assertFalse(cancion3.estaCubierta());

        // 4. Ejecución (Opción 4)
        // Contrata el resto (Song 2 y Song 3)
        productora.contratarArtistasParaRecital();

        // 5. Verificación Final
        // Song 2: David (costo 1500) es contratado. (David count = 1)
        assertTrue(cancion2.estaCubierta());
        assertEquals("David Bowie", cancion2.getAsignaciones().get(0).getArtistaAsignado().getNombre());

        // Song 3: Elton es contratado. (Elton count = 2)
        assertTrue(cancion3.estaCubierta());
        assertEquals("Elton John", cancion3.getAsignaciones().get(0).getArtistaAsignado().getNombre());
    }

    @Test
    @DisplayName("Consulta roles faltantes para canción")
    public void queConsultaRolesFaltantesParaCancion() {
        Cancion cancion = new Cancion("Test Song", List.of("voz_principal", "voz_principal", "bateria"));
        productora.getRecital().setCanciones(List.of(cancion));

        Map<String, Long> faltantes = productora.getRolesFaltantesParaCancion("Test Song");

        assertNotNull(faltantes);
        assertEquals(2, faltantes.size());
        assertEquals(2, faltantes.get("voz_principal"));
        assertEquals(1, faltantes.get("bateria"));
    }

    @Test
    @DisplayName("Consulta roles faltantes totales")
    public void queConsultaRolesFaltantesTotales() {
        Cancion cancion1 = new Cancion("Song 1", List.of("voz_principal", "bateria"));
        Cancion cancion2 = new Cancion("Song 2", List.of("voz_principal", "piano"));
        productora.getRecital().setCanciones(List.of(cancion1, cancion2));

        Map<String, Long> faltantes = productora.getRolesFaltantesTotales();

        assertNotNull(faltantes);
        assertEquals(3, faltantes.size());
        assertEquals(2, faltantes.get("voz_principal"));
        assertEquals(1, faltantes.get("bateria"));
        assertEquals(1, faltantes.get("piano"));
    }

    @Test
    @DisplayName("Consulta estado de canciones (antes y después)")
    public void queConsultaEstadoDeCanciones() {
        Cancion cancion1 = new Cancion("Song 1", List.of("bateria"));
        Cancion cancion2 = new Cancion("Song 2", List.of("rol_imposible"));
        productora.getRecital().setCanciones(List.of(cancion1, cancion2));

        // 1. Antes de contratar
        Map<String, String> estadoAntes = productora.getEstadoCanciones();
        assertEquals("Faltan roles", estadoAntes.get("Song 1"));
        assertEquals("Faltan roles", estadoAntes.get("Song 2"));

        // 2. Contratar
        productora.contratarArtistasParaRecital();

        // 3. Después de contratar
        Map<String, String> estadoDespues = productora.getEstadoCanciones();
        assertEquals("Completa", estadoDespues.get("Song 1")); // Roger cubrió esto
        assertEquals("Faltan roles", estadoDespues.get("Song 2")); // Nadie pudo
    }
}