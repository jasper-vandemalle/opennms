/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.ValidationReport;
import io.swagger.v3.oas.models.OpenAPI;
import org.opennms.features.config.rest.api.ApiManagerService;
import org.opennms.features.config.rest.api.ServiceValidator;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class SwaggerServiceValidator implements ServiceValidator {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerServiceValidator.class);

    private Map<String, OpenApiInteractionValidator> validators;

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    @Autowired
    private ApiManagerService apiManagerService;

    @Override
    public ValidationReport validateRequest(String serviceName, HttpServletRequest req) throws IOException {
        return validateRequest(serviceName, req, null);
    }

    @Override
    public ValidationReport validateRequest(String serviceName, HttpServletRequest req, String body) throws IOException {
        if (this.validators == null) {
            init();
        }
        OpenApiInteractionValidator validator = validators.get(serviceName);
        if (validator == null) {
            // FIXME: Need nicer error handling here
            throw new RuntimeException("No service found for: " + serviceName + ". Known services include: " + validators.keySet());
        }
        Request.Method method = Request.Method.valueOf(req.getMethod());
        Request request = null;

        switch (method) {
            case POST:
                request = SimpleRequest.Builder
                        .post(req.getPathInfo())
                        .withBody(body)
                        .withContentType(req.getContentType())
                        .build();
                break;
            case PATCH:
                request = SimpleRequest.Builder
                        .patch(req.getPathInfo())
                        .withBody(body)
                        .withContentType(req.getContentType())
                        .build();
                break;
            case PUT:
                request = SimpleRequest.Builder
                        .put(req.getPathInfo())
                        .withBody(body)
                        .withContentType(req.getContentType())
                        .build();
                break;
            case GET:
                request = SimpleRequest.Builder
                        .get(req.getPathInfo())
                        .withBody(body)
                        .withContentType(req.getContentType())
                        .build();
                break;
            case DELETE:
                request = SimpleRequest.Builder
                        .delete(req.getPathInfo())
                        .withBody(body)
                        .withContentType(req.getContentType())
                        .build();
                break;
        }
        return validator.validateRequest(request);
    }

    void init() throws IOException {
        // FIXME: Needs to be dynamic
        Map<String, OpenApiInteractionValidator> validators = new LinkedHashMap<>();
        Set<String> serviceNames = configurationManagerService.getServiceIds();
        for (String serviceName : serviceNames) {
            try {
                OpenAPI api = apiManagerService.getApiForConfiguration(serviceName);
                OpenApiInteractionValidator validator = OpenApiInteractionValidator.createFor(api).build();
                validators.put(serviceName, validator);
            } catch (RuntimeException | ClassNotFoundException e) {
                LOG.warn("No schema found for {}. Skipping.", serviceName);
            }
        }
        this.validators = validators;
    }
}
