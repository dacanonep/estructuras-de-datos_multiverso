/**
 * CLASE: OBJETO VISUAL (El Truco del Z-Buffer)
 * ---------------------------------------------------------
 * Esta clase es una herramienta auxiliar para el motor de renderizado.
 * * PROBLEMA:
 * En 2D, si dibujas dos cosas, la última tapa a la primera.
 * En 3D, necesitamos que los objetos LEJANOS se dibujen primero (fondo)
 * y los CERCANOS se dibujen al final (frente).
 * * * SOLUCIÓN (Algoritmo del Pintor):
 * Guardamos cada instrucción de dibujo con su profundidad (Z) en esta clase,
 * las ordenamos de mayor a menor profundidad, y luego las pintamos.
 * * * ROL ASIGNADO: El Artista (Integrante 3)
 */
public class ObjetoVisual implements Comparable<ObjetoVisual> {
    
    public double z;        // Profundidad: Qué tan lejos está de la cámara
    public Runnable dibujo; // La instrucción gráfica (ej: "Dibuja un círculo rojo aquí")

    /**
     * CONSTRUCTOR
     * Empaqueta la profundidad y la instrucción de dibujo.
     * @param z Distancia al observador (Mayor número = Más lejos)
     * @param dibujo Una función lambda () -> { ... } con el código de Graphics2D
     */
    public ObjetoVisual(double z, Runnable dibujo) {
        this.z = z; 
        this.dibujo = dibujo;
    }

    /**
     * MÉTODO DE ORDENAMIENTO (Comparable)
     * Java usa esto cuando llamamos a Collections.sort(lista).
     * * Definimos que un objeto es "mayor" que otro si está más cerca,
     * para que el ordenamiento los ponga de ATRÁS hacia ADELANTE.
     */
    @Override
    public int compareTo(ObjetoVisual o) {
        // CORRECCIÓN CRÍTICA: Orden Descendente (Mayor Z -> Menor Z)
        // Comparamos el "otro" contra "este" para invertir el orden natural.
        // Resultado: Primero se dibujará lo que tenga Z más grande (Fondo).
        return Double.compare(o.z, this.z); 
    }
}