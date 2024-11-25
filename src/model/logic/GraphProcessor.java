package model.logic;

import model.data_structures.*;
import model.data_structures.Country.ComparadorXKm;
import utils.Ordenamiento;
import java.util.Comparator;
import utils.Distancia;

public class GraphProcessor {

    private final GrafoListaAdyacencia grafo;
    private final ITablaSimbolos paises;
    private final ITablaSimbolos points;
    private final ITablaSimbolos landingIdTabla;
    private final ITablaSimbolos nombreCodigo;

    public GraphProcessor(GrafoListaAdyacencia grafo, ITablaSimbolos paises, ITablaSimbolos points,
                          ITablaSimbolos landingIdTabla, ITablaSimbolos nombreCodigo) {
        this.grafo = grafo;
        this.paises = paises;
        this.points = points;
        this.landingIdTabla = landingIdTabla;
        this.nombreCodigo = nombreCodigo;
    }
    
    // Refactorizado para mejorar legibilidad
    public String obtenerInfoCluseteresConectados(String punto1, String punto2) {
        ITablaSimbolos tabla = grafo.getSSC();
        ILista lista = tabla.valueSet();
        int maxComponentesConectados = obtenerMaxComponentesConectados(lista);
        
        String resultado = "La cantidad de componentes conectados es: " + maxComponentesConectados;
        resultado += verificarCluesteresConectados(punto1, punto2, tabla);
        return resultado;
    }
    
    private int obtenerMaxComponentesConectados(ILista lista) {
        int max = 0;
        for (int i = 1; i <= lista.size(); i++) {
            try {
                int valor = (int) lista.getElement(i);
                if (valor > max) {
                    max = valor;
                }
            } catch (PosException | VacioException e) {
                System.out.println(e.toString());
            }
        }
        return max;
    }

    private String verificarCluesteresConectados(String punto1, String punto2, ITablaSimbolos tabla) {
        String fragmento = "";
        try {
            String codigo1 = (String) nombreCodigo.get(punto1);
            String codigo2 = (String) nombreCodigo.get(punto2);
            Vertex vertice1 = (Vertex) ((ILista) landingIdTabla.get(codigo1)).getElement(1);
            Vertex vertice2 = (Vertex) ((ILista) landingIdTabla.get(codigo2)).getElement(1);
            
            int elemento1 = (int) tabla.get(vertice1.getId());
            int elemento2 = (int) tabla.get(vertice2.getId());
            
            if (elemento1 == elemento2) {
                fragmento = "\nLos landing points pertenecen al mismo clúster";
            } else {
                fragmento = "\nLos landing points no pertenecen al mismo clúster";
            }
        } catch (PosException | VacioException e) {
            e.printStackTrace();
        }
        return fragmento;
    }
    
    // Refactorizado para simplificar la lógica de iteración
    public String obtenerLandingConectados() {
        StringBuilder fragmento = new StringBuilder();
        ILista lista = landingIdTabla.valueSet();
        int contador = 0;

        for (int i = 1; i <= lista.size(); i++) {
            try {
                ILista landingList = (ILista) lista.getElement(i);
                if (landingList.size() > 1 && contador <= 10) {
                    Landing landing = (Landing) ((Vertex) landingList.getElement(1)).getInfo();
                    int cantidadArcos = contarArcos(landingList);
                    
                    fragmento.append("\nLanding: \nNombre: ").append(landing.getName())
                             .append("\nPaís: ").append(landing.getPais())
                             .append("\nId: ").append(landing.getId())
                             .append("\nCantidad de arcos: ").append(cantidadArcos);
                    contador++;
                }
            } catch (PosException | VacioException e) {
                e.printStackTrace();
            }
        }
        return fragmento.toString();
    }

    private int contarArcos(ILista landingList) {
        int cantidad = 0;
        for (int j = 1; j <= landingList.size(); j++) {
            Vertex vertice = (Vertex) landingList.getElement(j);
            cantidad += vertice.edges().size();
        }
        return cantidad;
    }

    // Simplificación de la obtención de ruta entre países
    public String obtenerRutaEntrePaises(String pais1, String pais2) {
        Country pais1Obj = (Country) paises.get(pais1);
        Country pais2Obj = (Country) paises.get(pais2);
        String capital1 = pais1Obj.getCapitalName();
        String capital2 = pais2Obj.getCapitalName();

        PilaEncadenada pila = grafo.minPath(capital1, capital2);
        return construirRuta(pila);
    }

    private String construirRuta(PilaEncadenada pila) {
        StringBuilder fragmento = new StringBuilder("Ruta: ");
        float distTotal = 0;

        while (!pila.isEmpty()) {
            Edge arco = (Edge) pila.pop();
            float distancia = calcularDistancia(arco);
            distTotal += distancia;

            fragmento.append("\nOrigen: ").append(obtenerNombre(arco.getSource()))
                     .append(" Destino: ").append(obtenerNombre(arco.getDestination()))
                     .append(" Distancia: ").append(distancia);
        }
        
        fragmento.append("\nDistancia total: ").append(distTotal);
        return fragmento.toString();
    }

    private float calcularDistancia(Edge arco) {
        double lonOrigen = obtenerLongitud(arco.getSource());
        double latOrigen = obtenerLatitud(arco.getSource());
        double lonDestino = obtenerLongitud(arco.getDestination());
        double latDestino = obtenerLatitud(arco.getDestination());
        return Distancia.calcularDistancia(lonDestino, latDestino, lonOrigen, latOrigen);
    }

