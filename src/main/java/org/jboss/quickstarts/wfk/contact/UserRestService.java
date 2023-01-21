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
 * <p>This class produces a RESTful service exposing the functionality of {@link UserService}.</p>
 *
 * <p>The Path annotation defines this as a REST Web Service using JAX-RS.</p>
 *
 * <p>By placing the Consumes and Produces annotations at the class level the methods all default to JSON.  However, they
 * can be overriden by adding the Consumes or Produces annotations to the individual methods.</p>
 *
 * <p>It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow
 * transaction demarcation when accessing the database." - Antonio Goncalves</p>
 *
 * <p>The full path for accessing endpoints defined herein is: api/users/*</p>
 * 
 * @author Joshua Wilson
 * @see ContactService
 * @see javax.ws.rs.core.Response
 */
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/users", description = "Operations about users")
@Stateless
public class UserRestService {
    @Inject
    private @Named("logger") Logger log;
    
    @Inject
    private UserService service;

    /**
     * <p>Return all the Users.  They are sorted alphabetically by name.</p>
     *
     * <p>The url may optionally include query parameters specifying a User's name</p>
     *
     * @return A Response containing a list of Users
     */
    @GET
    @ApiOperation(value = "Fetch all Users", notes = "Returns a JSON array of all stored User objects.")
    public Response retrieveAllUsers() {
        //Create an empty collection to contain the intersection of Users to be returned
        List<User> users;

        	users = service.findAllOrderedByName();

        return Response.ok(users).build();
    }

    /**
     * <p>Search for and return a User identified by email address.<p/>
     *
     * <p>Path annotation includes very simple regex to differentiate between email addresses and Ids.
     * <strong>DO NOT</strong> attempt to use this regex to validate email addresses.</p>
     *
     *
     * @param email The string parameter value provided as a User email
     * @return A Response containing a single User
     */
    @GET
    @Cache
    @Path("/email/{email:.+[%40|@].+}")
    @ApiOperation(
            value = "Fetch a User by Email",
            notes = "Returns a JSON representation of the User object with the provided email."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="User found"),
            @ApiResponse(code = 404, message = "User with email not found")
    })
    public Response retrieveUsersByEmail(
            @ApiParam(value = "Email of User to be fetched", required = true)
            @PathParam("email")
            String email) {

    	User user;
        try {
        	user = service.findByEmail(email);
        } catch (NoResultException e) {
            // Verify that the contact exists. Return 404, if not present.
            throw new RestServiceException("No User with the email " + email + " was found!", Response.Status.NOT_FOUND);
        }
        return Response.ok(user).build();
    }

    /**
     * <p>Search for and return a User identified by id.</p>
     *
     * @param id The long parameter value provided as a User id
     * @return A Response containing a single User
     */
    @GET
    @Cache
    @Path("/{id:[0-9]+}")
    @ApiOperation(
            value = "Fetch a User by id",
            notes = "Returns a JSON representation of the User object with the provided id."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="User found"),
            @ApiResponse(code = 404, message = "User with id not found")
    })
    public Response retrieveUserById(
            @ApiParam(value = "Id of User to be fetched", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            long id) {

    	User user = service.findById(id);
        if (user == null) {
            // Verify that the user exists. Return 404, if not present.
            throw new RestServiceException("No User with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found User = " + user.toString());

        return Response.ok(user).build();
    }

    /**
     * <p>Creates a new user from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param user The User object, constructed automatically from JSON input, to be <i>created</i> via
     * {@link UserService#create(User)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @ApiOperation(value = "Add a new User to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User created successfully."),
            @ApiResponse(code = 400, message = "Invalid User supplied in request body"),
            @ApiResponse(code = 409, message = "User supplied in request body conflicts with an existing User"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response createUser(
            @ApiParam(value = "JSON representation of User object to be added to the database", required = true)
            User user) {


        if (user == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Go add the new User.
            service.create(user);

            // Create a "Resource Created" 201 Response and pass the contact back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(user);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueEmailException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "That email is already used, please use a unique email");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (InvalidAreaCodeException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("area_code", "The telephone area code provided is not recognised, please provide another");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("createUser completed. User = " + user.toString());
        return builder.build();
    }

    /**
     * <p>Updates the user with the ID provided in the database. Performs validation, and will return a JAX-RS response
     * with either 200 (ok), or with a map of fields, and related errors.</p>
     *
     * @param user The User object, constructed automatically from JSON input, to be <i>updated</i> via
     * {@link UserService#update(Contact)}
     * @param id The long parameter value provided as the id of the User to be updated
     * @return A Response indicating the outcome of the create operation
     */
    @PUT
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Update a User in the database")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User updated successfully"),
            @ApiResponse(code = 400, message = "Invalid User supplied in request body"),
            @ApiResponse(code = 404, message = "User with id not found"),
            @ApiResponse(code = 409, message = "User details supplied in request body conflict with another existing User"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response updateUser(
            @ApiParam(value = "Id of User to be updated", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            long id,
            @ApiParam(value = "JSON representation of User object to be updated in the database", required = true)
            User user) {

        if (user == null || user.getId() == null) {
            throw new RestServiceException("Invalid User supplied in request body", Response.Status.BAD_REQUEST);
        }

        if (user.getId() != null && user.getId() != id) {
            // The client attempted to update the read-only Id. This is not permitted.
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("id", "The User ID in the request body must match that of the User being updated");
            throw new RestServiceException("User details supplied in request body conflict with another User",
                    responseObj, Response.Status.CONFLICT);
        }

        if (service.findById(user.getId()) == null) {
            // Verify that the User exists. Return 404, if not present.
            throw new RestServiceException("No User with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        Response.ResponseBuilder builder;

        try {
            // Apply the changes the User.
            service.update(user);

            // Create an OK Response and pass the user back in case it is needed.
            builder = Response.ok(user);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
        } catch (UniqueEmailException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "That email is already used, please use a unique email");
            throw new RestServiceException("User details supplied in request body conflict with another User",
                    responseObj, Response.Status.CONFLICT, e);
        } catch (InvalidAreaCodeException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("area_code", "The telephone area code provided is not recognised, please provide another");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("updateUser completed. User = " + user.toString());
        return builder.build();
    }

    /**
     * <p>Deletes a user using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the User to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Delete a User from the database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The User has been successfully deleted"),
            @ApiResponse(code = 400, message = "Invalid User id supplied"),
            @ApiResponse(code = 404, message = "User with id not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response deleteUser(
            @ApiParam(value = "Id of User to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            long id) {

        Response.ResponseBuilder builder;

        User user = service.findById(id);
        if (user == null) {
            // Verify that the user exists. Return 404, if not present.
            throw new RestServiceException("No User with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(user);

            builder = Response.noContent();

        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        log.info("deleteUser completed. User = " + user.toString());
        return builder.build();
    }
}
