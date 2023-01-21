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
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <p>This class produces a RESTful service exposing the functionality of {@link RestaurantService}.</p>
 *
 * <p>The Path annotation defines this as a REST Web Service using JAX-RS.</p>
 *
 * <p>By placing the Consumes and Produces annotations at the class level the methods all default to JSON.  However, they
 * can be overriden by adding the Consumes or Produces annotations to the individual methods.</p>
 *
 * <p>It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow
 * transaction demarcation when accessing the database." - Antonio Goncalves</p>
 *
 * <p>The full path for accessing endpoints defined herein is: api/restaurants/*</p>
 * 
 * @author Joshua Wilson
 * @see ContactService
 * @see javax.ws.rs.core.Response
 */
@Path("/restaurants")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/restaurants", description = "Operations about restaurants")
@Stateless
public class RestaurantRestService {
    @Inject
    private @Named("logger") Logger log;
    
    @Inject
    private RestaurantService service;

    /**
     * <p>Return all the Restaurants.  They are sorted alphabetically by name.</p>
     *
     * <p>The url may optionally include query parameters specifying a Restaurant's name</p>
     *
     * @return A Response containing a list of Restaurants
     */
    @GET
    @ApiOperation(value = "Fetch all Restaurants", notes = "Returns a JSON array of all stored Restaurant objects.")
    public Response retrieveAllRestaurants() {
        //Create an empty collection to contain the intersection of Contacts to be returned
        List<Restaurant> restaurants;

        restaurants = service.findAllOrderedByName();

        return Response.ok(restaurants).build();
    }

