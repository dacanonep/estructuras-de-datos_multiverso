import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * CLASE: LIENZO (El Motor Gráfico y Lógico)
 * ---------------------------------------------------------
 * Es el corazón visual del proyecto. Se encarga de:
 * 1. Dibujar el mundo 3D en una superficie 2D (JPanel).
 * 2. Gestionar la interacción del mouse (clicks, arrastre).
 * 3. Ejecutar la lógica de navegación automática (Modo Cine).
 * * ROL ASIGNADO: El Artista (Integrante 3)
 */
public class Lienzo extends JPanel {
    
    // --- REFERENCIAS EXTERNAS ---
    private Camara camara;       // Para saber desde dónde miramos
    private Controlador controlador; // Para acceder a los datos reales

    // --- DATOS VISUALES ---
    // Lista de todos los puntos ya calculados (X,Y,Z)
    public List<Punto3D> estructuraPuntos = new ArrayList<>();
    // Lista de anillos geométricos (para dibujar el "alambre" de la dona)
    public List<Anillo> listaAnillos = new ArrayList<>();

    // --- ESTADO DEL MOUSE ---
    public String idBajoMouse = null;    // ¿Qué universo está señalando el cursor?
    public String idSeleccionado = "U-1"; // ¿Cuál está seleccionado actualmente?
    public String idOrigenEnlace = null;  // Para cuando vas a conectar dos nodos
    private int lastMouseX, lastMouseY;   // Para calcular cuánto arrastraste el mouse
    private int cursorActualX = -1, cursorActualY = -1;

    // --- ESTADO DE ENLACES (CABLES) ---
    public String linkHoverOrigen = null;
    public String linkHoverDestino = null;
    public String linkSelectedOrigen = null;
    public String linkSelectedDestino = null;

    // --- ESTADO DE RUTAS (GPS) ---
    public List<String> rutaVisualizada = null; // La lista ordenada del camino
    public Set<String> rutaSet = new HashSet<>(); // Para búsquedas rápidas (O(1))
    public boolean mostrarSoloRuta = false; // Modo Aislamiento

    // --- ESTADO VISUAL ---
    public boolean mostrarEstructura = true; // Mostrar/Ocultar anillos de fondo
    // Qué capas están visibles (M, A, V, A, N, R)
    public boolean[] capasActivas = {false, false, false, false, false, false};
    
    // --- LOGICA DE EDICIÓN Y ANIMACIÓN ---
    public boolean enRecorrido = false; // ¿Estamos en "Modo Cine"?
    public boolean modoEdicion = false; // ¿Estamos conectando nodos manualmente?

    // Referencias a los checkboxes de la interfaz para actualizarlos
    public List<JCheckBox> listaCheckboxes = new ArrayList<>();

    // Variables para la animación del recorrido
    private javax.swing.Timer timerRecorrido;
    private List<String> rutaAnimacion = new ArrayList<>();
    private int pasoActualAnimacion = 0;
    public String bridgeOrigen = null;  // Flecha blanca temporal
    public String bridgeDestino = null; // Flecha blanca temporal

    public Lienzo(Camara camara, Controlador controlador, List<Anillo> anillos) {
        this.camara = camara;
        this.controlador = controlador;
        this.listaAnillos = anillos;
        
        this.setBackground(Color.BLACK); // Fondo del espacio
        this.setFocusable(true); // Para recibir eventos de teclado si quisieras
        
        inicializarMouse(); // Configurar los listeners
        reconstruirTodo();  // Generar la primera nube de puntos
    }
    
