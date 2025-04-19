package logging.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
	// ADDITIONAL TIPS ON THIS MATTER ARE GIVEN THROUGHOUT THE TUTORIAL SESSION!

	// Logging using the SLF facade enables you to control the logging done by spring and your own logging 
	// in the same logback.xml file. This is the main advantage of using SLF in combination with logback, i.e., as soon
	// as you combine multiple libraries where each has its own logging functionality SLF is the rescue to control them all.
	
	/* To get logging running, you need:
	 * 1) Create an instance of the logger, one should create a new one in each class
	 * 2) If the logging configuration is not picked up automatically:
	 * 	  	Add the logback configuration XML file as a resource. That XML file contains the configuration of your logger 
	 * 		(e.g., where, how, when should something be logged)
	 * 		An example configuration can be found in the tutorial slides on Moodle but also in this project (see res folder => logback.xml)
	 * 		To add the res folder as a source folder, right click on it => Build Path => Use as Source Folder. This is
	 * 		required so that the application will use that configuration when being executed to configure the logging libs accordingly
	 * 		Note, the example config will only log to the console, but more complex solutions are possible, e.g., logging to a file or onto a sever
	 * 		Some additional example configurations for this (e.g., how to log into a file) are given in the tutorial slides
	 * 3) Create log messages, note there are multiple log levels (choose appropriately) and also create useful log messages
	 * 4) Take the aspects in mind which were discussed during the tutorial session on creating helpful/good log-messages
	 * 
	 * Logging frequently is the only way to get any kind of information about bugs and misbehavior of your software. Use it wisely.
	 */
	
	// Instantiate the logger, adding 
	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		 //use different log levels wisely
		 //appropriate log levels are crucial when searching through log messages
	     logger.trace("Trace log message - low level details, e.g., loop values");
  		 logger.debug("Debug log message - curciual algorithm details, e.g., important variables");
	     logger.info("Info log message - curcial behaviour, e.g., specific application state changes");
	     logger.warn("Warn log message - issues and errors that could be compensated without any harm, e.g. if we can simply try it again");
	     logger.error("Error log message - issues and errors which could not be compensated (e.g., exceptions, critical failures)");
	    
	     //when logging an exception, the exception itself (i.e., the object representing it) should also be sent to the logger
	     //TIP: Whenever you would like to signal an error, incorrect data, missing parameters, and so on USE (THROW/CATCH, resp.) appropriate EXCEPTIONS!
	     Exception exampleException = new Exception("An exception, e.g., must normally be cought by a try/catch");
	     logger.error("Exception log message", exampleException);
	     
	     //example on some neat features
	     String name = "Bob", course = "SE1";
	     //the chosen log library has a handy formatter built right in, so instead of
	     logger.info("Hello " + name + "! Do you like " + course + "?");
	     //you can write the following, which is a bit more readable and less cluttered 
	     logger.info("Hello {}! Do you like {}?", name, course);
	     // for this a) denote the variables at the end, and b) mark where they should be inserted with {}
	}

}
