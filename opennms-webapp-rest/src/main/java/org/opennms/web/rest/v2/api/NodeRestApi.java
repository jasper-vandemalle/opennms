/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.netmgt.model.OnmsMetaDataList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v2.NodeCategoriesRestService;
import org.opennms.web.rest.v2.NodeHardwareInventoryRestService;
import org.opennms.web.rest.v2.NodeIpInterfacesRestService;
import org.opennms.web.rest.v2.NodeSnmpInterfacesRestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("nodes")
public interface NodeRestApi {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get all types of node", description = "Get all types of node  ", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Nodes not found",content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(summary = "Get nodes count", description = "Get a count's all types of nodes", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get the properties", description = "Get all types of properties", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "peoperties not found",content = @Content)
    })
    Response getProperties(@QueryParam("q") final String query) ;

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a properties ", description = "Get a properties  for a specific propertyId", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No corresponding propertyId found",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    Response getPropertyValues(@PathParam("propertyId") final String propertyId, @QueryParam("q") final String query, @QueryParam("limit") final Integer limit) ;

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get node by id", description = "Get node by id", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No corresponding Id found",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    Response get(@Context final UriInfo uriInfo, @PathParam("id") final String id) ;

    @POST
    @Path("{id}")
    @Operation(summary = "Create a node", description = "Create a node by id", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",content = @Content)
    })
    Response createSpecific() ;

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Create a node", description = "Create a node", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "201", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",content = @Content)
    })
    Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, OnmsNode object) ;

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Update many nodes", description = "Update many nodes", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) ;

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(summary = "Update  a node", description = "Update a node by id", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    Response update(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final Integer id, final OnmsNode object);

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(summary = "Update  property of node", description = "Update properties of a node  by id", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    Response updateProperties(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id, final MultivaluedMapImpl params) ;

    @DELETE
    @Operation(summary = " Delete Many  nodes", description = "Delete Many  nodes", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext) ;

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a node", description = "Delete a node by Id", tags = {"Nodes"})
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    Response delete(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final String id) ;

    @Path("{nodeCriteria}/ipinterfaces")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get  ip Interface resource for the context", description = "Get ip Interface resource for the context",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = NodeIpInterfacesRestService.class))),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    NodeIpInterfacesRestService getIpInterfaceResource(@Context final ResourceContext context) ;

    @Path("{nodeCriteria}/snmpinterfaces")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get a snap interface resource for the context", description = "Snap interface resource",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = NodeSnmpInterfacesRestService.class))),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    NodeSnmpInterfacesRestService getSnmpInterfaceResource(@Context final ResourceContext context) ;

    @Path("{nodeCriteria}/hardwareInventory")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get a hardware inventory resource for the context", description = "hardware inventory resource",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = NodeHardwareInventoryRestService.class))),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    NodeHardwareInventoryRestService getHardwareInventoryResource(@Context final ResourceContext context) ;

    @Path("{nodeCriteria}/categories")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get Categories for the context", description = "categories",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = NodeCategoriesRestService.class))),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    NodeCategoriesRestService getCategoriesResource(@Context final ResourceContext l) ;

    @GET
    @Path("{nodeCriteria}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get list of metadata", description = "list of  Metadata",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = OnmsMetaDataList.class))),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    OnmsMetaDataList getMetaData(@PathParam("nodeCriteria") String nodeCriteria);

    @GET
    @Path("{nodeCriteria}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get list of metadata for the context", description = "List of Metadata for context",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = OnmsMetaDataList.class))),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    OnmsMetaDataList getMetaData(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("context") String context);

    @GET
    @Path("{nodeCriteria}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get list of metadata for context and key", description = "List of metadata for context by key",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = OnmsMetaDataList.class))),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    OnmsMetaDataList getMetaData(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("context") String context, @PathParam("key") String key);
}

