package test.exampleJUnit5AndMockito;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.text.MessageFormat;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import test.dummy.DummyClassForTests;
import test.dummy.IDummyClassForTests;

//here, we use the newer jUnit5 style to define tests,
//the most significant difference between 4 and 5 is that the annotations have different names for the same functionality
//and that data-driven tests can more easily be defined
//see the unit testing slides on Moodle to get tips on how to migrate from jUnit 4 to jUnit 5
public class DummyClassForTests_Tests_UsingJUnit5 {

	// ADDITIONAL TIPS ON THIS MATTER ARE GIVEN THROUGHOUT THE TUTORIAL SESSION!

	/*
	 * Note: You can run tests normally, but you can also start tests using the
	 * debug feature of Eclipse to step through them. This is quite helpful when
	 * debugging tests so see why they fail.
	 * 
	 * In the following you see: 
	 * 1) how to set up and prepare tests and also clean them up after their execution
	 * 2) how to test for the correct handling of failures 
	 * 3) how to ignore tests
	 * 4) how to create normal tests for good cases
	 * 5) how to write data driven tests.
	 */

	/*
	 * Test organization and test naming When creating unit tests, one should also
	 * respect the separation of responsibility, hence: 
	 * 
	 * 1) For each class that you test create an individual unit test class 
	 * which only holds units test for the methods of such class under test. 
	 * For example tests for class A are stored in a test focused class ATest. 
	 * 
	 * 2) Each unit test should only test a small portion, i.e., a small amount of
	 * asserts so that it becomes easy to spot which behavior is broken when the
	 * test fails. Focus on testing logic that has a reasonably high possibility of
	 * containing bugs. 
	 * 
	 * 3) Naming tests correctly is as important as naming methods, classes, and
	 * variables correctly and in a readable manner I would recommend to apply
	 * following style: CurrentState_ExecutedAction_ExpectedState, e.g.
	 * NoUserRegistered_RegisterNewUser_NewUserIsPermanentlyStoredInTheDB 
	 * 
	 * 4) Organize your tests in multiple packages, just as you organize your logic
	 * classes in multiple to group together tests and related helper classes. 
	 * Mimic the structure of your business logic.
	 * 
	 * 5) Each test must cover arrange, act, assert. If you miss one the test isn't helpful.
	 * 
	 * NOTE: Take the aspects in mind which were discussed during the tutorial
	 * sessions on good unit testing. If you missed the tutorial at least check out 
	 * the provided testing slides referenced in the Teilaufgabe 2 assignment. 
	 */

	// see how this is set up by the @BeforeEach method
	private IDummyClassForTests dummy = null;

	// execute the tests and check out the console to see the order of the
	// before/after executions

	// MUST BE STATIC!
	@BeforeAll
	public static void setUpBeforeClass() {
		// executed once, before all tests in this class
		// use to prepare stuff and dependencies

		System.out.println("setUpBeforeClass");
	}

	// MUST BE STATIC!
	@AfterAll
	public static void tearDownAfterClass() {
		// executed once, after all tests in this class
		// use to clean up stuff and dependencies

		System.out.println("tearDownAfterClass");
	}

	@BeforeEach
	public void setUp() {
		// executed before each tests in this class
		// use to prepare stuff and dependencies
		dummy = new DummyClassForTests();

		System.out.println("setUpd");
	}

	@AfterEach
	public void tearDown() {
		// executed after each test in this class
		// use to clean up stuff and dependencies

		System.out.println("tearDown");
	}

	// same test as below but now uses HAMCREST matcher to improve readability,
	// some details on HAMCREST are given in the testing slides. Such matchers can
	// be used in jUnit 4 and 5
	@Test
	public void Division_withTwoInteger_shouldNotRound() {

		int result = dummy.div(2, 2);

		// the "is(1)" part is a HAMCREST matcher, compare with the additional test
		// given below
		// using such HAMCREST matchers improves readability as it can not be read just
		// like a sentence
		// i.e, Assert.assertThat(result, is(1)) can be read as: assert that result is 1
		assertThat(result, is(1));

		// there are more matchers, just some examples which show how easily they can be
		// read
		assertThat(result, is(equalTo(1)));
		assertThat(result, is(not(equalTo(2))));
	}

