package observer.model;

import java.util.Observable;

public class OModel extends Observable{

	private String someData;

	public String getSomeData() {
		return someData;
	}

	public void setSomeData(String someData) {
		this.someData = someData;
		
		// notify all listeners that something has changed
		this.setChanged();
		this.notifyObservers(someData);
	}
}
