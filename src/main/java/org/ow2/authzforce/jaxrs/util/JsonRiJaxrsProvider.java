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
package org.ow2.authzforce.jaxrs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.ow2.authzforce.xacml.json.model.LimitsCheckingJSONObject;

/**
 * JAX-RS entity provider for {@link JSONObject} input/output
 *
 */
@Produces({ "application/json", "application/*+json" })
@Consumes({ "application/json", "application/*+json" })
@Provider
public final class JsonRiJaxrsProvider implements MessageBodyReader<JSONObject>, MessageBodyWriter<JSONObject>
{
	private static final BadRequestException EMPTY_JSON_OBJECT_BAD_REQUEST_EXCEPTION = new BadRequestException("object cannot be empty");

	private interface JsonObjectFactory
	{
		JSONObject getInstance(final InputStream entityStream) throws ValidationException;
	}

	private static class BaseJsonObjectFactory implements JsonObjectFactory
	{
		protected JSONObject parse(final InputStream entityStream)
		{
			return new JSONObject(new JSONTokener(entityStream));
		}

		protected void schemaValidate(final JSONObject jsonObj)
		{
			// no validation
		}

		@Override
		public final JSONObject getInstance(final InputStream entityStream) throws ValidationException
		{
			final JSONObject jsonObj = parse(entityStream);
			schemaValidate(jsonObj);
			return jsonObj;
		}

	}

	private static final JsonObjectFactory DEFAULT_JSON_TOKENER_FACTORY = new BaseJsonObjectFactory();

	private final JsonObjectFactory jsonObjectFactory;

	/**
	 * Constructs JSON provider using default insecure {@link JSONTokener}. Only for trusted environments or protected by JSON-threat-mitigating proxy (e.g. WAF as in Web Application Firewall)
	 */
	public JsonRiJaxrsProvider()
	{
		jsonObjectFactory = DEFAULT_JSON_TOKENER_FACTORY;
	}

	/**
	 * Constructs JSON provider using default insecure {@link JSONTokener} with single JSON schema validation. Only for trusted environments or protected by JSON-threat-mitigating proxy (e.g. WAF as
	 * in Web Application Firewall)
	 * 
	 * @param schema
	 *            JSON schema, null iff no schema validation shall occur
	 */
	public JsonRiJaxrsProvider(final Schema schema)
	{
		jsonObjectFactory = schema == null ? DEFAULT_JSON_TOKENER_FACTORY : new BaseJsonObjectFactory()
		{

			@Override
			protected void schemaValidate(final JSONObject jsonObj) throws ValidationException
			{
				schema.validate(jsonObj);
			}

		};
	}

	/**
	 * Constructs JSON provider using default insecure {@link JSONTokener} with validation against a given schema depending on the input JSON root property. Only for trusted environments or protected
	 * by JSON-threat-mitigating proxy (e.g. WAF as in Web Application Firewall).
	 * 
	 * @param schemasByPropertyName
	 *            mappings of JSON property names to schemas, defining which schema to apply according to which (root) property the input JSON object has; if {@code schemasByPropertyName} is empty, or
	 *            {@code schemasByPropertyName} does not contain any schema for the input JSON (root) property, no schema validation shall occur. Any input JSON without any root property is considered
	 *            invalid.
	 */
	public JsonRiJaxrsProvider(final Map<String, Schema> schemasByPropertyName)
	{
		jsonObjectFactory = schemasByPropertyName == null || schemasByPropertyName.isEmpty() ? DEFAULT_JSON_TOKENER_FACTORY : new BaseJsonObjectFactory()
		{

			@Override
			protected void schemaValidate(final JSONObject jsonObj) throws ValidationException
			{
				final Iterator<String> keysIt = jsonObj.keys();
				if (!keysIt.hasNext())
				{
					/*
					 * JSONException extends RuntimeException so it is not caught as IllegalArgumentException
					 */
					throw EMPTY_JSON_OBJECT_BAD_REQUEST_EXCEPTION;
				}

				final Schema schema = schemasByPropertyName.get(keysIt.next());
				if (schema != null)
				{
					schema.validate(jsonObj);
				}
			}

		};
	}

	private static class LimitsCheckingJsonObjectFactory extends BaseJsonObjectFactory
	{
		private final int maxJsonStringSize;
		private final int maxNumOfImmediateChildren;
		private final int maxDepth;

		private LimitsCheckingJsonObjectFactory(final int maxJsonStringSize, final int maxNumOfImmediateChildren, final int maxDepth)
		{
			this.maxJsonStringSize = maxJsonStringSize;
			this.maxNumOfImmediateChildren = maxNumOfImmediateChildren;
			this.maxDepth = maxDepth;
		}

		@Override
		protected final JSONObject parse(final InputStream entityStream)
		{
			return new LimitsCheckingJSONObject(entityStream, maxJsonStringSize, maxNumOfImmediateChildren, maxDepth);
		}

	}

