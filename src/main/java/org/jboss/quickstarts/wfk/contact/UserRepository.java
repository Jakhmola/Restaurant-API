package org.jboss.quickstarts.wfk.contact;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>This is a Repository class and connects the Service/Control layer (see {@link UserService} with the
 * Domain/Entity Object (see {@link User}).<p/>
 *
 * <p>There are no access modifiers on the methods making them 'package' scope.  They should only be accessed by a
 * Service/Control object.<p/>
 *
 * @author Joshua Wilson
 * @see User
 * @see javax.persistence.EntityManager
 */
public class UserRepository {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private EntityManager em;

    /**
     * <p>Returns a List of all persisted {@link User} objects, sorted alphabetically by last name.</p>
     *
     * @return List of User objects
     */
    List<User> findAllOrderedByName() {
        TypedQuery<User> query = em.createNamedQuery(User.FIND_ALL, User.class);
        return query.getResultList();
    }

    /**
     * <p>Returns a single User object, specified by a Long id.<p/>
     *
     * @param id The id field of the User to be returned
     * @return The User with the specified id
     */
    User findById(Long id) {
        return em.find(User.class, id);
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
        TypedQuery<User> query = em.createNamedQuery(User.FIND_BY_EMAIL, User.class).setParameter("email", email);
        return query.getSingleResult();
    }

 


    /**
     * <p>Persists the provided User object to the application database using the EntityManager.</p>
     *
     * <p>{@link javax.persistence.EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
     * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
     *
     * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
     *
     * @param user The User object to be persisted
     * @return The User object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    User create(User user) throws ConstraintViolationException, ValidationException, Exception {
        log.info("UserRepository.create() - Creating " + user.getName());

        // Write the user to the database.
        em.persist(user);

        return user;
    }

    /**
     * <p>Updates an existing User object in the application database with the provided User object.</p>
     *
     * <p>{@link javax.persistence.EntityManager#merge(Object) merge(Object)} creates a new instance of your entity,
     * copies the state from the supplied entity, and makes the new copy managed. The instance you pass in will not be
     * managed (any changes you make will not be part of the transaction - unless you call merge again).</p>
     *
     * <p>merge(Object) however must have an object with the @Id already generated.</p>
     *
     * @param user The User object to be merged with an existing User
     * @return The User that has been merged
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    User update(User user) throws ConstraintViolationException, ValidationException, Exception {
        log.info("UserRepository.update() - Updating " + user.getName());

        // Either update the contact or add it if it can't be found.
        em.merge(user);

        return user;
    }

    /**
     * <p>Deletes the provided User object from the application database if found there</p>
     *
     * @param user The User object to be removed from the application database
     * @return The User object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    User delete(User user) throws Exception {
        log.info("UserRepository.delete() - Deleting " + user.getName());

        if (user.getId() != null) {
            /*
             * The Hibernate session (aka EntityManager's persistent context) is closed and invalidated after the commit(), 
             * because it is bound to a transaction. The object goes into a detached status. If you open a new persistent 
             * context, the object isn't known as in a persistent state in this new context, so you have to merge it. 
             * 
             * Merge sees that the object has a primary key (id), so it knows it is not new and must hit the database 
             * to reattach it. 
             * 
             * Note, there is NO remove method which would just take a primary key (id) and a entity class as argument. 
             * You first need an object in a persistent state to be able to delete it.
             * 
             * Therefore we merge first and then we can remove it.
             */
            em.remove(em.merge(user));

        } else {
            log.info("UserRepository.delete() - No ID was found so can't Delete.");
        }

        return user;
    }

}
