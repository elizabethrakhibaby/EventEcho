/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package managedbean;

import static com.sun.xml.ws.spi.db.BindingContextFactory.LOGGER;
import entity.Event;
import entity.Registration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
//import javax.enterprise.context.RequestScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import session.EventSessionLocal;
import session.PersonSessionLocal;
import javax.faces.view.ViewScoped;

/**
 *
 * @author elizabeth
 */
@Named(value = "eventManagedBean")
@ViewScoped
public class EventManagedBean implements Serializable {

    @EJB
    private EventSessionLocal eventSessionLocal;
    @EJB
    private PersonSessionLocal personSessionLocal;

    private String eventTitle;
    private Date date;
    private String location;
    private String description;
    private Date deadline;
    private List<Event> createdEventsForOneUser;
    private List<Event> allEventsInDB;
    private List<Registration> registrationsForOneUser;
    private List<Registration> allRegistrationsInDB;
    private List<Registration> attendees;
    private List<Registration> absentees;

    private String searchType = "EVENTTITLE";
    private String searchString;

    @Inject
    private AuthenticationManagedBean authenticationManagedBean;

    private Long eventId;
    private Event selectedEvent;

    private Registration selectedRegistration;

    /**
     * Creates a new instance of EventManagedBean
     */
    public EventManagedBean() {
    }

    @PostConstruct
    public void init() {
        //createdEventsForOneUser = eventSessionLocal.searchCreatedEvents(authenticationManagedBean.getPersonId());
        //llEventsInDB = eventSessionLocal.searchEvents(null); //no specific eventTitle being passed!
        registrationsForOneUser = eventSessionLocal.getRegistrations(authenticationManagedBean.getPersonId());
        allRegistrationsInDB = eventSessionLocal.getRegistrations(null);

        if (searchString == null || searchString.equals("")) {
            allEventsInDB = eventSessionLocal.searchEvents(null); //no specific eventTitle being passed!
            createdEventsForOneUser = eventSessionLocal.searchCreatedEvents(authenticationManagedBean.getPersonId());
        } else {
            switch (searchType) {
                case "EVENTTITLE":
                    allEventsInDB = eventSessionLocal.searchEvents(searchString); //no specific eventTitle being passed!
                    createdEventsForOneUser = eventSessionLocal.searchCreatedEventsByTitle(authenticationManagedBean.getPersonId(), searchString);
                    break;
                case "EVENTLOCATION": {
                    allEventsInDB = eventSessionLocal.searchEventsByLocation(searchString);
                    createdEventsForOneUser = eventSessionLocal.searchCreatedEventsByLocation(authenticationManagedBean.getPersonId(), searchString);
                    break;
                }
                case "EVENTDESCRIPTION": {
                    allEventsInDB = eventSessionLocal.searchEventsByDescription(searchString);
                    createdEventsForOneUser = eventSessionLocal.searchCreatedEventsByDescription(authenticationManagedBean.getPersonId(), searchString);
                    break;
                }
                default:
                    break;
            }
        }
        //trial
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext()
                .getRequestParameterMap();
        if (params.containsKey("eventId")) {
            getA();
        }
    } //end init

    public void getA() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext()
                .getRequestParameterMap();
        String eIdStr = params.get("eventId");
        Long eId = Long.parseLong(eIdStr);

        try {
            Event sEvent = eventSessionLocal.getEvent(eId);
            List<Registration> allR = sEvent.getRegistrationsForEvent();
            attendees = new ArrayList<Registration>();
            absentees = new ArrayList<Registration>();
            for (Registration r : allR) {
                if (r.isAttendanceStatus() == true) {
                    attendees.add(r);
                } else {
                    absentees.add(r);
                }
                //LOGGER.log(Level.INFO, "um the size is" + attendees.size());
            }

        } catch (Exception e) {
            //show with an error icon
            //context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete event"));
            return;
        }

