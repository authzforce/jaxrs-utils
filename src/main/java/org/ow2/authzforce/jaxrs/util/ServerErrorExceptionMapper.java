/**
 * Copyright 2012-2019 THALES.
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

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS {@link ExceptionMapper} for all 50X server errors
 */
@Provider
public class ServerErrorExceptionMapper implements ExceptionMapper<ServerErrorException>
{
	private final static Logger LOGGER = LoggerFactory.getLogger(ServerErrorExceptionMapper.class);
	private final static String INTERNAL_ERR_MSG = "Internal server error:";
	private final static JaxbErrorMessage ERROR = new JaxbErrorMessage("Internal server error. Retry later or contact the administrator.");

	@Override
	public Response toResponse(final ServerErrorException exception)
	{
		/*
		 * Hide any internal server error info to clients
		 */
		if (exception instanceof InternalServerErrorException)
		{
			LOGGER.error(INTERNAL_ERR_MSG, exception);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ERROR).build();
		}

		return exception.getResponse();
	}
}
