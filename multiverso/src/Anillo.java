import java.util.ArrayList;
import java.util.List;

public class Anillo {
    public int id;
    public int capa; 
    public double radioMayor, radioMenor;
    public double theta;
    public int geometriaBase;
    public int limiteDibujo;
    public double rotacionPropia = 0;
    public boolean sentidoHorario = false;

    public Anillo(int id, int capa, double rMayor, double rMenor, double theta, int geometriaBase) {
        this.id = id;
        this.capa = capa;
        this.radioMayor = rMayor;
        this.radioMenor = rMenor;
        this.theta = theta;
        this.geometriaBase = geometriaBase;
        this.limiteDibujo = geometriaBase;
    }

    public void setLimiteVisible(int cuantosPuntos) {
        this.limiteDibujo = cuantosPuntos;
    }

    public void rotar(double grados) {
        this.rotacionPropia = Math.toRadians(grados);
    }

    public void setSentidoHorario(boolean activar) {
        this.sentidoHorario = activar;
    }
    
    public double getPhi(int k) {
        double factorDireccion = sentidoHorario ? -1.0 : 1.0;
        return (k * 2 * Math.PI / geometriaBase) * factorDireccion + rotacionPropia;
    }

    public List<Punto3D> generarPuntos(int idInicial) {
        List<Punto3D> puntos = new ArrayList<>();
        double factorDireccion = sentidoHorario ? -1.0 : 1.0;
        for (int k = 0; k < limiteDibujo; k++) {
            double phi = (k * 2 * Math.PI / geometriaBase) * factorDireccion + rotacionPropia;
            double x = (radioMayor + radioMenor * Math.cos(phi)) * Math.cos(theta);
            double y = radioMenor * Math.sin(phi);
            double z = (radioMayor + radioMenor * Math.cos(phi)) * Math.sin(theta);
            String idVisual = "P-" + (idInicial + k);
            puntos.add(new Punto3D(x, y, z, idVisual, this.id, this.capa));
        }
        return puntos;
    }
    
}