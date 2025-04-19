package propchange.view;

import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

import observer.controller.OController;
import observer.model.OModel;
import propchange.controller.PCController;
import propchange.model.PCModel;

public class PCView{

	/* To respect single responsibility, integrate the logic to
	 * print something to the console (or convert something into a string to print
	 * it later) here. If the class gets too large by doing so, move that logic into its own class.	  
	 */
	
	private PCController controller;

	public PCView(PCModel model, PCController controller) {
		this.controller = controller;
		model.addPropertyChangeListener(modelChangedListener);
	}

	// using a lambda expression or anonymous classes to define the handler of an event change listener
	// here, we show how to use lambda expressions
    final PropertyChangeListener modelChangedListener = event -> {

    	Object model = event.getSource();
    	Object newValue = event.getNewValue();
    	
		System.out.println("Display of the changed value:" + newValue);

		if(model instanceof PCModel)
		{
			System.out.println("You even get the whole model.");
			
			PCModel castedModel = (PCModel)model;
			System.out.println("After casting I can access the data of the model:" + castedModel.getSomeData());
		}
    };

    /* normally, model changes are triggered by user input
    * but as the MVC permits the controller too to change the model, we can use the controller
    * to directly change the model and thus output new game information to the user
	*/
    public void handleUserButtonPress() {
		this.controller.doThings();
	}
}
