/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB30/SessionLocal.java to edit this template
 */
package session;

import entity.Event;
import entity.Registration;
import java.util.List;
import javax.ejb.Local;
import javax.persistence.NoResultException;

/**
 *
 * @author elizabeth
 */
@Local
public interface EventSessionLocal {
    public List<Event> searchEvents(String name);
    public List<Event> searchCreatedEvents(Long personId);
    public Event getEvent(Long eId) throws NoResultException;
    public void createEvent(Event e);
    public Event createEvent(Event e, Long personId);
    public void updateEvent(Event e) throws NoResultException;
    public void addRegistration(Long eventId, Registration r) throws NoResultException;
    public Registration addRegistration(Long personId,Long eventId) throws NoResultException;
    public void deleteRegistration(Long registartionId) throws NoResultException;
    public void deleteEvent(Long eventId) throws NoResultException;
    public List<Registration> getRegistrations(Long personId) throws NoResultException;
    public boolean checkUserAlreadyRegisteredForEvent(Long eventId,Long personId) throws NoResultException;
    public boolean checkEventRegistrationAbility(Long eventId) throws NoResultException;
    public boolean checkUnregistrationAbility(Long rId) throws NoResultException;
    public List<Registration> getRegistrationsForOneEvent(Long eventId) throws NoResultException;
    public void markUserAsPresent(Long personId, Long registrationId) throws NoResultException;
    public void unmarkUserAsPresent(Long personId, Long registrationId) throws NoResultException;
    public void changeAttendance(Long personId, Long registrationId) throws NoResultException;
    public Long getEventByRegistrationId(Long rId) throws NoResultException;

    public List<Event> searchEventsByLocation(String searchString);

    public List<Event> searchEventsByDescription(String searchString);

    public List<Event> searchCreatedEventsByTitle(Long personId, String searchString);

    public List<Event> searchCreatedEventsByLocation(Long personId, String searchString);

    public List<Event> searchCreatedEventsByDescription(Long personId, String searchString);
}
