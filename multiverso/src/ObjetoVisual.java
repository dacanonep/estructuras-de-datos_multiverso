class ObjetoVisual implements Comparable<ObjetoVisual> {
    double z; 
    Runnable dibujo; 

    public ObjetoVisual(double z, Runnable dibujo) {
        this.z = z; this.dibujo = dibujo;
    }

    @Override
    
    public int compareTo(ObjetoVisual o) {
        return Double.compare(this.z, o.z); 
    }
    
}