package cta.designe.listener;

import cta.Consulta;

public class StateCommand extends Consulta {
	
	//Un StateCommand extiende consulta y añaden nuevos campos necesarios
	
	//state; {Instantiated, Handled, Finalized, TimeOut}
	
	//Necesitamos dar un estado al objeto porque sera tratado por diferntes receivers y solo uno se encargara de procesarlo. 
	//El proceso sera detectarlo Instantiated, para manejarlo por un solo receiver el cual sera el propietario del comando (Owner)
	//filtrando los vMasks que caracterizan al comando y volcandolos al text Area de Cheks
	//Detectar la mascara de Final de Test para entrar en estado Finalized
	//Ser eliminado por el receiver Propietario una vez alcanzado  el estado Finalized o haberse alcanzado un TimeOut prefijado
	String state; //El Estado 
    String Owner; //El Receiver Owner
	EventMask maskEndTest; //La mascara definitoria de final de comando o test alcanzado
	long maxTimeOut; // EL tiempo transcurrido maximo permitido
	long refTimeInit; // El tiempo de inicializacion del comando (Debe haber un receiver activo)
	
	//Opcionalmente podran recopilarse las lineas de resultado y enviarlas a un broker en formato JSON
}
