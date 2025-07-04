package observer.main;

import observer.controller.OController;
import observer.model.OModel;
import observer.view.OView;

public class OMain {

	/*
	 * Using the observer implementation that is part of the Java Framework is not recommended as its marked as deprecated.
	 * We have included it just for the sake of completeness. We recommend using the PropertyChange based implementation included 
	 * in this project.
	 */
	
	public static void main(String[] args) {
		OModel model = new OModel();
		OController controller = new OController(model);
		OView view = new OView(model, controller);

		// we simulate a user by assuming that he/she has, e.g., pressed a button, and the UI updates accordingly
		view.handleUserButtonPress();
	}
}
