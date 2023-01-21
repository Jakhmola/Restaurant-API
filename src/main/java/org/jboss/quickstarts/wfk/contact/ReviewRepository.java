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
 * <p>This is a Repository class and connects the Service/Control layer (see {@link ReviewService} with the
 * Domain/Entity Object (see {@link Review}).<p/>
 *
 * <p>There are no access modifiers on the methods making them 'package' scope.  They should only be accessed by a
 * Service/Control object.<p/>
 *
 * @author Joshua Wilson
 * @see Review
 * @see javax.persistence.EntityManager
 */
public class ReviewRepository {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private EntityManager em;
    
    @Inject
    private UserService service;
    
    List<Review> findAll() {
        TypedQuery<Review> query = em.createNamedQuery(Review.FIND_ALL, Review.class);
        return query.getResultList();
    }
    
    List<Review> findByUserId(Long user_id) {
        TypedQuery<Review> query = em.createNamedQuery(Review.FIND_BY_USER_ID, Review.class).setParameter("userId", user_id);
        return query.getResultList();
    }


    Review create(Review review) throws ConstraintViolationException, ValidationException, Exception {
        log.info("ReviewRepository.create() - Creating " + review.getUserId() + review.getRestaurantId());
        User user = em.getReference(User.class, review.getUserId());
        review.setUser(user);
        user.addReview(review);
        // Write the user to the database.
        //em.getTransaction().begin();
        em.persist(review);
        //em.getTransaction().commit();
        
        return review;
    }
    
    
    
}

