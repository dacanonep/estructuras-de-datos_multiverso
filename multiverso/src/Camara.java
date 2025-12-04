public class Camara {
    public double x = 0, y = 0, z = 0;
    public double anguloX = 0;
    public double anguloY = 0;
    public double factorZoom = 1;
    public double objX = 0, objY = 0, objZ = 0;
    public double objetivoZoom = 1;

    public Camara() {
        this.x = 0; this.y = 0; this.z = 0;
        this.objX = 0; this.objY = 0; this.objZ = 0;
        this.anguloX = 0; 
        this.anguloY = 0;
        this.factorZoom = 1;
        this.objetivoZoom = 1;
    }

    public void actualizarSuavizado() {
        x += (objX - x) * 0.1;
        y += (objY - y) * 0.1;
        z += (objZ - z) * 0.1;
        factorZoom += (objetivoZoom - factorZoom) * 0.1;
    }

    public void reset() {
        this.objX = 0; this.objY = 0; this.objZ = 0;
        this.objetivoZoom = 1;
        this.anguloX = 0;
        this.anguloY = 0;
    }

}