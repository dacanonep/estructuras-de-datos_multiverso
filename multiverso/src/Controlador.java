public class Controlador {
    private static final int MAX_UNIVERSOS = 186;
    private Universo[] listaUniversos;
    private int cantidadUniversos;
    private int contadorIds;

    public Controlador() {
        this.listaUniversos = new Universo[MAX_UNIVERSOS];
        this.cantidadUniversos = 0;
        this.contadorIds = 1;
    }

    public int obtenerCantidadTotal() {
        return this.cantidadUniversos;
    }

    public void reiniciarTodo() {
        for (int i = 0; i < cantidadUniversos; i++) {
            listaUniversos[i] = null;
        }
        this.cantidadUniversos = 0;
        this.contadorIds = 1;
    }

    public Universo crearUniverso() {
        if (cantidadUniversos >= MAX_UNIVERSOS) {
            System.out.println("Error: Memoria llena (Máximo " + MAX_UNIVERSOS + ")");
            return null;
        }
        String nuevoId = "U-" + contadorIds;
        contadorIds++;
        Universo nuevo = new Universo(nuevoId);
        listaUniversos[cantidadUniversos] = nuevo;
        cantidadUniversos++;
        return nuevo;
    }

    public Universo buscarUniverso(String id) {
        for (int i = 0; i < cantidadUniversos; i++) {
            if(listaUniversos[i].getId().equals(id)) {
                return listaUniversos[i];
            }
        }
        return null;
    }

    public Universo[] obtenerTodos() {
        Universo[] export = new Universo[cantidadUniversos];
        for (int i = 0; i < cantidadUniversos; i++) {
            export[i] = listaUniversos[i];
        }
        return export;
    }

    public boolean destruirUniverso(String id) {
        int indiceBorrar = -1;
        Universo nodoABorrar = null;
        for (int i = 0; i < cantidadUniversos; i++) {
            if (listaUniversos[i].getId().equals(id)) {
                indiceBorrar = i;
                nodoABorrar = listaUniversos[i];
                break;
            }
        }
        if (indiceBorrar == -1) {
            return false;
        }
        for (int i = 0; i < cantidadUniversos; i++) {
            listaUniversos[i].desconectarDe(nodoABorrar);
        }
        for (int i = indiceBorrar; i < cantidadUniversos; i++) {
            listaUniversos[i].desconectarDe(nodoABorrar);
        }
        for (int i = indiceBorrar; i < cantidadUniversos - 1; i++) {
            listaUniversos[i] = listaUniversos[i + 1];
        }
        listaUniversos[cantidadUniversos - 1] = null;
        cantidadUniversos--;
        return true;
    }
    
}