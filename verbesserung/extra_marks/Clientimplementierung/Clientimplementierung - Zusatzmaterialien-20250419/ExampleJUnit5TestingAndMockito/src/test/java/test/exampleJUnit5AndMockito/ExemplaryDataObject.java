package test.exampleJUnit5AndMockito;

// solely used to show that ParameterizedTests using @MethodSource can use custom objects too
// hence you can also use, e.g., your own data objects
public class ExemplaryDataObject {

	private final int first, second;

	public ExemplaryDataObject(int first, int second) {
		super();
		this.first = first;
		this.second = second;
	}

	public int getSecond() {
		return second;
	}

	public int getFirst() {
		return first;
	}
}
