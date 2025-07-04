package observer.controller;

import observer.model.OModel;

public class OController {
	private OModel model;

	public OController(OModel model) {
		this.model = model;
	}

	public void doThings() {
		this.model.setSomeData("foo bar");
	}
}
