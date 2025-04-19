package test.exampleJUnit5AndMockito;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import test.dummy.IDummyClassForTests;
import test.dummy.IUserRepository;
import test.dummy.UserManager;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.MatcherAssert.assertThat;

//this class demonstrates the most important features of Mockito, which can be useful
//while implementing the client or the server. For the Client, you could, e.g., 
//use Mockito to fake network interactions. 
public class MixedClassForTests_Tests_UsingJUnit5AndMockito {

	/* For information and help on jUnit5 see the other classes in this package.
	 * 
	 * 1) Why was Mockito chosen? It is one of the most mature and widely used
	 * Mocking frameworks. In addition, it was found to work reliably by me. Nevertheless, a range of alternatives exists.
	 * 
	 * 2) Mocks vs. Spies in Mockito? Mockito supports mocking (i.e., entirely fake behavior based on an interface or abstract class) and
	 * spying (monitoring/adapting/verifying an actual implementation and verifying its behavior, e.g., if expected methods were
	 * called with expected parameters or partly mocking only some methods).
	 * Here, we will concentrate on the mock functionality. Check out Mockito.spy if you are interested in spies.
	 * 
	 * 3) Mockito can be applied in multiple ways, e.g., based on Mockito.mock or using @Mock annotations. For the sake 
	 * of simplicity, we will concentrate on Mockito.mock here only. 
	 * 
	 * 4) When to use? Use Mockito when you want to decouple your implementation, e.g., from external or overly
	 * complex (i.e., hard to create during testing) dependencies as it enables you to define stubs and fakes with ease.
	 * Note, if you find that you are constantly in need to fake some complex objects/parameters/dependencies during testing
	 * then this indicates a bad (e.g., overly coupled) architecture.
	 * Check out the details given throughout the lecture, such as the interface segregation principle, encapsulation,
	 * and single responsibility principle to improve it. 
	 * 
	 * 5) Use the potential of mocks during the assignment, i.e., overwrite behavior and verify the utilization of your mock.
	 * If you don't know how a mock could integrate with your business logic check out Dependency Injection (its covered in the VO material).
	 * 
	 * Additional details are found in the UnitTesting Slides on Moodle and will also be given throughout the respective
	 * testing-focused tutorial session. There also tips will be given on how and when to apply Mocks throughout 
	 * the assignment. 
	 */
	
	@Test
	public void givenDivMethodMocked_WhenDivInvoked_ThenMockValueReturned() {
		// mock the implementation based on an interface
		IDummyClassForTests mocked = Mockito.mock(IDummyClassForTests.class);
		
		// stub some behavior, here we specify that when div is called with anyInt (i.e., arbitrary) int 
		// parameter values we always return 1. Here always 1 will be returned.
		// Note, you will need to choose the correct parameter type, i.e., there is any() (for arbitrary objects),
		// anyInt, anyBoolean, anyLong, anyList, anyObject, and so on.
		Mockito.when(mocked.div(anyInt(), anyInt())).thenReturn(1);
		
		// call the mock to execute the mocked behavior
		int mockedCalcResult = mocked.div(1337, 7331);
	
		// mocks are integrated into using tests so verify if the mock worked correctly
		Assertions.assertEquals(1, mockedCalcResult, "Expected to be mocked to 1");
	}
	
	@Test
	public void givenDivMethodMocked_WhenDivInvokedMultipleTimes_ThenMockValueReturned() {
	
		// mock the implementation based on an interface
		IDummyClassForTests mocked = Mockito.mock(IDummyClassForTests.class);
		
		// you can define different stub behavior for different and specific parameters as well
		// here the use of eq() enables us to mix anyInt() with with a specific value
		Mockito.when(mocked.div(eq(1337), anyInt())).thenReturn(1);
		Mockito.when(mocked.div(eq(7331), anyInt())).thenReturn(2);
		
		// call the mock to execute the mocked behavior
		int mockedCalcResult = mocked.div(1337, 0);
			
		// mocks are integrated into using tests so verify if the mock worked correctly
		Assertions.assertEquals(1, mockedCalcResult, "Expected to be mocked to 1");	
		
		// mocks for further calls, for a specific parameter value first and afterwards
		// for an arbitrary one
		mockedCalcResult = mocked.div(7331, 0);
		Assertions.assertEquals(2, mockedCalcResult, "Expected to be mocked to 2");	

		// you can even define a list of entries that are returned throughout multiple calls into a mocked method		
		Mockito.when(mocked.div(anyInt(), anyInt())).thenReturn(3, 4, 5, 6);
		
		mockedCalcResult = mocked.div(10000, 0);
		Assertions.assertEquals(3, mockedCalcResult, "Expected to be mocked to 3");	
	}
	
