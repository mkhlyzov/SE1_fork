package observer.view;

import java.util.Observable;
import java.util.Observer;

import observer.controller.OController;
import observer.model.OModel;
import propchange.model.PCModel;

public class OView implements Observer {

	/* To respect single responsibility, integrate the logic to
	 * print something to the console (or convert something into a string to print
	 * it later) here. If the class gets too large by doing so, move that logic into its own class.	  
	 */
	
	private OController controller;

	public OView(OModel model, OController controller) {
		this.controller = controller;
		model.addObserver(this);
	}

	@Override
	public void update(Observable observable, Object newValue) {
		// need to cast to access the raw data
		if (observable instanceof OModel) {
			OModel model = (OModel) observable;

			System.out.println("Simulate display of value:" + model.getSomeData());
			System.out.println("You even get the whole model");
		}
	}

    /* typically, model changes can be triggered by user input as well (button press, text box entries, etc.)
    * but as the MVC permits the controller too to change the model, we can use the controller
    * to directly change the model and thus output new game information to the user
	*/
	public void handleUserButtonPress() {
		this.controller.doThings();
	}
}
