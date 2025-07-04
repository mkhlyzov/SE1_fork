package propchange.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class PCModel {

	//you can create multiple PropertyChangeSupport instances to enable the individual monitoring of individual properties
	private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private String someData;

	public String getSomeData() {
		return someData;
	}

	public void setSomeData(String someData) {
		String beforeChange = this.someData;
		this.someData = someData;
		
		//inform all interested parties about changes
		changes.firePropertyChange("someData", beforeChange, someData);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		//enables to register new listeners
		changes.addPropertyChangeListener(listener);
	}
}
