package org.jboss.jaxrs.reproducer.validator;

import javax.ejb.Stateless;

@Stateless
public class UserResourceImpl implements UserResource {
        
	public User register(String email,  String password)
			throws EmailAleadyRegisteredException {
		return new User(email, password);
	}
	
}
