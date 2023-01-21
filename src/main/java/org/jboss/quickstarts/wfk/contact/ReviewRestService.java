/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.quickstarts.wfk.contact;

import io.swagger.annotations.*;
import org.jboss.quickstarts.wfk.area.InvalidAreaCodeException;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.resteasy.annotations.cache.Cache;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * <p>This class produces a RESTful service exposing the functionality of {@link ReviewService}.</p>
 *
 * <p>The Path annotation defines this as a REST Web Service using JAX-RS.</p>
 *
 * <p>By placing the Consumes and Produces annotations at the class level the methods all default to JSON.  However, they
 * can be overriden by adding the Consumes or Produces annotations to the individual methods.</p>
 *
 * <p>It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow
 * transaction demarcation when accessing the database." - Antonio Goncalves</p>
 *
 * <p>The full path for accessing endpoints defined herein is: api/reviews/*</p>
 * 
 * @author Joshua Wilson
 * @see ReviewService
 * @see javax.ws.rs.core.Response
 */
@Path("/reviews")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/reviews", description = "Operations about reviews")
@Stateless
public class ReviewRestService {
    @Inject
    private @Named("logger") Logger log;
    
    @Inject
    private ReviewService service;

    /**
     * <p>Return all the Reviews.  They are sorted alphabetically by name.</p>
     *
     * @return A Response containing a list of Reviews
     */
    @GET
    @ApiOperation(value = "Fetch all Reviews", notes = "Returns a JSON array of all stored Review objects.")
    public Response retrieveAllReviews() {
        //Create an empty collection to contain the intersection of Reviews to be returned
        List<Review> reviews;

        reviews = service.findAll();

        return Response.ok(reviews).build();
    }

    /**
     * <p>Search for and return a Reviews identified by user id.<p/>
     *
     *
     * @param user_id The string parameter value provided as a User's user id
     * @return A Response containing a list of Reviews
     */

    @GET
    @Cache
    @Path("/{user_id:[0-9]+}")
    @ApiOperation(
            value = "Fetch Reviews by user_id",
            notes = "Returns a JSON representation of the Reviews object with the provided id."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="Reviews found"),
            @ApiResponse(code = 404, message = "Reviews with user_id not found")
    })
    public Response retrieveReviewsById(
            @ApiParam(value = "User ID of Reviews to be fetched", allowableValues = "range[0, infinity]", required = true)
            @PathParam("user_id")
            long user_id) {

    	List<Review> reviews = service.findByUser_id(user_id);
        if (reviews.isEmpty()) {
            // Verify that the reviews exists. Return 404, if not present.
            throw new RestServiceException("No review with the user id " + user_id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findByUser_id " + user_id + ": found Reviews = " + reviews.toString());

        return Response.ok(reviews).build();
    }

    /**
     * <p>Creates a new review from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param review The Review object, constructed automatically from JSON input, to be <i>created</i> via
     * {@link ReviewService#create(Review)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @ApiOperation(value = "Add a new Review to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Review created successfully."),
            @ApiResponse(code = 400, message = "Invalid Review supplied in request body"),
            @ApiResponse(code = 409, message = "Review supplied in request body conflicts with an existing Review"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response createReview(
            @ApiParam(value = "JSON representation of Review object to be added to the database", required = true)
            Review review) {


        if (review == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Go add the new Review.
        	
            service.create(review);
            
            // Create a "Resource Created" 201 Response and pass the contact back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(review);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueReviewException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("review", "The user has already given review for that restaurant");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (InvalidAreaCodeException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("area_code", "The telephone area code provided is not recognised, please provide another");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
        }
        catch (EntityNotFoundException e) {
        	Map<String, String> responseObj = new HashMap<>();
        	responseObj.put("user_id", "The user id does not exist");
        	throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
        }
         catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
   
        log.info("createReview completed. Review "+review.toString());
        return builder.build();
    }

    

    
}

