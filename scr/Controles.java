import javax.swing.*;
import java.awt.*;
import java.util.HashSet;

/**
 * CLASE: CONTROLES (La Interfaz de Usuario)
 * ---------------------------------------------------------
 * Contiene todos los botones, paneles y cuadros de texto.
 * * RESPONSABILIDAD:
 * Capturar los eventos del usuario (clicks) y llamar a los métodos correspondientes
 * en el 'Lienzo' o el 'Controlador' para ejecutar la acción.
 * * ROL ASIGNADO: El Piloto (Integrante 4)
 */
public class Controles extends JPanel {

    // Referencia al controlador para poder buscar universos en la navegación inteligente
    private Controlador controlador; 

    public Controles(Lienzo lienzo, Camara camara, Controlador controlador) {
        this.controlador = controlador;

        // Diseño del Panel Principal (FlowLayout para que se ajusten los bloques)
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        this.setBackground(new Color(30, 30, 30));

        // =====================================================
        // 1. SECCIÓN UNIVERSOS (Gestión + Navegación)
        // =====================================================
        JPanel pnlUniversos = new JPanel(new BorderLayout(5, 5));
        pnlUniversos.setOpaque(false);
        pnlUniversos.setBorder(BorderFactory.createTitledBorder(null, "UNIVERSOS", 0, 0, new Font("Arial", Font.BOLD, 10), Color.CYAN));

        // A. Sub-panel de Generación Masiva
        JPanel pnlGen = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        pnlGen.setOpaque(false);
        JTextField txtGen = new JTextField("37", 3);
        JButton btnGen = new JButton("GENERAR");
        btnGen.setBackground(Color.CYAN);
        btnGen.setMargin(new Insets(2, 5, 2, 5));
        btnGen.addActionListener(e -> {
            // Llama a la lógica de "Big Bang" en el Lienzo
            try { lienzo.accionGenerarMasa(Integer.parseInt(txtGen.getText())); } 
            catch (Exception ex) {}
        });
        pnlGen.add(txtGen);
        pnlGen.add(btnGen);

        // B. Sub-panel de CRUD (+ Añadir / - Eliminar)
        JPanel pnlCrud = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pnlCrud.setOpaque(false);
        JButton btnAdd = new JButton("➕"); // Botón para crear uno nuevo
        btnAdd.setToolTipText("Añadir un nuevo universo");
        btnAdd.setBackground(new Color(100, 255, 100));
        btnAdd.addActionListener(e -> lienzo.accionCrear());
        
        JButton btnDel = new JButton("➖"); // Botón para destruir el seleccionado
        btnDel.setToolTipText("Eliminar universo seleccionado");
        btnDel.setBackground(new Color(255, 100, 100));
        btnDel.addActionListener(e -> lienzo.accionDestruir());
        pnlCrud.add(btnAdd);
        pnlCrud.add(btnDel);

        // C. Sub-panel de Navegación Inteligente (Prev / Next)
        JPanel pnlNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        pnlNav.setOpaque(false);
        JTextField txtIr = new JTextField("1", 3);
        
        JButton btnPrev = new JButton("◀"); 
        btnPrev.setBackground(Color.WHITE);
        
        JButton btnNext = new JButton("▶"); 
        btnNext.setBackground(Color.WHITE);

        // LÓGICA DE SALTO INTELIGENTE:
        // Si borramos el universo 2, al pasar del 1 no nos quedamos trabados,
        // saltamos automáticamente al 3 (o al siguiente disponible).
        btnPrev.addActionListener(e -> {
            try {
                int actual = lienzo.obtenerNumeroActual();
                // Bucle hacia atrás buscando uno que exista
                for (int i = actual - 1; i >= 1; i--) {
                    if (controlador.buscarUniverso("U-" + i) != null) {
                        txtIr.setText("" + i);
                        lienzo.irANodo(i); // Viajar
                        return;
                    }
                }
            } catch (Exception ex) {}
        });

        btnNext.addActionListener(e -> {
            try {
                int actual = lienzo.obtenerNumeroActual();
                // Bucle hacia adelante
                for (int i = actual + 1; i <= 216; i++) {
                    if (controlador.buscarUniverso("U-" + i) != null) {
                        txtIr.setText("" + i);
                        lienzo.irANodo(i);
                        return;
                    }
                }
            } catch (Exception ex) {}
        });
        
        JButton btnIr = new JButton("IR"); // Salto directo
        btnIr.setBackground(new Color(200, 200, 255));
        btnIr.setMargin(new Insets(2, 5, 2, 5));
        btnIr.addActionListener(e -> {
            try { lienzo.irANodo(Integer.parseInt(txtIr.getText())); } catch (Exception ex) {}
        });

        pnlNav.add(btnPrev); pnlNav.add(txtIr); pnlNav.add(btnIr); pnlNav.add(btnNext);

        // Ensamblaje del panel Universos
        JPanel pnlTopU = new JPanel(new GridLayout(2, 1));
        pnlTopU.setOpaque(false);
        pnlTopU.add(pnlGen); pnlTopU.add(pnlCrud);
        pnlUniversos.add(pnlTopU, BorderLayout.NORTH);
        pnlUniversos.add(pnlNav, BorderLayout.SOUTH);


        // =====================================================
        // 2. SECCIÓN ENLACES (Conectar / Desconectar)
        // =====================================================
        JPanel pnlEnlaces = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlEnlaces.setOpaque(false);
        pnlEnlaces.setBorder(BorderFactory.createTitledBorder(null, "ENLACES", 0, 0, new Font("Arial", Font.BOLD, 10), Color.YELLOW));

        // Botón Toggle para modo edición (Click origen -> Click destino)
        JToggleButton btnConectar = new JToggleButton("➕ CONECTAR");
        btnConectar.setBackground(new Color(150, 255, 150));
        btnConectar.setFont(new Font("Arial", Font.BOLD, 11));
        
        btnConectar.addActionListener(e -> {
            if (btnConectar.isSelected()) {
                if (lienzo.idSeleccionado != null) {
                    // Activamos modo edición en el lienzo
                    lienzo.modoEdicion = true;
                    lienzo.idOrigenEnlace = lienzo.idSeleccionado;
                    btnConectar.setBackground(Color.YELLOW);
                    btnConectar.setText("CLICK DESTINO...");
                } else {
                    JOptionPane.showMessageDialog(this, "Selecciona un universo origen primero.");
                    btnConectar.setSelected(false);
                }
            } else {
                // Cancelar modo edición
                lienzo.modoEdicion = false;
                lienzo.idOrigenEnlace = null;
                btnConectar.setBackground(new Color(150, 255, 150));
                btnConectar.setText("CONECTAR");
            }
        });

        JButton btnRomper = new JButton("➖ ROMPER");
        btnRomper.setBackground(new Color(255, 150, 150)); 
        btnRomper.setFont(new Font("Arial", Font.BOLD, 11));
        btnRomper.addActionListener(e -> {
            // Verifica si seleccionamos un cable (click en la línea)
            if (lienzo.linkSelectedOrigen != null && lienzo.linkSelectedDestino != null) {
                try {
                   int idDest = Integer.parseInt(lienzo.linkSelectedDestino.replace("U-", ""));
                   // Truco: Forzamos la selección al origen para que accionDesconectar sepa quién es
                   lienzo.idSeleccionado = lienzo.linkSelectedOrigen; 
                   lienzo.accionDesconectar(idDest);
                   
                   lienzo.linkSelectedOrigen = null; 
                   lienzo.linkSelectedDestino = null;
                   lienzo.repaint();
                } catch(Exception ex) {}
            } else {
                JOptionPane.showMessageDialog(this, "Haz click en un cable primero.");
            }
        });

        pnlEnlaces.add(btnConectar);
        pnlEnlaces.add(btnRomper);


        // =====================================================
        // 3. SECCIÓN RUTAS (GPS y Animación)
        // =====================================================
        JPanel pnlRutas = new JPanel(new GridLayout(2, 3, 2, 2));
        pnlRutas.setOpaque(false);
        pnlRutas.setBorder(BorderFactory.createTitledBorder(null, "RUTAS", 0, 0, new Font("Arial", Font.BOLD, 10), Color.MAGENTA));

        // A. Botón Play/Stop
        JButton btnPlay = new JButton("▶");
        btnPlay.setToolTipText("Recorrer ruta actual");
        btnPlay.setBackground(new Color(0, 200, 200));
        btnPlay.addActionListener(e -> {
            if (lienzo.enRecorrido) {
                lienzo.detenerRecorrido();
                btnPlay.setText("▶");
                btnPlay.setBackground(new Color(0, 200, 200));
            } else {
                if (lienzo.rutaVisualizada != null && !lienzo.rutaVisualizada.isEmpty()) {
                    lienzo.iniciarRecorrido(lienzo.rutaVisualizada);
                    btnPlay.setText("⏹");
                    btnPlay.setBackground(Color.RED);
                } else {
                    JOptionPane.showMessageDialog(null, "No hay ruta trazada.");
                }
            }
        });

        // B. Botón GPS (Calcula la ruta más corta)
        JButton btnViajar = new JButton("🗺");
        btnViajar.setToolTipText("Trazar nueva ruta");
        btnViajar.setBackground(Color.ORANGE);
        btnViajar.addActionListener(e -> {
             if (lienzo.enRecorrido) { lienzo.detenerRecorrido(); return; }
            
            // Diálogo personalizado para ingresar Origen y Destino
            JPanel myPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            JTextField campoInicio = new JTextField(5);
            JTextField campoDestino = new JTextField(5);
            if (lienzo.idSeleccionado != null) campoInicio.setText(lienzo.idSeleccionado.replace("U-", ""));

            myPanel.add(new JLabel("Desde ID:")); myPanel.add(campoInicio);
            myPanel.add(new JLabel("Hasta ID:")); myPanel.add(campoDestino);

            // Centramos el diálogo en la ventana padre
            Window ventanaPrincipal = SwingUtilities.getWindowAncestor(this);
            int result = JOptionPane.showConfirmDialog(ventanaPrincipal, myPanel, "GPS", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                String tInicio = campoInicio.getText().trim();
                String tDestino = campoDestino.getText().trim();
                if (!tInicio.startsWith("U-")) tInicio = "U-" + tInicio;
                if (!tDestino.startsWith("U-")) tDestino = "U-" + tDestino;
                
                // Llamamos al algoritmo BFS en el Lienzo
                java.util.List<String> camino = lienzo.buscarRuta(tInicio, tDestino);
                
                if (camino != null) {   
                        lienzo.rutaVisualizada = camino; 
                        lienzo.rutaSet = new HashSet<>(camino); 
                        lienzo.repaint();      
                } else {
                    JOptionPane.showMessageDialog(this, "No existe camino.");
                }
            }
        });

        // C. Botón Limpiar
        JButton btnBorrarRuta = new JButton("❌");
        btnBorrarRuta.setToolTipText("Borrar ruta");
        btnBorrarRuta.setBackground(new Color(50, 50, 50));
        btnBorrarRuta.setForeground(Color.RED);
        btnBorrarRuta.addActionListener(e -> {
            lienzo.rutaVisualizada = null;
            lienzo.rutaSet.clear();
            lienzo.repaint(); 
        });

        // D. Botón Aleatorio (Arquitecto de Redes)
        JButton btnRandom = new JButton("🎲");
        btnRandom.setToolTipText("Conexiones Aleatorias");
        btnRandom.setBackground(new Color(200, 100, 255));
        btnRandom.addActionListener(e -> lienzo.accionConectarAleatorio());

        // E. Botón Aislamiento (Solo ver ruta)
        JToggleButton btnIso = new JToggleButton("👁");
        btnIso.setToolTipText("Modo Aislamiento (Solo Ruta)");
        btnIso.setBackground(new Color(50, 50, 50));
        btnIso.setForeground(Color.CYAN);
        btnIso.addActionListener(e -> {
            lienzo.mostrarSoloRuta = btnIso.isSelected();
            btnIso.setBackground(btnIso.isSelected() ? Color.CYAN : new Color(50, 50, 50));
            btnIso.setForeground(btnIso.isSelected() ? Color.BLACK : Color.CYAN);
            lienzo.repaint();
        });

        pnlRutas.add(btnPlay); pnlRutas.add(btnViajar); pnlRutas.add(btnBorrarRuta);
        pnlRutas.add(btnRandom); pnlRutas.add(btnIso);


        // =====================================================
        // 4. SECCIÓN VISUALIZACIÓN (Capas y Modos)
        // =====================================================
        JPanel pnlVis = new JPanel(new BorderLayout());
        pnlVis.setOpaque(false);
        pnlVis.setBorder(BorderFactory.createTitledBorder(null, "VISUALIZACIÓN", 0, 0, new Font("Arial", Font.BOLD, 10), Color.WHITE));

        // Subpanel Checkboxes (Control manual de capas)
        JPanel pnlCapas = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        pnlCapas.setOpaque(false);
        String[] nCapas = {"M", "A", "V", "A", "N", "R"};
        Color[] cCapas = {new Color(148, 0, 211), Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED};

        lienzo.listaCheckboxes.clear(); 
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            JCheckBox chk = new JCheckBox(nCapas[i], false);
            chk.setEnabled(false);
            chk.setOpaque(false);
            chk.setForeground(cCapas[i]);
            chk.setFont(new Font("Arial", Font.BOLD, 12));
            chk.addActionListener(e -> {
                lienzo.capasActivas[idx] = chk.isSelected();
                lienzo.repaint();
            });
            lienzo.listaCheckboxes.add(chk);
            pnlCapas.add(chk);
        }

