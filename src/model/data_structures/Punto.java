package model.data_structures;

public class Punto {
    private double x;
    private double y;

    // Constructor para inicializar las coordenadas
    public Punto(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Métodos para obtener las coordenadas
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Métodos para modificar las coordenadas
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    // Método para mostrar las coordenadas como un String
    @Override
    public String toString() {
        return "Punto(" + x + ", " + y + ")";
    }

    // Método para calcular la distancia entre dos puntos
    public double calcularDistancia(Punto otroPunto) {
        return Math.sqrt(Math.pow(this.x - otroPunto.x, 2) + Math.pow(this.y - otroPunto.y, 2));
    }
}
