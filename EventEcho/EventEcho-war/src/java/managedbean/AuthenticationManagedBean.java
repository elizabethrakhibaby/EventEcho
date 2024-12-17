package managedbean;

import entity.Person;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.ejb.EJB;
import session.PersonSessionLocal;

@Named(value = "authenticationManagedBean")
@SessionScoped
public class AuthenticationManagedBean implements Serializable {

    @EJB
    private PersonSessionLocal personSessionLocal;

    private String username = null;
    private String password = null;
    private Long personId = -1L; // Initialize with -1L as a default value

    public AuthenticationManagedBean() {
    }

    // Existing fields and methods...
    public String login() {
        Person validatedUser = personSessionLocal.validateUser(username, password);

        if (validatedUser != null) {
            // Login successful
            personId = validatedUser.getPersonId();
            username = null; // Clear for security reasons
            password = null; // Clear for security reasons
            return "/index.xhtml?faces-redirect=true";
        } else {
            // Login failed
            username = null;
            password = null;
            personId = -1L;
            return "/login.xhtml?faces-redirect=true";
        }
    }

    public String logout() {
        username = null;
        password = null;
        personId = -1L;

        return "/login.xhtml?faces-redirect=true";
    } //end logout

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }
}
