package cta.remote.service;

import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cta.Consulta;
import cta.Visualizador;
import cta.designe.listener.ModelFilter;
import java.util.Vector;

@Component
public class RemoteEngine {

	@Autowired
	private Visualizador vis;

	public void SelectSistema(String sistema) {
		vis.getComboSistemas().setSelectedItem(sistema);
	}

	public void bConectar() {
		vis.getConectar().doClick();
	}

	public void bDesconectar() {
		vis.getDesconectar().doClick();
	}

	public void adjustNumTop(int num) {
		vis.spinner.setValue(num);
	}

	public void incluirModelFilterAListener(String nameModelFilter) {
		vis.incluirModelFilterAListenersRemote(nameModelFilter);
	}

	public void borrarModelFilterDeListenerActivos(String nameModelFilterActivo) {
		ModelFilter modelFilter = vis.getCatalogoModelFilters().get(nameModelFilterActivo);
		vis.borrarModelFilterDeListener(modelFilter);
	}

	// Ojo la clave es compuesta con sistema:nameconsulta
	public void selectConsulta(String nameSistemaConsulta) {
		vis.getCombo_Consultas().setSelectedItem(vis.getCatalogoConsultas().get(nameSistemaConsulta));

	}

	public String[] getSistemasRegistrados() {
		return vis.aSistemas;
	}

	public String[] getModulosRegistrados(String pareEsteSistema) {
		ConcurrentHashMap<String, Consulta> catConsultas = vis.getCatalogoConsultas();
		Vector<String> v = new Vector<String>();
		Set<Entry<String, Consulta>> set = catConsultas.entrySet();

		Iterator<Entry<String, Consulta>> it = set.iterator();
		while (it.hasNext()) {
			Entry<String, Consulta> e = it.next();
			if (e.getKey().contains(pareEsteSistema + ":")) {
				v.add(e.getValue().getNombreConsultaFull());
			}
		}
		;
		return v.toArray(new String[v.size()]);
	}

	public String[] getListenersRegistrados() {
		Vector<String> vKeys = new Vector<String>();
		for (Enumeration<String> enumKeys = vis.getCatalogoModelFilters().keys(); enumKeys.hasMoreElements();) {
			vKeys.add(enumKeys.nextElement());
		}
		return vKeys.toArray(new String[vKeys.size()]);

	}

	public String[] getListenersActivos() {
		return vis.getListenersActivos();

	}

	public void setTextListenerRapido(String text) {
		vis.getTextFieldListener1().setText(text);

	}

	public void doClickButtonListenerRapido() {
		vis.btnIncluirListenerRapido.doClick();

	}

	public void doClickButtonPublishToSocket(boolean status) {
		// remoteControl
		vis.chckbxPublishToWebsocket.setSelected(status);
	}

}
