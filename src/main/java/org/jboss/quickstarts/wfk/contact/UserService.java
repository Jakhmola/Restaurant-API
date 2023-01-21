package org.jboss.quickstarts.wfk.contact;

import org.jboss.quickstarts.wfk.area.Area;
import org.jboss.quickstarts.wfk.area.AreaService;
import org.jboss.quickstarts.wfk.area.InvalidAreaCodeException;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>This Service assumes the Control responsibility in the ECB pattern.</p>
 *
 * <p>The validation is done here so that it may be used by other Boundary Resources. Other Business Logic would go here
 * as well.</p>
 *
 * <p>There are no access modifiers on the methods, making them 'package' scope.  They should only be accessed by a
 * Boundary / Web Service class with public methods.</p>
 *
 *
 * @author Joshua Wilson
 * @see UserValidator
 * @see UserRepository
 */
//The @Dependent is the default scope is listed here so that you know what scope is being used.
@Dependent
public class UserService {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private UserValidator validator;

    @Inject
    private UserRepository crud;

    private ResteasyClient client;

    /**
     * <p>Create a new client which will be used for our outgoing REST client communication</p>
     */
    public UserService() {
        // Create client service instance to make REST requests to upstream service
        client = new ResteasyClientBuilder().build();
    }

    /**
     * <p>Returns a List of all persisted {@link User} objects, sorted alphabetically by name.<p/>
     *
     * @return List of User objects
     */
    List<User> findAllOrderedByName() {
        return crud.findAllOrderedByName();
    }

    /**
     * <p>Returns a single User object, specified by a Long id.<p/>
     *
     * @param id The id field of the User to be returned
     * @return The User with the specified id
     */
    User findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Returns a single User object, specified by a String email.</p>
     *
     * <p>If there is more than one User with the specified email, only the first encountered will be returned.<p/>
     *
     * @param email The email field of the User to be returned
     * @return The first User with the specified email
     */
    User findByEmail(String email) {
        return crud.findByEmail(email);
    }


    /**
     * <p>Writes the provided User object to the application database.<p/>
     *
     * <p>Validates the data in the provided User object using a {@link UserValidator} object.<p/>
     *
     * @param contact The User object to be written to the database using a {@link UserRepository} object
     * @return The User object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    User create(User user) throws ConstraintViolationException, ValidationException, Exception {
        log.info("UserService.create() - Creating " + user.getName());
        
        // Check to make sure the data fits with the parameters in the User model and passes validation.
        validator.validateUser(user);

        /*//Create client service instance to make REST requests to upstream service
        ResteasyWebTarget target = client.target("http://ec2-18-119-125-232.us-east-2.compute.amazonaws.com/");
        AreaService service = target.proxy(AreaService.class);

        try {
            Area area = service.getAreaById(Integer.parseInt(contact.getPhoneNumber().substring(1, 4)));
            contact.setState(area.getState());
        } catch (ClientErrorException e) {
            if (e.getResponse().getStatusInfo() == Response.Status.NOT_FOUND) {
                throw new InvalidAreaCodeException("The area code provided does not exist", e);
            } else {
                throw e;
            }
        }*/

        // Write the user to the database.
        return crud.create(user);
    }

    /**
     * <p>Updates an existing User object in the application database with the provided User object.<p/>
     *
     * <p>Validates the data in the provided User object using a UserValidator object.<p/>
     *
     * @param user The User object to be passed as an update to the application database
     * @return The User object that has been successfully updated in the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    User update(User user) throws ConstraintViolationException, ValidationException, Exception {
        log.info("UserService.update() - Updating " + user.getName());
        
        // Check to make sure the data fits with the parameters in the User model and passes validation.
        validator.validateUser(user);

        // Set client target location and define the proxy API class
        /*ResteasyWebTarget target = client.target("http://ec2-18-119-125-232.us-east-2.compute.amazonaws.com/");
        AreaService service = target.proxy(AreaService.class);

        try {
            Area area = service.getAreaById(Integer.parseInt(contact.getPhoneNumber().substring(1, 4)));
            contact.setState(area.getState());
        } catch (ClientErrorException e) {
            if (e.getResponse().getStatusInfo() == Response.Status.NOT_FOUND) {
                throw new InvalidAreaCodeException("The area code provided does not exist", e);
            } else {
                throw e;
            }
        }*/

        // Either update the user or add it if it can't be found.
        return crud.update(user);
    }

    /**
     * <p>Deletes the provided User object from the application database if found there.<p/>
     *
     * @param user The User object to be removed from the application database
     * @return The User object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    User delete(User user) throws Exception {
        log.info("delete() - Deleting " + user.toString());

        User deletedUser = null;

        if (user.getId() != null) {
            deletedUser = crud.delete(user);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedUser;
    }
}

