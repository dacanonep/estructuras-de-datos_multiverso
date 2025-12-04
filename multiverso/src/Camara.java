/**
 * CLASE: CAMARA (El Ojo Virtual)
 * ---------------------------------------------------------
 * Responsable de mantener las coordenadas (x, y, z) desde donde vemos el mundo.
 * * RESPONSABILIDAD:
 * Calcular la posición del observador y aplicar "Suavizado" (Lerp) para
 * que los movimientos de la cámara sean fluidos y cinematográficos.
 * * ROL ASIGNADO: El Matemático (Integrante 2)
 */
public class Camara {
    // --- ESTADO ACTUAL (Lo que se renderiza en este frame) ---
    public double x = 0, y = 0, z = 0; // Posición en el espacio 3D
    public double anguloX = 0; // Rotación vertical (Pitch): Mirar arriba/abajo
    public double anguloY = 0; // Rotación horizontal (Yaw): Mirar izquierda/derecha
    public double factorZoom = 1; // Nivel de acercamiento actual

    // --- ESTADO OBJETIVO (A donde queremos llegar) ---
    // El 'Lienzo' cambia estos valores cuando hacemos click en un botón.
    // La cámara perseguirá estos valores poco a poco.
    public double objX = 0, objY = 0, objZ = 0;
    public double objetivoZoom = 1;

    /**
     * CONSTRUCTOR
     * Inicializa la cámara en el punto cero (0,0,0) sin rotación.
     */
    public Camara() {
        // Es vital que el objetivo sea igual al actual al inicio
        // para evitar un "salto" brusco al arrancar el programa.
        this.x = 0; this.y = 0; this.z = 0;
        this.objX = 0; this.objY = 0; this.objZ = 0;
        this.anguloX = 0; 
        this.anguloY = 0;
        this.factorZoom = 1;
        this.objetivoZoom = 1;
    }

    /**
     * ALGORITMO DE SUAVIZADO (LERP - Linear Interpolation)
     * Este método se llama 30 veces por segundo desde el Main.
     */
    public void actualizarSuavizado() {
        // FÓRMULA: Posicion = Posicion + (Destino - Posicion) * Velocidad
        // El 0.1 significa que recorremos el 10% de la distancia restante en cada frame.
        
        x += (objX - x) * 0.1;
        y += (objY - y) * 0.1;
        z += (objZ - z) * 0.1;
        
        // También suavizamos el zoom para que no sea de golpe
        factorZoom += (objetivoZoom - factorZoom) * 0.1;
    }

    /**
     * REINICIO DE VISTA
     * Devuelve la cámara a la posición inicial (útil si nos perdemos).
     */
    public void reset() {
        this.objX = 0; this.objY = 0; this.objZ = 0;
        this.objetivoZoom = 1;
        this.anguloX = 0;
        this.anguloY = 0;
    }
}