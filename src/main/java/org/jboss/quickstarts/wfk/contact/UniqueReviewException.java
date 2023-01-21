/**
 * jboss-wfk-quickstarts
 * <p/>
 * Copyright (c) 2015 Jonny Daenen, Hugo Firth & Bas Ketsman
 * Email: <me@hugofirth.com/>
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.quickstarts.wfk.contact;

import javax.validation.ValidationException;

/**
 * <p>ValidationException caused if a Review's user id and restaurant id conflicts with that of another Review.</p>
 *
 * <p>This violates the uniqueness constraint.</p>
 *
 * @author hugofirth
 * @see Contact
 */
public class UniqueReviewException extends ValidationException {

    public UniqueReviewException(String message) {
        super(message);
    }

    public UniqueReviewException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniqueReviewException(Throwable cause) {
        super(cause);
    }
}



