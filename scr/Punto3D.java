/**
 * CLASE: PUNTO 3D (La Unidad de Espacio)
 * ---------------------------------------------------------
 * Es una estructura de datos simple (POJO) que representa una coordenada
 * en el espacio tridimensional, más algunos metadatos útiles.
 * * * ROL ASIGNADO: El Matemático (Integrante 2)
 */
public class Punto3D {
    // Coordenadas espaciales
    public double x, y, z;
    
    // Metadatos para saber "quién es este punto"
    public String id;     // Identificador del universo (ej: "U-1")
    public int idAnillo;  // A qué anillo geométrico pertenece
    public int idCapa;    // En qué capa de profundidad está (0 a 5)

    public Punto3D(double x, double y, double z, String id, int idAnillo, int idCapa) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.idAnillo = idAnillo;
        this.idCapa = idCapa;
    }
}