        // Subpanel Modos de Vista
        JPanel pnlModos = new JPanel(new GridLayout(1, 2, 5, 0));
        pnlModos.setOpaque(false);
        
        JButton btnEnfoque = new JButton("ENFOQUE");
        btnEnfoque.setBackground(Color.DARK_GRAY);
        btnEnfoque.setForeground(Color.WHITE);
        btnEnfoque.setFont(new Font("Arial", Font.PLAIN, 10));
        btnEnfoque.addActionListener(e -> {
            if (lienzo.idSeleccionado != null) {
                // Modo Enfoque: Solo prende la capa del universo seleccionado
                try {
                    int num = Integer.parseInt(lienzo.idSeleccionado.replace("U-", ""));
                    int index = num - 1;
                    if (index < lienzo.estructuraPuntos.size()) {
                        int capaTarget = lienzo.estructuraPuntos.get(index).idCapa;
                        // Apagar todas, encender target
                        for(int k=0; k<6; k++) lienzo.capasActivas[k] = (k == capaTarget);
                        lienzo.actualizarCheckboxesInterfaz();
                        lienzo.repaint();
                    }
                } catch(Exception ex){}
            } else {
                JOptionPane.showMessageDialog(null, "Selecciona un universo primero.");
            }
        });

        JButton btnGeneral = new JButton("GENERAL");
        btnGeneral.setBackground(Color.WHITE);
        btnGeneral.setFont(new Font("Arial", Font.PLAIN, 10));
        btnGeneral.addActionListener(e -> {
            // Modo General: Prende todo y quita la selección
            for(int k=0; k<6; k++) lienzo.capasActivas[k] = true;
            lienzo.actualizarCheckboxesInterfaz();
            lienzo.idSeleccionado = null;
            lienzo.repaint();
        });

        pnlModos.add(btnEnfoque);
        pnlModos.add(btnGeneral);

        pnlVis.add(pnlCapas, BorderLayout.CENTER);
        pnlVis.add(pnlModos, BorderLayout.SOUTH);

        // AGREGAR TODO AL PANEL PRINCIPAL
        this.add(pnlUniversos);
        this.add(pnlEnlaces);
        this.add(pnlRutas);
        this.add(pnlVis);
    }
}