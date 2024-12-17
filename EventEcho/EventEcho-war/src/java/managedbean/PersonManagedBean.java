/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package managedbean;

import static com.sun.xml.ws.spi.db.BindingContextFactory.LOGGER;
import entity.Event;
import entity.Person;
import entity.Registration;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import session.PersonSessionLocal;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.Part;

/**
 *
 * @author elizabeth
 */
@Named(value = "personManagedBean")
@ViewScoped
public class PersonManagedBean implements Serializable {

    @EJB
    private PersonSessionLocal personSessionLocal;
    private String username;
    private String password;
    private String name;
    //IM NOT PUTTING PROFILE PHOTO YET! RMB TO AD DIN LATER
    private String profilePicture;
    private String contactNumber;
    private String email;
    private List<Event> createdEvents;

    private List<Registration> registrationsForPerson;

    @Inject
    private AuthenticationManagedBean authenticationManagedBean;

    //used by profilePage.xhtml
    private Long pId;
    private Person selectedPerson;

    private Part uploadedFile;
    private String fileName = "";

    /**
     * Creates a new instance of PersonManagedBean
     */
    public PersonManagedBean() {
    }

    public void addPerson(ActionEvent evt) throws IOException {
        uploadPP();
        Person person = new Person();
        person.setUsername(username);
        person.setPassword(password);
        person.setName(name);
        person.setProfilePicture(profilePicture);
        person.setContactNumber(contactNumber);
        person.setEmail(email);
        personSessionLocal.createPerson(person);
    }

    public void loadSelectedPerson() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            selectedPerson = personSessionLocal.getPerson(authenticationManagedBean.getPersonId());
            username = this.selectedPerson.getUsername();
            password = this.selectedPerson.getPassword();
            name = this.selectedPerson.getName();
            profilePicture = this.selectedPerson.getProfilePicture();
            contactNumber = this.selectedPerson.getContactNumber();
            email = this.selectedPerson.getEmail();
            email = this.selectedPerson.getEmail();
            createdEvents = this.selectedPerson.getCreatedEvents();
            registrationsForPerson = this.selectedPerson.getRegistrationsForPerson();

        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to load customer"));
        }
    } //end loadSelectedPerson

    public void updatePerson(ActionEvent evt) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (selectedPerson == null) {
            LOGGER.log(Level.INFO, "sorry sis");
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Selected person is null"));
            return;
        }
        //selectedPerson.setUsername(username);
        //selectedPerson.setPassword(password);
        //console.log(name);
        selectedPerson.setName(name);
        selectedPerson.setContactNumber(contactNumber);
        selectedPerson.setEmail(email);
        //selectedPerson.setCreatedEvents(createdEvents);
        //selectedPerson.setRegistrationsForPerson(registrationsForPerson);

        try {
            personSessionLocal.updatePerson(selectedPerson);
            context.addMessage(null, new FacesMessage("Success", "Successfully updated profile page"));
        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update profile page"));
            return;
        }
    } //end updateCustomer

    public void testMethod() {
        LOGGER.log(Level.INFO, "Test method was called");
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Info", "Test method executed successfully"));
    }

    public void uploadPP() throws IOException {
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

        //get the deployment path
        String UPLOAD_DIRECTORY = ctx.getRealPath("/") + "upload/";
        System.out.println("#UPLOAD_DIRECTORY : " + UPLOAD_DIRECTORY);

        //debug purposes
        setFileName(Paths.get(uploadedFile.getSubmittedFileName()).getFileName().toString());
        System.out.println("filename: " + getFileName());
        //---------------------
        profilePicture = fileName;

        //replace existing file
        Path path = Paths.get(UPLOAD_DIRECTORY + getFileName());
        InputStream bytes = uploadedFile.getInputStream();
        Files.copy(bytes, path, StandardCopyOption.REPLACE_EXISTING);
    }
    
        public void uploadPPExistingPerson() throws IOException {
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

        //get the deployment path
        String UPLOAD_DIRECTORY = ctx.getRealPath("/") + "upload/";
        System.out.println("#UPLOAD_DIRECTORY : " + UPLOAD_DIRECTORY);

        //debug purposes
        setFileName(Paths.get(uploadedFile.getSubmittedFileName()).getFileName().toString());
        System.out.println("filename: " + getFileName());
        //---------------------
        profilePicture = fileName;

        //replace existing file
        Path path = Paths.get(UPLOAD_DIRECTORY + getFileName());
        InputStream bytes = uploadedFile.getInputStream();
        Files.copy(bytes, path, StandardCopyOption.REPLACE_EXISTING);

        Person p1 = personSessionLocal.getPerson(authenticationManagedBean.getPersonId());
        p1.setProfilePicture(profilePicture);
        personSessionLocal.updatePerson(p1);
        //loadSelectedPerson();
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Event> getCreatedEvents() {
        return createdEvents;
    }

    public void setCreatedEvents(List<Event> createdEvents) {
        this.createdEvents = createdEvents;
    }

    public List<Registration> getRegistrationsForPerson() {
        return registrationsForPerson;
    }

    public void setRegistrationsForPerson(List<Registration> registrationsForPerson) {
        this.registrationsForPerson = registrationsForPerson;
    }

    public Long getpId() {
        return pId;
    }

    public void setpId(Long pId) {
        this.pId = pId;
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
    
    

}
