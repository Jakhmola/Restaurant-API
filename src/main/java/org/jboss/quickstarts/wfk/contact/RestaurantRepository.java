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
 * <p>This is a Repository class and connects the Service/Control layer (see {@link RestaurantService} with the
 * Domain/Entity Object (see {@link Restaurant}).<p/>

 */
public class RestaurantRepository {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private EntityManager em;

    /**
     * <p>Returns a List of all persisted {@link Restaurant} objects, sorted alphabetically by last name.</p>
     *
     * @return List of Restaurant objects
     */
    List<Restaurant> findAllOrderedByName() {
        TypedQuery<Restaurant> query = em.createNamedQuery(Restaurant.FIND_ALL, Restaurant.class);
        return query.getResultList();
    }

    /**
     * <p>Returns a single Restaurant object, specified by a Long id.<p/>
     *
     * @param id The id field of the Restaurant to be returned
     * @return The Restaurant with the specified id
     */
    Restaurant findById(Long id) {
        return em.find(Restaurant.class, id);
    }

    /**
     * <p>Returns a single Restaurant object, specified by a String phoneNumber.</p>
     *
     * <p>If there is more than one Restaurant with the specified phoneNumber, only the first encountered will be returned.<p/>
     *
     * @param email The phoneNumber field of the Restaurant to be returned
     * @return The first Restaurant with the specified phoneNumber
     */
    Restaurant findByPhoneNumber(String phoneNumber) {
        TypedQuery<Restaurant> query = em.createNamedQuery(Restaurant.FIND_BY_PHONENO, Restaurant.class).setParameter("phoneNumber", phoneNumber);
        return query.getSingleResult();
    }

 


    /**
     * <p>Persists the provided Restaurant object to the application database using the EntityManager.</p>
     *
     * <p>{@link javax.persistence.EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
     * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
     *
     * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
     *
     * @param restaurant The Restaurant object to be persisted
     * @return The Restaurant object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Restaurant create(Restaurant restaurant) throws ConstraintViolationException, ValidationException, Exception {
        log.info("RestaurantRepository.create() - Creating " + restaurant.getName());

        // Write the user to the database.
        em.persist(restaurant);

        return restaurant;
    }

    /**
     * <p>Updates an existing Restaurant object in the application database with the provided Restaurant object.</p>
     *
     * <p>{@link javax.persistence.EntityManager#merge(Object) merge(Object)} creates a new instance of your entity,
     * copies the state from the supplied entity, and makes the new copy managed. The instance you pass in will not be
     * managed (any changes you make will not be part of the transaction - unless you call merge again).</p>
     *
     * <p>merge(Object) however must have an object with the @Id already generated.</p>
     *
     * @param restaurant The Restaurant object to be merged with an existing Restaurant
     * @return The Restaurant that has been merged
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Restaurant update(Restaurant restaurant) throws ConstraintViolationException, ValidationException, Exception {
        log.info("RestaurantRepository.update() - Updating " + restaurant.getName());

        // Either update the contact or add it if it can't be found.
        em.merge(restaurant);

        return restaurant;
    }

    /**
     * <p>Deletes the provided Restaurant object from the application database if found there</p>
     *
     * @param contact The Restaurant object to be removed from the application database
     * @return The Restaurant object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Restaurant delete(Restaurant restaurant) throws Exception {
        log.info("RestaurantRepository.delete() - Deleting " + restaurant.getName());

        if (restaurant.getId() != null) {
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
            em.remove(em.merge(restaurant));

        } else {
            log.info("RestaurantRepository.delete() - No ID was found so can't Delete.");
        }

        return restaurant;
    }

}