    /**
     * <p>Search for and return a Restaurant identified by phone number.<p/>
     *
     * @param phone number The string parameter value provided as a Restaurant's phone number
     * @return A Response containing a single Restaurant
     */
    @GET
    @Cache
    @Path("/phoneNumber/{phoneNumber:0[0-9]{10}}")
    @ApiOperation(
            value = "Fetch a Restaurant by phone number",
            notes = "Returns a JSON representation of the Restaurant object with the provided phone number."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="Restaurant found"),
            @ApiResponse(code = 404, message = "Restaurant with phone number not found")
    })
    public Response retrieveRestaurantsByPhoneNumber(
            @ApiParam(value = "Email of Restaurant to be fetched", required = true)
            @PathParam("phoneNumber")
            String phoneNumber) {

    	Restaurant restaurant;
        try {
        	restaurant = service.findByPhoneNumber(phoneNumber);
        } catch (NoResultException e) {
            // Verify that the contact exists. Return 404, if not present.
            throw new RestServiceException("No restaurant with the phone number " + phoneNumber + " was found!", Response.Status.NOT_FOUND);
        }
        return Response.ok(restaurant).build();
    }

    /**
     * <p>Search for and return a Restaurant identified by id.</p>
     *
     * @param id The long parameter value provided as a Restaurant's id
     * @return A Response containing a single Restaurant
     */
    @GET
    @Cache
    @Path("/{id:[0-9]+}")
    @ApiOperation(
            value = "Fetch a Restaurant by id",
            notes = "Returns a JSON representation of the Restaurant object with the provided id."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="Restaurant found"),
            @ApiResponse(code = 404, message = "Restaurant with id not found")
    })
    public Response retrieveRestaurantById(
            @ApiParam(value = "Id of Restaurant to be fetched", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            long id) {

    	Restaurant restaurant = service.findById(id);
        if (restaurant == null) {
            // Verify that the contact exists. Return 404, if not present.
            throw new RestServiceException("No User with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found Restaurant = " + restaurant.toString());

        return Response.ok(restaurant).build();
    }

    /**
     * <p>Creates a new restaurant from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param restaurant The Restaurant object, constructed automatically from JSON input, to be <i>created</i> via
     * {@link RestaurantService#create(Restaurant)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @ApiOperation(value = "Add a new Restaurant to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Restaurant created successfully."),
            @ApiResponse(code = 400, message = "Invalid Restaurant supplied in request body"),
            @ApiResponse(code = 409, message = "Restaurant supplied in request body conflicts with an existing Restaurant"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response createRestaurant(
            @ApiParam(value = "JSON representation of Restaurant object to be added to the database", required = true)
            Restaurant restaurant) {


        if (restaurant == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Go add the new Restaurant.
            service.create(restaurant);

            // Create a "Resource Created" 201 Response and pass the contact back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(restaurant);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniquePhoneNumberException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("phone number", "That phone number is already used, please use a unique phone number");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        }
         catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("createRestaurant completed. Restaurant = " + restaurant.toString());
        return builder.build();
    }

    /**
     * <p>Updates the contact with the ID provided in the database. Performs validation, and will return a JAX-RS response
     * with either 200 (ok), or with a map of fields, and related errors.</p>
     *
     * @param contact The Restaurant object, constructed automatically from JSON input, to be <i>updated</i> via
     * {@link RestaurantService#update(Restaurant)}
     * @param id The long parameter value provided as the id of the Restaurant to be updated
     * @return A Response indicating the outcome of the create operation
     */
    @PUT
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Update a Restaurant in the database")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Restaurant updated successfully"),
            @ApiResponse(code = 400, message = "Invalid Restaurant supplied in request body"),
            @ApiResponse(code = 404, message = "Restaurant with id not found"),
            @ApiResponse(code = 409, message = "Restaurant details supplied in request body conflict with another existing Restaurant"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response updateRestaurant(
            @ApiParam(value = "Id of Restaurant to be updated", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            long id,
            @ApiParam(value = "JSON representation of Restaurant object to be updated in the database", required = true)
            Restaurant restaurant) {

        if (restaurant == null || restaurant.getId() == null) {
            throw new RestServiceException("Invalid Restaurant supplied in request body", Response.Status.BAD_REQUEST);
        }

        if (restaurant.getId() != null && restaurant.getId() != id) {
            // The client attempted to update the read-only Id. This is not permitted.
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("id", "The Restaurant ID in the request body must match that of the Restaurant being updated");
            throw new RestServiceException("Restaurant details supplied in request body conflict with another Restaurant",
                    responseObj, Response.Status.CONFLICT);
        }

        if (service.findById(restaurant.getId()) == null) {
            // Verify that the contact exists. Return 404, if not present.
            throw new RestServiceException("No Restaurant with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        Response.ResponseBuilder builder;

        try {
            // Apply the changes the Contact.
            service.update(restaurant);

            // Create an OK Response and pass the contact back in case it is needed.
            builder = Response.ok(restaurant);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
        } catch (UniquePhoneNumberException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("phone number", "That phone number is already used, please use a unique phone number");
            throw new RestServiceException("Restaurant details supplied in request body conflict with another Restaurant",
                    responseObj, Response.Status.CONFLICT, e);
        } catch (InvalidAreaCodeException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("area_code", "The telephone area code provided is not recognised, please provide another");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("updateUser completed. Restaurant = " + restaurant.toString());
        return builder.build();
    }

    /**
     * <p>Deletes a Restaurant using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Restaurant to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Delete a Restaurant from the database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The Restaurant has been successfully deleted"),
            @ApiResponse(code = 400, message = "Invalid Restaurant id supplied"),
            @ApiResponse(code = 404, message = "Restaurant with id not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response deleteRestaurant(
            @ApiParam(value = "Id of Restaurant to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            long id) {

        Response.ResponseBuilder builder;

        Restaurant restaurant = service.findById(id);
        if (restaurant == null) {
            // Verify that the contact exists. Return 404, if not present.
            throw new RestServiceException("No Restaurant with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(restaurant);

            builder = Response.noContent();

        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        log.info("deleteRestaurant completed. User = " + restaurant.toString());
        return builder.build();
    }
}