	@Test
	public void givenMockedList_WhenAddingValues_ThenCallBehaviourIsAsExpected() {
		
		// Mockito can not only create stubs but also verify if some interactions take place as expected
		// for example, if methods are called with expected parameters an expected amount of times
		// in general, this functionality is referred to as mocking
		
		// here, we create a mock of Java's built-in List interface
		List<String> mocked = Mockito.mock(List.class);

		mocked.size(); // call size once, possible even without an explicit mocked behavior is specified
		
		// you can verify if methods were called an expected amount of time
		// here, we check if the size() method was called exactly once (thus the size() call above)

		Mockito.verify(mocked, times(1)).size(); 
		
		/* There are further options for this kind of check, for example
		 * instead of times one could write atLeastOnce(), atMost(2) for at most 2 calls on the method, atLeast(1) 
		 * for at least one call, never() if the method should never be called, or only() if only this method
		 * should be called and no other. 
		 * If the mock was not used as expected the test will automatically fail.
		 */
		
		mocked.add("ExpectedParamValue"); 
		
		// further, you can verify if a method was called with the expected parameters
		Mockito.verify(mocked).add("ExpectedParamValue");
		
		/* There are further options which could be used instead of a real value
		 * for example one could write 
		 * 		Mockito.verify(mocked).add(anyString());
		 * to permit any String to be observed. For methods with expect an Object
		 * you can e.g., use any(String.class) to specify a specify type to be observed.
		 * Of course this works also for other standard types (or even your own custom types).
		 */ 
	}
	
	@Test
	public void givenMockedList_WhenInUse_CanSimulareComplexBehaviour() {
		
		// finally, Mockito can also be used to simulate "complex" behavior, e.g., to store some values in a fake database
		// when some class other is interacting with it (i.e., creating a fake). Below it will be shown how to do this with Mockito
		// throughout the lecture, we will also discuss and see how this can be done with plain Java easily 
		// (which is sufficient for the exams and many common use cases).
		
		// in the following we assume that we have a UserRepository (i.e., a user database) and a 
		// UserManager (i.e., some functionality to add users to the database). To decouple the test from external 
		// dependencies (i.e., a real database) we will fake the UserRepository using Mockito and also verify if its used 
		// as expected by the UserManager at the same time.
		
		// create the mock of the dependency
		IUserRepository mockedDependency = Mockito.mock(IUserRepository.class);

		// create the class which uses the external dependency and inject the mock using dependency injection
		UserManager userManager = new UserManager(mockedDependency);
		
		// use the userManager and verify if it's correctly interacting with the mocked external dependency.
		// executing behavior
		userManager.addUser("TestUser");
		
		// verifying use of behavior/mock
		Mockito.verify(mockedDependency).storeUsername("TestUser");

		//let's simulate some more complex behavior, i.e., such that added users are preserved
		// For this, we fake the database based on a simple list
		final List<String> fakeDatabaseContent = new ArrayList<>();
		
		// then, we ensure that all usernames which should be stored are preserved
		// for this, we use a lambda expression to define a specify behavior that should be executed when
		// storeUsername is called
		Mockito.doAnswer(storeMethodInvocation -> {
			// get the first argument of the storeUsername method, i.e., the username to store
			String userName = storeMethodInvocation.getArgument(0);
			// store the username in the fake database, i.e., the in-memory list
			fakeDatabaseContent.add(userName);	
			return null; //a return is expected, but as storeUsername returns void we can apply this shortcut
		}).when(mockedDependency).storeUsername(anyString());
		
		// finally, we can use this to fake the readUsers method as well.
		Mockito.when(mockedDependency.readUsernames()).thenReturn(fakeDatabaseContent);
		
		// now, we can completely simulate a database without having a real one 
		// abstracting our code from external dependencies to get useful tests without external dependencies, yay :)
		
		userManager.addUser("FirstUser");
		userManager.addUser("SecondUser");
		
		List<String> readUsers = userManager.readUsers();
		
		// using HAMCREST matchers (i.e., assertThat and contains) to get a nicely readable assert which verifies 
		// if the user manager is correctly forwarding and reading data to and from the UserRepository
        assertThat(readUsers, contains("FirstUser", "SecondUser"));		
	}
}
