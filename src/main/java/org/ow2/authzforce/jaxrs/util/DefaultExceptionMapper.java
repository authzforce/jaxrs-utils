/*
 * Copyright 2012-2024 THALES.
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
package org.ow2.authzforce.jaxrs.util;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default JAX-RS {@link ExceptionMapper} for all exceptions not supported by other {@link ExceptionMapper}
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable>
{
	private final static Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);
	private final static String INTERNAL_ERR_MSG = "Internal server error:";
	private final static JaxbErrorMessage ERROR = new JaxbErrorMessage("Internal server error. Retry later or contact the administrator.", null);

	@Override
	public Response toResponse(final Throwable exception)
	{
		if(exception instanceof WebApplicationException ex) {
			final Response httpResp = ex.getResponse();
			switch(httpResp.getStatusInfo().getFamily()) {
				// Normal response (not a server error or unknown type of error), return as is.
				case SUCCESSFUL, CLIENT_ERROR, INFORMATIONAL, REDIRECTION -> { return httpResp;}
			}
		}
		/*
		 * Hide any internal server error info to clients
		 */
		LOGGER.error(INTERNAL_ERR_MSG, exception);
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ERROR).build();
	}
}
