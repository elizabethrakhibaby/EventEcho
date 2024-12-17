/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB30/StatelessEjbClass.java to edit this template
 */
package session;

import entity.Person;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author elizabeth
 */
@Stateless
public class PersonSession implements PersonSessionLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Person getPerson(Long pId) throws NoResultException {
        Person person = em.find(Person.class, pId);

        if (person != null) {
            return person;
        } else {
            throw new NoResultException("Person not found");
        }
    }

//    @Override
//    public void createPerson(Person p) {
//        em.persist(p);
//    }
    @Override
    public Person createPerson(Person p) {
        em.persist(p);
        return p;
    }

    @Override
    public Person validateUser(String username, String password) {
        try {
            Person person = (Person) em.createQuery("SELECT p FROM Person p WHERE p.username = :username AND p.password = :password")
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();
            return person;
        } catch (NoResultException e) {
            // If there is no result
            return null;
        }
    }

    @Override
    public void updatePerson(Person p) throws NoResultException {
        Person oldP = getPerson(p.getPersonId());

        //oldP.setUsername(p.getUsername());
        //oldP.setPassword(p.getPassword());
        oldP.setName(p.getName());
        oldP.setProfilePicture(p.getProfilePicture());
        oldP.setContactNumber(p.getContactNumber());
        oldP.setEmail(p.getEmail());
        //oldP.setCreatedEvents(p.getCreatedEvents());
        //oldP.setRegistrationsForPerson(p.getRegistrationsForPerson());
    }

}
