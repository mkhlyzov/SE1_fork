package test.dummy;

import java.util.List;

//used to show a more complex and realistic usage of mocks - simulates a database dependency
//a similar complexity should be used throughout the assignment to get a reasonable and beneficial use of Mockito.
public class UserManager {

	private final IUserRepository repository;
	
	public UserManager(IUserRepository repository)
	{
		this.repository = repository;
	}
	
	public void addUser(String username) {
		this.repository.storeUsername(username);		
	}
	
	public List<String> readUsers() {
		return this.repository.readUsernames();		
	}
}
