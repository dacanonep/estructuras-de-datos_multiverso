import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MultiversoMain extends JFrame {

    public MultiversoMain() {
        this.setTitle("Multiverso Toroidal");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        Controlador controlador = new Controlador();
        Camara camara = new Camara();
        List<Anillo> listaAnillos = GeneradorGeometria.crearAnillos();
        Lienzo lienzo = new Lienzo(camara, controlador, listaAnillos);
        lienzo.setBackground(Color.BLACK);
        Controles controles = new Controles(lienzo, camara, controlador);
        this.add(lienzo, BorderLayout.CENTER);
        this.add(controles, BorderLayout.SOUTH);
        Timer timerRender = new Timer(30, e -> {
            camara.actualizarSuavizado();
            lienzo.repaint(); 
        });
        timerRender.start();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(false);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.ddoffscreen", "false");
        System.setProperty("sun.java2d.noddraw", "true");
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception ex) {}
        SwingUtilities.invokeLater(() -> {
            new MultiversoMain();
        });
    }

}