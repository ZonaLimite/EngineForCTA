package cta;

import java.net.DatagramPacket;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import cta.designe.listener.Algoritmos;
import cta.designe.listener.STATE_COMMAND;
import cta.designe.listener.StatedCommand;
import cta.remote.stompbroker.ModelEventTrace;

public class Receiver implements Runnable, IReceiver {
	
	private ConcurrentHashMap<String, StatedCommand> catalogCommandregistry;
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
		String nameSocketSistema= cTarea.getNameSocketSistema();
		log.info("Thread de "+ nameSocketSistema  +"running ...");
		Vector<Receiver> vThreads = vis.getThreadReceiverRegistry().get(nameSocketSistema);
		vThreads.add(this);
		catalogCommandregistry = vis.getCatalogCommandsRegistry();
		vis.getThreadReceiverRegistry().put(nameSocketSistema, vThreads);
		vis.refreshLedsSocketsStatus();

		String cadenaMensaje;
		algoritmos = new Algoritmos();
		
		int sizeBuffer=2048;;
		try {
			sizeBuffer =  mySocket.getReceiveBufferSize() / 2; //Ajustado a implementacion del sistema propietario
			log.info("Tama�o ajustado de buffer DatagramSocket :" + sizeBuffer);
			log.info("Desde Hilo " + nameSocketSistema + " trabajando " + cTarea.getNombreConsultaFull());
		} catch (SocketException e) {
			e.printStackTrace();
		}
	
