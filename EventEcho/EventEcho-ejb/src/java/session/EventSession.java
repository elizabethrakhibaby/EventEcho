/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB30/StatelessEjbClass.java to edit this template
 */
package session;

import entity.Event;
import entity.Person;
import entity.Registration;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author elizabeth
 */
@Stateless
public class EventSession implements EventSessionLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private PersonSessionLocal personSessionLocal;

    public void persist(Object object) {
        em.persist(object);
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    @Override
    public List<Event> searchEvents(String eventTitle) {
        Query q;
        if (eventTitle != null) {
            q = em.createQuery("SELECT e FROM Event e WHERE "
                    + "LOWER(e.eventTitle) LIKE :eventTitle");
            q.setParameter("eventTitle", "%" + eventTitle.toLowerCase() + "%");
        } else {
            q = em.createQuery("SELECT e FROM Event e");
        }

        return q.getResultList();
    } //end searchEvents

    @Override
    public List<Event> searchCreatedEvents(Long personId) {
        Query q;
        if (personId != null) {
            q = em.createQuery("SELECT e FROM Event e WHERE e.creator = :creator");
            q.setParameter("creator", personSessionLocal.getPerson(personId));
            return q.getResultList();
        } else {
            return null;
        }
    }

    @Override
    public Event getEvent(Long eId) throws NoResultException {
        Event event = em.find(Event.class, eId);

        if (event != null) {
            return event;
        } else {
            throw new NoResultException("Event not found");
        }
    }

    //wrong
    @Override
    public void createEvent(Event e) {
        em.persist(e);
    }

    @Override
    public Event createEvent(Event e, Long personId) {
        System.out.println("in session bean createEvent(e,personId) :" + e.getEventTitle());
        System.out.println("in session bean createEvent(e,personId) :" + personId);
        Person creator = em.find(Person.class, personId);
        e.setCreator(creator);
        creator.getCreatedEvents().add(e);
        em.persist(e);
        return e;
    }

    @Override
    public void updateEvent(Event e) throws NoResultException {
        Event oldE = getEvent(e.getEventId());

        oldE.setEventTitle(e.getEventTitle());
        oldE.setDate(e.getDate());
        oldE.setLocation(e.getLocation());
        oldE.setDescription(e.getDescription());
        oldE.setDeadline(e.getDeadline());
        oldE.setCreator(e.getCreator());
        oldE.setRegistrationsForEvent(e.getRegistrationsForEvent());

    } //end updateEvent

    @Override
    public void addRegistration(Long eventId, Registration r) throws NoResultException {
        Event event = getEvent(eventId);
        r.setEvent(event);
        em.persist(r);
        event.getRegistrationsForEvent().add(r);
    }

    @Override
    public Registration addRegistration(Long personId, Long eventId) throws NoResultException {
        Event event = getEvent(eventId);
        Person person = em.find(Person.class, personId);
        Registration r = new Registration();
        r.setRegistrationDate(new Date());
        r.setAttendanceStatus(false);
        r.setRegisteredPerson(person);
        r.setEvent(event);
        em.persist(r);
        person.getRegistrationsForPerson().add(r);
        event.getRegistrationsForEvent().add(r);
        return r;
    }

    @Override
    public void deleteRegistration(Long registartionId) throws NoResultException {
        Registration registrationToBeDeleted = em.find(Registration.class, registartionId);

        if (registrationToBeDeleted != null) {
            registrationToBeDeleted.getEvent().getRegistrationsForEvent().remove(registrationToBeDeleted);
            registrationToBeDeleted.getRegisteredPerson().getRegistrationsForPerson().remove(registrationToBeDeleted);
            em.remove(registrationToBeDeleted);
        } else {
            throw new NoResultException("Not registration found");
        }
    }

    @Override
    public void deleteEvent(Long eventId) throws NoResultException {
        //LOGGER.log(Level.INFO, "hi xinyu the string profilePicture is" + eventId);

        Event eventToBeDeleted = getEvent(eventId);
        if (eventToBeDeleted != null) {
            Person creator = eventToBeDeleted.getCreator();
            creator.getCreatedEvents().remove(eventToBeDeleted);
            em.merge(creator);
            List<Registration> registrationsToBeDeletedAlso = eventToBeDeleted.getRegistrationsForEvent();
            for (Registration r : registrationsToBeDeletedAlso) {
                Person personWhoRegistered = r.getRegisteredPerson();
                personWhoRegistered.getRegistrationsForPerson().remove(r);
                em.remove(r);
                em.merge(personWhoRegistered);
            }
            em.remove(eventToBeDeleted);

        } else {
            throw new NoResultException("No event to be deleted found");
        }
    }

    @Override
    public List<Registration> getRegistrations(Long personId) throws NoResultException {
        //getRegistrations(null) for all registrations
        //getRegistrations(personId) for registrations for just one person!
        Query q;
        if (personId != null) {
            q = em.createQuery("SELECT r FROM Registration r where r.registeredPerson = :registeredPerson");
            q.setParameter("registeredPerson", personSessionLocal.getPerson(personId));
            return q.getResultList();
        } else {
            q = em.createQuery("SELECT r FROM Registration r");
            return q.getResultList();
        }
    }

    @Override
    public List<Registration> getRegistrationsForOneEvent(Long eventId) throws NoResultException {
        Query q;
        if (eventId != null) {
            Event event = getEvent(eventId);
            q = em.createQuery("SELECT r FROM Registration r where r.event = :event");
            q.setParameter("event", event);
            return q.getResultList();
        } else {
            throw new NoResultException("Event not found");
        }
    }

    @Override
    public boolean checkUserAlreadyRegisteredForEvent(Long eventId, Long personId) throws NoResultException {
        Event event = getEvent(eventId);
        if (event == null) {
            // Handle the case where event is not found
            throw new NoResultException("Event not found");
        }

        Person person = personSessionLocal.getPerson(personId);
        if (person == null) {
            // Handle the case where person is not found
            throw new NoResultException("Person not found");
        }

        List<Registration> registrationsForEvent = event.getRegistrationsForEvent();
        for (Registration registration : registrationsForEvent) {
            Person p = registration.getRegisteredPerson();
            if (p != null && p.equals(person)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkEventRegistrationAbility(Long eventId) throws NoResultException {
        Event event = getEvent(eventId);
        if (event == null) {
            // Handle the case where event is not found
            throw new NoResultException("Event not found");
        }
        Date deadline = event.getDeadline();
        Date todayDate = new Date(); // Current date and time
// Convert deadline and todayDate to Calendar to extract year, month, and day
        Calendar deadlineCal = Calendar.getInstance();
        deadlineCal.setTime(deadline);
        int deadlineYear = deadlineCal.get(Calendar.YEAR);
        int deadlineMonth = deadlineCal.get(Calendar.MONTH);
        int deadlineDay = deadlineCal.get(Calendar.DAY_OF_MONTH);

        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(todayDate);
        int todayYear = todayCal.get(Calendar.YEAR);
        int todayMonth = todayCal.get(Calendar.MONTH);
        int todayDay = todayCal.get(Calendar.DAY_OF_MONTH);

// Compare year, month, and day components
        if (deadlineYear > todayYear
                || (deadlineYear == todayYear && deadlineMonth > todayMonth)
                || (deadlineYear == todayYear && deadlineMonth == todayMonth && deadlineDay >= todayDay)) {
            // This will be true if deadline is today or in the future
            System.out.println("hi");
            System.out.println(deadline);
            System.out.println(todayDate);
            System.out.println("The deadline has not passed yet.");
            // Hence you CAN register
            return true;
        } else {
            System.out.println("hi");
            System.out.println(deadline);
            System.out.println(todayDate);
            System.out.println("The deadline has already passed.");
            // Hence you CANNOT register
            return false;
        }

    }

    @Override
    public boolean checkUnregistrationAbility(Long rId) throws NoResultException {
        Registration r = em.find(Registration.class, rId);
        if (r == null) {
            // Handle the case where event is not found
            throw new NoResultException("Registration not found");
        }
        Event event = r.getEvent();
        Date dateOfEvent = event.getDate();
        Date todayDate = new Date();
        if (!todayDate.after(dateOfEvent)) {
            // This will be true if todayDate is before dateOfEvent
            System.out.println("todayDate is before dateOfEvent");
            // Hence you CAN unregister
            return true;
        } else {
            System.out.println("todayDate is after dateOfEvent");
            // Hence you CANNOT unregister
            return false;
        }

    }

    @Override
    public void markUserAsPresent(Long personId, Long registrationId) throws NoResultException {
        Registration registration = em.find(Registration.class, registrationId);
        Person person = em.find(Person.class, personId);
        if (registration == null) {
            throw new NoResultException("Registration not found");
        } else if (person == null) {
            throw new NoResultException("Person not found");
        }
        registration.setAttendanceStatus(true);

    }

    @Override
    public void unmarkUserAsPresent(Long personId, Long registrationId) throws NoResultException {
        Registration registration = em.find(Registration.class, registrationId);
        Person person = em.find(Person.class, personId);
        if (registration == null) {
            throw new NoResultException("Registration not found");
        } else if (person == null) {
            throw new NoResultException("Person not found");
        }
        registration.setAttendanceStatus(false);
    }

    public void changeAttendance(Long personId, Long registrationId) throws NoResultException {
        Registration registration = em.find(Registration.class, registrationId);
        if (registration == null) {
            throw new NoResultException("Registration not found");
        } else {
            boolean currentStatus = registration.isAttendanceStatus();
            if (currentStatus) {
                //user is currently marked as present
                unmarkUserAsPresent(personId, registrationId);
            } else {
                markUserAsPresent(personId, registrationId);
            }
        }
    }

    @Override
    public Long getEventByRegistrationId(Long rId) {
        Registration registration = em.find(Registration.class, rId);
        if (registration == null) {
            throw new NoResultException("Registration not found");
        }
        return registration.getEvent().getEventId();
    }

    @Override
    public List<Event> searchEventsByLocation(String searchString) {
        if (searchString != null) {
            Query q = em.createQuery("SELECT e FROM Event e WHERE LOWER(e.location) LIKE :searchString");
            q.setParameter("searchString", "%" + searchString.toLowerCase() + "%");
            return q.getResultList();
        } else {
            return Collections.emptyList(); // Return an empty list if searchString is null
        }
    }

    @Override
    public List<Event> searchEventsByDescription(String searchString) {
        if (searchString != null) {
            Query q = em.createQuery("SELECT e FROM Event e WHERE LOWER(e.description) LIKE :searchString");
            q.setParameter("searchString", "%" + searchString.toLowerCase() + "%");
            return q.getResultList();
        } else {
            return Collections.emptyList(); // Return an empty list if searchString is null
        }
    }

    @Override
    public List<Event> searchCreatedEventsByTitle(Long personId, String searchString) {
        if (personId != null && searchString != null) {
            Query q = em.createQuery("SELECT e FROM Event e WHERE e.creator = :creator AND LOWER(e.eventTitle) LIKE :searchString");
            q.setParameter("creator", personSessionLocal.getPerson(personId));
            q.setParameter("searchString", "%" + searchString.toLowerCase() + "%");
            return q.getResultList();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Event> searchCreatedEventsByLocation(Long personId, String searchString) {
        if (personId != null && searchString != null) {
            Query q = em.createQuery("SELECT e FROM Event e WHERE e.creator = :creator AND LOWER(e.location) LIKE :searchString");
            q.setParameter("creator", personSessionLocal.getPerson(personId));
            q.setParameter("searchString", "%" + searchString.toLowerCase() + "%");
            return q.getResultList();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Event> searchCreatedEventsByDescription(Long personId, String searchString) {
        if (personId != null && searchString != null) {
            Query q = em.createQuery("SELECT e FROM Event e WHERE e.creator = :creator AND LOWER(e.description) LIKE :searchString");
            q.setParameter("creator", personSessionLocal.getPerson(personId));
            q.setParameter("searchString", "%" + searchString.toLowerCase() + "%");
            return q.getResultList();
        } else {
            return Collections.emptyList();
        }
    }

}
