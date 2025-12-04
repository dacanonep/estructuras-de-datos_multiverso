import java.util.ArrayList;
import java.util.List;

/**
 * CLASE: GENERADOR DE GEOMETRÍA (El Arquitecto)
 * ---------------------------------------------------------
 * Esta clase se encarga de crear la estructura base del multiverso.
 * No dibuja nada, solo calcula dónde deben ir los anillos matemáticamente.
 * * RESPONSABILIDAD:
 * Instanciar y configurar todos los objetos 'Anillo' con sus radios,
 * ángulos y rotaciones iniciales.
 * * ROL ASIGNADO: El Matemático (Integrante 2)
 */
public class GeneradorGeometria {

    /**
     * MÉTODO DE FÁBRICA
     * Genera la lista completa de anillos que conforman el toroide.
     * Se llama una sola vez al iniciar el programa.
     */
    public static List<Anillo> crearAnillos() {
        List<Anillo> anillos = new ArrayList<>();
        
        // --- PARÁMETROS DE CONFIGURACIÓN ---
        int numCapas = 6;          // Profundidad (Cebolla)
        int numAnillos = 6;        // Cortes transversales por vuelta
        int nodosPorAnillo = 6;    // Universos por cada anillo pequeño
        
        // Dimensiones físicas del Toroide
        double rMenorBase = 50;    // Radio del "tubo" de la dona
        double rMayorBase = 150;   // Radio total desde el centro del agujero
        
        // Cuánto giramos en cada paso (360 grados / 6 nodos = 60 grados)
        double gradosPorPaso = 360.0 / nodosPorAnillo;

        // BUCLE 1: CAPAS (De adentro hacia afuera)
        for (int capa = 0; capa < numCapas; capa++) {
            
            // Crecimiento Exponencial: Cada capa es el doble de grande que la anterior
            // Esto permite que el multiverso sea inmenso hacia afuera.
            double rMenorActual = rMenorBase * Math.pow(2, capa);
            double rMayorActual = rMayorBase + rMenorActual;

            // BUCLE 2: ANILLOS (La vuelta completa a la dona)
            for (int i = 0; i < numAnillos; i++) {
                
                // Theta: Ángulo de posición del anillo en la dona grande (0 a 2*PI)
                // El (-1) y la resta (i - capa) crean un efecto de espiral visual.
                double theta = (-1) * (i - capa) * (2 * Math.PI / numAnillos);
                
                // ID único para el anillo
                int idAnillo = (capa * numAnillos) + i;
                
                // Crear el objeto Anillo con las matemáticas calculadas
                Anillo nuevoAnillo = new Anillo(idAnillo, capa, rMayorActual, rMenorActual, theta, nodosPorAnillo);

                // --- LÓGICA DE ESTILO Y ROTACIÓN ---
                // Esto hace que los anillos no se vean aburridos y alineados,
                // sino que parezcan engranajes girando en diferentes sentidos.
                
                if (capa == 0) {
                    // Capa central (Núcleo): Configuración simple
                    nuevoAnillo.setSentidoHorario(true);
                    nuevoAnillo.rotar((-2 + i) * gradosPorPaso);
                } else {
                    // Capas externas:
                    // Dejamos un hueco visible (5 nodos en vez de 6) para ver el interior
                    nuevoAnillo.setLimiteVisible(5);
                    
                    // Alternamos el giro (Pares vs Impares) para efecto de maquinaria
                    if (i % 2 == 0) {
                        nuevoAnillo.rotar(4 * gradosPorPaso);
                        nuevoAnillo.setSentidoHorario(false);
                    } else {
                        nuevoAnillo.rotar(2 * gradosPorPaso);
                        nuevoAnillo.setSentidoHorario(true);
                    }
                }
                
                // Guardamos el anillo listo en la lista
                anillos.add(nuevoAnillo);
            }
        }
        return anillos;
    }
}