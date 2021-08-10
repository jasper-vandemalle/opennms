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

package org.opennms.features.config.rest.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.opennms.features.config.dao.api.ConfigItem;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.info.Info;

public class SwaggerConverter {

    private final Map<ConfigItem, Schema<?>> schemasByItem = new LinkedHashMap<>();
    private final Map<ConfigItem, String> pathsByItem = new LinkedHashMap<>();
    private final Map<String, PathItem> pathItemsByPath = new LinkedHashMap<>();

    private final OpenAPI openAPI = new OpenAPI();

    private String prefix = "/";

    public String convertToString(ConfigItem item, String prefix) {
        OpenAPI openapi = convert(item, prefix);

        try {
            return convertOpenAPIToString(openapi);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    static public String convertOpenAPIToString(OpenAPI openapi) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        final String intermediateJson = objectMapper.writeValueAsString(openapi);
        final String almostSwaggerJson = intermediateJson.replaceAll("[\\n\\r\\s]*\"exampleSetFlag\".*,", "");
        return almostSwaggerJson.replaceAll(",?[\\n\\r\\s]*\"exampleSetFlag\".*", "");

    }
    public OpenAPI convert(ConfigItem item, String prefix) {
        this.prefix = prefix;

        // Create an empty set of components
        Components components = new Components();
        openAPI.setComponents(components);

         // Create a basic info section
         Info info = new Info();
         info.setDescription("OpenNMS Data Model");
         info.setVersion("1.0.0");
         info.setTitle("OpenNMS Model");

         openAPI.setInfo(info);
        // Generate schemas for the items
        walk(null, item, this::generateSchemasForItems);
        schemasByItem.forEach((k,v) -> {
            if (ConfigItem.Type.OBJECT.equals(k.getType())) {
                components.addSchemas(v.getName(),v);
            }
        });

        // Create an empty set of paths
        Paths paths = new Paths();
        openAPI.setPaths(paths);

        // Generate paths for the items
        walk(null, item, this::generatePathsForItems);
        pathItemsByPath.forEach(paths::addPathItem);

        return openAPI;
    }

    private void generatePathsForItems(ConfigItem parent, ConfigItem item) {
        // Skip simple types - they can be set on the parent object and have no children
        if (item.getType().isSimple()) {
            return;
        }

        // Build the path to this element
        boolean isParentAnArray = false;
        String path;
        if (parent != null) {
            path = pathsByItem.get(parent);
            if (ConfigItem.Type.ARRAY.equals(parent.getType())) {
                isParentAnArray = true;
                path += "/{" + item.getName() + "Index}";
            } else {
                path += "/" + item.getName();
            }
        } else {
            path = prefix;
        }

        // Index the path for future reference
        pathsByItem.put(item, path);

        Schema schemaForCurrentItem = new Schema();
        schemaForCurrentItem.setName(item.getName());
        schemaForCurrentItem.set$ref("#/components/schemas/" + item.getName());


        PathItem pathItem = new PathItem();

        List<String> urlParameters = buildUrlParamList(path);

        for (Iterator<String> iterator = urlParameters.iterator(); iterator.hasNext(); ) {
            String param = iterator.next();
            Parameter parameter = new Parameter();
            parameter.setName(param);
            parameter.setIn("path");
            parameter.setSchema(new IntegerSchema());
            parameter.setRequired(true);
            parameter.setDescription("Index of item in the array");
            pathItem.addParametersItem(parameter);
        }

        String tagName = getTagName(path);

        //============= POST =================
        Operation post = new Operation();
        post.tags(Arrays.asList(tagName));
        post.summary("Configure " + item.getName());
        pathItem.setPost(post);

        // Request body for POST
        RequestBody requestBody = new RequestBody();
        Content jsonObjectContent = new Content();
        MediaType mediaType = new MediaType();
        mediaType.schema(schemaForCurrentItem);
        jsonObjectContent.addMediaType("application/json", mediaType);
        requestBody.setContent(jsonObjectContent);
        post.requestBody(requestBody);

        // 200 OK for POST
        ApiResponses postApiResponses = new ApiResponses();
        ApiResponse postOkApiResponse = new ApiResponse();
        postOkApiResponse.setDescription("OK");
        postApiResponses.addApiResponse("200", postOkApiResponse);
        post.responses(postApiResponses);

        //============== PUT =================
        Operation put = new Operation();
        put.tags(Arrays.asList(tagName));
        put.summary("Overwrite " + item.getName());
        pathItem.setPut(put);

        // Request body for PUT
        RequestBody requestBodyPut = new RequestBody();
        Content jsonObjectContentPut = new Content();
        MediaType mediaTypePut = new MediaType();
        mediaTypePut.schema(schemaForCurrentItem);
        jsonObjectContentPut.addMediaType("application/json", mediaTypePut);
        requestBodyPut.setContent(jsonObjectContentPut);
        put.requestBody(requestBodyPut);

        // 200 OK for PUT
        ApiResponses putApiResponses = new ApiResponses();
        ApiResponse putOkApiResponse = new ApiResponse();
        putOkApiResponse.setDescription("OK");
        putApiResponses.addApiResponse("200", putOkApiResponse);
        put.responses(putApiResponses);

        //============== PATCH =================
        Operation patch = new Operation();
        patch.tags(Arrays.asList(tagName));
        patch.summary("Overwrite " + item.getName());
        pathItem.setPatch(patch);

        // Request body for PATCH
        RequestBody requestBodyPatch = new RequestBody();
        Content jsonObjectContentPatch = new Content();
        MediaType mediaTypePatch = new MediaType();
        mediaTypePatch.schema(schemaForCurrentItem);
        jsonObjectContentPatch.addMediaType("application/json", mediaTypePatch);
        requestBodyPatch.setContent(jsonObjectContentPatch);
        patch.requestBody(requestBodyPatch);

        // 200 OK for PATCH
        ApiResponses patchApiResponses = new ApiResponses();
        ApiResponse patchOkApiResponse = new ApiResponse();
        patchOkApiResponse.setDescription("OK");
        patchApiResponses.addApiResponse("200", patchOkApiResponse);
        patch.responses(patchApiResponses);

        //============== GET =================
        Operation get = new Operation();
        get.tags(Arrays.asList(tagName));
        get.summary("Get " + item.getName() + " configuration");

        // 200 OK for GET
        ApiResponses getApiResponses = new ApiResponses();
        ApiResponse getOkResponse = new ApiResponse();
        getOkResponse.setDescription(item.getName() + " configuration");
        getOkResponse.setContent(jsonObjectContent);
        getApiResponses.addApiResponse("200", getOkResponse);
        get.responses(getApiResponses);
        pathItem.setGet(get);

        //============== DELETE =================
        Operation delete = new Operation();
        delete.tags(Arrays.asList(tagName));
        delete.summary("Delete " + item.getName() + " configuration");

        // 200 OK for DELETE
        ApiResponses deleteApiResponses = new ApiResponses();
        ApiResponse deleteOkResponse = new ApiResponse();
        deleteOkResponse.setDescription(item.getName() + " configuration");
        deleteApiResponses.addApiResponse("200", deleteOkResponse);
        delete.responses(deleteApiResponses);
        pathItem.setDelete(delete);

        // Save
        pathItemsByPath.put(path, pathItem);
    }

    private String getTagName(String path) {
        String releventPath = path.replace(prefix, "");

        if (releventPath.isEmpty()) {
            // Top level config - use the last part of the prefix, should be the service name
            String[] prefixElements = path.split("/");
            return prefixElements[prefixElements.length-1];
        }

        String[] pathElements = releventPath.split("/");
        if (pathElements.length < 2) {
            return releventPath;
        }
        return pathElements[1];
    }

    private List<String> buildUrlParamList(String path) {
        List<String> params = new ArrayList();
        Matcher m = Pattern.compile("\\{[\\w\\-\\.]+\\}").matcher(path);
        while (m.find()) {
            String urlItem = m.group();
            params.add(urlItem.substring(1, (urlItem.length()-1)));
        }
        return params;
    }

    private void generateSchemasForItems(ConfigItem parent, ConfigItem item) {
        final Schema<?> schema;
        switch (item.getType()) {
            case OBJECT:
                schema = new ObjectSchema();
                break;
            case ARRAY:
                schema = new ArraySchema();
                break;
            case STRING:
                schema = new StringSchema();
                break;
            case NUMBER:
                schema = new NumberSchema();
                break;
            case INTEGER:
                schema = new IntegerSchema();
                break;
            case LONG:
                schema = new NumberSchema();
                schema.setFormat("int64");
                break;
            case BOOLEAN:
                schema = new BooleanSchema();
                break;
            default:
                throw new RuntimeException("Unsupported type " + item);
        }
        schema.setName(item.getName());

        if (item.isMinSet()) {
            schema.setMinimum(BigDecimal.valueOf(item.getMin()));
        }
        if (item.isMaxSet()) {
            schema.setMaximum(BigDecimal.valueOf(item.getMax()));
        }
        if (parent != null) {
            // Add the item to the parent
            Schema<?> schemaForParent = schemasByItem.get(parent);
            Schema<?> schemaForCurrentItem = schema;
            if (ConfigItem.Type.OBJECT.equals(item.getType())) {
                // Use a reference - these have no actual type set
                schemaForCurrentItem = new Schema();
                schemaForCurrentItem.setName(schema.getName());
                schemaForCurrentItem.set$ref("#/components/schemas/" + schema.getName());
            }

            if (ConfigItem.Type.ARRAY.equals(parent.getType())) {
                // If the parent is an array, then add the the child as an item, and not a property
                ((ArraySchema)schemaForParent).setItems(schemaForCurrentItem);
            } else {
                schemaForParent.addProperties(schemaForCurrentItem.getName(), schemaForCurrentItem);
                if (item.isRequired()) {
                    schemaForParent.addRequiredItem(schemaForCurrentItem.getName());
                }
            }

        }

        // Index the schema for future reference
        schemasByItem.put(item, schema);
    }

    public static void walk(ConfigItem parent, ConfigItem item, BiConsumer<ConfigItem, ConfigItem> consumer) {
        consumer.accept(parent, item);
        for (ConfigItem childItem : item.getChildren()) {
            walk(item, childItem, consumer);
        }
    }

}