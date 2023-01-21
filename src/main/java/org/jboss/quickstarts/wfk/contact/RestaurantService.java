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
 * @see RestaurantValidator
 * @see RestaurantRepository
 */
//The @Dependent is the default scope is listed here so that you know what scope is being used.
@Dependent
public class RestaurantService {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private RestaurantValidator validator;

    @Inject
    private RestaurantRepository crud;

    private ResteasyClient client;

    /**
     * <p>Create a new client which will be used for our outgoing REST client communication</p>
     */
    public RestaurantService() {
        // Create client service instance to make REST requests to upstream service
        client = new ResteasyClientBuilder().build();
    }

    /**
     * <p>Returns a List of all persisted {@link Restaurant} objects, sorted alphabetically by name.<p/>
     *
     * @return List of Restaurant objects
     */
    List<Restaurant> findAllOrderedByName() {
        return crud.findAllOrderedByName();
    }

    /**
     * <p>Returns a single Restaurant object, specified by a Long id.<p/>
     *
     * @param id The id field of the Restaurant to be returned
     * @return The Restaurant with the specified id
     */
    Restaurant findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Returns a single Restaurant object, specified by a String phoneNumber.</p>
     *
     * <p>If there is more than one Restaurant with the specified phoneNumber, only the first encountered will be returned.<p/>
     *
     * @param phoneNumber The phoneNumber field of the Restaurant to be returned
     * @return The first Restaurant with the specified phoneNumber
     */
    Restaurant findByPhoneNumber(String phoneNumber) {
        return crud.findByPhoneNumber(phoneNumber);
    }


    /**
     * <p>Writes the provided Restaurant object to the application database.<p/>
     *
     * <p>Validates the data in the provided Restaurant object using a {@link RestaurantValidator} object.<p/>
     *
     * @param contact The Restaurant object to be written to the database using a {@link RestaurantRepository} object
     * @return The Restaurant object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Restaurant create(Restaurant restaurant) throws ConstraintViolationException, ValidationException, Exception {
        log.info("RestaurantService.create() - Creating " + restaurant.getName());
        
        // Check to make sure the data fits with the parameters in the Contact model and passes validation.
        validator.validateRestaurant(restaurant);

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

        // Write the contact to the database.
        return crud.create(restaurant);
    }

    /**
     * <p>Updates an existing Restaurant object in the application database with the provided Restaurant object.<p/>
     *
     * <p>Validates the data in the provided Restaurant object using a RestaurantValidator object.<p/>
     *
     * @param restaurant The Restaurant object to be passed as an update to the application database
     * @return The Restaurant object that has been successfully updated in the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Restaurant update(Restaurant restaurant) throws ConstraintViolationException, ValidationException, Exception {
        log.info("RestaurantService.update() - Updating " + restaurant.getName());
        
        // Check to make sure the data fits with the parameters in the Contact model and passes validation.
        validator.validateRestaurant(restaurant);

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

        // Either update the contact or add it if it can't be found.
        return crud.update(restaurant);
    }

    /**
     * <p>Deletes the provided Restaurant object from the application database if found there.<p/>
     *
     * @return The Restaurant object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Restaurant delete(Restaurant restaurant) throws Exception {
        log.info("delete() - Deleting " + restaurant.toString());

        Restaurant deletedRestaurant = null;

        if (restaurant.getId() != null) {
            deletedRestaurant = crud.delete(restaurant);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedRestaurant;
    }
}


