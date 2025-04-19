package propchange.controller;

import observer.model.OModel;
import propchange.model.PCModel;

public class PCController {
	private PCModel model;

	public PCController(PCModel model) {
		this.model = model;
	}

	public void doThings() {
		this.model.setSomeData("foo bar");
	}
}