        //context.addMessage(null, new FacesMessage("Success", "Successfully deleted event"));

    }

    public void handleSearch() {
        init();
    } //end handleSearch

    public void getA(Long eId) {

        Event sEvent = eventSessionLocal.getEvent(eId);
        List<Registration> allR = sEvent.getRegistrationsForEvent();
        attendees = new ArrayList<Registration>();
        absentees = new ArrayList<Registration>();
        for (Registration r : allR) {
            if (r.isAttendanceStatus() == true) {
                attendees.add(r);
            } else {
                absentees.add(r);
            }
            //LOGGER.log(Level.INFO, "um the size is" + attendees.size());
        }

    }

    public void addEvent(ActionEvent evt) {
        Event event = new Event();
        event.setEventTitle(eventTitle);
        event.setDate(date);
        event.setLocation(location);
        event.setDescription(description);
        event.setDeadline(deadline);
        event.setCreator(personSessionLocal.getPerson(authenticationManagedBean.getPersonId()));
        event.setRegistrationsForEvent(new ArrayList<Registration>());
        eventSessionLocal.createEvent(event);
        init();
    }

    public void deleteEvent() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext()
                .getRequestParameterMap();
        String eIdStr = params.get("eventId");
        Long eId = Long.parseLong(eIdStr);

        try {
            eventSessionLocal.deleteEvent(eId);
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete event"));
            return;
        }

        context.addMessage(null, new FacesMessage("Success", "Successfully deleted event"));
        init();

    } //end deleteEvent

    public void createRegistration(Long eventId) {
        FacesContext context = FacesContext.getCurrentInstance();

        Long pId = authenticationManagedBean.getPersonId();
        if (userAlreadyRegisteredForEvent(eventId, pId)) {
            // User is already registered for the event
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Already registered for this event."));
            return;
        }
        Event event = eventSessionLocal.getEvent(eventId);
        Date currentDate = new Date();
        Date deadlineToRegisterForEvent = event.getDeadline();

        if (currentDate.after(deadlineToRegisterForEvent)) {
            // Current date is past the deadline so cannot register!
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Current date is past the deadline for registering for the event."));
            return;
        }

        try {
            Registration r = new Registration();
            r.setRegistrationDate(new Date());
            r.setRegisteredPerson(personSessionLocal.getPerson(pId));
            eventSessionLocal.addRegistration(eventId, r);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Registered successfully."));
            init();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to register for event"));
            return;
        }
        init();
    }

    private boolean userAlreadyRegisteredForEvent(Long eventId, Long personId) {
        return eventSessionLocal.checkUserAlreadyRegisteredForEvent(eventId, personId);
    }

    public void deleteRegistration() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext()
                .getRequestParameterMap();
        String rIdStr = params.get("registrationId");
        Long rId = Long.parseLong(rIdStr);

        try {
            eventSessionLocal.deleteRegistration(rId);
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete registration"));
            return;
        }
        context.addMessage(null, new FacesMessage("Success", "Successfully deleted registration"));
        init();

    } //end deleteRegistration

    public void markAsPresent() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext()
                .getRequestParameterMap();
        String rIdStr = params.get("registrationId");
        Long rId = Long.parseLong(rIdStr);

        try {
            eventSessionLocal.markUserAsPresent(authenticationManagedBean.getPersonId(), rId);
            Long eventId = eventSessionLocal.getEventByRegistrationId(rId);
            getA(eventId);
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to mark registartion as present"));
            return;
        }

        context.addMessage(null, new FacesMessage("Success", "Successfully marked registartion as presen"));
        init();

    }

    public void markAsAbsent() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext()
                .getRequestParameterMap();
        String rIdStr = params.get("registrationId");
        Long rId = Long.parseLong(rIdStr);

        try {
            eventSessionLocal.unmarkUserAsPresent(authenticationManagedBean.getPersonId(), rId);
            Long eventId = eventSessionLocal.getEventByRegistrationId(rId);
            getA(eventId);
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to mark registartion as present"));
            return;
        }

        context.addMessage(null, new FacesMessage("Success", "Successfully marked registartion as presen"));
        init();

    }

    public String getRegistrationsDebugString() {
        if (selectedEvent != null && selectedEvent.getRegistrationsForEvent() != null) {
            String debugInfo = "Total registrations: " + selectedEvent.getRegistrationsForEvent().size();
            LOGGER.info(debugInfo); // Log to server console
            return debugInfo; // Return for display in XHTML
        } else {
            String message = "No event selected or no registrations found";
            LOGGER.info(message); // Log to server console
            return message; // Return for display in XHTML
        }
    }

    //this is for editEvent page!!!
    public void loadSelectedEvent() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            this.selectedEvent = eventSessionLocal.getEvent(eventId);
            eventTitle = this.selectedEvent.getEventTitle();
            location = this.selectedEvent.getLocation();
            description = this.selectedEvent.getDescription();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to load event"));
        }
    } //end loadSelectedEvent

    public void updateEvent(ActionEvent evt) {
        FacesContext context = FacesContext.getCurrentInstance();
        selectedEvent.setEventTitle(eventTitle);
        selectedEvent.setLocation(location);
        selectedEvent.setDescription(description);

        try {
            eventSessionLocal.updateEvent(selectedEvent);
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update event"));
            return;
        }

        //need to make sure reinitialize the events collection
        init();
        context.addMessage(null, new FacesMessage("Success", "Successfully updated event"));
    } //end updateCustomer
    //this is for editEvent page!!! >> end!

    public List<Registration> getRegistrationsForOneEvent(Long eventId) {
        return new ArrayList<Registration>();
    }

    public List<Event> getCreatedEventsForOneUser() {
        return createdEventsForOneUser;
    }

    public void setCreatedEventsForOneUser(List<Event> createdEventsForOneUser) {
        this.createdEventsForOneUser = createdEventsForOneUser;
    }

    public List<Event> getAllEventsInDB() {
        return allEventsInDB;
    }

    public void setAllEventsInDB(List<Event> allEventsInDB) {
        this.allEventsInDB = allEventsInDB;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Event getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public List<Registration> getRegistrationsForOneUser() {
        return registrationsForOneUser;
    }

    public void setRegistrationsForOneUser(List<Registration> registrationsForOneUser) {
        this.registrationsForOneUser = registrationsForOneUser;
    }

    public List<Registration> getAllRegistrationsInDB() {
        return allRegistrationsInDB;
    }

    public void setAllRegistrationsInDB(List<Registration> allRegistrationsInDB) {
        this.allRegistrationsInDB = allRegistrationsInDB;
    }

    public List<Registration> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<Registration> attendees) {
        this.attendees = attendees;
    }

    public List<Registration> getAbsentees() {
        return absentees;
    }

    public void setAbsentees(List<Registration> absentees) {
        this.absentees = absentees;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public Registration getSelectedRegistration() {
        return selectedRegistration;
    }

    public void setSelectedRegistration(Registration selectedRegistration) {
        this.selectedRegistration = selectedRegistration;
    }

}
