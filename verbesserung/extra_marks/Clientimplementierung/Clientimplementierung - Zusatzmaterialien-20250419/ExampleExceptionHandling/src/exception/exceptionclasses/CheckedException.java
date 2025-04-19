package exception.exceptionclasses;

//note the extension, name your Exceptions as follows: TypeOfError + Exception, e.g., InvalidMapException
public class CheckedException extends Exception {

	// the custom message allows a fine granular specification of the error
	// enabling and supporting reuse, i.e., to reuse the same exception type in multiple similar but still slightly different situations
	// otherwise we would need an unreasonably detailed exception hierarchy
	public CheckedException(String message) {
		super(message);
	}

	// add code, i.e., at least define exception messages. Typically the ctor with a string variable is used for this 
	// which can be forwarded to the Exception ctor 
	// generate related code in Eclipse:
	// Right Click -> Source -> Generate Constructors from Superclass - choose only the ones you will use in your code
	
	// Tip: you can store data in Exceptions, i.e., data required to understand/process the related error situation
	
    // create individual exception classes for individual error types to separate them in try/catch statements
	// for this use inheritance and create exception hierarchies
	// this permits to differentiate errors during development and debugging and handle them appropriately	
}
