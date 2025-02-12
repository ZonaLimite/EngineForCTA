package cta.designe.listener;

import java.io.Serializable;
import java.util.Vector;

public class EventMask implements Serializable{
	private String nameMaskEvent;
	private String nameMaskEventDescrip;
	private String formatMaskPublish;
	private String topicBroker;
	private boolean enabled;
	
	public EventMask(String nameMaskEvent, String nameMaskEventDescrip, String maskPublic, String topicBroker, boolean enabled) {
		this.nameMaskEvent = nameMaskEvent;
		this.nameMaskEventDescrip = nameMaskEventDescrip;
		this.formatMaskPublish = maskPublic;
		this.topicBroker = topicBroker;
		this.enabled = enabled;
	}
	
	
	public EventMask() {
		super();
		this.nameMaskEvent = "mascara";
		this.nameMaskEventDescrip = "Descripcion mascara";
		this.formatMaskPublish = "MASK";
		this.topicBroker = "topic";
		this.enabled = new Boolean(true);
	}


	public Vector<Object> toVector() {
		Vector<Object> outVector = new Vector<Object>();
		outVector.add(nameMaskEvent);
		outVector.add(nameMaskEventDescrip);
		outVector.add(formatMaskPublish);
		outVector.add(topicBroker);
		outVector.add(enabled);
		return outVector;
	}
	
	public Vector<String> toVectorColumns(){
		Vector<String> outVector =new Vector<String>();
		outVector.add(nameMaskEvent.toString());
		outVector.add(nameMaskEventDescrip.toString());
		outVector.add(formatMaskPublish.toString());
		outVector.add(topicBroker.toString());
		outVector.add("enabled");
		return outVector;
		
	}

	public String getFormatMaskPublish() {
		return formatMaskPublish;
	}

	public void setFormatMaskPublish(String formatMaskPublish) {
		this.formatMaskPublish = formatMaskPublish;
	}

	
	public String getNameMaskEvent() {
		return nameMaskEvent;
	}



	public void setNameMaskEvent(String nameMaskEvent) {
		this.nameMaskEvent = nameMaskEvent;
	}



	public String getNameMaskEventDescrip() {
		return nameMaskEventDescrip;
	}



	public void setNameMaskEventDescrip(String nameMaskEventDescrip) {
		this.nameMaskEventDescrip = nameMaskEventDescrip;
	}



	public String getTopicBroker() {
		return topicBroker;
	}



	public void setTopicBroker(String topicBroker) {
		this.topicBroker = topicBroker;
	}



	public boolean isEnabled() {
		return enabled;
	}



	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}



	@Override
	public String toString() {
		return "EventMask [nameMaskEvent=" + nameMaskEvent + ", nameMaskEventDescrip=" + nameMaskEventDescrip
				+ ", formatMaskPublish=" + formatMaskPublish + ", topicBroker=" + topicBroker + ", enabled=" + enabled
				+ "]";
	}
	
	

}
