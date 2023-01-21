package org.jboss.quickstarts.wfk.contact;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>This class provides methods to check Restaurant objects against arbitrary requirements.</p>
 *
 * @author Joshua Wilson
 * @see Restaurant
 * @see RestaurantRepository
 * @see javax.validation.Validator
 */
public class RestaurantValidator {
    @Inject
    private Validator validator;

    @Inject
    private RestaurantRepository crud;

    /**
     * <p>Validates the given Restaurant object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing restaurant with the same PhoneNumber is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     *
     * @param restaurant The Restaurant object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If restaurant with the same PhoneNumber already exists
     */
    void validateRestaurant(Restaurant restaurant) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Restaurant>> violations = validator.validate(restaurant);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the email address
        if (phoneNumberAlreadyExists(restaurant.getPhoneNumber(), restaurant.getId())) {
            throw new UniquePhoneNumberException("Unique PhoneNumber Violation");
        }
    }

    /**
     * <p>Checks if a restaurant with the same phoneNumber is already registered. This is the only way to easily capture the
     * "@UniqueConstraint(columnNames = "phoneNumber")" constraint from the Restaurant class.</p>
     *
     * <p>Since Update will being using an phoneNumber that is already in the database we need to make sure that it is the phoneNumber
     * from the record being updated.</p>
     *
     * @param phoneNumber The phoneNumber to check is unique
     * @param id The user id to check the phoneNumber against if it was found
     * @return boolean which represents whether the phoneNumber was found, and if so if it belongs to the restaurant with id
     */
    boolean phoneNumberAlreadyExists(String phoneNumber, Long id) {
    	Restaurant restaurant = null;
    	Restaurant restaurantWithID = null;
        try {
        	restaurant = crud.findByPhoneNumber(phoneNumber);
        } catch (NoResultException e) {
            // ignore
        }

        if (restaurant != null && id != null) {
            try {
            	restaurantWithID = crud.findById(id);
                if (restaurantWithID != null && restaurantWithID.getPhoneNumber().equals(phoneNumber)) {
                	restaurant = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return restaurant != null;
    }
}



