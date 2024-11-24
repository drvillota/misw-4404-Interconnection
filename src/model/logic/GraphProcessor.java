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
    private final ITablaSimbolos landingidtabla;
    private final ITablaSimbolos nombrecodigo;

    public GraphProcessor(GrafoListaAdyacencia grafo, ITablaSimbolos paises, ITablaSimbolos points,
                          ITablaSimbolos landingidtabla, ITablaSimbolos nombrecodigo) {
        this.grafo = grafo;
        this.paises = paises;
        this.points = points;
        this.landingidtabla = landingidtabla;
        this.nombrecodigo = nombrecodigo;
    }
    
    public String req1String(String punto1, String punto2)
	{
		ITablaSimbolos tabla= grafo.getSSC();
		ILista lista= tabla.valueSet();
		int max=0;
		for(int i=1; i<= lista.size(); i++)
		{
			try
			{
				if((int) lista.getElement(i)> max)
				{
					max= (int) lista.getElement(i);
				}
			}
			catch(PosException | VacioException  e)
			{
				System.out.println(e.toString());
			}
			
		}
		
		String fragmento="La cantidad de componentes conectados es: " + max;
		
		try 
		{
			String codigo1= (String) nombrecodigo.get(punto1);
			String codigo2= (String) nombrecodigo.get(punto2);
			Vertex vertice1= (Vertex) ((ILista) landingidtabla.get(codigo1)).getElement(1);
			Vertex vertice2= (Vertex) ((ILista) landingidtabla.get(codigo2)).getElement(1);
			
			int elemento1= (int) tabla.get(vertice1.getId());
			int elemento2= (int) tabla.get(vertice2.getId());
			
			if(elemento1== elemento2)
			{
				fragmento+= "\n Los landing points pertenecen al mismo clúster";
			}
			else
			{
				fragmento+= "\n Los landing points no pertenecen al mismo clúster";
			}
		} 
		catch (PosException | VacioException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return fragmento;
		
	}
	
	public String req2String()
	{
		String fragmento="";
		
		ILista lista= landingidtabla.valueSet();
		
		int cantidad=0;
		
		int contador=0;
		
		for(int i=1; i<= lista.size(); i++)
		{
			try 
			{
				if( ( (ILista) lista.getElement(i) ).size()>1 && contador<=10)
				{
					Landing landing= (Landing) ((Vertex) ((ILista) lista.getElement(i) ).getElement(1)).getInfo();
					
					for(int j=1; j<=((ILista) lista.getElement(i)).size(); j++)
					{
						cantidad+= ((Vertex) ((ILista) lista.getElement(i)).getElement(j)).edges().size();
					}
					
					fragmento+= "\n Landing " + "\n Nombre: " + landing.getName() + "\n País: " + landing.getPais() + "\n Id: " + landing.getId() + "\n Cantidad: " + cantidad;
					
					contador++;
				}
			}
			catch (PosException | VacioException e) 
			{
				e.printStackTrace();
			}
			
		}
		
		return fragmento;
		
	}
	
	public String req3String(String pais1, String pais2)
	{
		Country pais11= (Country) paises.get(pais1);
		Country pais22= (Country) paises.get(pais2);
		String capital1=pais11.getCapitalName();
		String capital2=pais22.getCapitalName();

		PilaEncadenada pila= grafo.minPath(capital1, capital2);

		float distancia=0;

		String fragmento="Ruta: ";

		float disttotal=0;
		
		double longorigen=0;
		double longdestino=0;
		double latorigen=0;
		double latdestino=0;
		String origennombre="";
		String destinonombre="";

		while(!pila.isEmpty())
		{
			Edge arco= ((Edge)pila.pop());

			if(arco.getSource().getInfo().getClass().getName().equals("model.data_structures.Landing"))
			{
				longorigen=((Landing)arco.getSource().getInfo()).getLongitude();
				latorigen=((Landing)arco.getSource().getInfo()).getLongitude();
				origennombre=((Landing)arco.getSource().getInfo()).getLandingId();
			}
			if(arco.getSource().getInfo().getClass().getName().equals("model.data_structures.Country"))
			{
				longorigen=((Country)arco.getSource().getInfo()).getLongitude();
				latorigen=((Country)arco.getSource().getInfo()).getLongitude();
				origennombre=((Country)arco.getSource().getInfo()).getCapitalName();
			}
			if (arco.getDestination().getInfo().getClass().getName().equals("model.data_structures.Landing"))
			{
				latdestino=((Landing)arco.getDestination().getInfo()).getLatitude();
				longdestino=((Landing)arco.getDestination().getInfo()).getLatitude();
				destinonombre=((Landing)arco.getDestination().getInfo()).getLandingId();
			}
			if(arco.getDestination().getInfo().getClass().getName().equals("model.data_structures.Country"))
			{
				longdestino=((Country)arco.getDestination().getInfo()).getLatitude();
				latdestino=((Country)arco.getDestination().getInfo()).getLatitude();
				destinonombre=((Country)arco.getDestination().getInfo()).getCapitalName();
			}

			distancia = Distancia.calcularDistancia(longdestino,latdestino, longorigen, latorigen);
			fragmento+= "\n \n Origen: " +origennombre + "  Destino: " + destinonombre + "  Distancia: " + distancia;
			disttotal+=distancia;

		}

		fragmento+= "\n Distancia total: " + disttotal;	

		return fragmento;
		
	}
	
	public String req4String()
	{
		String fragmento="";
		ILista lista1= landingidtabla.valueSet();
		
		String llave="";
		
		int distancia=0;
		
		try
		{
			int max=0;
			for(int i=1; i<= lista1.size(); i++)
			{
				if(((ILista)lista1.getElement(i)).size()> max)
				{
					max= ((ILista)lista1.getElement(i)).size();
					llave= (String) ((Vertex)((ILista)lista1.getElement(i)).getElement(1)).getId();
				}
			}
			
			ILista lista2= grafo.mstPrimLazy(llave);
			
			ITablaSimbolos tabla= new TablaHashSeparteChaining<>(2);
			ILista candidatos= new ArregloDinamico<>(1);
			for(int i=1; i<= lista2.size(); i++)
			{
				Edge arco= ((Edge) lista2.getElement(i));
				distancia+= arco.getWeight();
				
				candidatos.insertElement(arco.getSource(), candidatos.size()+1);
				
				candidatos.insertElement(arco.getDestination(), candidatos.size()+1);
				
				tabla.put(arco.getDestination().getId(),arco.getSource() );
			}
			
			ILista unificado= unificar(candidatos, "Vertice");
			fragmento+= " La cantidad de nodos conectada a la red de expansión mínima es: " + unificado.size() + "\n El costo total es de: " + distancia;
			
			int maximo=0;
			int contador=0;
			PilaEncadenada caminomax=new PilaEncadenada();
			for(int i=1; i<= unificado.size(); i++)
			{

				PilaEncadenada path= new PilaEncadenada();
				String idBusqueda= (String) ((Vertex) unificado.getElement(i)).getId();
				Vertex actual;

				while( (actual= (Vertex) tabla.get(idBusqueda))!=null && actual.getInfo()!=null)
				{
					path.push(actual);
					idBusqueda= (String) ((Vertex)actual).getId();
					contador++;
				}
				
				if(contador>maximo)
				{
					caminomax=path;
				}
			}
			
			fragmento+="\n La rama más larga está dada por lo vértices: ";
			for(int i=1; i<=caminomax.size(); i++)
			{
				Vertex pop= (Vertex) caminomax.pop();
				fragmento+= "\n Id " + i + " : "+ pop.getId();
			}
		}
		catch (PosException | VacioException | NullException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(fragmento.equals(""))
		{	
			return "No hay ninguna rama";
		}
		else 
		{
			return fragmento;
		}
	}
	
	public ILista req5(String punto)
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
						
						float distancia= Distancia.calcularDistancia(pais.getLongitude(), pais.getLatitude(), landing.getLongitude(), landing.getLatitude());
							
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
	
	public String req5String(String punto)
	{
		ILista afectados= req5(punto);
		
		String fragmento="La cantidad de paises afectados es: " + afectados.size() + "\n Los paises afectados son: ";
	
		for(int i=1; i<=afectados.size(); i++)
		{
			try {
				fragmento+= "\n Nombre: " + ((Country) afectados.getElement(i)).getCountryName() + "\n Distancia al landing point: " + ((Country) afectados.getElement(i)).getDistlan();
			} catch (PosException | VacioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return fragmento;
	}

    public ILista unificar(ILista lista, String criterio)
	{

		ILista lista2=new ArregloDinamico(1);

		if(criterio.equals("Vertice"))
		{
			Comparator<Vertex<String, Landing>> comparador=null;

			Ordenamiento<Vertex<String, Landing>> algsOrdenamientoEventos=new Ordenamiento<Vertex<String, Landing>>();;

			comparador= new Vertex.ComparadorXKey();


			try 
			{

				if (lista!=null)
				{
					algsOrdenamientoEventos.ordenarMergeSort(lista, comparador, false);

					for(int i=1; i<=lista.size(); i++)
					{
						Vertex actual= (Vertex) lista.getElement(i);
						Vertex siguiente= (Vertex) lista.getElement(i+1);

						if(siguiente!=null)
						{
							if(comparador.compare(actual, siguiente)!=0)
							{
								lista2.insertElement(actual, lista2.size()+1);
							}
						}
						else
						{
							Vertex anterior= (Vertex) lista.getElement(i-1);

							if(anterior!=null)
							{
								if(comparador.compare(anterior, actual)!=0)
								{
									lista2.insertElement(actual, lista2.size()+1);
								}
							}
							else
							{
								lista2.insertElement(actual, lista2.size()+1);
							}
						}

					}
				}
			} 
			catch (PosException | VacioException| NullException  e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			Comparator<Country> comparador=null;

			Ordenamiento<Country> algsOrdenamientoEventos=new Ordenamiento<Country>();;

			comparador= new Country.ComparadorXNombre();

			try 
			{

				if (lista!=null)
				{
					algsOrdenamientoEventos.ordenarMergeSort(lista, comparador, false);
				}

					for(int i=1; i<=lista.size(); i++)
					{
						Country actual= (Country) lista.getElement(i);
						Country siguiente= (Country) lista.getElement(i+1);

						if(siguiente!=null)
						{
							if(comparador.compare(actual, siguiente)!=0)
							{
								lista2.insertElement(actual, lista2.size()+1);
							}
						}
						else
						{
							Country anterior= (Country) lista.getElement(i-1);

							if(anterior!=null)
							{
								if(comparador.compare(anterior, actual)!=0)
								{
									lista2.insertElement(actual, lista2.size()+1);
								}
							}
							else
							{
								lista2.insertElement(actual, lista2.size()+1);
							}
						}

					}
				}
			
			catch (PosException | VacioException| NullException  e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return lista2;
	}
	
	public ITablaSimbolos unificarHash(ILista lista)
	{

		Comparator<Vertex<String, Landing>> comparador=null;

		Ordenamiento<Vertex<String, Landing>> algsOrdenamientoEventos=new Ordenamiento<Vertex<String, Landing>>();;

		comparador= new Vertex.ComparadorXKey();
		
		ITablaSimbolos tabla= new TablaHashSeparteChaining<>(2);


		try 
		{
			if (lista!=null)
			{
				algsOrdenamientoEventos.ordenarMergeSort(lista, comparador, false);

				for(int i=1; i<=lista.size(); i++)
				{
					Vertex actual= (Vertex) lista.getElement(i);
					Vertex siguiente= (Vertex) lista.getElement(i+1);

					if(siguiente!=null)
					{
						if(comparador.compare(actual, siguiente)!=0)
						{
							tabla.put(actual.getId(), actual);
						}
					}
					else
					{
						Vertex anterior= (Vertex) lista.getElement(i-1);

						if(anterior!=null)
						{
							if(comparador.compare(anterior, actual)!=0)
							{
								tabla.put(actual.getId(), actual);
							}
						}
						else
						{
							tabla.put(actual.getId(), actual);
						}
					}

				}
			}
		} 
		catch (PosException | VacioException| NullException  e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tabla;
	}

	
}