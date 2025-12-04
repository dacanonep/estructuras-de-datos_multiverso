import java.util.ArrayList;
import java.util.List;

public class GeneradorGeometria {

    public static List<Anillo> crearAnillos() {
        List<Anillo> anillos = new ArrayList<>();
        int numCapas = 6;
        int numAnillos = 6;
        int nodosPorAnillo = 6;
        double rMenorBase = 50;
        double rMayorBase = 150;
        double gradosPorPaso = 360.0 / nodosPorAnillo;
        for (int capa = 0; capa < numCapas; capa++) {
            double rMenorActual = rMenorBase * Math.pow(2, capa);
            double rMayorActual = rMayorBase + rMenorActual;
            for (int i = 0; i < numAnillos; i++) {
                double theta = (-1) * (i - capa) * (2 * Math.PI / numAnillos);
                int idAnillo = (capa * numAnillos) + i;
                Anillo nuevoAnillo = new Anillo(idAnillo, capa, rMayorActual, rMenorActual, theta, nodosPorAnillo);
                if (capa == 0) {
                    nuevoAnillo.setSentidoHorario(true);
                    nuevoAnillo.rotar((-2 + i) * gradosPorPaso);
                } else {
                    nuevoAnillo.setLimiteVisible(5);
                    if (i % 2 == 0) {
                        nuevoAnillo.rotar(4 * gradosPorPaso);
                        nuevoAnillo.setSentidoHorario(false);
                    } else {
                        nuevoAnillo.rotar(2 * gradosPorPaso);
                        nuevoAnillo.setSentidoHorario(true);
                    }
                }
                anillos.add(nuevoAnillo);
            }
        }
        return anillos;
    }

}