package cta;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.logging.Logger;

public class Dispatcher implements Runnable {
	private DatagramSocket mySocketSistema = null;
	private DatagramSocket mySocketDispatcher = null;

	private Visualizador vis;
	private String sistemaSocket;

	Logger log = Logger.getLogger("Dispatcher");

	public void run() {

		int sizeBufferSistema = 0;

		try {
			sizeBufferSistema = mySocketSistema.getReceiveBufferSize();
			log.info("Tamano ajustado de buffer DatagramSocket :" + sizeBufferSistema);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int sizeBufferDatagramPacket = sizeBufferSistema;
		int sizeReadBytes = 2048;

		byte[] mensaje_bytes = new byte[256];
		DatagramPacket paquete;
		InetAddress address = null;
		try {
			address = InetAddress.getByName("127.0.0.0");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String mensaje = "conectado a dispatcher .... de sistema :" + sistemaSocket;
		mensaje_bytes = mensaje.getBytes();
		paquete = new DatagramPacket(mensaje_bytes, mensaje.length(), address, mySocketDispatcher.getPort());
		try {
			mySocketDispatcher.send(paquete);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		do {
			byte[] RecogerServidor_bytes = new byte[sizeBufferDatagramPacket];

			try {
				// EsperamoHilo Finalizado"s a recibir un paquete/
				DatagramPacket servPaquete = new DatagramPacket(RecogerServidor_bytes, sizeReadBytes);
				mySocketSistema.receive(servPaquete);

				String sPacket = new String(RecogerServidor_bytes).trim();

				// Dispatch este paquete a cada Receiver registrado activo
				Vector<Receiver> vReceivers = vis.getThreadReceiverRegistry().get(sistemaSocket);
				for (Receiver receiver : vReceivers) {

				}
				// for each Receiver(sistema).send packet

			} catch (Exception e) {
				System.err.println("Hilo " + sistemaSocket + " " + this + " " + e.getMessage());
			}

		} while ((vis.getFlagDisconnectRegistry()).get(sistemaSocket) == 0);
		// hacemos limpieza en el registro
		// vThreads= vis.getThreadReceiverRegistry().get(sistemaSocket);
		// vThreads.remove(this);
		// vis.getThreadReceiverRegistry().put(sistemaSocket, vThreads);
		// vis.refreshLedsSocketsStatus();
		// handlerWriteLine("Hilo Finalizado");
		log.info("Dispatcher de sistema " + this.sistemaSocket + ":" + this + " finalizado.");
		mySocketSistema = null;

	}

	public Dispatcher(DatagramSocket socketSistema, DatagramSocket socketDispatcher, Visualizador singletonVisualizador,
			String sistemaSocket) {
		this.mySocketSistema = socketSistema;
		this.mySocketDispatcher = socketDispatcher;
		this.vis = singletonVisualizador;
		this.sistemaSocket = sistemaSocket;

	}
}
