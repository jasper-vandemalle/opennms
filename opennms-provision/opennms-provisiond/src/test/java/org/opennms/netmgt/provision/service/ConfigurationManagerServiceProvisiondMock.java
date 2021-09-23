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

package org.opennms.netmgt.provision.service;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;

public class ConfigurationManagerServiceProvisiondMock implements ConfigurationManagerService {
    /** Registers a new schema. The schema name must not have been used before. */
    @Override
    public void registerSchema(String configName, String xsdName, String topLevelElement) throws IOException, JAXBException{}

    /** Upgrades an existing schema to a new version. Existing data is validated against the new schema version. */
    @Override
    public void upgradeSchema(String configName, String xsdName, String topLevelElement) throws IOException, JAXBException{}

    @Override
    public Optional<ConfigSchema<?>> getRegisteredSchema(String configName) throws IOException {
        return Optional.empty();
    }

    @Override
    public void registerConfiguration(String configName, String configId, JsonAsString configObject) throws IOException {

    }

    @Override
    public void unregisterConfiguration(String configName, String configId) throws IOException {

    }

    @Override
    public void updateConfiguration(String configName, String configId, JsonAsString configObject) throws IOException {

    }

    @Override
    public Optional<String> getXmlConfiguration(String configName, String configId) throws IOException {
        if ("provisiond".equals(configName)) {
            ProvisiondConfiguration entity = new ProvisiondConfiguration();
            RequisitionDef requisitionDef =  new RequisitionDef();
            requisitionDef.setImportUrlResource("http://localhost");
            requisitionDef.setImportName("test");
            requisitionDef.setCronSchedule("1 * * * * *");
            entity.addRequisitionDef(requisitionDef);
            return Optional.of(JaxbUtils.marshal(entity));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<JSONObject> getJSONConfiguration(String configName, String configId) throws IOException {
        return Optional.empty();
    }

    @Override
    public String getJSONStrConfiguration(String configName, String configId) throws IOException {
        return null;
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigData(String configName) throws IOException {
        return Optional.empty();
    }

    @Override
    public Set<String> getConfigNames() throws IOException {
        return null;
    }

    @Override
    public void unregisterSchema(String configName) throws IOException {

    }

    @Override
    public Set<String> getConfigIds(String configName) throws IOException {
        return null;
    }
}
