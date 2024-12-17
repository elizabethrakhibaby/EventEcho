/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB30/SessionLocal.java to edit this template
 */
package session;

import entity.Person;
import javax.ejb.Local;
import javax.persistence.NoResultException;

/**
 *
 * @author elizabeth
 */
@Local
public interface PersonSessionLocal {
    //public void createPerson(Person p);
    public Person createPerson(Person p);
    public Person validateUser(String username, String password);
    public Person getPerson(Long pId) throws NoResultException;
    public void updatePerson(Person p) throws NoResultException;
}
