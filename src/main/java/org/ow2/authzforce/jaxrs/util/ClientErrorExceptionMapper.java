/**
 * Copyright 2012-2018 THALES.
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

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * JAX-RS {@link ExceptionMapper} for {@link ClientErrorException}
 */
@Provider
public class ClientErrorExceptionMapper implements ExceptionMapper<ClientErrorException>
{

	@Override
	public Response toResponse(final ClientErrorException exception)
	{

		// if NotFoundException has root cause, we expect the root cause message to be more specific
		// on what resource could not be found, so return this message to the client
		if (exception.getCause() != null)
		{
			final JaxbErrorMessage errorEntity = new JaxbErrorMessage(exception.getCause().getMessage());
			return Response.status(exception.getResponse().getStatus()).entity(errorEntity).build();
		}

		// if not, return response as is (no change)
		return exception.getResponse();
	}
}
