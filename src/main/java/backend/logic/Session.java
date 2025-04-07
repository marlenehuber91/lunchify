package backend.logic;

import backend.model.User;

public class Session { // created by AI
	private static User currentUser;
	
	public static void setCurrentUser (User user) {
		currentUser = user;
	}
	
	public static User getCurrentUser() {
		return currentUser;
	}
	
	public static void clear() {
		currentUser=null;
	}
}
