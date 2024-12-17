/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package webservices.restful;

import entity.Registration;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import session.EventSessionLocal;

/**
 *
 * @author elizabeth
 */
@Path("registrations")
@RequestScoped
public class RegistrationsResource {

    @EJB
    private EventSessionLocal eventSessionLocal;

    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public List<Registration> getAllRegistrations(@Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        String personId = principal.getName();
        List<Registration> listOfRegistrations = eventSessionLocal.getRegistrations(Long.parseLong(personId));
        List<Registration> newListOfRegistrations = newListOfRegistrations(listOfRegistrations);
        return newListOfRegistrations;
    } //end getAllEvents

    public List<Registration> newListOfRegistrations(List<Registration> oldListOfRegistrations) {
        List<Registration> newListOfRegistrations = new ArrayList();
        for (Registration r : oldListOfRegistrations) {
            r.setEvent(null);
            r.setRegisteredPerson(null);
            newListOfRegistrations.add(r);
        }
        return newListOfRegistrations;
    }

}
