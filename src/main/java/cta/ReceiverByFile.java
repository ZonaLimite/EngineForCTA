package cta;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import cta.designe.listener.Algoritmos;
import cta.designe.listener.STATE_COMMAND;
import cta.designe.listener.StatedCommand;
import cta.remote.stompbroker.ModelEventTrace;



public class ReceiverByFile implements Runnable,IReceiver {
	
	private Visualizador vis;
	private ConsultaTarea cTarea;
	private SimpMessagingTemplate smt;

	private ConcurrentHashMap<String, StatedCommand> catalogCommandregistry;
	
	
	public ConsultaTarea getcTarea() {
		return cTarea;
	}


	Algoritmos algoritmos;
	Logger log = Logger.getLogger("ReceiverByFile");


	public void run() {
		//Registrar este receiver para el sistema dado (max. 3 hilos)
		catalogCommandregistry = vis.getCatalogCommandsRegistry();
		vis.getThreadReceiverByFileRegistry().put(cTarea.getNameSocketSistema(), this);
		vis.refreshLedsSocketsStatus();
		
		String cadenaMensaje=null;
		algoritmos = new Algoritmos();

		String nameConsulta = cTarea.getNombreConsultaFull();
	
		FileReader fr = null;
		BufferedReader br = null;
		
		try {

			fr = new FileReader(cTarea.getNameFileSource());
			br = new  BufferedReader(fr);
			String[] sArrayFilter = null;
			cadenaMensaje = br.readLine();
			while ((vis.getFlagDisconnectRegistry()).get(cTarea.getNameSocketSistema()) == 0 &&
					cadenaMensaje!=null	) {
				try {
					// filtrar por catalogo de filtros texto (normalmente por cada linea)
					sArrayFilter = vis.getCatalogFiltersRegistry(nameConsulta);
					Thread.yield();
					Thread.sleep(1);
				
					//cadenaMensaje = br.readLine();
	
					checkCommands(cadenaMensaje);
	
					if(algoritmos.filterMatch(cadenaMensaje, sArrayFilter, vis.getFilterExclusive().isSelected())) {
						handlerWriteLine(cadenaMensaje);
					}
				} catch (Exception e) {
					String nameConsultaFull = cTarea.getNombreConsultaFull();
					Vector<String> paraVerMasks = new Vector<String>();
					for (String mask: vis.getCatalogFiltersRegistry(nameConsultaFull)) {
						paraVerMasks.add(mask);
					}
					System.err.println("Hilo "+ cTarea.nombreConsultaTareaFull() +" "+this +" trabajando Mascaras :"+paraVerMasks+ " "+ e.getLocalizedMessage());
				}
				cadenaMensaje=br.readLine();
			
			} 
		} catch (IOException e) {
			System.out.println("Problema al tratar fichero :" + cTarea.getNameFileSource());
			System.out.println(e.getMessage());
		}finally {
			//hacemos limpieza en el registro
			vis.getThreadReceiverByFileRegistry().remove(cTarea.getNameSocketSistema());
			vis.refreshLedsSocketsStatus();
			handlerWriteLine("Hilo ByFile Finalizado");
			log.info("Hilo ByFile de sistema " + this.cTarea.getNameSocketSistema() + ":" +this+ " finalizado.");
			
			try {
				if(br!=null)br.close();
				if(fr!=null)fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		
			if (algoritmos.filterMatch(cadena,vis.getCatalogListener(), true)) {
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
				
			
				//EL stateComand esta HANDLED y se ha pasado el tiempo de procesamiento sin resultado FINALIZED
				if(state == STATE_COMMAND.HANDLED & sistemaComando.equals(cTarea.getNameSocketSistema())) {
					long timeNow = new Date().getTime();
					if(timeNow > statedCommand.getRefTimeInit() + statedCommand.getMaxTimeOut() ) {
						statedCommand.setStateCommand(STATE_COMMAND.TIMEOUT);
						vis.getjTextAreaComandos().append(statedCommand.getResult()); //Imprimimos el test al Textarea
						vis.getjTextAreaComandos().append(System.getProperty("line.separator") + " TIMEOUT " + statedCommand.getNameComando() ); //Imprimimos el test al Textarea
						log.info("statedCommand " +keyCommand + " TIMEOUT from receiver " + this.getcTarea().getNameSocketSistema());
						catalogCommandregistry.remove(keyCommand);//iteratorKeysCommands.remove(); // eliminamos este comando del registro	
						continue;
					}
				}
				
				//El stateCommand esta DECLARED y coincide con el sistema base. Se cambia a estado HANLED y se hace Owner a este receiver.
				if(state == STATE_COMMAND.DECLARED & sistemaComando.equals(cTarea.getNameSocketSistema())) {
					statedCommand.setOwner(this);
					statedCommand.setStateCommand(STATE_COMMAND.HANDLED);
					log.info("statedCommand " + keyCommand + " HANDLED for receiver " + this.getcTarea().getNameSocketSistema());
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
							log.info("statedCommand " + keyCommand + " FINALIZED for receiver " + this.getcTarea().getNameSocketSistema());
							continue; //ya no hace falta seguir
						
						}
					} else {
						if(cadenaMensaje.contains(maskEndTest)) {
							statedCommand.setResult(statedCommand.getResult() +  cadenaMensaje.concat(System.getProperty("line.separator")));
							cadenaMensajeConsumed = true ;
							statedCommand.setStateCommand(STATE_COMMAND.FINALIZED);
							vis.getjTextAreaComandos().append(statedCommand.getResult()); //Imprimimos el test al Textarea
							catalogCommandregistry.remove(keyCommand);//iteratorKeysCommands.remove(); // eliminamos este comando del registro	
							log.info("statedCommand " + keyCommand + " FINALIZED for receiver " + this.getcTarea().getNameSocketSistema());
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


	public ReceiverByFile( Visualizador visualizador, ConsultaTarea cTarea, SimpMessagingTemplate smt ) {
		
		this.vis = visualizador;
		this.cTarea = cTarea;
		this.smt=smt;
	}
}
