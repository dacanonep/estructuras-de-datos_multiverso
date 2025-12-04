/**
 * CLASE: UNIVERSO (El Nodo)
 * ---------------------------------------------------------
 * Representa la unidad mínima de información en el sistema.
 * En términos de Estructura de Datos, esto es un "Vértice" o "Nodo" de un Grafo.
 * * * ROL ASIGNADO: El Arquitecto (Integrante 1)
 */
public class Universo {
    
    // Regla de Negocio: Un universo no puede interactuar con más de 6 vecinos
    // (Simulando una estructura hexagonal o compacta)
    private final int LIMITE_TOTAL = 6; 
    
    private String id; // Identificador único (Ej: "U-1")
    
    // Array fijo para guardar las referencias a los vecinos (Aristas del grafo)
    private Universo[] conexiones; 
    
    // Contadores para controlar el límite de interacciones
    private int cantidadSalientes; // Conexiones que YO inicié
    private int cantidadEntrantes; // Conexiones que OTROS hicieron hacia mí

    public Universo(String id) {
        this.id = id;
        this.conexiones = new Universo[LIMITE_TOTAL];
        this.cantidadSalientes = 0;
        this.cantidadEntrantes = 0;
    }

    public String getId() {
        return id;
    }

    public int getCantidadConexiones() {
        return cantidadSalientes;
    }

    // Acceso seguro al array de vecinos
    public Universo getVecinoEn(int indice) {
        if (indice >= 0 && indice < cantidadSalientes) {
            return conexiones[indice];
        }
        return null;
    }

    /**
     * MÉTODO: CONECTAR
     * Intenta crear un enlace unidireccional hacia otro universo.
     * Incluye validaciones estrictas para no exceder la capacidad.
     */
    public boolean conectarA(Universo destino) {
        // 1. Calcular carga actual de ambos universos
        int misInteracciones = this.cantidadSalientes + this.cantidadEntrantes;
        int susInteracciones = destino.cantidadSalientes + destino.cantidadEntrantes;

        // 2. Validar límites (Regla de negocio)
        if (misInteracciones >= LIMITE_TOTAL) {
            System.out.println("Bloqueo: El universo origen " + this.id + " ya tiene " + misInteracciones + " interacciones.");
            return false; 
        }
        if (susInteracciones >= LIMITE_TOTAL) {
            System.out.println("Bloqueo: El universo destino " + destino.getId() + " ya tiene " + susInteracciones + " interacciones.");
            return false;
        }

        // 3. Validar consistencia (No conectarse a sí mismo ni duplicar cables)
        if (this == destino) return false;
        for (int i = 0; i < cantidadSalientes; i++) {
            if (conexiones[i] == destino) return false; // Ya existe
        }

        // 4. Guardar conexión (Insertar en el array)
        conexiones[cantidadSalientes] = destino;
        
        // 5. Actualizar contadores
        cantidadSalientes++;
        destino.cantidadEntrantes++; // Avisar al destino que tiene una nueva conexión entrante
        
        return true;
    }

    /**
     * MÉTODO: DESCONECTAR
     * Elimina una conexión y reorganiza el array para no dejar huecos.
     */
    public void desconectarDe(Universo destino) {
        int indiceEncontrado = -1;
        
        // 1. Buscar si estamos conectados a ese destino
        for (int i = 0; i < cantidadSalientes; i++) {
            if(conexiones[i] == destino) {
                indiceEncontrado = i;
                break;
            }
        }
        
        if (indiceEncontrado == -1) return; // No estábamos conectados

        // 2. Algoritmo de Desplazamiento (Shift Left)
        // Mueve todos los elementos a la izquierda para tapar el hueco borrado
        for (int i = indiceEncontrado; i < cantidadSalientes - 1; i++) {
            conexiones[i] = conexiones[i + 1];
        }
        
        // 3. Limpieza final y ajuste de contadores
        conexiones[cantidadSalientes - 1] = null;
        cantidadSalientes--;
        destino.cantidadEntrantes--; // El destino libera un espacio de interacción
    }
    
    @Override
    public String toString() {
        return "Universo " + id + " (Total interacciones: " + (cantidadSalientes + cantidadEntrantes) + ")";
    }
}