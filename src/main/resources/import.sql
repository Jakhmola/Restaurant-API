--
-- JBoss, Home of Professional Open Source
-- Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
-- contributors by the @authors tag. See the copyright.txt in the
-- distribution for a full listing of individual contributors.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- http://www.apache.org/licenses/LICENSE-2.0
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- You can use this file to load seed data into the database using SQL statements
-- Since the database doesn't know to increase the Sequence to match what is manually loaded here it starts at 1 and tries
--  to enter a record with the same PK and create an error.  If we use a high we don't interfere with the sequencing (at least until later).
-- NOTE: this file should be removed for production systems. 
insert into Contact (id, first_name, last_name, email, phone_number, birth_date) values (10001, 'John', 'Smith', 'john.smith@mailinator.com', '(212) 555-1212', '1963-06-03')
insert into Contact (id, first_name, last_name, email, phone_number, birth_date) values (10002, 'Davey', 'Jones', 'davey.jones@locker.com', '(212) 555-3333', '1996-08-07')
insert into Restaurant (id, name, post_code, phone_number) values (101, 'Dilli Darbar', 'NE14DD', '01234567894')
insert into Restaurant (id, name, post_code, phone_number) values (102, 'Madina', 'NSS4DD', '01234567877')
insert into User (id, name, email, phone_number) values (10001, 'John', 'john.smith@mailinator.com', '01234567894')
insert into User (id, name, email, phone_number) values (10002, 'Jane', 'davey.jones@locker.com', '01234567874')
insert into Review (id, user, restaurant, review, rating, user_id) values (1001, '10001', '101', 'good food', '4', 10001)
insert into Review (id, user, restaurant, review, rating, user_id) values (2000, '10002', '102', 'great meal', '5',10002)
insert into Review (id, user, restaurant, review, rating, user_id) values (3000, '10001', '102', 'bad food', '0', 10001)





