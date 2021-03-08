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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB-annotated class representing error message
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "message", "cause" })
@XmlRootElement(name = "error")
public class JaxbErrorMessage
{

	@XmlElement(required = true)
	protected String message;

	@XmlElement
	protected JaxbErrorMessage cause;

	/**
	 * Fully-initialising value constructor
	 * 
	 * @param message
	 *            error message
	 * @param cause
	 *            optional cause for the error
	 * 
	 */
	public JaxbErrorMessage(final String message, final JaxbErrorMessage cause)
	{
		this.message = message;
		this.cause = cause;
	}

	/**
	 * Required no-arg constructor
	 * 
	 */
	public JaxbErrorMessage()
	{
		this("", null);
	}

	/**
	 * Gets the value of the message property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * Gets the value of the cause property.
	 * 
	 * @return possible object is {@link JaxbErrorMessage }
	 * 
	 */
	public JaxbErrorMessage getCause()
	{
		return cause;
	}

}
