package model.logic;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import model.data_structures.ArregloDinamico;
import model.data_structures.Country;
import model.data_structures.Country.ComparadorXKm;
import model.data_structures.Edge;
import model.data_structures.GrafoListaAdyacencia;
import model.data_structures.ILista;
import model.data_structures.ITablaSimbolos;
import model.data_structures.Landing;
import model.data_structures.ListaEncadenada;
import model.data_structures.NodoTS;
import model.data_structures.NullException;
import model.data_structures.PilaEncadenada;
import model.data_structures.PosException;
import model.data_structures.TablaHashLinearProbing;
import model.data_structures.TablaHashSeparteChaining;
import model.data_structures.VacioException;
import model.data_structures.Vertex;
import model.data_structures.YoutubeVideo;
import utils.Ordenamiento;
import utils.Distancia;

/**
 * Definicion del modelo del mundo
 *
 */
public class Modelo {
	/**
	 * Atributos del modelo del mundo
	 */
	private ILista datos;
	
	private GrafoListaAdyacencia grafo;
	
	private ITablaSimbolos paises;
	
	private ITablaSimbolos points;
	
	private ITablaSimbolos landingidtabla;
	
	private ITablaSimbolos nombrecodigo;

	private GraphProcessor graphProcessor;

	/**
	 * Constructor del modelo del mundo con capacidad dada
	 * @param tamano
	 */
	public Modelo(int capacidad)
	{
		datos = new ArregloDinamico<>(capacidad);
	}

	public void cargar() throws IOException
	{
		grafo= new GrafoListaAdyacencia(2);
		paises= new TablaHashLinearProbing(2);
		points= new  TablaHashLinearProbing(2);
		landingidtabla= new TablaHashSeparteChaining(2);
		nombrecodigo=new TablaHashSeparteChaining(2);
		
		Reader in = new FileReader("./data/countries.csv");
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
		int contador=1;
		for (CSVRecord record : records) 
		{
			if(!record.get(0).equals(""))
			{
				String countryName= record.get(0);
				
				String capitalName= record.get(1);
				
				double latitude= Double.parseDouble(record.get(2));
				
				double longitude= Double.parseDouble(record.get(3));
				
				String code= record.get(4);
				
				String continentName= record.get(5);
				
				float population= Float.parseFloat(record.get(6).replace(".", ""));
				
				double users= Double.parseDouble(record.get(7).replace(".", ""));;
				
				Country pais= new Country(countryName, capitalName, latitude, longitude, code, continentName, population, users);
				
				grafo.insertVertex(capitalName, pais);
				paises.put(countryName, pais);

				contador++;
			}

		}
		
		
		Reader in2 = new FileReader("./data/landing_points.csv");
		Iterable<CSVRecord> records2 = CSVFormat.RFC4180.withHeader().parse(in2);

		int contador2=1;
		
		for (CSVRecord record2 : records2) 
		{

			String landingId= record2.get(0);
			
			String id=record2.get(1);
			
			String[] x= record2.get(2).split(", ");
			
			String name= x[0];
			
			String paisnombre= x[x.length-1];
			
			double latitude= Double.parseDouble(record2.get(3));
			
			double longitude= Double.parseDouble(record2.get(4));
			
			Landing landing= new Landing(landingId, id, name, paisnombre, latitude, longitude);
			
			points.put(landingId, landing);
			
			Country pais= null;
		}
		
		
		Reader in3 = new FileReader("./data/connections.csv");
		Iterable<CSVRecord> records3 = CSVFormat.RFC4180.withHeader().parse(in3);

		int contador3=1;
		for (CSVRecord record3 : records3) 
		{
			String origin= record3.get(0);

			String destination= record3.get(1);

			String cableid= record3.get(3);

			String[] lengths= record3.get(4).split(" ");

			String length= lengths[0];


			Landing landing1= (Landing) points.get(origin);
			
			grafo.insertVertex(landing1.getLandingId()+ cableid, landing1);
			
			Vertex vertice1= grafo.getVertex(landing1.getLandingId()+ cableid);

			
			Landing landing2= (Landing) points.get(destination);

			grafo.insertVertex(landing2.getLandingId()+ cableid, landing2);
			
			Vertex vertice2= grafo.getVertex(landing2.getLandingId()+ cableid);
			
			
			String nombrepais1= landing1.getPais();
			
			String nombrepais2= landing2.getPais();
					
			Country pais1=null;
			Country pais2=null;
			if(nombrepais1.equals("Côte d'Ivoire"))
			{
				pais1= (Country) paises.get("Cote d'Ivoire");
			}
			else if(nombrepais2.equals("Côte d'Ivoire"))
			{
				pais2= (Country) paises.get("Cote d'Ivoire");
			}
			else
			{
				pais1= (Country) paises.get(nombrepais1);
				pais2= (Country) paises.get(nombrepais2);
			}
			

			if(pais1!=null)
			{
				float weight=Distancia.calcularDistancia(pais1.getLongitude(), pais1.getLatitude(), landing1.getLongitude(), landing1.getLatitude());

				grafo.addEdge(pais1.getCapitalName(),landing1.getLandingId()+ cableid , weight);
			}
	
			if(pais2!=null)
			{
				float weight2=Distancia.calcularDistancia(pais2.getLongitude(), pais2.getLatitude(), landing1.getLongitude(), landing1.getLatitude());

				grafo.addEdge(pais2.getCapitalName(),landing2.getLandingId()+ cableid , weight2);

			}
			

			if(landing1!=null)
			{
				if(landing2!=null)
				{
					Edge existe1= grafo.getEdge(landing1.getLandingId() + cableid, landing2.getLandingId() + cableid);
	
					if(existe1==null)
					{
						float weight3=Distancia.calcularDistancia(landing1.getLongitude(), landing1.getLatitude(), landing2.getLongitude(), landing2.getLatitude());
						grafo.addEdge(landing1.getLandingId() + cableid, landing2.getLandingId() + cableid, weight3);
					}
					else
					{
						float weight3=Distancia.calcularDistancia(landing1.getLongitude(), landing1.getLatitude(), landing2.getLongitude(), landing2.getLatitude());
						float peso3= existe1.getWeight();
	
						if(weight3> peso3)
						{
							existe1.setWeight(weight3);
						}
					}
				}
			}
			
			try
			{
				
				ILista elementopc= (ILista) landingidtabla.get(landing1.getLandingId());
				if (elementopc==null)
				{
					ILista valores=new ArregloDinamico(1);
					valores.insertElement(vertice1, valores.size() +1);

					landingidtabla.put(landing1.getLandingId(), valores);
					
				}
				else if (elementopc!=null)
				{
					elementopc.insertElement(vertice1, elementopc.size()+1);
				}
			
				elementopc= (ILista) landingidtabla.get(landing2.getLandingId());
	
				if (elementopc==null)
				{
					ILista valores=new ArregloDinamico(1);
					valores.insertElement(vertice2, valores.size() +1);
	
					landingidtabla.put(landing2.getLandingId(), valores);
	
				}
				else if (elementopc!=null)
				{
					elementopc.insertElement(vertice2, elementopc.size()+1);
	
				}
				
				elementopc= (ILista) nombrecodigo.get(landing1.getLandingId());
				
				if (elementopc==null)
				{
					String nombre=landing1.getName();
					String codigo=landing1.getLandingId();

					nombrecodigo.put(nombre, codigo);
	
				}
			}
			catch(PosException | NullException e)
			{
				e.printStackTrace();
			}
			
			

		}
		
		try
		{
			ILista valores = landingidtabla.valueSet();
			
			for(int i=1; i<=valores.size(); i++)
			{
				for(int j=1; j<=((ILista) valores.getElement(i)).size(); j++)
				{
					Vertex vertice1;
					if((ILista) valores.getElement(i) != null)
					{
						vertice1= (Vertex) ((ILista) valores.getElement(i)).getElement(j);
						for(int k=2; k<= ((ILista) valores.getElement(i)).size(); k++)
						{
							Vertex vertice2= (Vertex) ((ILista) valores.getElement(i)).getElement(k);
							grafo.addEdge(vertice1.getId(), vertice2.getId(), 100);
						}
					}
				}
			}
		}
		catch(PosException | VacioException  e)
		{
			e.printStackTrace();
		}

		// Inicialización del GraphProcessor con las estructuras cargadas
        graphProcessor = new GraphProcessor(grafo, paises, points, landingidtabla, nombrecodigo);
	}