		String[] sArrayFilter = null;

		
		do {
			byte[] RecogerServidor_bytes = new byte[sizeBuffer];

			try {

				DatagramPacket servPaquete = new DatagramPacket(RecogerServidor_bytes, RecogerServidor_bytes.length);
				mySocket.receive(servPaquete);

				String sPacket = new String(servPaquete.getData()).trim();
				
				String nameConsulta = cTarea.getNombreConsultaFull();
				
				// Splitamos el mensaje recibido en lineas
				// Por cada LinesistemaSocket
				StringTokenizer st = new StringTokenizer(sPacket, System.getProperty("line.separator") + "|\r");
				while (st.hasMoreTokens()) {
					cadenaMensaje = st.nextToken();
					
					//A�adimos identificador de sistema origen a la cadena
					cadenaMensaje = cTarea.getNameSocketSistema().concat(" "+cadenaMensaje);

					//Posible hilo
					checkCommands(cadenaMensaje);   
					
					// Filtrar por catalogo de filtros texto (normalmente por cada linea)
					sArrayFilter = vis.getCatalogFiltersRegistry(nameConsulta);
					if (algoritmos.filterMatch(cadenaMensaje, sArrayFilter, vis.getFilterExclusive().isSelected())) {
						//Posible hilo
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
		// Dispatching evento A WEBSOCKET
		if(vis.chckbxPublishToWebsocket.isSelected()) {
			if (algoritmos.filterMatch(cadena,vis.getCatalogListener(), true)) {
				smt.convertAndSend("/channel/traces", new ModelEventTrace("eventTrace",cadena));
				}
		}
	}
	
	
	/*
	 * Algoritmo de proceso commands
	 * Parameter : String con la cadena a checkera como cadena de comando
	 * Return: Boolean true if la cadena es consumida
	 */
	public boolean checkCommands(String cadenaMensaje) {
		// Filtrar por catalogo de Comandos
		boolean cadenaMensajeConsumed = false;
		if(vis.getCatalogCommandsRegistry().size() >= 1) { //Hay comandos registrados
			
			Iterator<String> iteratorKeysCommands = catalogCommandregistry.keys().asIterator();
		
			while(iteratorKeysCommands.hasNext()) {
				String keyCommand = iteratorKeysCommands.next();
				
				//Procesar statedCommand por cada key registrada
				
				StatedCommand statedCommand =  vis.getCatalogCommandsRegistry().get(keyCommand);
				String sistemaComando = statedCommand.getSistemaComando();
				STATE_COMMAND state = statedCommand.getStateCommand(); 
				String nameSocketSistema = cTarea.getNameSocketSistema();
				
			
				//EL stateComand esta HANDLED y se ha pasado el tiempo de procesamiento sin resultado FINALIZED
				if(state == STATE_COMMAND.HANDLED & sistemaComando.equals(nameSocketSistema)) {
					long timeNow = new Date().getTime();
					if(timeNow > statedCommand.getRefTimeInit() + statedCommand.getMaxTimeOut() ) {
						statedCommand.setStateCommand(STATE_COMMAND.TIMEOUT);
						vis.getjTextAreaComandos().append(statedCommand.getResult()); //Imprimimos el test al Textarea
						vis.getjTextAreaComandos().append(System.getProperty("line.separator") + " TIMEOUT " + keyCommand ); //Imprimimos el test al Textarea
						log.info("statedCommand " +keyCommand + " TIMEOUT from receiver " + nameSocketSistema);
						catalogCommandregistry.remove(keyCommand);//iteratorKeysCommands.remove(); // eliminamos este comando del registro	
						continue;
					}
				}
				
				//El stateCommand esta DECLARED y coincide con el sistema base. Se cambia a estado HANLED y se hace Owner a este receiver.
				if(state == STATE_COMMAND.DECLARED & sistemaComando.equals(nameSocketSistema)) {
					statedCommand.setOwner(this);
					statedCommand.setStateCommand(STATE_COMMAND.HANDLED);
					log.info("statedCommand " + keyCommand + " HANDLED for receiver " + nameSocketSistema);
				}
				
				// El stateCommand esta HANDLED y este receiver es su OWnwer. 
				if(state == STATE_COMMAND.HANDLED && statedCommand.getOwner() == this) {
					//////////////////////////////////////////////////////////////////////////////
					//Comprobar si es final de Comando,guardar linea y cambiar a estado FINALIZED
					/////////////////////////////////////////////////////////////////////////////
					String maskEndTest = statedCommand.getMaskEndTest();
				
					if (maskEndTest.contains("&")) {
						String mask_AND[] = maskEndTest.split("&");
						int match_AND = 0;
						for (String m : mask_AND) {
							if (cadenaMensaje.contains(m))
								match_AND++;
						}
						if (match_AND == mask_AND.length) {
							statedCommand.setResult(statedCommand.getResult() +  cadenaMensaje.concat(System.getProperty("line.separator")));
							cadenaMensajeConsumed = true ;
							statedCommand.setStateCommand(STATE_COMMAND.FINALIZED);
							vis.getjTextAreaComandos().append(statedCommand.getResult()); //Imprimimos el test al Textarea
							catalogCommandregistry.remove(keyCommand);//iteratorKeysCommands.remove(); // eliminamos este comando del registro	
							log.info("statedCommand " + keyCommand + " FINALIZED for receiver " + nameSocketSistema);
							continue; //ya no hace falta seguir
						
						}
					} else {
						if(cadenaMensaje.contains(maskEndTest)) {
							statedCommand.setResult(statedCommand.getResult() +  cadenaMensaje.concat(System.getProperty("line.separator")));
							cadenaMensajeConsumed = true ;
							statedCommand.setStateCommand(STATE_COMMAND.FINALIZED);
							vis.getjTextAreaComandos().append(statedCommand.getResult()); //Imprimimos el test al Textarea
							catalogCommandregistry.remove(keyCommand);//iteratorKeysCommands.remove(); // eliminamos este comando del registro	
							log.info("statedCommand " + keyCommand + " FINALIZED for receiver " +nameSocketSistema);
							continue; //ya no hace falta seguir
						}
					}
					//////////////////////////////////////////////////////////////////////////////////
					//Procesar lineas y añadir a result si esta linea contiene alguna de las mascasras
					//////////////////////////////////////////////////////////////////////////////////
					Iterator<String> itMasks = statedCommand.getModelMasks().iterator();
					while(itMasks.hasNext()) {
						// cONTROL FILTRO COPULATIVO
						String mask = itMasks.next();
						if (mask.contains("&")) {
							String mask_AND[] = mask.split("&");
							int match_AND = 0;
							for (String m : mask_AND) {
								if (cadenaMensaje.contains(m))
									match_AND++;
							}
							if (match_AND == mask_AND.length) {
								statedCommand.setResult(statedCommand.getResult() +  cadenaMensaje.concat(System.getProperty("line.separator")));
								cadenaMensajeConsumed = true ;
							
							}
								
						} else {
							
							if(cadenaMensaje.contains(mask)) {
								statedCommand.setResult(statedCommand.getResult() +  cadenaMensaje.concat(System.getProperty("line.separator")));
								cadenaMensajeConsumed = true ;
								
							}
						}
					}
				}
				//if(cadenaMensajeConsumed == true) break;
			}
			
		}
		return cadenaMensajeConsumed;
	}
	
	public Receiver(DatagramSocket socket, Visualizador visualizador, ConsultaTarea cTarea, SimpMessagingTemplate smt) {
		this.mySocket = socket;
		this.vis = visualizador;
		this.cTarea = cTarea;
		this.smt = smt;
	}
}
