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


import io.swagger.v3.oas.models.OpenAPI;
import org.opennms.features.config.dao.api.ServiceSchema;
import org.opennms.features.config.rest.api.ApiManagerService;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Component
public class ApiManagerServiceImpl implements ApiManagerService {

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    public void setConfigurationManagerService(ConfigurationManagerService configurationManagerService) {
        this.configurationManagerService = configurationManagerService;
    }

//
//    public ApiManagerServiceImpl(ConfigurationManagerService configurationManagerService) {
//        this.configurationManagerService = configurationManagerService;
//    }

    @Override
    public OpenAPI getApiForConfiguration(final String serviceName) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(configurationManagerService);

        final Optional<ServiceSchema> schema = configurationManagerService.getRegisteredSchema(serviceName);

        SwaggerConverter swaggerConverter = new SwaggerConverter();
        return swaggerConverter.convert(schema.get().getConfigItem(), "/configurationManagerService/config/" + serviceName);
    }

    @Override
    public String getStringApiForConfiguration(final String serviceName) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(configurationManagerService);

        final Optional<ServiceSchema> schema = configurationManagerService.getRegisteredSchema(serviceName);

        SwaggerConverter swaggerConverter = new SwaggerConverter();
        return swaggerConverter.convertToString(schema.get().getConfigItem(), "/configurationManagerService/config/" + serviceName);
    }
}
