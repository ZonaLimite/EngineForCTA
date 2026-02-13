package cta;

import java.io.File;
import java.io.Serializable;

//Una consulta es un vector de modulos + filtro y su nombre de consulta que contiene el sistema asociado a la misma
//Una ConsultaTarea es una consulta a la que se añade una maquina y opcionalmente un file para tratamiento ByFile
public class ConsultaTarea implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Consulta consulta;
	String numeroMaquina;
	File nameFileSource;

	public File getNameFileSource() {
		return nameFileSource;
	}

	public String getSistemaConsulta() {
		return this.consulta.getSistemaConsulta();
	}

	public void setNameFileSource(File nameFileSource) {
		this.nameFileSource = nameFileSource;
	}

	public Consulta getConsulta() {
		return consulta;
	}

	public void setConsulta(Consulta consulta) {
		this.consulta = consulta;
	}

	public String getNumeroMaquina() {
		return numeroMaquina;
	}

	public void setNumeroMaquina(String numeroMaquina) {
		this.numeroMaquina = numeroMaquina;
	}

	public String nombreConsultaTareaFull() {
		return this.consulta.getSistemaConsulta() + ":" + getNumeroMaquina() + ":" + this.consulta.getNameConsulta();
	}

	public String getNombreConsultaFull() {
		return consulta.getNombreConsultaFull();
	}

	public String getNameSocketSistema() {
		return consulta.getSistemaConsulta() + ":" + numeroMaquina;
	}

	public ConsultaTarea(Consulta consulta, String numeroMaquina, File nameFileSource) {
		super();
		this.consulta = consulta;
		this.numeroMaquina = numeroMaquina;
		this.nameFileSource = nameFileSource;
	}

}
