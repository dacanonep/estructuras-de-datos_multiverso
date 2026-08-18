import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * CLASE: MULTIVERSO MAIN (El Punto de Entrada)
 * ---------------------------------------------------------
 * Es la ventana principal (JFrame) y el "Gerente" del proyecto.
 * * RESPONSABILIDAD:
 * 1. Inicializar todos los subsistemas (Controlador, Cámara, Lienzo).
 * 2. Conectarlos entre sí (Inyección de Dependencias).
 * 3. Iniciar el "Bucle de Juego" (Game Loop) que mantiene el programa vivo.
 * * ROL ASIGNADO: El Arquitecto (Integrante 1)
 */
public class MultiversoMain extends JFrame {

    public MultiversoMain() {
        // --- CONFIGURACIÓN DE LA VENTANA ---
        this.setTitle("Multiverso Toroidal - Proyecto Final");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar todo al salir
        this.setLayout(new BorderLayout()); // Diseño: Centro (Dibujo) y Sur (Botones)

        // --- 1. CREACIÓN DE OBJETOS (MODELO) ---
        // Instanciamos el cerebro (Datos) y el ojo (Cámara)
        Controlador controlador = new Controlador();
        Camara camara = new Camara();

        // --- 2. GENERACIÓN MATEMÁTICA ---
        // Pedimos al arquitecto geométrico que calcule los puntos de la dona una sola vez
        List<Anillo> listaAnillos = GeneradorGeometria.crearAnillos();

        // --- 3. CREACIÓN DE LA VISTA (LIENZO) ---
        // Le entregamos al lienzo la cámara y los datos para que sepa qué pintar
        Lienzo lienzo = new Lienzo(camara, controlador, listaAnillos);
        lienzo.setBackground(Color.BLACK); // El espacio es negro

        // --- 4. CREACIÓN DE LA INTERFAZ (CONTROLES) ---
        // Le damos el lienzo y el controlador para que los botones funcionen
        Controles controles = new Controles(lienzo, camara, controlador);

        // --- 5. ENSAMBLAJE FINAL ---
        // Ponemos el dibujo en el centro y los controles abajo
        this.add(lienzo, BorderLayout.CENTER);
        this.add(controles, BorderLayout.SOUTH);

        // --- 6. GAME LOOP (Bucle de Renderizado) ---
        // Este Timer se dispara cada 30 milisegundos (aprox. 30 FPS).
        // Es el corazón que hace que la cámara se mueva suave y el dibujo se actualice.
        Timer timerRender = new Timer(30, e -> {
            camara.actualizarSuavizado(); // Calcula el siguiente paso de movimiento
            lienzo.repaint();             // Ordena volver a pintar la pantalla
        });
        timerRender.start();

        // Configuración final de visualización (Pantalla Completa)
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(false); // Mantener bordes de ventana (cerrar/minimizar)
        this.setVisible(true);
    }

    /**
     * MÉTODO MAIN (Inicio del Programa)
     */
    public static void main(String[] args) {
        // --- OPTIMIZACIONES DE RENDIMIENTO ---
        // Estas líneas evitan parpadeos (flickering) en tarjetas gráficas modernas
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.ddoffscreen", "false");
        System.setProperty("sun.java2d.noddraw", "true");

        // Intentamos que la ventana se vea bonita (estilo nativo del sistema operativo)
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception ex) {}

        // Iniciamos la ventana en el Hilo de Eventos de Swing (Seguridad de Hilos)
        SwingUtilities.invokeLater(() -> {
            new MultiversoMain();
        });
    }
}