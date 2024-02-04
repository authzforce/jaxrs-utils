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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;

/**
 * FastInfoset request blocker
 *
 */
public final class AcceptMediaTypeCheckingRequestFilter implements ContainerRequestFilter
{
	private static final NotAcceptableException NOT_ACCEPTABLE_EXCEPTION = new NotAcceptableException();

	private final Set<MediaType> validMediaTypes;

	/**
	 * Constructs the filter to allow only specific media types for Accept request header
	 * 
	 * @param mediaTypes
	 *            only accepted media types, besides {@value MediaType#WILDCARD} which is always implicitly allowed
	 */
	public AcceptMediaTypeCheckingRequestFilter(final Iterable<String> mediaTypes)
	{
		final Set<MediaType> updatableSet = StreamSupport.stream(mediaTypes.spliterator(), false).map(MediaType::valueOf).collect(Collectors.toSet());
		updatableSet.add(MediaType.WILDCARD_TYPE);
		validMediaTypes = Collections.unmodifiableSet(updatableSet);
	}

	@Override
	public void filter(final ContainerRequestContext context)
	{
		final List<MediaType> acceptMediaTypes = context.getAcceptableMediaTypes();
		if (acceptMediaTypes.isEmpty())
		{
			return;
		}

		if (validMediaTypes.contains(acceptMediaTypes.get(0)))
		{
			return;
		}

		throw NOT_ACCEPTABLE_EXCEPTION;
	}
}