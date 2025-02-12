package cta.designe.listener;

import java.util.Date;
import java.util.Vector;

import cta.IReceiver;
import cta.Receiver;

public class StatedCommand {
	String sistemaComando;              	//El sistema base al que pertenece este comando
	String nameComando;                 	//El nombre del comando para ejecutar 
	STATE_COMMAND stateCommand;          	//El Estado del comando
	Vector<String> modelMasks;            	//El conjunto de mascaras que definen las lineas de salida del comando
    IReceiver Owner; 						//El Receiver Owner
	String maskEndTest; 					//La mascara definitoria de final de comando o test alcanzado
	long maxTimeOut; 						//EL tiempo transcurrido maximo permitido (TimeOut)
	long refTimeInit; 						//El tiempo de inicializacion del comando (Debe haber un receiver activo)
	String result; 							//La deteccion de lineas de comando se concatenara en un String result
	
	public StatedCommand(String sistemaComando, String nameComando,	 Vector<String> modelMasks, String maskEndTest, long maxTimeOut) {
		this.sistemaComando=sistemaComando;
		this.nameComando=nameComando;
		this.stateCommand =  STATE_COMMAND.DECLARED;
		this.modelMasks = modelMasks;
		this.Owner=null;
		this.maskEndTest = maskEndTest;
		this.refTimeInit = new Date().getTime();
		this.maxTimeOut = maxTimeOut;
		this.result="";
	}
	
	
	
	public void setStateCommand(STATE_COMMAND stateCommand) {
		this.stateCommand = stateCommand;
	}
	
	public String getSistemaComando() {
		return sistemaComando;
	}

	public String getNameComando() {
		return nameComando;
	}

	public Vector<String> getModelMasks() {
		return modelMasks;
	}

	public String getResult() {
		return result;
	}
	
	public void setResult(String lines) {
		this.result = lines;
	}
	public STATE_COMMAND getStateCommand() {
		return stateCommand;
	}
	public IReceiver getOwner() {
		return this.Owner;
	}
	public void setOwner(IReceiver owner) {
		this.Owner = owner;
	}
	public long getMaxTimeOut() {
		return maxTimeOut;
	}
	public long getRefTimeInit() {
		return refTimeInit;
	}
	public String getMaskEndTest() {
		return maskEndTest;
	}
	
	//Opcionalmente podran recopilarse las lineas de resultado y enviarlas a un broker en formato JSON
	
}
