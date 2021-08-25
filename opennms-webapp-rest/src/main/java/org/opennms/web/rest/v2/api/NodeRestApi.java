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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.opennms.netmgt.model.OnmsMetaDataList;
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
    @Path("{nodeCriteria}/ipinterfaces")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get  ip Interface resource for the context", description = "Get ip Interface resouce for the context",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = NodeIpInterfacesRestService.class))),
            @ApiResponse(responseCode = "204", description = "No corresponding element found",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    NodeIpInterfacesRestService getIpInterfaceResource(@Context final ResourceContext context) ;

    @GET
    @Path("{nodeCriteria}/snmpinterfaces")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get a snap interface resource for the context", description = "Snap interface resource",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = NodeSnmpInterfacesRestService.class))),
            @ApiResponse(responseCode = "204", description = "No corresponding element found",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    NodeSnmpInterfacesRestService getSnmpInterfaceResource(@Context final ResourceContext context) ;

    @GET
    @Path("{nodeCriteria}/hardwareInventory")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get a hardware inventory resource for the context", description = "hardware inventory resource",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = NodeHardwareInventoryRestService.class))),
            @ApiResponse(responseCode = "204", description = "No corresponding element found",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    NodeHardwareInventoryRestService getHardwareInventoryResource(@Context final ResourceContext context) ;

    @GET
    @Path("{nodeCriteria}/categories")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get Categories for the context", description = "categories",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = NodeCategoriesRestService.class))),
            @ApiResponse(responseCode = "204", description = "No corresponding element found",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    NodeCategoriesRestService getCategoriesResource(@Context final ResourceContext l) ;

    @GET
    @Path("{nodeCriteria}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get list of metadata", description = "list of  Metadata",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = OnmsMetaDataList.class))),
            @ApiResponse(responseCode = "204", description = "No corresponding element found",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    OnmsMetaDataList getMetaData(@PathParam("nodeCriteria") String nodeCriteria);

    @GET
    @Path("{nodeCriteria}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get list of metadata for the context", description = "List of Metadata for context",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = OnmsMetaDataList.class))),
            @ApiResponse(responseCode = "204", description = "No corresponding element found",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    OnmsMetaDataList getMetaData(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("context") String context);

    @GET
    @Path("{nodeCriteria}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(summary = "Get list of metadata for context and key", description = "List of metadata for context and key",tags = {"Nodes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",content = @Content(schema = @Schema(implementation = OnmsMetaDataList.class))),
            @ApiResponse(responseCode = "204", description = "No corresponding element found",content = @Content),
            @ApiResponse(responseCode = "404", description = "Node not found",content = @Content)
    })
    OnmsMetaDataList getMetaData(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("context") String context, @PathParam("key") String key);

}

