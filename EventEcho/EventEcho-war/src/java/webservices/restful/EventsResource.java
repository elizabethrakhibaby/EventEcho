/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/WebServices/GenericResource.java to edit this template
 */
package webservices.restful;

import static com.sun.xml.ws.spi.db.BindingContextFactory.LOGGER;
import entity.Event;
import entity.Person;
import entity.Registration;
import java.security.Principal;
//import error.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import session.EventSessionLocal;
import session.PersonSessionLocal;

/**
 * REST Web Service
 *
 * @author elizabeth
 */
@Path("events")
@RequestScoped
public class EventsResource {

    @EJB
    private EventSessionLocal eventSessionLocal;
    @EJB
    private PersonSessionLocal personSessionLocal;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> getAllEvents() {
        List<Event> listOfEvents = eventSessionLocal.searchEvents(null);
        System.out.println(listOfEvents.size());
        List<Event> newListOfEvents = newListOfEvents(listOfEvents);
        return newListOfEvents;
    } //end getAllEvents

    @Secured
    @GET
    @Path("/myCreatedEvents")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> getAllCreatedEvents(@Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        String userId = principal.getName();
        Long personId = Long.parseLong(userId);
        System.out.println("HI RAKHI ");
        System.out.println("#userId: " + personId);
        List<Event> listOfEvents = eventSessionLocal.searchCreatedEvents(personId);
        List<Event> newListOfEvents = newListOfEvents(listOfEvents);
        return newListOfEvents;
    } //end getAllCreatedEvents

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editEvent(@PathParam("id") Long eId, Event event) {
        event.setEventId(eId);
        event.setCreator(eventSessionLocal.getEvent(eId).getCreator());
        event.setRegistrationsForEvent(eventSessionLocal.getEvent(eId).getRegistrationsForEvent());
        try {
            eventSessionLocal.updateEvent(event);
            return Response.status(204).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end editEvent

    public List<Event> newListOfEvents(List<Event> oldListOfEvents) {
        List<Event> newListOfEvents = new ArrayList<>();

        for (Event e : oldListOfEvents) {
            // Break the back-reference from Event to Person (creator)
            e.setCreator(null);

            // Iterate over the Registrations of each Event
            List<Registration> registrations = e.getRegistrationsForEvent();
            if (registrations != null) {
                for (Registration r : registrations) {
                    // Break the back-reference from Registration to Event
                    r.setEvent(null);

                    // Break the back-reference from Registration to Person (registeredPerson)
                    Person registeredPerson = r.getRegisteredPerson();
                    if (registeredPerson != null) {
                        registeredPerson.setRegistrationsForPerson(null);
                        registeredPerson.setCreatedEvents(null);
                        //r.setRegisteredPerson(null); // If you want to also sever this reference
                    }
                }
            }

            // Add the detached Event to the new list
            newListOfEvents.add(e);
        }
        return newListOfEvents;
    }

    public Event newEventCyclic(Event oldE) {
        // Break the back-reference from Event to Person (creator)
        oldE.setCreator(null);

        // Iterate over the Registrations of each Event
        List<Registration> registrations = oldE.getRegistrationsForEvent();
        if (registrations != null) {
            for (Registration r : registrations) {
                // Break the back-reference from Registration to Event
                r.setEvent(null);

                // Break the back-reference from Registration to Person (registeredPerson)
                Person registeredPerson = r.getRegisteredPerson();
                if (registeredPerson != null) {
                    registeredPerson.setRegistrationsForPerson(null);
                    registeredPerson.setCreatedEvents(null);
                    //r.setRegisteredPerson(null); // If you want to also sever this reference
                }
            }
        }
        return oldE;
    }
    
    

    /*
CYCLIC
     */
    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchAllEvents(@QueryParam("EVENTTITLE") String EVENTTITLE,
            @QueryParam("EVENTLOCATION") String EVENTLOCATION,
            @QueryParam("EVENTDESCRIPTION") String EVENTDESCRIPTION) {
        if (EVENTTITLE != null) {
            List<Event> results1
                    = eventSessionLocal.searchEvents(EVENTTITLE);
            List<Event> results = newListOfEvents(results1);
            GenericEntity<List<Event>> entity = new GenericEntity<List<Event>>(results) {
            };
            return Response.status(200).entity(
                    entity
            ).build();
        } else if (EVENTLOCATION != null) {
            List<Event> results1
                    = eventSessionLocal.searchEventsByLocation(EVENTLOCATION);
            List<Event> results = newListOfEvents(results1);
            GenericEntity<List<Event>> entity = new GenericEntity<List<Event>>(results) {
            };
            return Response.status(200).entity(
                    entity
            ).build();
        } else if (EVENTDESCRIPTION != null) {
            List<Event> results1
                    = eventSessionLocal.searchEventsByDescription(EVENTDESCRIPTION);
            List<Event> results = newListOfEvents(results1);
            GenericEntity<List<Event>> entity = new GenericEntity<List<Event>>(results) {
            };
            return Response.status(200).entity(
                    entity
            ).build();
        } else {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "No query conditions")
                    .build();
            return Response.status(400).entity(exception).build();
        }
    } //end searchAllEvents
    //this one need to pass in creator's personid... idk check with prof!!

    @Secured
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Event createEvent(@Context SecurityContext securityContext, Event e) {
        Principal principal = securityContext.getUserPrincipal();
        String personIdString = principal.getName();
        System.out.println("#Event :" + e.getEventTitle());
        System.out.println("#userId: " + personIdString);
        Long personId = Long.parseLong(personIdString);
        Event event = eventSessionLocal.createEvent(e, personId);
        Event newEvent = newEventCyclic(event);
        return newEvent;
    } //end createEvent

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvent(@PathParam("id") Long eId) {
        //System.out.println("hello elizabeth");

        try {
            Event event = eventSessionLocal.getEvent(eId);
            Event newEvent = newEventCyclic(event);
            return Response.status(200).entity(
                    newEvent
            ).type(MediaType.APPLICATION_JSON).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end getEvent

    @DELETE
    @Path("/{event_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteEvent(@PathParam("event_id") Long eId) {
        try {
            eventSessionLocal.deleteEvent(eId);
            LOGGER.log(Level.INFO, "hapy");
            return Response.status(204).build();
        } catch (NoResultException e) {
            LOGGER.log(Level.INFO, "sad");
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Event not found")
                    .build();
            return Response.status(404).entity(exception).build();
        }
    } //end deleteEvent

    @DELETE
    @Path("deleteRegistration/{rId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRegistration(@PathParam("rId") Long rId) {
        try {
            eventSessionLocal.deleteRegistration(rId);

            return Response.status(204).build();
        } catch (NoResultException e) {

            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Registration not found")
                    .build();
            return Response.status(404).entity(exception).build();
        }
    } //end deleteEvent

    @Secured
    @Path("/checkRegistration/{eventId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkUserAlreadyRegisteredForEvent(@Context SecurityContext securityContext, @PathParam("eventId") Long eventId) {
        Principal principal = securityContext.getUserPrincipal();
        String userId = principal.getName();
        Long personId = Long.parseLong(userId);
        try {
            boolean isRegistered = eventSessionLocal.checkUserAlreadyRegisteredForEvent(eventId, personId);
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("isRegistered", isRegistered)
                    .build();
            return Response.status(Response.Status.OK).entity(responseJson).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", e.getMessage())
                    .build();
            return Response.status(Response.Status.NOT_FOUND).entity(exception).build();
        }
    }

    @Path("/checkRegistrationAbility/{eventId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkEventRegistrationAbility(@PathParam("eventId") Long eventId) {

        try {
            boolean isEventAvailForRegistration = eventSessionLocal.checkEventRegistrationAbility(eventId);
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("isEventAvailForRegistration", isEventAvailForRegistration)
                    .build();
            return Response.status(Response.Status.OK).entity(responseJson).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", e.getMessage())
                    .build();
            return Response.status(Response.Status.NOT_FOUND).entity(exception).build();
        }
    }
    
    @Path("/checkUnregistrationAbility/{rId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkUnregistrationAbility(@PathParam("rId") Long rId) {

        try {
            boolean isRegistrationAvailForUnreg = eventSessionLocal.checkUnregistrationAbility(rId);
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("isRegistrationAvailForUnreg", isRegistrationAvailForUnreg)
                    .build();
            return Response.status(Response.Status.OK).entity(responseJson).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", e.getMessage())
                    .build();
            return Response.status(Response.Status.NOT_FOUND).entity(exception).build();
        }
    }

    @Secured
    @Path("/registerForOneEvent/{eventId}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Registration createRegistration(@Context SecurityContext securityContext, @PathParam("eventId") Long eventId) {
        Principal principal = securityContext.getUserPrincipal();
        String personIdString = principal.getName();
        System.out.println("#Event Id :" + eventId);
        System.out.println("#userId: " + personIdString);
        Long personId = Long.parseLong(personIdString);
        Registration r = eventSessionLocal.addRegistration(personId, eventId);
        System.out.println("hey there");
        System.out.println("registered date" + r.getRegistrationDate());
        Registration newRegistration = newRegistrationCyclic(r);
        return newRegistration;
    } //end createRegistartion

    public Registration newRegistrationCyclic(Registration oldR) {
        oldR.setRegisteredPerson(null);
        Event event = oldR.getEvent();
        event.setRegistrationsForEvent(null);
        Person creator = event.getCreator();
        creator.setCreatedEvents(null);
        creator.setRegistrationsForPerson(null);
        return oldR;

    }

    @Secured
    @GET
    @Path("/myRegistrations")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Registration> getMyRegistrations(@Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        String userId = principal.getName();
        Long personId = Long.parseLong(userId);
        System.out.println("HI PJA ");
        System.out.println("#userId: " + personId);
        List<Registration> listOfRegistrations = eventSessionLocal.getRegistrations(personId);
        List<Registration> newListOfRegistrations = newListOfRegistrations(listOfRegistrations);
        return newListOfRegistrations;
    } //end getMyRegistrations

    public List<Registration> newListOfRegistrations(List<Registration> oldListOfRegistrations) {
        List<Registration> newListOfRegistrations = new ArrayList();
        for (Registration oldR : oldListOfRegistrations) {
            oldR.setRegisteredPerson(null);
            Event event = oldR.getEvent();
            event.setRegistrationsForEvent(null);
            Person creator = event.getCreator();
            creator.setCreatedEvents(null);
            creator.setRegistrationsForPerson(null);
            newListOfRegistrations.add(oldR);
        }
        return newListOfRegistrations;
    }

    @PUT
    @Path("/changeAttendance/{personId}/{rId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeAttendance(@PathParam("personId") Long personId, @PathParam("rId") Long rId) {
        try {
            eventSessionLocal.changeAttendance(personId, rId);

            return Response.status(204).build();
        } catch (NoResultException e) {

            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Registration not found")
                    .build();
            return Response.status(404).entity(exception).build();
        }
    }

}
