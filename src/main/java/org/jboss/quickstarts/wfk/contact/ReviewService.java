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
 * @see ReviewValidator
 * @see ReviewRepository
 */
//The @Dependent is the default scope is listed here so that you know what scope is being used.
@Dependent
public class ReviewService {

    @Inject
    private @Named("logger") Logger log;
    
    @Inject
    private ReviewValidator validator;

    @Inject
    private ReviewRepository crud;

    private ResteasyClient client;

    /**
     * <p>Create a new client which will be used for our outgoing REST client communication</p>
     */
    public ReviewService() {
        // Create client service instance to make REST requests to upstream service
        client = new ResteasyClientBuilder().build();
    }

    /**
     * <p>Returns a List of all persisted {@link Review} objects<p/>
     *
     * @return List of Review objects
     */
    List<Review> findAll() {
        return crud.findAll();
    }

    /**
     * <p>Returns a list of Review objects, specified by a Long user id.<p/>
     *
     * @param user_id The id field of the user 
     * @return The list of Reviews given by the user
     */
    List<Review> findByUser_id(Long user_id) {
        return crud.findByUserId(user_id);
    }


    /**
     * <p>Writes the provided Review object to the application database.<p/>
     *
     * <p>Validates the data in the provided Review object using a {@link ReviewValidator} object.<p/>
     *
     * @param contact The Review object to be written to the database using a {@link ReviewRepository} object
     * @return The Review object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Review create(Review review) throws ConstraintViolationException, ValidationException, Exception {
        log.info("ReviewService.create() - Creating " + review.getUserId() + review.getRestaurantId());
        
        // Check to make sure the data fits with the parameters in the Review model and passes validation.
        validator.validateReview(review);
        // Write the Review to the database.
        return crud.create(review);
    }

}

