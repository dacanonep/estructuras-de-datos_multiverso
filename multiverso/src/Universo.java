public class Universo {
    private final int LIMITE_TOTAL = 6; 
    private String id;
    private Universo[] conexiones; 
    private int cantidadSalientes;
    private int cantidadEntrantes; 

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

    public Universo getVecinoEn(int indice) {
        if (indice >= 0 && indice < cantidadSalientes) {
            return conexiones[indice];
        }
        return null;
    }

    public boolean conectarA(Universo destino) {
        int misInteracciones = this.cantidadSalientes + this.cantidadEntrantes;
        int susInteracciones = destino.cantidadSalientes + destino.cantidadEntrantes;
        if (misInteracciones >= LIMITE_TOTAL) {
            System.out.println("Bloqueo: El universo origen " + this.id + " ya tiene " + misInteracciones + " interacciones totales.");
            return false; 
        }
        if (susInteracciones >= LIMITE_TOTAL) {
            System.out.println("Bloqueo: El universo destino " + destino.getId() + " ya tiene " + susInteracciones + " interacciones totales.");
            return false;
        }
        if (this == destino) return false;
        for (int i = 0; i < cantidadSalientes; i++) {
            if (conexiones[i] == destino) return false;
        }
        conexiones[cantidadSalientes] = destino;
        cantidadSalientes++;
        destino.cantidadEntrantes++;
        return true;
    }

    public void desconectarDe(Universo destino) {
        int indiceEncontrado = -1;
        for (int i = 0; i < cantidadSalientes; i++) {
            if(conexiones[i] == destino) {
                indiceEncontrado = i;
                break;
            }
        }
        if (indiceEncontrado == -1) return;
        for (int i = indiceEncontrado; i < cantidadSalientes - 1; i++) {
            conexiones[i] = conexiones[i + 1];
        }
        conexiones[cantidadSalientes - 1] = null;
        cantidadSalientes--;
        destino.cantidadEntrantes--;
    }
    
    @Override

    public String toString() {
        return "Universo " + id + " (Total interacciones: " + (cantidadSalientes + cantidadEntrantes) + ")";
    }

}