    /**
     * CONFIGURACIÓN DE INTERACCIÓN
     * Aquí definimos qué pasa cuando haces click, arrastras o usas la rueda.
     */
    private void inicializarMouse() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { 
                lastMouseX = e.getX(); 
                lastMouseY = e.getY(); 
                if (enRecorrido) return; // Si viajas, no puedes tocar nada

                // CASO 1: CLICK EN UN UNIVERSO
                if (idBajoMouse != null) {
                    // Verificamos si la capa está activa antes de permitir click
                    try {
                        int num = Integer.parseInt(idBajoMouse.replace("U-", ""));
                        int index = num - 1;
                        if (index < estructuraPuntos.size()) {
                            int capa = estructuraPuntos.get(index).idCapa;
                            if (!capasActivas[capa]) return; // Ignorar clicks en fantasmas
                        }
                    } catch (Exception ex) {}

                    if (modoEdicion) {
                        gestionarClickEdicion(idBajoMouse); // Lógica de conectar
                    } else {
                        idSeleccionado = idBajoMouse; // Selección normal
                    }
                    // Limpiar selección de cables
                    linkSelectedOrigen = null;
                    linkSelectedDestino = null;
                } 
                // CASO 2: CLICK EN UN CABLE
                else if (linkHoverOrigen != null && linkHoverDestino != null) {
                    linkSelectedOrigen = linkHoverOrigen;
                    linkSelectedDestino = linkHoverDestino;
                    idSeleccionado = null; 
                    System.out.println("Enlace seleccionado: " + linkSelectedOrigen + " -> " + linkSelectedDestino);
                }
                // CASO 3: CLICK EN EL VACÍO
                else {
                    linkSelectedOrigen = null;
                    linkSelectedDestino = null;
                }
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                cursorActualX = e.getX();
                cursorActualY = e.getY();
                // No llamamos repaint() aquí para ahorrar CPU, solo cuando sea necesario
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (enRecorrido) return;
                
                // ROTACIÓN DE CÁMARA
                // Cambiamos los ángulos según cuánto moviste el mouse
                camara.anguloY += (e.getX() - lastMouseX) * 0.01;
                camara.anguloX += (e.getY() - lastMouseY) * 0.01;
                
                lastMouseX = e.getX(); 
                lastMouseY = e.getY();
                repaint(); // Actualizar vista
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                 if (enRecorrido) return;
                 
                 // ZOOM CON LA RUEDA
                 double rotacion = e.getPreciseWheelRotation();
                 if (rotacion < 0) {
                     camara.objetivoZoom *= 1.1; // Acercar
                 } else {
                     camara.objetivoZoom *= 0.9; // Alejar
                 }
                 
                 // Límites para no romper la matriz de proyección
                 if (camara.objetivoZoom < 0.01) camara.objetivoZoom = 0.01;
                 if (camara.objetivoZoom > 50.0) camara.objetivoZoom = 50.0;
                 repaint();
            }
        };
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        this.addMouseWheelListener(mouseHandler);
    }

    // Genera la lista plana de puntos 3D a partir de la geometría de anillos
    public void reconstruirTodo() {
        estructuraPuntos.clear();
        int contadorGlobal = 1;
        for (Anillo a : listaAnillos) {
            List<Punto3D> puntosDelAnillo = a.generarPuntos(contadorGlobal);
            estructuraPuntos.addAll(puntosDelAnillo);
            contadorGlobal += puntosDelAnillo.size();
        }
        System.out.println("Estructura visual reconstruida. Total nodos: " + estructuraPuntos.size());
        repaint();
    }    

    // Mueve la cámara y enfoca la capa correcta para ver un nodo específico
    public void irANodo(int numero) {
        String idBusqueda = "U-" + numero;
        if (controlador.buscarUniverso(idBusqueda) != null) {
            this.idSeleccionado = idBusqueda; 
            int index = numero - 1;
            if (index < estructuraPuntos.size()) {
                int capaDelDestino = estructuraPuntos.get(index).idCapa;
                gestionarEnfoqueAutomatico(capaDelDestino); // Prender capa necesaria
            }
            repaint();
        } else {
            System.out.println("El universo " + idBusqueda + " no existe.");
        }
    }

    // Métodos auxiliares de navegación simple
    public void siguienteNodo() {
        try {
            String numeroStr = idSeleccionado.replace("U-", "");
            int actual = Integer.parseInt(numeroStr);
            irANodo(actual + 1); 
        } catch (Exception e) {
            irANodo(1);
        }
    }

    public void anteriorNodo() {
        try {
            String numeroStr = idSeleccionado.replace("U-", "");
            int actual = Integer.parseInt(numeroStr);
            if (actual > 1) {
                irANodo(actual - 1);
            }
        } catch (Exception e) {
            irANodo(1);
        }
    }

    public int obtenerNumeroActual() {
        try {
            return Integer.parseInt(idSeleccionado.replace("U-", ""));
        } catch (Exception e) { return 1; }
    }

    // --- ACCIONES DE GESTIÓN (Llamadas desde los botones) ---

    public void accionCrear() {
        Universo nuevo = controlador.crearUniverso();
        if (nuevo != null) {
            System.out.println("Nació: " + nuevo.getId());
            int idNuevo = Integer.parseInt(nuevo.getId().replace("U-", ""));
            
            // Auto-conexión (cadena simple)
            if (idNuevo > 1) {
                String idAnterior = "U-" + (idNuevo - 1);
                Universo anterior = controlador.buscarUniverso(idAnterior);
                if (anterior != null) {
                    boolean conectado = anterior.conectarA(nuevo);
                    if (conectado) {
                        System.out.println("Auto-conexión: " + anterior.getId() + " -> " + nuevo.getId());
                    }
                }
            }
            irANodo(idNuevo); 
        } else {
            JOptionPane.showMessageDialog(this, "Límite de memoria visual alcanzado.");
        }
        actualizarDisponibilidadCapas(); // Habilitar checkboxes si es necesario
        repaint();
    }

    public void accionDestruir() {
        boolean exito = controlador.destruirUniverso(idSeleccionado);
        if (exito) {
            System.out.println("Universo destruido: " + idSeleccionado);
            anteriorNodo(); 
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo destruir (¿Ya no existe?).");
        }
        actualizarDisponibilidadCapas();
        repaint();
    }

    public void accionGenerarMasa(int cantidadDeseada) {
        if (cantidadDeseada < 1) return;
        if (cantidadDeseada > 216) { // Límite por diseño visual (6x6x6)
            JOptionPane.showMessageDialog(this, "El límite máximo visual es 216.");
            cantidadDeseada = 216;
        }
        controlador.reiniciarTodo();
        Universo anterior = null;
        for (int i = 0; i < cantidadDeseada; i++) {
            Universo nuevo = controlador.crearUniverso();
            if (anterior != null && nuevo != null) {
                anterior.conectarA(nuevo);
            }
            anterior = nuevo;
        }
        System.out.println("Generada secuencia de " + cantidadDeseada + " universos.");
        actualizarDisponibilidadCapas();
        irANodo(1);
        repaint();
    }

    public void accionConectar(int idDestino) {
        // ... (Lógica interna usada por el modo edición) ...
        Universo origen = controlador.buscarUniverso(idSeleccionado);
        String idDestStr = "U-" + idDestino;
        Universo destino = controlador.buscarUniverso(idDestStr);
        if (origen != null && destino != null) {
            // Regla de distancia visual para no ensuciar la pantalla
            int numOrigen = Integer.parseInt(origen.getId().replace("U-", ""));
            int numDestino = idDestino;
            int distancia = Math.abs(numOrigen - numDestino);
            if (distancia > 7) { 
                JOptionPane.showMessageDialog(this, "¡Conexión demasiado lejana! Se verá feo.\nSolo conecta vecinos cercanos.");
                return;
            }
            boolean resultado = origen.conectarA(destino);
            if (resultado) {
                System.out.println("Conexión exitosa: " + origen.getId() + " -> " + destino.getId());
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo conectar (Máx 6 conexiones o ya existe).");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Universo origen o destino no válidos.");
        }
        repaint();
    }

    public void accionDesconectar(int idDestino) {
        Universo origen = controlador.buscarUniverso(idSeleccionado);
        String idDestStr = "U-" + idDestino;
        Universo destino = controlador.buscarUniverso(idDestStr);
        if (origen != null && destino != null) {
            origen.desconectarDe(destino);
            System.out.println("Enlace roto: " + origen.getId() + " -/-> " + destino.getId());
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró el universo destino para desconectar.");
        }
    }

    // Generador aleatorio de conexiones (El Dado)
    public void accionConectarAleatorio() {
        Universo[] todos = controlador.obtenerTodos();
        int conexionesCreadas = 0;
        for (int i = 0; i < todos.length; i++) {
            Universo origen = todos[i];
            if (origen == null) continue;
            int intentos = 0;
            // Intentamos conectar hasta tener 6 vecinos
            while (intentos < 5 && origen.getCantidadConexiones() < 6) {
                intentos++;
                int idRandom = (int) (Math.random() * todos.length);
                Universo destino = todos[idRandom];
                if (destino != null && origen != destino) {
                    // Validamos reglas geométricas antes de conectar
                    if (cumpleReglasDeVecindad(origen, destino)) {
                        if (origen.conectarA(destino)) {
                            conexionesCreadas++;
                        }
                    }
                }
            }
        }
        repaint();
        JOptionPane.showMessageDialog(this, "Se crearon " + conexionesCreadas + " conexiones orgánicas.");
    }

    // Gestiona el segundo click en "Modo Conectar"
    private void gestionarClickEdicion(String idClickeado) {
        if (idOrigenEnlace == null) {
            idOrigenEnlace = idClickeado;
            System.out.println("Origen seleccionado para enlace: " + idOrigenEnlace);
        } 
        else if (!idOrigenEnlace.equals(idClickeado)) {
            Universo origen = controlador.buscarUniverso(idOrigenEnlace);
            Universo destino = controlador.buscarUniverso(idClickeado);
            if (origen != null && destino != null) {
                if (origen.conectarA(destino)) {
                     System.out.println("Conectado: " + idOrigenEnlace + " -> " + idClickeado);
                } else {
                     JOptionPane.showMessageDialog(this, "No se pudo conectar (Límite o duplicado).");
                }
            }
            modoEdicion = false;
            idOrigenEnlace = null;
            JOptionPane.showMessageDialog(this, "Conexión finalizada. Modo edición desactivado.");
        }
    }

    // --- ALGORITMOS DE RUTA (BFS) ---
    // Busca el camino más corto en un grafo no ponderado.
    public List<String> buscarRuta(String idInicio, String idFin) {
        java.util.Queue<Universo> cola = new java.util.LinkedList<>();
        java.util.Map<Universo, Universo> predecesores = new java.util.HashMap<>();
        java.util.Set<Universo> visitados = new java.util.HashSet<>();
        
        Universo inicio = controlador.buscarUniverso(idInicio);
        Universo fin = controlador.buscarUniverso(idFin);
        if (inicio == null || fin == null) return null;
        
        cola.add(inicio);
        visitados.add(inicio);
        predecesores.put(inicio, null);
        boolean encontrado = false;
        
        while (!cola.isEmpty()) {
            Universo actual = cola.poll();
            if (actual == fin) {
                encontrado = true;
                break;
            }
            // Explorar vecinos
            for (int i = 0; i < actual.getCantidadConexiones(); i++) {
                Universo vecino = actual.getVecinoEn(i);
                if (vecino != null && !visitados.contains(vecino)) {
                    visitados.add(vecino);
                    predecesores.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        if (!encontrado) return null;
        
        // Reconstrucción del camino (Backtracking)
        List<String> ruta = new java.util.LinkedList<>();
        Universo curr = fin;
        while (curr != null) {
            ruta.add(0, curr.getId());
            curr = predecesores.get(curr);
        }
        return ruta;
    }

    // Inicia la animación automática "Modo Cine"
    public void iniciarRecorrido(List<String> rutaPersonalizada) {
        if (enRecorrido) return;
        this.rutaAnimacion = (rutaPersonalizada != null) ? rutaPersonalizada : new ArrayList<>();
        if (rutaAnimacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay ruta para recorrer.");
            return;
        }
        enRecorrido = true;
        pasoActualAnimacion = 0;
        try {
            int inicioId = Integer.parseInt(rutaAnimacion.get(0).replace("U-", ""));
            irANodo(inicioId);
        } catch (Exception e) {}
        
        // Timer lento (600ms) para disfrutar el viaje
        timerRecorrido = new Timer(600, e -> pasoRecorrido()); 
        timerRecorrido.start();
    }

    private void pasoRecorrido() {
        if (pasoActualAnimacion >= rutaAnimacion.size() - 1) {
            JOptionPane.showMessageDialog(this, "¡Llegada al destino!");
            detenerRecorrido();
            return;
        }
        String idActual = rutaAnimacion.get(pasoActualAnimacion);
        String idSiguiente = rutaAnimacion.get(pasoActualAnimacion + 1);
        
        bridgeOrigen = idActual;
        bridgeDestino = idSiguiente;
        
        try {
            int numSiguiente = Integer.parseInt(idSiguiente.replace("U-", ""));
            irANodo(numSiguiente);
            
            // Efecto cinematográfico: Ajustar zoom y ángulo según capa
            int index = numSiguiente - 1;
            if (index < estructuraPuntos.size()) {
                int capa = estructuraPuntos.get(index).idCapa;
                gestionarEnfoqueAutomatico(capa);
                ajustarCamaraParaCapa(capa);
            }
        } catch (Exception e) {}
        pasoActualAnimacion++;
        repaint();
    }

    public void detenerRecorrido() {
        if (timerRecorrido != null) timerRecorrido.stop();
        enRecorrido = false; 
        bridgeOrigen = null;
        bridgeDestino = null;
        // Restaurar estado visual
        try {
            int actual = obtenerNumeroActual();
            int index = actual - 1;
            if (index < estructuraPuntos.size()) {
                int capaActual = estructuraPuntos.get(index).idCapa;
                gestionarEnfoqueAutomatico(capaActual); 
            }
        } catch (Exception e) {
            for (int i = 0; i < 6; i++) capasActivas[i] = true; 
        }
        actualizarCheckboxesInterfaz();
        JOptionPane.showMessageDialog(this, "Has llegado a tu destino. Puedes mover la cámara.");
        repaint();
    }

    // Configura la cámara ideal para cada capa (Director de Fotografía)
    private void ajustarCamaraParaCapa(int nCapa) {
        double nuevoZoom = 1.0;
        int gradosInclinacion = 30;
        int gradosRotacion = 0;
        switch (nCapa) {
            case 0: // Violeta
                nuevoZoom = 2.0; gradosInclinacion = -30; gradosRotacion = 60; break;
            case 1: // Azul
                nuevoZoom = 1.2; gradosInclinacion = -40; gradosRotacion = 60; break;
            case 2: // Verde
                nuevoZoom = 0.8; gradosInclinacion = -50; gradosRotacion = 60; break;
            case 3: // Amarillo
                nuevoZoom = 0.5; gradosInclinacion = -60; gradosRotacion = 60; break;
            case 4: // Naranja
                nuevoZoom = 0.3; gradosInclinacion = -70; gradosRotacion = 60; break;
            case 5: // Rojo
                nuevoZoom = 0.18; gradosInclinacion = -80; gradosRotacion = 60; break;
        }
        camara.objetivoZoom = nuevoZoom;
        camara.anguloX = Math.toRadians(gradosInclinacion); 
        camara.anguloY = Math.toRadians(gradosRotacion);
    }

    private void gestionarEnfoqueAutomatico(int idCapaActual) {
        for (int i = 0; i < 6; i++) {
            capasActivas[i] = (i == idCapaActual);
        }
        actualizarCheckboxesInterfaz();
    }

    // Método PÚBLICO para que Controles pueda llamarlo
    public void actualizarCheckboxesInterfaz() {
        for (int i = 0; i < listaCheckboxes.size(); i++) {
            if (i < 6) {
                listaCheckboxes.get(i).setSelected(capasActivas[i]);
            }
        }
    }

    public void actualizarDisponibilidadCapas() {
        boolean[] capaOcupada = new boolean[6]; 
        Universo[] todos = controlador.obtenerTodos();
        for (Universo u : todos) {
            if (u != null) {
                try {
                    int idNum = Integer.parseInt(u.getId().replace("U-", ""));
                    int indexEstructura = idNum - 1;
                    if (indexEstructura < estructuraPuntos.size()) {
                        int idCapa = estructuraPuntos.get(indexEstructura).idCapa;
                        if (idCapa >= 0 && idCapa < 6) {
                            capaOcupada[idCapa] = true; 
                        }
                    }
                } catch (Exception e) {}
            }
        }
        for (int i = 0; i < listaCheckboxes.size(); i++) {
            JCheckBox chk = listaCheckboxes.get(i);
            if (capaOcupada[i]) {
                if (!chk.isEnabled()) {
                    chk.setEnabled(true);
                    chk.setSelected(false); // No auto-activar
                    capasActivas[i] = false;
                }
            } else {
                chk.setEnabled(false);
                chk.setSelected(false);
                capasActivas[i] = false;
            }
        }
        repaint();
    }

    private boolean cumpleReglasDeVecindad(Universo u1, Universo u2) {
        try {
            int idx1 = Integer.parseInt(u1.getId().replace("U-", "")) - 1;
            int idx2 = Integer.parseInt(u2.getId().replace("U-", "")) - 1;
            if (idx1 >= estructuraPuntos.size() || idx2 >= estructuraPuntos.size()) return false;
            Punto3D p1 = estructuraPuntos.get(idx1);
            Punto3D p2 = estructuraPuntos.get(idx2);
            int diffCapa = Math.abs(p1.idCapa - p2.idCapa);
            if (diffCapa > 1) return false; 
            int diffID = Math.abs(idx1 - idx2);
            int umbral = (diffCapa == 0) ? 6 : 12; 
            if (diffID > umbral) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Color obtenerColorCapa(int capa, int alpha) {
        if (capasActivas[capa]) {
            return obtenerColorEspectro(capa, alpha);
        } else {
            int alphaFantasma = Math.min(alpha, 30); 
            return new Color(100, 100, 100, alphaFantasma);
        }
    }

    private Color obtenerColorEspectro(int indiceCapa, int alpha) {
        if (alpha > 255) alpha = 255; if (alpha < 0) alpha = 0;
        switch (indiceCapa % 6) { 
            case 0: return new Color(148, 0, 211, alpha);
            case 1: return new Color(0, 0, 255, alpha);
            case 2: return new Color(0, 255, 0, alpha);
            case 3: return new Color(255, 255, 0, alpha);
            case 4: return new Color(255, 127, 0, alpha);
            case 5: return new Color(255, 0, 0, alpha);
            default: return new Color(255, 255, 255, alpha);
        }
    }

    private Punto3D proyectarPunto(Punto3D p) {
        // Transformación 3D -> 2D
        double xr = p.x - camara.x;
        double yr = p.y - camara.y;
        double zr = p.z - camara.z;
        // Rotación Y
        double xRot = xr * Math.cos(camara.anguloY) - zr * Math.sin(camara.anguloY);
        double zRot = xr * Math.sin(camara.anguloY) + zr * Math.cos(camara.anguloY);
        // Rotación X
        double yRot = yr * Math.cos(camara.anguloX) - zRot * Math.sin(camara.anguloX);
        return new Punto3D(xRot, yRot, zRot, p.id, p.idAnillo, p.idCapa);
    }

    private double distanciaPuntoASegmento(int px, int py, int x1, int y1, int x2, int y2) {
        // Fórmula matemática para saber si el mouse toca una línea
        double l2 = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if (l2 == 0) return Math.sqrt(Math.pow(px - x1, 2) + Math.pow(py - y1, 2));
        double t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2;
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);
        return Math.sqrt(Math.pow(px - projX, 2) + Math.pow(py - projY, 2));
    }

    private void dibujarFlecha(Graphics2D g2, int x1, int y1, int x2, int y2, double escalaDestino) {
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(x1, y1, x2, y2);
        
        // Matemáticas para dibujar la cabeza triangular
        double angulo = Math.atan2(y2 - y1, x2 - x1);
        int tamano = (int) (12 * escalaDestino);
        if (tamano < 4) tamano = 4;
        
        double xPunta1 = x2 - tamano * Math.cos(angulo - Math.PI / 6);
        double yPunta1 = y2 - tamano * Math.sin(angulo - Math.PI / 6);
        double xPunta2 = x2 - tamano * Math.cos(angulo + Math.PI / 6);
        double yPunta2 = y2 - tamano * Math.sin(angulo + Math.PI / 6);
        
        Polygon cabeza = new Polygon();
        cabeza.addPoint(x2, y2); cabeza.addPoint((int) xPunta1, (int) yPunta1); cabeza.addPoint((int) xPunta2, (int) yPunta2);
        g2.fillPolygon(cabeza);
    }

    // --- MOTOR DE RENDERIZADO (El Corazón) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Activar Anti-Aliasing para que se vea suave
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int centroX = getWidth() / 2;
        int centroY = getHeight() / 2;
        List<ObjetoVisual> listaDibujo = new ArrayList<>();
        
        // 1. DIBUJAR ESTRUCTURA (ANILLOS)
        if (mostrarEstructura && !mostrarSoloRuta) {
             int numCapas = 6; 
            int anillosPorCapa = 6;
            for (int c = 0; c < numCapas; c++) {
                final int capaActual = c;
                int indexJefe = c * anillosPorCapa;
                if (indexJefe < listaAnillos.size()) {
                    Anillo jefe = listaAnillos.get(indexJefe);
                    for (int k = 0; k < jefe.geometriaBase; k++) {
                        double phi = jefe.getPhi(k);
                        Polygon riel = new Polygon();
                        double profundidadPromedio = 0;
                        for (int step = 0; step <= 60; step++) {
                            // Cálculo matemático de posición en el toroide
                            double theta = (step * 2 * Math.PI / 60);
                            double x = (jefe.radioMayor + jefe.radioMenor * Math.cos(phi)) * Math.cos(theta);
                            double y = jefe.radioMenor * Math.sin(phi);
                            double z = (jefe.radioMayor + jefe.radioMenor * Math.cos(phi)) * Math.sin(theta);
                            
                            // Rotación según cámara
                            double xr = x - camara.x; double yr = y - camara.y; double zr = z - camara.z;
                            double xRot = xr * Math.cos(camara.anguloY) - zr * Math.sin(camara.anguloY);
                            double zRot = xr * Math.sin(camara.anguloY) + zr * Math.cos(camara.anguloY);
                            double yRot = yr * Math.cos(camara.anguloX) - zRot * Math.sin(camara.anguloX);
                            
                            riel.addPoint((int)(centroX + xRot*camara.factorZoom), (int)(centroY + yRot*camara.factorZoom));
                            profundidadPromedio += zRot; 
                        }
                        double zFinalRiel = profundidadPromedio / 60;
                        listaDibujo.add(new ObjetoVisual(zFinalRiel, () -> {
                            g2.setStroke(new BasicStroke(2));
                            g2.setColor(obtenerColorCapa(capaActual, 100)); // Color o Gris según capa
                            g2.drawPolyline(riel.xpoints, riel.ypoints, riel.npoints);
                        }));
                    }
                }
            }
            // Dibujar Aros Transversales
            for (Anillo anillo : listaAnillos) {
                double phiMuestra = anillo.getPhi(0);
                double zMuestra = (anillo.radioMayor + anillo.radioMenor * Math.cos(phiMuestra)) * Math.sin(anillo.theta) - camara.z;
                double zRotAnillo = 0 * Math.sin(camara.anguloY) + zMuestra * Math.cos(camara.anguloY); 
                listaDibujo.add(new ObjetoVisual(zRotAnillo, () -> {
                    Polygon aro = new Polygon();
                    for (int k = 0; k <= 30; k++) {
                        double factorDir = anillo.sentidoHorario ? -1.0 : 1.0;
                        double anguloLocal = (k * 2 * Math.PI / 30) * factorDir + anillo.rotacionPropia;
                        double x = (anillo.radioMayor + anillo.radioMenor * Math.cos(anguloLocal)) * Math.cos(anillo.theta);
                        double y = anillo.radioMenor * Math.sin(anguloLocal);
                        double z = (anillo.radioMayor + anillo.radioMenor * Math.cos(anguloLocal)) * Math.sin(anillo.theta);
                        
                        double xr = x - camara.x; double yr = y - camara.y; double zr = z - camara.z;
                        double xRot = xr * Math.cos(camara.anguloY) - zr * Math.sin(camara.anguloY);
                        double zRot = xr * Math.sin(camara.anguloY) + zr * Math.cos(camara.anguloY);
                        double yRot = yr * Math.cos(camara.anguloX) - zRot * Math.sin(camara.anguloX);
                        
                        aro.addPoint((int)(centroX + xRot*camara.factorZoom), (int)(centroY + yRot*camara.factorZoom));
                    }
                    g2.setStroke(new BasicStroke(2)); 
                    g2.setColor(obtenerColorCapa(anillo.capa, 180));
                    g2.drawPolyline(aro.xpoints, aro.ypoints, aro.npoints);
                }));
            }
        }
        
        // 2. CALCULAR PROYECCIONES (3D -> 2D)
        class PuntoProy { int x, y; double z; double escala; }
        List<PuntoProy> proyecciones = new ArrayList<>();
        for (Punto3D p : estructuraPuntos) {
            Punto3D rotado = proyectarPunto(p); 
            PuntoProy pp = new PuntoProy();
            pp.x = (int) (centroX + (rotado.x * camara.factorZoom));
            pp.y = (int) (centroY + (rotado.y * camara.factorZoom));
            pp.z = rotado.z; 
            
            // Efecto de perspectiva (Lo lejano es pequeño)
            double d = 600; 
            double distancia = d - rotado.z;
            if (distancia < 1) distancia = 1;
            double scale = d / distancia;
            if (scale > 0.9) scale = 0.9;
            if (scale < 0.4) scale = 0.4;
            pp.escala = scale;
            proyecciones.add(pp);
        }
        
        // 3. DETECCIÓN DE MOUSE
        idBajoMouse = null; 
        double distMin = 1000;
        for (int i = 0; i < proyecciones.size(); i++) {
            PuntoProy pp = proyecciones.get(i);
            double dist = Math.sqrt(Math.pow(cursorActualX - pp.x, 2) + Math.pow(cursorActualY - pp.y, 2));
            if (dist < 15 && pp.escala > 0) {
                String idTemp = "U-" + (i + 1);
                int capa = estructuraPuntos.get(i).idCapa;
                boolean esParteRuta = !rutaSet.isEmpty() && rutaSet.contains(idTemp);
                // Solo detectar si es visible o parte de la ruta
                if (capasActivas[capa] || esParteRuta) {  
                    if (dist < distMin) {
                        distMin = dist;
                        idBajoMouse = idTemp;
                    }
                }
            }
        }
        
        // 4. PREPARAR DIBUJO DE ENLACES
        g2.setStroke(new BasicStroke(2));
        linkHoverOrigen = null; linkHoverDestino = null;
        Universo[] universosActivos = controlador.obtenerTodos();
        for (Universo origen : universosActivos) {
            if (origen == null) continue;
            try {
                int idx1 = Integer.parseInt(origen.getId().replace("U-", "")) - 1;
                if (idx1 >= 0 && idx1 < proyecciones.size()) {
                    PuntoProy p1 = proyecciones.get(idx1);
                    Punto3D datosOrigen = estructuraPuntos.get(idx1); 
                    for (int k = 0; k < origen.getCantidadConexiones(); k++) {
                        Universo destino = origen.getVecinoEn(k);
                        if (destino != null) {
                            int idx2 = Integer.parseInt(destino.getId().replace("U-", "")) - 1;
                            if (idx2 >= 0 && idx2 < proyecciones.size()) {
                                PuntoProy p2 = proyecciones.get(idx2);
                                Punto3D datosDestino = estructuraPuntos.get(idx2);
                                double zPromedio = (p1.z + p2.z) / 2;
                                
                                // Lógica de Ruta (GPS)
                                boolean origenEnRuta = !rutaSet.isEmpty() && rutaSet.contains(origen.getId());
                                boolean destinoEnRuta = !rutaSet.isEmpty() && rutaSet.contains(destino.getId());
                                boolean esCableDeRuta = false;
                                if (origenEnRuta && destinoEnRuta && rutaVisualizada != null) {
                                    int iOr = rutaVisualizada.indexOf(origen.getId());
                                    int iDes = rutaVisualizada.indexOf(destino.getId());
                                    if (Math.abs(iOr - iDes) == 1) esCableDeRuta = true;
                                }
                                
                                // Lógica de Visibilidad
                                boolean origenVisible = capasActivas[datosOrigen.idCapa];
                                boolean destinoVisible = capasActivas[datosDestino.idCapa];
                                if (!esCableDeRuta) {
                                    if (!origenVisible && !destinoVisible) continue; // Si ambos son invisibles, ocultar
                                    if (mostrarSoloRuta) continue;
                                }
                                
                                // Mouse Over en Cable
                                if (distanciaPuntoASegmento(cursorActualX, cursorActualY, p1.x, p1.y, p2.x, p2.y) < 6) {
                                    linkHoverOrigen = origen.getId(); linkHoverDestino = destino.getId();
                                }
                                
                                boolean esSeleccionada = (origen.getId().equals(linkSelectedOrigen) && destino.getId().equals(linkSelectedDestino));
                                boolean esHover = (origen.getId().equals(linkHoverOrigen) && destino.getId().equals(linkHoverDestino));
                                final boolean fEsCableDeRuta = esCableDeRuta;
                                
                                // Añadir a la lista de dibujo (Ordenado por Z)
                                listaDibujo.add(new ObjetoVisual(zPromedio, () -> {
                                    int alpha = (int) Math.min(255, Math.max(50, 255 * p1.escala));
                                    if (fEsCableDeRuta) {
                                        // Ruta GPS: Magenta Neón y Gigante
                                        g2.setStroke(new BasicStroke(6.0f));
                                        g2.setColor(Color.MAGENTA);
                                        dibujarFlecha(g2, p1.x, p1.y, p2.x, p2.y, p2.escala * 3.0);
                                        // Brillo blanco interior
                                        g2.setColor(Color.WHITE);
                                        dibujarFlecha(g2, p1.x, p1.y, p2.x, p2.y, p2.escala * 1.5);
                                    }
                                    else if (esSeleccionada) {
                                        g2.setStroke(new BasicStroke(5.0f)); g2.setColor(Color.CYAN);
                                        dibujarFlecha(g2, p1.x, p1.y, p2.x, p2.y, p2.escala);
                                    }
                                    else if (esHover) {
                                        g2.setStroke(new BasicStroke(4.0f)); g2.setColor(Color.LIGHT_GRAY);
                                        dibujarFlecha(g2, p1.x, p1.y, p2.x, p2.y, p2.escala);
                                    }
                                    else {
                                        // Enlace Normal: Color de la capa destino
                                        g2.setStroke(new BasicStroke(2.5f));
                                        g2.setColor(obtenerColorEspectro(datosDestino.idCapa, alpha));
                                        dibujarFlecha(g2, p1.x, p1.y, p2.x, p2.y, p2.escala);
                                    }
                                }));
                            }
                        }
                    }
                }
            } catch (Exception e) { }
        }
        
        // 5. PREPARAR DIBUJO DE NODOS (ESFERAS)
        for (int i = 0; i < estructuraPuntos.size(); i++) {
            Punto3D pDatos = estructuraPuntos.get(i);
            String idLogico = "U-" + (i + 1);
            boolean esNodoRuta = !rutaSet.isEmpty() && rutaSet.contains(idLogico);
            
            // Filtro de visibilidad
            if (!esNodoRuta) {
                if (mostrarSoloRuta) continue;
                if (!capasActivas[pDatos.idCapa]) continue;
            }
            
            Universo u = controlador.buscarUniverso(idLogico);
            if (u != null) {
                PuntoProy pp = proyecciones.get(i);
                if (pp.escala > 0) { 
                    listaDibujo.add(new ObjetoVisual(pp.z, () -> {
                        int radio = (int) (18 * pp.escala);
                        if (radio > 80) radio = 80;
                        int brillo = (int) Math.min(255, Math.max(50, 150 * pp.escala));
                        
                        Color base = esNodoRuta ? Color.MAGENTA : obtenerColorEspectro(pDatos.idCapa, 255);
                        if (esNodoRuta) brillo = 255;
                        
                        g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), brillo));
                        g2.fillOval(pp.x - radio/2, pp.y - radio/2, radio, radio);
                        
                        if (esNodoRuta || idLogico.equals(idSeleccionado)) {
                            g2.setColor(esNodoRuta ? Color.WHITE : Color.CYAN);
                            g2.setStroke(new BasicStroke(3));
                            g2.drawOval(pp.x - radio/2 - 4, pp.y - radio/2 - 4, radio + 8, radio + 8);
                        }
                        
                        // Texto (ID)
                        int tamanoLetra = Math.max(10, (int)(14 * pp.escala));
                        g2.setFont(new Font("Arial", Font.BOLD, tamanoLetra));
                        g2.setColor(Color.BLACK); 
                        g2.drawString(u.getId(), pp.x + radio/2 + 2, pp.y - radio/2 - 2);
                        g2.setColor(Color.WHITE); 
                        g2.drawString(u.getId(), pp.x + radio/2, pp.y - radio/2 - 4);
                    }));
                }
            }
        }
        
        // 6. Z-BUFFER: ORDENAR Y DIBUJAR
        // Esto es lo que hace que el 3D se vea real (lo de atrás no tapa a lo de adelante)
        Collections.sort(listaDibujo);
        for (ObjetoVisual ov : listaDibujo) ov.dibujo.run();
        
        // HUD / DEBUG
        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.drawString("Vista: " + (idSeleccionado==null?"Libre":idSeleccionado), 20, 20);
        g2.drawString("Zoom: " + String.format("%.2f", camara.factorZoom), getWidth() - 150, 20);
    }
}