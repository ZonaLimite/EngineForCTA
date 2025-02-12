package cta;

import java.util.Date;
import java.util.Vector;

import cta.designe.listener.EventMask;
import cta.designe.listener.ModelFilter;
import cta.designe.listener.STATE_COMMAND;
import cta.designe.listener.StatedCommand;

public class InfoCommandsMaker {

	public InfoCommandsMaker() {
		super();
	}

	/*
	 * //Necesitamos dar un estado al objeto porque podria ser tratado por diferentes receivers y solo uno se encargara de procesarlo. 
	 * //Una vez instanciado quedara registrado en en el registro catalogCommandsRegistry y podra ser accesible por cada receiver.  
	 * //El proceso consistira en detectar sus estados.El estado Instantiated, para manejarlo por un solo receiver el cual sera el propietario del comando (Owner)
	 * //Entonces pasara al estado HANDLED filtrando los vMasks que caracterizan al comando e ir concatenado lineas de test en campo result 
	 * //Detectar la mascara de Final de Test para entrar en estado Finalized
	 * //Una vez su estado pase a Finalized se mostrara el String result sobre el Text Area Checks
	 * //Ademas de ser eliminado por el receiver Propietario una vez alcanzado  el estado Finalized o haberse alcanzado un TimeOut prefijado
	 * //maxTimeOut esta em milisegun , asi que 3 segundos = 3000 
	 */
	
	StatedCommand makeFactoryCommand(String nameCommand, String sistema, String moduloMaquina,String maquina,Integer flagDisconnnectSistema) {

		StatedCommand statedCommand = null;
		String baseSistemaCommand = sistema+":"+maquina+":"+nameCommand;
		switch (baseSistemaCommand) { 
	    case "SCO:1:gapl": //Get All Plates Labels
	    	Vector<String> modelMasksSCO1 = new Vector<String>();
	    	String MaskSCO1_1 = "CMD UpperCnv -& with [Label:"; 
	    	String MaskSCO1_2 = "T2kBucketConveyor::onCommandGetAllPlateLabel processed";

	    	modelMasksSCO1.add(MaskSCO1_1);
	    	modelMasksSCO1.add(MaskSCO1_2);
	    	
	    	String maskEndTestSCO1 = "CMD UpperCnv - Plate[423] with [Label:";
	    		
	    	statedCommand = new StatedCommand(sistema+":"+maquina,nameCommand,modelMasksSCO1,maskEndTestSCO1,10000);

	     break;
	     
	    case "SCO:2:gapl": //Get All Plates Labels
	    	Vector<String> modelMasksSCO2 = new Vector<String>();
	    	String MaskSCO2_1 = "CMD UpperCnv -& with [Label:"; 
	    	String MaskSCO2_2 = "T2kBucketConveyor::onCommandGetAllPlateLabel processed";

	    	modelMasksSCO2.add(MaskSCO2_1);
	    	modelMasksSCO2.add(MaskSCO2_2);
	    	
	    	String maskEndTestSCO2 = "CMD UpperCnv - Plate[423] with [Label:";
	    		
	    	statedCommand = new StatedCommand(sistema+":"+maquina,nameCommand,modelMasksSCO2,maskEndTestSCO2,10000);

	     break;
	    default:
	     // Default secuencia de sentencias.
		}
		
		return statedCommand;
		
	}

}
