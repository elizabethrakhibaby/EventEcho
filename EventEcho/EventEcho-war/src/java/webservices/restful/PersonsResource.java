/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package webservices.restful;

import entity.Event;
import entity.Person;
import entity.Registration;
import java.security.Principal;
import java.util.List;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import session.PersonSessionLocal;

/**
 *
 * @author elizabeth
 */
@Path("persons")
public class PersonsResource {

    @EJB
    private PersonSessionLocal personSessionLocal;

//    @GET
//    @Secured
//    @Path("/me")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getMyProfile(@Context SecurityContext securityContext) {
//        Principal principal = securityContext.getUserPrincipal();
//        String userId = principal.getName();
//
//        //from this userId, we can then get the data from the session bean accordingly
//        System.out.println("userId : " + userId);
//        return Response.status(200).entity(
//                null
//        ).type(MediaType.APPLICATION_JSON).build();
//    }
    @GET
    @Secured
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Person getMyProfile(@Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        String userId = principal.getName();
        Long personId = Long.parseLong(userId);
        System.out.println("person id is " + personId);
        Person person = personSessionLocal.getPerson(personId);
        Person newPerson = newPersonCyclic(person);
        return newPerson;
    }

    public Person newPersonCyclic(Person oldP) {
        // Break the back-reference from Person to their Registrations
        oldP.setRegistrationsForPerson(null);

        // Get and check created events
        List<Event> createdEvents = oldP.getCreatedEvents();
        if (createdEvents != null) {
            for (Event e : createdEvents) {
                // Break the back-reference from Event to its creator
                e.setCreator(null);

                // Optionally break cyclic references inside each Event
                List<Registration> registrations = e.getRegistrationsForEvent();
                if (registrations != null) {
                    for (Registration r : registrations) {
                        // Break the back-reference from Registration to Event
                        r.setEvent(null);
                        // Break the back-reference from Registration to Person
                        r.setRegisteredPerson(null);
                    }
                }
            }
        }

        return oldP;
    }

    @Secured
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editPerson(@Context SecurityContext securityContext, Person person) {
        Principal principal = securityContext.getUserPrincipal();
        String userId = principal.getName();
        Long personId = Long.parseLong(userId);
        System.out.println("userid is" + personId);
        person.setPersonId(personId);
        try {
            personSessionLocal.updatePerson(person);
            return Response.status(204).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end editPerson

    @Path("/createNewPerson")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Person createPerson(Person p1) {

        Person person = personSessionLocal.createPerson(p1);
        Person newPerson = newPersonCyclic(person);
        return newPerson;
    } //end createPerson

}
