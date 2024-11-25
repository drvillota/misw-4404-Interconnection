package view;

import model.data_structures.TablaSimbolos;
import model.logic.Modelo;

public class View {
	/**
	 * Metodo constructor
	 */
	public View() {

	}

	public String generarMenu() {
		return "1. Cargar datos\n2. Componentes conectados\n3. Encontrar landings interconexión\n4. Ruta mínima\n5. Red de expansión mínima\n6. Fallas en conexión\n7. Exit\nDar el numero de opcion a resolver, luego oprimir tecla Return: (e.g., 1):\n";
	}

	public void printMenu() {
		System.out.println(generarMenu());
	}

	// Método que reemplaza printMessage y printModelo
	public void printObject(Object objeto) {
		System.out.println(objeto.toString());
	}
}