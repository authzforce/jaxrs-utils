/**
 * Copyright 2012-2021 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.ow2.authzforce.jaxrs.util;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * JAX-RS {@link ExceptionMapper} for all 50X server errors
 */
@Provider
public class UnsupportedOperationExceptionMapper implements ExceptionMapper<UnsupportedOperationException>
{

	@Override
	public Response toResponse(final UnsupportedOperationException exception)
	{
		final JaxbErrorMessage error = new JaxbErrorMessage(exception.getMessage(), null);
		return Response.status(Response.Status.NOT_IMPLEMENTED).entity(error).build();
	}
}
