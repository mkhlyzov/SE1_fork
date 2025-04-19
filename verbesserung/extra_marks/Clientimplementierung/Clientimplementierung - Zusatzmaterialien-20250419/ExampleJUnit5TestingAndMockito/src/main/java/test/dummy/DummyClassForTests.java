package test.dummy;

// we only use this class to get some behavior to test
// check out the src/test/java folder to get details on how to write unit tests with jUnit
public class DummyClassForTests implements IDummyClassForTests {

	@Override
	public int div(int a, int b) {
		return a / b;
	}
}
