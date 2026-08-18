import java.util.ArrayList;
import java.util.List;

/**
 * CLASE: ANILLO (Geometría del Toroide)
 * ---------------------------------------------------------
 * Representa un "corte transversal" o sección circular de la dona (toroide).
 * * RESPONSABILIDAD:
 * Calcular la posición matemática (X, Y, Z) de cada universo que pertenece
 * a este anillo, usando coordenadas toroidales.
 * * ROL ASIGNADO: El Matemático (Integrante 2)
 */
public class Anillo {
    // --- ATRIBUTOS DE IDENTIFICACIÓN ---
    public int id;          // Identificador único del anillo (ej: 0, 1, 2...)
    public int capa;        // A qué capa de profundidad pertenece (0 = Centro, 5 = Exterior)
    
    // --- ATRIBUTOS GEOMÉTRICOS (Dimensiones) ---
    public double radioMayor; // Distancia desde el centro del agujero hasta el centro del tubo
    public double radioMenor; // Grosor del tubo de la dona
    
    // --- ATRIBUTOS DE POSICIÓN ANGULAR ---
    public double theta;      // Ángulo de posición del anillo en la dona grande (0 a 2*PI)
    public double rotacionPropia = 0; // Giro interno del anillo sobre su propio eje
    public boolean sentidoHorario = false; // Dirección de giro (relojes o contra-relojes)

    // --- CONFIGURACIÓN DE PUNTOS ---
    public int geometriaBase; // Cuántos nodos (universos) caben teóricamente en este anillo
    public int limiteDibujo;  // Cuántos nodos dibujamos realmente (por si queremos ocultar algunos)

    /**
     * CONSTRUCTOR
     * Inicializa las dimensiones matemáticas del anillo.
     */
    public Anillo(int id, int capa, double rMayor, double rMenor, double theta, int geometriaBase) {
        this.id = id;
        this.capa = capa;
        this.radioMayor = rMayor;
        this.radioMenor = rMenor;
        this.theta = theta; // Define en qué parte de la vuelta completa está este anillo
        this.geometriaBase = geometriaBase;
        this.limiteDibujo = geometriaBase; // Por defecto dibujamos todos
    }

    // Define cuántos puntos serán visibles (útil para efectos visuales)
    public void setLimiteVisible(int cuantosPuntos) {
        this.limiteDibujo = cuantosPuntos;
    }

    // Aplica una rotación inicial para que los anillos no se vean alineados aburridamente
    public void rotar(double grados) {
        this.rotacionPropia = Math.toRadians(grados); // Convertimos grados a radianes para Java
    }

    // Define si los nodos se generan hacia la derecha o izquierda
    public void setSentidoHorario(boolean activar) {
        this.sentidoHorario = activar;
    }
    
    /**
     * CALCULA EL ÁNGULO PHI (Posición local)
     * Determina el ángulo exacto de un nodo específico 'k' dentro de este anillo.
     */
    public double getPhi(int k) {
        // Si es horario (-1), gira al revés. Si es antihorario (1), gira normal.
        double factorDireccion = sentidoHorario ? -1.0 : 1.0;
        
        // Fórmula: (Paso * k) + Rotación Inicial
        return (k * 2 * Math.PI / geometriaBase) * factorDireccion + rotacionPropia;
    }

    /**
     * GENERADOR DE PUNTOS 3D (El corazón matemático)
     * Convierte la geometría abstracta en coordenadas XYZ reales.
     * * @param idInicial El número con el que empieza a nombrar los puntos (ej: 1 para U-1)
     * @return Una lista de objetos Punto3D listos para ser pintados
     */
    public List<Punto3D> generarPuntos(int idInicial) {
        List<Punto3D> puntos = new ArrayList<>();
        double factorDireccion = sentidoHorario ? -1.0 : 1.0;

        for (int k = 0; k < limiteDibujo; k++) {
            // 1. Calculamos el ángulo local (phi) para este nodo k
            double phi = (k * 2 * Math.PI / geometriaBase) * factorDireccion + rotacionPropia;
            
            // 2. FÓRMULAS DE TOROIDE (Conversión de Esféricas a Cartesianas)
            // X y Z dependen del radio mayor (vuelta grande) y menor (grosor)
            // Y depende solo del radio menor (altura del tubo)
            
            double x = (radioMayor + radioMenor * Math.cos(phi)) * Math.cos(theta);
            double y = radioMenor * Math.sin(phi);
            double z = (radioMayor + radioMenor * Math.cos(phi)) * Math.sin(theta);
            
            // 3. Creamos el objeto Punto3D con los metadatos necesarios
            // Nota: El ID visual (ej: "U-1") es lo que el usuario ve en pantalla
            String idVisual = "U-" + (idInicial + k); // Corrección: Antes decía "P-", debe ser "U-" para Universos
            
            puntos.add(new Punto3D(x, y, z, idVisual, this.id, this.capa));
        }
        return puntos;
    }
}