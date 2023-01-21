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
 * <p>This class provides methods to check User objects against arbitrary requirements.</p>
 *
 * @author Joshua Wilson
 * @see User
 * @see UserRepository
 * @see javax.validation.Validator
 */
public class UserValidator {
    @Inject
    private Validator validator;

    @Inject
    private UserRepository crud;

    /**
     * <p>Validates the given User object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing user with the same email is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     *
     * @param contact The User object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If user with the same email already exists
     */
    void validateUser(User user) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the email address
        if (emailAlreadyExists(user.getEmail(), user.getId())) {
            throw new UniqueEmailException("Unique Email Violation");
        }
    }

    /**
     * <p>Checks if a user with the same email address is already registered. This is the only way to easily capture the
     * "@UniqueConstraint(columnNames = "email")" constraint from the User class.</p>
     * 
     * @param email The email to check is unique
     * @param id The user id to check the email against if it was found
     * @return boolean which represents whether the email was found, and if so if it belongs to the user with id
     */
    boolean emailAlreadyExists(String email, Long id) {
    	User user = null;
    	User userWithID = null;
        try {
            user = crud.findByEmail(email);
        } catch (NoResultException e) {
            // ignore
        }

        if (user != null && id != null) {
            try {
            	userWithID = crud.findById(id);
                if (userWithID != null && userWithID.getEmail().equals(email)) {
                	user = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return user != null;
    }
}


