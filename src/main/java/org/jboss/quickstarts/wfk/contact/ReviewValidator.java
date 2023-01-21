package org.jboss.quickstarts.wfk.contact;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * <p>This class provides methods to check Review objects against arbitrary requirements.</p>
 *
 * @author Joshua Wilson
 * @see Review
 * @see ReviewRepository
 * @see javax.validation.Validator
 */
public class ReviewValidator {
	
	@Inject
    private @Named("logger") Logger log;
	
    @Inject
    private Validator validator;

    @Inject
    private ReviewRepository crud;
    
    @Inject
    private EntityManager em;

    /**
     * <p>Validates the given Review object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing review with the same user id and restaurant id is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     *
     * @param review The Review object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If contact with the same email already exists
     */
    void validateReview(Review review) throws ConstraintViolationException, ValidationException, EntityNotFoundException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Review>> violations = validator.validate(review);

        if (!violations.isEmpty()) {
        	log.info("Review validated successfuly " + review.toString());
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }
        
        em.getReference(User.class, review.getUserId());
  
        // Check the uniqueness of the email address
        if (reviewAlreadyExists(review.getUserId(), review.getRestaurantId())) {
        	log.info("Review validated successfuly " + review.toString());
            throw new UniqueReviewException("Unique Review Violation");
        }
        log.info("Review validated successfuly " + review.toString());

    }

    /**
     * <p>Checks if a review with the same user_id and restaurant_id is already registered.
     */
    boolean reviewAlreadyExists(Long user_id, Long restaurant_id) {
    	Boolean flag = false;
    	List<Review> reviews = null;
        try {
        	reviews = crud.findByUserId(user_id);
        } catch (NoResultException e) {
            // ignore
        }

        if (!reviews.isEmpty()) {
            try {
            	for(Review review: reviews) {
                if (review.getRestaurantId().equals(restaurant_id)) {
                	flag = true;
                	break;
                }}
            } catch (NoResultException e) {
                // ignore
            }
        }
        return flag;
    }
}



