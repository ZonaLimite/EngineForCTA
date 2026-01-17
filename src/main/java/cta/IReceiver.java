package cta;

public interface IReceiver {
	public void run();
	public void handlerWriteLine(String cadena);
	public abstract boolean checkCommands(String cadenaMensaje);
}
