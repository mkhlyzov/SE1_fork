package test.dummy;

import java.util.List;

//used to show a more complex and realistic usage of mocks - simulates a database dependency
//a similar complexity should be used throughout the assignment to get a useful use of Mockito.
public interface IUserRepository {

	public void storeUsername(String username);
	
	public List<String> readUsernames();
}
