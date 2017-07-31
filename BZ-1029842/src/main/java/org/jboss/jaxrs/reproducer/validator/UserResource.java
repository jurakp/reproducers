package org.jboss.jaxrs.reproducer.validator;

import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/user")
public interface UserResource {
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    User register(@NotNull @FormParam("email") String email, @FormParam("password") String password)
            throws EmailAleadyRegisteredException;
}