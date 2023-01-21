package org.jboss.quickstarts.wfk.contact;
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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Date;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * <p>A suite of tests, run with {@link org.jboss.arquillian Arquillian} to test the JAX-RS endpoint for
 * Contact creation functionality
 * (see {@link ContactRestService#createContact(Contact) createContact(Contact)}).<p/>
 *
 * @author balunasj
 * @author Joshua Wilson
 * @see ContactRestService
 */
@RunWith(Arquillian.class)
public class RestaurantRegistrationTest {

    /**
     * <p>Compiles an Archive using Shrinkwrap, containing those external dependencies necessary to run the tests.</p>
     *
     * <p>Note: This code will be needed at the start of each Arquillian test, but should not need to be edited, except
     * to pass *.class values to .addClasses(...) which are appropriate to the functionality you are trying to test.</p>
     *
     * @return Micro test war to be deployed and executed.
     */
    @Deployment
    public static Archive<?> createTestArchive() {
        // This is currently not well tested. If you run into issues, comment line 67 (the contents of 'resolve') and
        // uncomment 65. This will build our war with all dependencies instead.
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
//                .importRuntimeAndTestDependencies()
                .resolve(
                        "io.swagger:swagger-jaxrs:1.5.16"
        ).withTransitivity().asFile();

        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addPackages(true, "org.jboss.quickstarts.wfk")
                .addAsLibraries(libs)
                .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("arquillian-ds.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    RestaurantRestService restaurantRestService;

    @Inject
    @Named("logger") Logger log;

    //Set millis 498484800000 from 1985-10-10T12:00:00.000Z
    private Date date = new Date(498484800000L);

    @Test
    @InSequence(1)
    public void testRegister() throws Exception {
    	Restaurant restaurant = createRestaurantInstance("Jack Doe", "NE14SS", "01234567891");
        Response response = restaurantRestService.createRestaurant(restaurant);

        assertEquals("Unexpected response status", 201, response.getStatus());
        log.info(" New restaurant was persisted and returned status " + response.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    @InSequence(2)
    public void testInvalidRegister() {
    	Restaurant restaurant = createRestaurantInstance("", "", "");

        try {
        	restaurantRestService.createRestaurant(restaurant);
            fail("Expected a RestServiceException to be thrown");
        } catch(RestServiceException e) {
            assertEquals("Unexpected response status", Response.Status.BAD_REQUEST, e.getStatus());
            assertEquals("Unexpected response body", 3, e.getReasons().size());
            log.info("Invalid user register attempt failed with return code " + e.getStatus());
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    @InSequence(3)
    public void testDuplicatePhoneNumber() throws Exception {
        // Register an initial restaurant
    	Restaurant restaurant = createRestaurantInstance("Jane Doe", "NE14SS", "01334567894");
    	restaurantRestService.createRestaurant(restaurant);

        // Register a different restaurant with the same phone number
    	Restaurant anotherRestaurant = createRestaurantInstance("John Doe", "NE14SS", "01334567894");

        try {
        	restaurantRestService.createRestaurant(anotherRestaurant);
            fail("Expected a RestServiceException to be thrown");
        } catch(RestServiceException e) {
        	System.out.println(e.getMessage());
            assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
            assertTrue("Unexpected error. Should be Unique phone number violation", e.getCause() instanceof UniquePhoneNumberException);
            assertEquals("Unexpected response body", 1, e.getReasons().size());
            log.info("Duplicate restaurant register attempt failed with return code " + e.getStatus());
        }

    }


    private Restaurant createRestaurantInstance(String name, String postCode, String phone) {
    	Restaurant restaurant = new Restaurant();
    	restaurant.setName(name);
    	restaurant.setPost_code(postCode);
    	restaurant.setPhoneNumber(phone);
        return restaurant;
    }

}

