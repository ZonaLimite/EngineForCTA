package cta;

import java.io.Serializable;

public class Modulo implements Serializable,Cloneable {
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	String nombre;
	String descripcion;
	String mask;
	String sistema;
	
	
	
	public Modulo() {
		super();
	}
	
	public String getSistema() {
		return sistema;
	}
	public void setSistema(String sistema) {
		this.sistema = sistema;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getMask() {
		return mask;
	}
	public void setMask(String mask) {
		this.mask = mask;
	}
	public String toString() {
		return this.getNombre()+"("+ this.getDescripcion()+")->"+this.getMask();
	}
	
	
	
}