	/**
	 * Servicio de consulta de numero de elementos presentes en el modelo 
	 * @return numero de elementos presentes en el modelo
	 */
	public int darTamano()
	{
		return datos.size();
	}


	/**
	 * Requerimiento buscar dato
	 * @param dato Dato a buscar
	 * @return dato encontrado
	 * @throws VacioException 
	 * @throws PosException 
	 */
	public YoutubeVideo getElement(int i) throws PosException, VacioException
	{
		return (YoutubeVideo) datos.getElement( i);
	}

	public String toString()
	{
		String fragmento="Info básica:";
		
		fragmento+= "\n El número total de conexiones (arcos) en el grafo es: " + grafo.edges().size();
		fragmento+="\n El número total de puntos de conexión (landing points) en el grafo: " + grafo.vertices().size();
		fragmento+= "\n La cantidad total de países es:  " + paises.size();
		Landing landing=null;
		try 
		{
			landing = (Landing) ((NodoTS) points.darListaNodos().getElement(1)).getValue();
			fragmento+= "\n Info primer landing point " + "\n Identificador: " + landing.getId() + "\n Nombre: " + landing.getName()
			+ " \n Latitud " + landing.getLatitude() + " \n Longitud" + landing.getLongitude();
			
			Country pais= (Country) ((NodoTS) paises.darListaNodos().getElement(paises.darListaNodos().size())).getValue();
			
			fragmento+= "\n Info último país: " + "\n Capital: "+ pais.getCapitalName() + "\n Población: " + pais.getPopulation()+
			"\n Usuarios: "+ pais.getUsers();
		} 
		catch (PosException | VacioException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fragmento;
	}
	
	public String obtenerInfoCluseteresConectados(String punto1, String punto2) {
        return graphProcessor.obtenerInfoCluseteresConectados(punto1, punto2);
    }

    public String obtenerLandingConectados() {
        return graphProcessor.obtenerLandingConectados();
    }

    public String obtenerRutaEntrePaises(String pais1, String pais2) {
        return graphProcessor.obtenerRutaEntrePaises(pais1, pais2);
    }

    public String obtenerRedExpansiva() {
        return graphProcessor.obtenerRedExpansiva();
    }

    public String req5String(String punto) {
        return graphProcessor.req5String(punto);
    }
}
