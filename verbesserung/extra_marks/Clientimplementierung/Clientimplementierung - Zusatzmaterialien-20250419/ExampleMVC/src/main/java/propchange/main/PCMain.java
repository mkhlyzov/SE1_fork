package propchange.main;

import propchange.controller.PCController;
import propchange.model.PCModel;
import propchange.view.PCView;

public class PCMain {

	public static void main(String[] args) {
		PCModel model = new PCModel();
		PCController controller = new PCController(model);
		PCView view = new PCView(model, controller);

		// we simulate a user by assuming that he/she has, e.g., pressed a button and the UI updates accordingly
		view.handleUserButtonPress();
	}
}