    private double obtenerLongitud(Vertex vertice) {
        if (vertice.getInfo() instanceof Landing) {
            return ((Landing) vertice.getInfo()).getLongitude();
        }
        return ((Country) vertice.getInfo()).getLongitude();
    }

    private double obtenerLatitud(Vertex vertice) {
        if (vertice.getInfo() instanceof Landing) {
            return ((Landing) vertice.getInfo()).getLatitude();
        }
        return ((Country) vertice.getInfo()).getLatitude();
    }

    private String obtenerNombre(Vertex vertice) {
        if (vertice.getInfo() instanceof Landing) {
            return ((Landing) vertice.getInfo()).getLandingId();
        }
        return ((Country) vertice.getInfo()).getCapitalName();
    }

    // Simplificación en el cálculo de la red de expansión mínima
    public String obtenerRedExpansiva() {
        StringBuilder fragmento = new StringBuilder();
        ILista lista1 = landingIdTabla.valueSet();
        String llave = obtenerLlaveMayorConexiones(lista1);

        ILista lista2 = grafo.mstPrimLazy(llave);
        ITablaSimbolos tabla = new TablaHashSeparteChaining<>(2);
        ILista candidatos = new ArregloDinamico<>(1);
        int costoTotal = calcularCostoExpansivo(lista2, tabla, candidatos);

        ILista unificado = unificar(candidatos, "Vertice");
        fragmento.append("La cantidad de nodos conectados a la red de expansión mínima es: ").append(unificado.size())
                 .append("\nEl costo total es de: ").append(costoTotal);

        return fragmento.toString();
    }

    private String obtenerLlaveMayorConexiones(ILista lista1) {
        int max = 0;
        String llave = "";
        for (int i = 1; i <= lista1.size(); i++) {
            ILista landingList = (ILista) lista1.getElement(i);
            if (landingList.size() > max) {
                max = landingList.size();
                llave = (String) ((Vertex) landingList.getElement(1)).getId();
            }
        }
        return llave;
    }

    private int calcularCostoExpansivo(ILista lista2, ITablaSimbolos tabla, ILista candidatos) {
        int costoTotal = 0;
        for (int i = 1; i <= lista2.size(); i++) {
            Edge arco = (Edge) lista2.getElement(i);
            float distancia = calcularDistancia(arco);
            costoTotal += distancia;
            if (!tabla.containsKey(arco.getDestination().getId())) {
                tabla.put(arco.getDestination().getId(), arco.getDestination());
                candidatos.add(arco.getDestination());
            }
        }
        return costoTotal;
    }
    public ILista obtenerAfectados(String punto)
	{
		String codigo= (String) nombrecodigo.get(punto);
		ILista lista= (ILista) landingidtabla.get(codigo);
		
		ILista countries= new ArregloDinamico<>(1);
		try 
		{
			Country paisoriginal=(Country) paises.get(((Landing) ((Vertex)lista.getElement(1)).getInfo()).getPais());
			countries.insertElement(paisoriginal, countries.size() + 1);
		} 
		catch (PosException | VacioException | NullException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(int i=1; i<= lista.size(); i++)
		{
			try 
			{
				Vertex vertice= (Vertex) lista.getElement(i);
				ILista arcos= vertice.edges();
				
				for(int j=1; j<= arcos.size(); j++)
				{
					Vertex vertice2= ((Edge) arcos.getElement(j)).getDestination();
					
					Country pais=null;
					if (vertice2.getInfo().getClass().getName().equals("model.data_structures.Landing"))
					{
						Landing landing= (Landing) vertice2.getInfo();
						pais= (Country) paises.get(landing.getPais());
						countries.insertElement(pais, countries.size() + 1);
						
						float distancia= distancia(pais.getLongitude(), pais.getLatitude(), landing.getLongitude(), landing.getLatitude());
							
						pais.setDistlan(distancia);
					}
					else
					{
						pais=(Country) vertice2.getInfo();
					}
				}
				
			} catch (PosException | VacioException | NullException e) 
			{
				e.printStackTrace();
			}
		}
		
		ILista unificado= unificar(countries, "Country");
		
		Comparator<Country> comparador=null;

		Ordenamiento<Country> algsOrdenamientoEventos=new Ordenamiento<Country>();

		comparador= new ComparadorXKm();

		try 
		{

			if (lista!=null)
			{
				algsOrdenamientoEventos.ordenarMergeSort(unificado, comparador, true);
			}	
		}
		catch (PosException | VacioException| NullException  e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return unificado;
		
		
	}
    public String obtenerPaisesAfectadosPorLanding(String punto) {
        ILista afectados = obtenerAfectados(punto);
        
        StringBuilder fragmento = new StringBuilder();
        fragmento.append("La cantidad de países afectados es: ").append(afectados.size()).append("\nLos países afectados son: ");
        
        for (int i = 1; i <= afectados.size(); i++) {
            try {
                Country pais = (Country) afectados.getElement(i);
                fragmento.append("\nNombre: ").append(pais.getCountryName())
                        .append("\nDistancia al landing point: ").append(pais.getDistlan());
            } catch (PosException | VacioException e) {
                e.printStackTrace();
            }
        }
        
        return fragmento.toString();
    }

    private ILista unificar(ILista lista, String tipo) {
        ILista unificado = new ArregloDinamico<>();
        for (int i = 1; i <= lista.size(); i++) {
            Object objeto = lista.getElement(i);
            if (tipo.equals("Vertice")) {
                unificado.add(((Vertex) objeto).getInfo());
            }
        }
        return unificado;
    }
}