	@Test
	public void Division_withTwoInteger_shouldRoundToLowerResult() {

		int result = dummy.div(1, 2);

		Assertions.assertEquals(0, result, "Expected zero when dividing one by two");
	}

	// repeat this test a given number of times (here two times). Could be
	// interesting to test the map generation
	// such that it can generate like ten maps in a row without generating an island
	@RepeatedTest(value = 2)
	public void Division_withTwoInteger_shouldBehaveEquallyEachTime() {

		int result = dummy.div(1, 2);

		Assertions.assertEquals(0, result, "Expected zero when dividing one by two");
	}

	// you can even expect that a specific exception is thrown to test failure cases
	// too. Below is a classic example of a negative test.
	// For your own negative tests focus on testing your own custom exceptions to verify your
	// error handling logic
	@Test
	public void Division_withZero_shouldThrowException() {
		// you should not only test for the good cases, but also the bad ones
		// e.g., is an expected exception thrown when required?

		// code that will be executed during the test
		Executable testCode = () -> dummy.div(1, 0);

		// execution of the code and the expected exception
		Assertions.assertThrows(ArithmeticException.class, testCode,
				"We expected a exception because of the division by zero, but it was not thrown");
	}

	// data-driven tests execute the same test logic with different input/output
	// data to speed up the testing of different scenarios
	// ParameterizedTest will only work in jUnit5 but not in jUnit4 - but both
	// examples here implement the same functionality
	// this is only one example of data-driven tests in jUnit5. There are even
	// cooler ones
	// where you can supply the data by method calls using @MethodSource, see below
	@ParameterizedTest
	@CsvSource({ "1,1,1", "2,2,1" }) // simple comma separated values, i.e. CSV - will be inserted into the method
										// parameters
	public void Division_withParametervalues_shouldProduceExpectedResult(int a, int b, int expected) {
		int result = dummy.div(a, b);

		Assertions.assertEquals(expected, result,
				MessageFormat.format("Expected {0} when dividing {1} by {2}", expected, a, b));
	}

	// this ParameterizedTest gets its data from a method, i.e., you can supply it
	// with your own custom objects
	// in the @MethodSource annotation, we give the name of the method where the
	// data is loaded from. 
	// Recommended approach for data driven tests as it provides more flexibility and better object orientation.
	@ParameterizedTest
	@MethodSource("createDataForParameterizedExampleTest")
	public void Division_withParametervalues_shouldProduceExpectedResult(ExemplaryDataObject divData, int expected) {
		int result = dummy.div(divData.getFirst(), divData.getSecond());

		Assertions.assertEquals(expected, result, MessageFormat.format("Expected {0} when dividing {1} by {2}",
				expected, divData.getFirst(), divData.getSecond()));
	}

	// data source for the ParameterizedTest given above must be static
	// as we refer to this method by the @MethodSource annotation
	// Eclipse does not detect that its used any maybe displays a related warning -
	// which can be ignored.
	private static Stream<Arguments> createDataForParameterizedExampleTest() {
		return Stream.of(Arguments.of(new ExemplaryDataObject(1, 1), 1),
				Arguments.of(new ExemplaryDataObject(2, 2), 1));
	}

	@Test
	@Disabled("For teaching purposes")
	public void ignoredTest() {
		// you can ignore individual tests, don't forget to provide a reason why it was
		// ignored.
		// In addition, this should only be a rare temporary measure during active development to handle some tool chains.
		// So when finalizing a feature a) enable all tests again and b) if tests they fail fix the them or the code they are testing.
	}
}
