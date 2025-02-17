package cta;

import java.util.Vector;

import org.springframework.stereotype.Component;

import cta.designe.listener.StatedCommand;

@Component
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
	
	synchronized StatedCommand  makeFactoryCommand(String nameCommand, String sistema, String moduloMaquina,String maquina ) {

		StatedCommand statedCommand = null;
		String baseSistemaCommand = sistema+":"+maquina+":"+moduloMaquina+":"+nameCommand;
		String mask1,mask2,mask3,mask4;
		String maskEndTest1;
		Vector<String> modelMasks;
		
		switch (nameCommand) { 
	    case "gapl": //Get All Plates Labels
	    	modelMasks = new Vector<String>();
	    	mask1 = "CMD "+moduloMaquina+" -& with [Label:"; 
	    	mask2 = "T2kBucketConveyor::onCommandGetAllPlateLabel processed";

	    	modelMasks.add(mask1);
	    	modelMasks.add(mask2);
	    	
	    	maskEndTest1 = "CMD "+moduloMaquina+" - Plate[423] with [Label:";
	    		
	    	statedCommand = new StatedCommand(sistema+":"+maquina,nameCommand,modelMasks,maskEndTest1,10000);
	    	statedCommand.setResult("###################### ETIQUETAS LEIDAS DE "+ moduloMaquina+ "######################## "+ 
	    			 	 System.getProperty("line.separator") + "|\r");

	     break;
	     

	    
	    case "ds": //Diagnostica paso Plates UpperCnv
	    	modelMasks = new Vector<String>();
	    	
	    	mask1 = "CMD SOR"+moduloMaquina+" -"; 

	    	modelMasks.add(mask1);
	    	
	    	maskEndTest1 = "CMD SOR"+moduloMaquina+ "- =============== Total of detection :";
	    		
	    	statedCommand = new StatedCommand(sistema+":"+maquina,nameCommand,modelMasks,maskEndTest1,10000);
	    	statedCommand.setResult(	"#################### FALLOS DETECCION PLATES EN "+ moduloMaquina+"####################"+
	    				System.getProperty("line.separator") + "|\r");

	     break;

	    case "SCO:1:SOR13L~S:ds": //Diagnostica paso Plates LowerCnv
	    	modelMasks = new Vector<String>();
	    	mask1 = "CMD SOR13L~S -"; 

	    	modelMasks.add(mask1);
	    	
	    	maskEndTest1 = "CMD SOR13L~S - =============== Total of detection :";
	    		
	    	statedCommand = new StatedCommand(sistema+":"+maquina,nameCommand,modelMasks,maskEndTest1,10000);
	    	statedCommand.setResult(	"#################### FALLOS DETECCION PLATES EN CARRUSEL BAJO ####################"+
    				System.getProperty("line.separator") + "|\r");

	     break;

	    case "SCO:2:SOR13L-S:ds": //Diagnostica paso Plates UpperCnv
	    	modelMasks = new Vector<String>();
	    	mask1 = "CMD SOR13L-S -"; 

	    	modelMasks.add(mask1);
	    	
	    	maskEndTest1 = "CMD SOR13L-S - =============== Total of detection :";
	    		
	    	statedCommand = new StatedCommand(sistema+":"+maquina,nameCommand,modelMasks,maskEndTest1,10000);
	    	statedCommand.setResult(	"#################### FALLOS DETECCION PLATES EN CARRUSEL ALTO ####################"+
	    				System.getProperty("line.separator") + "|\r");

	     break;

	    case "SCO:2:SOR13L~S:ds": //Diagnostica paso Plates LowerCnv
	    	modelMasks = new Vector<String>();
	    	mask1 = "CMD SOR13L~S -"; 

	    	modelMasks.add(mask1);
	    	
	    	maskEndTest1 = "CMD SOR13L~S - =============== Total of detection :";
	    		
	    	statedCommand = new StatedCommand(sistema+":"+maquina,nameCommand,modelMasks,maskEndTest1,10000);
	    	statedCommand.setResult(	"#################### FALLOS DETECCION PLATES EN CARRUSEL BAJO ####################"+
    				System.getProperty("line.separator") + "|\r");

	     break;
	     
	     
	    default:
	     // Default secuencia de sentencias.
		}
		
		return statedCommand;
		
	}

}
