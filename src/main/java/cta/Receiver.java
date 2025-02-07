package cta;

import java.net.DatagramPacket;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import cta.designe.listener.Algoritmos;
import cta.remote.stompbroker.ModelEventTrace;

public class Receiver implements Runnable {

	private DatagramSocket mySocket = null;
	private Visualizador vis;
	private ConsultaTarea cTarea;
	public ConsultaTarea getcTarea() {
		return cTarea;
	}

	public void setcTarea(ConsultaTarea cTarea) {
		this.cTarea = cTarea;
	}

	private SimpMessagingTemplate smt;
	
	Algoritmos algoritmos;
	Logger log = Logger.getLogger("Receiver");

	public void run() {
		// Registrar este receiver para el sistema dado (max. 3 hilos)
		Vector<Receiver> vThreads = vis.getThreadReceiverRegistry().get(cTarea.getNameSocketSistema());
		vThreads.add(this);
		vis.getThreadReceiverRegistry().put(cTarea.getNameSocketSistema(), vThreads);
		vis.refreshLedsSocketsStatus();

		String cadenaMensaje;
		algoritmos = new Algoritmos();

		try {
			log.info("Tama�o ajustado de buffer DatagramSocket :" + mySocket.getReceiveBufferSize());
			log.info("Desde Hilo " + cTarea.getNameSocketSistema() + " trabajando " + cTarea.getNombreConsultaFull());
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		int sizeBufferDatagramPacket = 4096; // 8 Kbytes
		//int sizeReadBytes = 2048;
		String[] sArrayFilter = null;

		
		do {
			byte[] RecogerServidor_bytes = new byte[sizeBufferDatagramPacket];

			try {
				// EsperamoHilo Finalizado"s a recibir un paquete/

				DatagramPacket servPaquete = new DatagramPacket(RecogerServidor_bytes, RecogerServidor_bytes.length);
				mySocket.receive(servPaquete);

				String sPacket = new String(servPaquete.getData()).trim();

				
				String nameConsulta = cTarea.getNombreConsultaFull();
				
				
				sArrayFilter = vis.getCatalogFiltersRegistry(nameConsulta);

				// Splitamos el mensaje recibido en lineas
				// Por cada LinesistemaSocket
				StringTokenizer st = new StringTokenizer(sPacket, System.getProperty("line.separator") + "|\r");
				while (st.hasMoreTokens()) {
					cadenaMensaje = st.nextToken();

					// filtrar por catalogo de filtros texto (normalmente por cada linea)
					// Tambien se puede aqui hacer un dispatch de consultasComando para mostrarlo en el TaxtArea del tab ChecksComandos
					if (algoritmos.filterMatch(cadenaMensaje, sArrayFilter, vis.getFilterExclusive().isSelected())) {
						handlerWriteLine(cadenaMensaje);
					}
				}

			} catch (Exception e) {
				Vector<String> paraVerMasks = new Vector<String>();
				for (String mask : vis.getCatalogFiltersRegistry(cTarea.getNombreConsultaFull())) {
					paraVerMasks.add(mask);
				}
				System.err.println("Hilo " + cTarea.nombreConsultaTareaFull() + " " + this + " trabajando Mascaras "
						+ cTarea.getNombreConsultaFull() + paraVerMasks + " " + e.getMessage());
			}
		} while ((vis.getFlagDisconnectRegistry()).get(cTarea.getNameSocketSistema()) == 0);
		// hacemos limpieza en el registro
		vThreads = vis.getThreadReceiverRegistry().get(cTarea.getNameSocketSistema());
		vThreads.remove(this);
		vis.getThreadReceiverRegistry().put(cTarea.getNameSocketSistema(), vThreads);
		
		handlerWriteLine("Hilo Finalizado");
		log.info("Hilo de sistema " + this.cTarea.getNameSocketSistema() + ":" + this + " finalizado.");
		vis.refreshLedsSocketsStatus();
		mySocket = null;

	}

	public void handlerWriteLine(String cadena) {
		
		//A�adimos identificador de sistema origen a la cadena
		cadena = cTarea.getNameSocketSistema().concat(" "+cadena);

		// Configuracion 1		
		// Solo Imprimimos el paquete recibido a caja visualizador(opcionalmente)
		if(vis.getCheckMostrarLineasTextArea().isSelected()) {
			vis.getTextArea().append(cadena.concat(System.getProperty("line.separator")));
		}
		// Poblamos stack Lineas de referencia (opcionalmente)
		if(vis.getChckbxBufferearConsulta().isSelected()) {
			synchronized(vis.getCadenasFiltradas()){
				try {
					vis.getCadenasFiltradas().add(cadena);
				}catch(java.lang.IllegalStateException ise) {
					vis.getCadenasFiltradas().poll();
					vis.getCadenasFiltradas().add(cadena);
				}
			}
			vis.getLinkedListCounter().setText(vis.getCadenasFiltradas().size()+"/"+ vis.Max_Size_Queue +" lineas");
		}
		
		// Configuracion 2
		// Escribimos linea a fichero
		

		// Configuracion 3
		// Comprobar Listeners
		if (vis.getCheckListener1().isSelected()) {  
		
			if (algoritmos.filterMatch(cadena,vis.getCatalogListener() , true)&& !cadena.contains("DBGM")) {
				vis.getTextAreaHandlers().append(cadena.concat(System.getProperty("line.separator")));
			}
		}
		// Configuracion 4
				// Dispatching evento
		if(vis.chckbxPublishToWebsocket.isSelected()) {
			if (algoritmos.filterMatch(cadena,vis.getCatalogListener(), true)) {
				smt.convertAndSend("/channel/traces", new ModelEventTrace("eventTrace",cadena));
				}
		}
	}
	
	public Receiver(DatagramSocket socket, Visualizador visualizador, ConsultaTarea cTarea, SimpMessagingTemplate smt) {
		this.mySocket = socket;
		this.vis = visualizador;
		this.cTarea = cTarea;
		this.smt = smt;
	}
}
