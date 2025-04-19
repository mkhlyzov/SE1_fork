package exception.main;

import exception.exceptionclasses.CheckedException;
import exception.exceptionclasses.UncheckedException;

public class Main {

	/* For error handling in Java use exceptions instead of integers or booleans:
	 * 
	 * 1) Exceptions can contain error messages
	 * 2) Exceptions can be separated by try catches in a readable manner
	 * 3) Exceptions contain stack traces showing the line which has thrown an error
	 * 4) When logging exceptions automatically most of the interesting details will be contained in the log
	 * 5) Integers and booleans are typically seen as a code smell - used on old code bases and programming languages, such as, C
	 * 6) Exceptions can force the caller of a method to handle them, hence you prevent that error handling is forgotten or overlooked 
	 */
	
	// Check out the commends here and in the exception classes to learn about important exception best practices.
	
	public static void main(String[] args) {

		try	{
			// checked example with forces callers to use try catch, however you can add a throws to this method too to pass it to the next caller
			// Never add a throws to the main method (code smell) as this would ignore the exception, and exceptions should never
			// be simply ignored or used as normal part of the implemented logic => handle the exceptions before!
			checkedExceptionExample();
		}
		// Note how we have not written catch(Exception e) below
		// this is important as it would disable most benefits provided from checked exceptions
		// as now, new or changed error situations could easily be overlooked by Software Engineers
		catch(CheckedException e) {
			// handle exception, i.e., logging plus a solution
		}
		catch(IllegalArgumentException e) {
			// you can separate the handling of different errors with multiple catches
		}
		catch(ArithmeticException | ClassCastException e) {
			// or you can handle multiple exceptions with the same catch
		}
		finally
		{
			// executed always after the try. Can, e.g., be used to free resources. With current java versions
			// resources can be freed automatically based on "try (Resource someResource = con.createResource())" 
			// this feature is called "try with resource" and will call close() on someResource automatically
		}

		// compare with the checked exception example
		// why is no try catch use/necessary here? But how can then unchecked exceptions be detected and handled?
		// Think: When should one use checked exceptions and when unchecked? Why? How do they differ from each other?
		uncheckedExceptionExample();
	}

	
	private static void checkedExceptionExample() throws CheckedException {
		// exceptions are intended to report errors to the calling code, hence:
		// never throw and catch an exception in the same method. 
		
		// instead: 
		// a) solve error in the method where it occurs without using exceptions
		// b) if a) is not possible delegate the error handling to the calling code by throwing an exception.
		// catch the exception as soon as possible (i.e., at the first method in the method call chain that can solve the error)
		// if necessary you can forward errors using rethrow (hardly necessary for our project, mainly used on the borders of frameworks and libraries).
		throw new CheckedException("Some great error description for the checked exception.");
	}
	
	private static void uncheckedExceptionExample()
	{
		throw new UncheckedException("Some great error description for the unchecked exception.");
	}
}