	/**
	 * Constructs JSON provider using hardened {@link JSONTokener} that checks limits on JSON structures, such as arrays and strings, in order to mitigate content-level attacks. Downside: it is slower
	 * at parsing than for {@link JsonRiJaxrsProvider#JsonRiJaxrsProvider()}.
	 * 
	 * @param schema
	 *            JSON schema, null iff no schema validation shall occur
	 * 
	 * @param maxJsonStringSize
	 *            allowed maximum size of JSON keys and string values. Negative or zero values not allowed.
	 * @param maxNumOfImmediateChildren
	 *            allowed maximum number of keys (therefore key-value pairs) in JSON object, or items in JSON array. Negative or zero values not allowed.
	 * @param maxDepth
	 *            allowed maximum depth of JSON object. Negative or zero values not allowed.
	 */
	public JsonRiJaxrsProvider(final Schema schema, final int maxJsonStringSize, final int maxNumOfImmediateChildren, final int maxDepth)
	{
		if (maxJsonStringSize <= 0 || maxNumOfImmediateChildren <= 0 || maxDepth <= 0)
		{
			throw new IllegalArgumentException("one of the arguments maxJsonStringSize, maxNumOfImmediateChildren or maxDepth is negative or null");
		}
		jsonObjectFactory = schema == null ? new LimitsCheckingJsonObjectFactory(maxJsonStringSize, maxNumOfImmediateChildren, maxDepth)
		        : new LimitsCheckingJsonObjectFactory(maxJsonStringSize, maxNumOfImmediateChildren, maxDepth)
		        {
			        @Override
			        protected void schemaValidate(final JSONObject jsonObj) throws ValidationException
			        {
				        schema.validate(jsonObj);
			        }

		        };
	}

	/**
	 * Constructs JSON provider using hardened {@link JSONTokener} that checks limits on JSON structures, such as arrays and strings, in order to mitigate content-level attacks. Downside: it is slower
	 * at parsing than for {@link JsonRiJaxrsProvider#JsonRiJaxrsProvider()}. This provider also validates input JSON against a given schema depending on the input JSON root property.
	 * 
	 * @param schemasByPropertyName
	 *            mappings of JSON property names to schemas, defining which schema to apply according to which (root) property the input JSON object has; if {@code schemasByPropertyName} is empty, or
	 *            {@code schemasByPropertyName} does not contain any schema for the input JSON (root) property, no schema validation shall occur. Any input JSON without any root property is considered
	 *            invalid.
	 * 
	 * @param maxJsonStringSize
	 *            allowed maximum size of JSON keys and string values. Negative or zero values not allowed.
	 * @param maxNumOfImmediateChildren
	 *            allowed maximum number of keys (therefore key-value pairs) in JSON object, or items in JSON array. Negative or zero values not allowed.
	 * @param maxDepth
	 *            allowed maximum depth of JSON object. Negative or zero values not allowed.
	 */
	public JsonRiJaxrsProvider(final Map<String, Schema> schemasByPropertyName, final int maxJsonStringSize, final int maxNumOfImmediateChildren, final int maxDepth)
	{
		if (maxJsonStringSize <= 0 || maxNumOfImmediateChildren <= 0 || maxDepth <= 0)
		{
			throw new IllegalArgumentException("one of the arguments maxJsonStringSize, maxNumOfImmediateChildren or maxDepth is negative or null");
		}

		jsonObjectFactory = schemasByPropertyName == null || schemasByPropertyName.isEmpty() ? new LimitsCheckingJsonObjectFactory(maxJsonStringSize, maxNumOfImmediateChildren, maxDepth)
		        : new LimitsCheckingJsonObjectFactory(maxJsonStringSize, maxNumOfImmediateChildren, maxDepth)
		        {
			        @Override
			        protected void schemaValidate(final JSONObject jsonObj) throws ValidationException
			        {
				        final Iterator<String> keysIt = jsonObj.keys();
				        if (!keysIt.hasNext())
				        {
					        /*
					         * JSONException extends RuntimeException so it is not caught as IllegalArgumentException
					         */
					        throw EMPTY_JSON_OBJECT_BAD_REQUEST_EXCEPTION;
				        }

				        final Schema schema = schemasByPropertyName.get(keysIt.next());
				        if (schema != null)
				        {
					        schema.validate(jsonObj);
				        }
			        }

		        };
	}

	@Override
	public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType)
	{
		return JSONObject.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(final JSONObject o, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType)
	{
		return -1;
	}

	@Override
	public void writeTo(final JSONObject o, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
	        final OutputStream entityStream) throws IOException, WebApplicationException
	{
		try (final OutputStreamWriter writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8))
		{
			o.write(writer);
		}
	}

	@Override
	public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType)
	{
		return JSONObject.class.isAssignableFrom(type);
	}

	@Override
	public JSONObject readFrom(final Class<JSONObject> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
	        final InputStream entityStream) throws IOException, WebApplicationException
	{
		try
		{
			return jsonObjectFactory.getInstance(entityStream);
		}
		catch (final JSONException | ValidationException e)
		{
			/*
			 * JSONException/ValidationException extend RuntimeException so it is not caught as IllegalArgumentException
			 */
			throw new BadRequestException(e);
		}
		catch (final IllegalArgumentException e)
		{
			// exception related to limits checking
			throw new ClientErrorException(Status.REQUEST_ENTITY_TOO_LARGE, e);
		}
	}

}
