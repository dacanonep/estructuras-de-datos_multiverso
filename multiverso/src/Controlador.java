/**
 * CLASE: CONTROLADOR (El Cerebro de los Datos)
 * ---------------------------------------------------------
 * Esta clase actúa como el "Modelo" en nuestra arquitectura MVC.
 * * RESPONSABILIDAD:
 * Gestionar la memoria del programa. Aquí se crean, guardan, buscan y 
 * destruyen los universos. Es el único que tiene acceso directo a los datos.
 * * ROL ASIGNADO: El Arquitecto (Integrante 1)
 */
public class Controlador {

    // Límite de memoria visual (para no saturar la pantalla)
    private static final int MAX_UNIVERSOS = 186;
    
    // Array estático para almacenar los objetos (Estructura de Datos Lineal)
    private Universo[] listaUniversos;
    
    // Contadores para saber cuántos hay y generar IDs únicos
    private int cantidadUniversos;
    private int contadorIds;

    public Controlador() {
        this.listaUniversos = new Universo[MAX_UNIVERSOS];
        this.cantidadUniversos = 0;
        this.contadorIds = 1; // Empezamos en U-1
    }

    public int obtenerCantidadTotal() {
        return this.cantidadUniversos;
    }

    /**
     * REINICIO TOTAL (Big Bang)
     * Borra todas las referencias para que el Recolector de Basura (GC)
     * de Java libere la memoria.
     */
    public void reiniciarTodo() {
        for (int i = 0; i < cantidadUniversos; i++) {
            listaUniversos[i] = null;
        }
        this.cantidadUniversos = 0;
        this.contadorIds = 1;
    }

    /**
     * FACTORY METHOD (Creación)
     * Crea un universo, le asigna un ID único y lo guarda en el array.
     */
    public Universo crearUniverso() {
        // 1. Verificación de límites
        if (cantidadUniversos >= MAX_UNIVERSOS) {
            System.out.println("Error: Memoria llena (Máximo " + MAX_UNIVERSOS + ")");
            return null;
        }
        
        // 2. Generación de ID (ej: "U-5")
        String nuevoId = "U-" + contadorIds;
        contadorIds++;
        
        // 3. Almacenamiento en memoria
        Universo nuevo = new Universo(nuevoId);
        listaUniversos[cantidadUniversos] = nuevo; // Guardar en la primera posición libre
        cantidadUniversos++;
        
        return nuevo;
    }

    /**
     * BÚSQUEDA LINEAL
     * Recorre el array para encontrar un universo por su nombre.
     */
    public Universo buscarUniverso(String id) {
        for (int i = 0; i < cantidadUniversos; i++) {
            // Usamos .equals() porque son Strings
            if(listaUniversos[i].getId().equals(id)) {
                return listaUniversos[i];
            }
        }
        return null; // No encontrado
    }

    /**
     * EXPORTAR DATOS
     * Devuelve una copia limpia del array (sin espacios nulos) 
     * para que la Vista (Lienzo) pueda dibujarlos.
     */
    public Universo[] obtenerTodos() {
        Universo[] export = new Universo[cantidadUniversos];
        for (int i = 0; i < cantidadUniversos; i++) {
            export[i] = listaUniversos[i];
        }
        return export;
    }

    /**
     * ALGORITMO DE ELIMINACIÓN (Shift Left)
     * Este es el método más complejo del controlador.
     * 1. Encuentra el objeto.
     * 2. Rompe sus conexiones (para no dejar punteros sueltos).
     * 3. Mueve todos los elementos siguientes una posición atrás para tapar el hueco.
     */
    public boolean destruirUniverso(String id) {
        int indiceBorrar = -1;
        Universo nodoABorrar = null;
        
        // PASO 1: Buscar el índice del elemento a borrar
        for (int i = 0; i < cantidadUniversos; i++) {
            if (listaUniversos[i].getId().equals(id)) {
                indiceBorrar = i;
                nodoABorrar = listaUniversos[i];
                break;
            }
        }
        
        if (indiceBorrar == -1) {
            return false; // No existía
        }

        // PASO 2: Desconectar de todos los vecinos (Limpieza de Grafo)
        // Es vital avisar a los demás que este nodo va a desaparecer
        for (int i = 0; i < cantidadUniversos; i++) {
            listaUniversos[i].desconectarDe(nodoABorrar);
        }

        // PASO 3: Reorganizar el Array (Shift Left)
        // Movemos los elementos desde [i+1] hacia [i]
        // Ejemplo: [A, B, (Borrar C), D, E] -> [A, B, D, E, null]
        for (int i = indiceBorrar; i < cantidadUniversos - 1; i++) {
            listaUniversos[i] = listaUniversos[i + 1];
        }
        
        // Limpiamos la última posición que quedó duplicada
        listaUniversos[cantidadUniversos - 1] = null;
        cantidadUniversos--;
        
        return true;
    }
}