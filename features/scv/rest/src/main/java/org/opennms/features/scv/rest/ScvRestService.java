/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.scv.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/scv")
public interface ScvRestService {

    /**
     * Get Credentials for an alias.
     *
     * @param alias alias for the credentials.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{alias}")
    Response getCredentials(@PathParam("alias") String alias);

    /**
     * Get set of aliases
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response getAliases();

    /**
     * Add Credentials to Scv
     *
     * @param credentialsDTO Credentials with an alias
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    Response addCredentials(CredentialsDTO credentialsDTO);

    /**
     * Add Credentials to Scv
     *
     * @param credentialsDTO Credentials with an alias
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("{alias}")
    Response editCredentials(@PathParam("alias") String alias, CredentialsDTO credentialsDTO);


}
