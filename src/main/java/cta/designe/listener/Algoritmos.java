package cta.designe.listener;

import java.util.List;
import java.util.Vector;

public class Algoritmos {
	public boolean filterMatch(String cadenaMensaje, String[] sArrayFilter, boolean filterExclusiveSelected) {
		boolean match = false;
		// filtrar por catalogo de filtros texto
		if(sArrayFilter.length==0)return true;
		
		for (String sFilter : sArrayFilter) {
			// cONTROL FILTRO COPULATIVO
			if (sFilter.contains("&")) {
				String mask_AND[] = sFilter.split("&");
				int match_AND = 0;
				for (String s : mask_AND) {
					if (cadenaMensaje.contains(s))
						match_AND++;
				}
				if (match_AND == mask_AND.length)
					return true;
			} else {
				
				if (cadenaMensaje.contains(sFilter))return true;
			}
		}
		return match;
	}
	
	public Vector<Splited> splitedMatch(String cadenaAnalizada, int inicialIndexLinea , String sSelected){
		Vector<Splited> vSpliteds =  new Vector<Splited>();
		
		if (sSelected.contains("&")) {
			String criteriosBusqueda[] = sSelected.split("&");
			int testCopulativo = 0;
			for (String criterio : criteriosBusqueda) {
				int pos = cadenaAnalizada.indexOf(criterio, 0);
				if( pos != -1) {
					Splited splited  = new Splited (criterio, (pos+inicialIndexLinea));
					vSpliteds.add(splited);
					testCopulativo ++;
				}
				
			}
			if( testCopulativo != criteriosBusqueda.length)return null;
		}else {
			
			int contadorMatchs = 0;
			int pos = cadenaAnalizada.indexOf(sSelected, 0);
			while(pos != -1) {
				contadorMatchs++;
				Splited splited  = new Splited (sSelected, (pos+inicialIndexLinea));
				vSpliteds.add(splited);
				pos = cadenaAnalizada.indexOf(sSelected, pos+sSelected.length());
			}
			if (contadorMatchs == 0) return null;
		}

		return vSpliteds;
	}
}

