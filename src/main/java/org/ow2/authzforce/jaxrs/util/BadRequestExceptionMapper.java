/*
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
package org.ow2.authzforce.jaxrs.util;

import java.beans.ConstructorProperties;
import java.util.regex.Pattern;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * JAX-RS {@link ExceptionMapper} for {@link BadRequestException}
 */
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BadRequestExceptionMapper.class);
	private static final Pattern JAXBEXCEPTION_MSG_START_PATTERN = Pattern.compile("^JAXBException occurred :", Pattern.CASE_INSENSITIVE);
	private static final String INVALID_PARAM_MSG_PREFIX = "Invalid parameters: ";
	private final int verbosityLevel;

	private static JaxbErrorMessage newJaxbErrorMessage(final Throwable cause, final int errVerbosityLevel)
	{
		if (errVerbosityLevel == 0 || cause == null)
		{
			return null;
		}

		return new JaxbErrorMessage(cause.getMessage(), newJaxbErrorMessage(cause.getCause(), errVerbosityLevel - 1));
	}

	/**
	 * Constructor
	 * 
	 * @param verbosityLevel
	 *            level of verbosity of error information, i.e. depth of exception stacktrace to include in the response returned from {@link #toResponse(BadRequestException)}. Not applicable for
	 *            {@link SAXException}, {@link JAXBException} or {@link ClassCastException}.
	 */
	@ConstructorProperties({ "verbosityLevel" })
	public BadRequestExceptionMapper(final int verbosityLevel)
	{
		if (verbosityLevel < 0)
		{
			throw new IllegalArgumentException("Invalid verbosity level: " + verbosityLevel + ". Expected >= 0.");
		}

		this.verbosityLevel = verbosityLevel;
	}

	/**
	 * Default constructor
	 */
	public BadRequestExceptionMapper()
	{
		this(0);
	}

	@Override
	public Response toResponse(final BadRequestException exception)
	{
		LOGGER.info("Bad request", exception);
		final Response oldResp = exception.getResponse();
		final String errMsg;
		final Throwable cause = exception.getCause();
		final Throwable returnedCause;

		if (verbosityLevel == 0)
		{
			errMsg = "";
			returnedCause = null;
		}
		else if (cause == null)
		{
			returnedCause = null;

			/*
			 * handle case where cause message is only in the response message (no exception object in stacktrace), e.g. JAXBException
			 */
			final Object oldEntity = oldResp.getEntity();
			if (oldEntity instanceof String)
			{
				// hide "JAXBException..." when it occurs and only keep the JAXBException message
				errMsg = JAXBEXCEPTION_MSG_START_PATTERN.matcher((String) oldEntity).replaceFirst(INVALID_PARAM_MSG_PREFIX);
			}
			else
			{
				return oldResp;
			}

		}
		else
		{
			/*
			 * cause != null && verbosityLevel >= 1
			 */
			// JAXB schema validation error
			if (cause instanceof SAXException)
			{
				final Throwable internalCause = cause.getCause();
				if (internalCause instanceof JAXBException)
				{
					final Throwable linkedEx = ((JAXBException) internalCause).getLinkedException();
					errMsg = INVALID_PARAM_MSG_PREFIX + linkedEx.getMessage();
					returnedCause = linkedEx.getCause();
				}
				else
				{
					errMsg = INVALID_PARAM_MSG_PREFIX + cause.getMessage();
					returnedCause = cause.getCause();
				}
			}
			else if (cause instanceof JAXBException)
			{
				final Throwable linkedEx = ((JAXBException) cause).getLinkedException();
				errMsg = INVALID_PARAM_MSG_PREFIX + linkedEx.getMessage();
				returnedCause = linkedEx.getCause();
			}
			else if (cause instanceof ClassCastException)
			{
				errMsg = "Wrong type of input: " + cause.getMessage();
				returnedCause = null;
			}
			else
			{
				errMsg = cause.getMessage();
				returnedCause = cause.getCause();
			}
		}

		final JaxbErrorMessage errorEntity = errMsg == null ? new JaxbErrorMessage("", null) : new JaxbErrorMessage(errMsg, newJaxbErrorMessage(returnedCause, verbosityLevel - 1));
		return Response.status(Response.Status.BAD_REQUEST).entity(errorEntity).build();
	}

}
