package org.trpr.platform.batch.impl.spring.web;

public class User {
    public String firstname;

    public User() {
    }
    
    public User(String firstname) {
        this.firstname = firstname;
 
    }
    public String getFirstName()
    {
    	return this.firstname;
    	
    }
    public String setFirstName(String firstname)
    {
    	return this.firstname=firstname;
    	
    }
    
 
}