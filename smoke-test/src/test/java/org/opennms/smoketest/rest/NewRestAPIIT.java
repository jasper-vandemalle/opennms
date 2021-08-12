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

package org.opennms.smoketest.rest;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.features.newgui.rest.model.IPAddressScanRequestDTO;
import org.opennms.features.newgui.rest.model.SNMPConfigDTO;
import org.opennms.features.newgui.rest.model.SNMPFitRequestDTO;
import org.opennms.features.newgui.rest.model.SNMPFitResultDTO;
import org.opennms.features.newgui.rest.model.ScanResultDTO;
import org.opennms.smoketest.stacks.OpenNMSStack;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

import java.util.Arrays;
import java.util.List;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class NewRestAPIIT {
    private static final String BASE_PATH = "/opennms/rest/nodediscover";
    private static final String PATH_SCAN = BASE_PATH + "/scan";
    private static final String PATH_DETECT = BASE_PATH + "/detect";
    private static final String PATH_PROVISION = BASE_PATH + "/provision";

    @ClassRule
    public static final OpenNMSStack statck = OpenNMSStack.MINIMAL;

    @BeforeClass
    public static void setupGlobal() {
        RestAssured.baseURI = statck.opennms().getBaseUrlExternal().toString();
        RestAssured.port = statck.opennms().getWebPort();
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    @AfterClass
    public static void tearDownGlobal() {
        given().delete("SmokeTests:TestMachine1");
    }

    @Test
    public void testScan() {
        RestAssured.basePath = PATH_SCAN;
        IPAddressScanRequestDTO requestObj = new IPAddressScanRequestDTO();
        requestObj.setLocation("Default");
        requestObj.setStartIP("127.0.0.1");
        requestObj.setStartIP("127.0.0.3");
        String requestData = new JSONObject(requestObj).toString();
        ScanResultDTO result = given().body(requestData).contentType(ContentType.JSON).post()
                .then().statusCode(200).assertThat()
                .body("size()", is(2)).extract().as(ScanResultDTO.class);

        assertThat(result.getLocation(), is(requestObj.getLocation()));
        assertThat(result.getScanResults().size(), is(3));
        result.getScanResults().forEach(r -> {
            assertThat(r.getHostname(), notNullValue());
            assertThat(r.getIpAddress(), greaterThanOrEqualTo(requestObj.getStartIP()));
            assertThat(r.getIpAddress(), lessThanOrEqualTo(requestObj.getEndIP()));
            assertThat(r.getRtt(), greaterThan(0D));
        });
    }

    @Test
    public void testDetect() {
        RestAssured.basePath = PATH_DETECT;
        SNMPFitRequestDTO requestObj = new SNMPFitRequestDTO();
        requestObj.setLocation("Default");
        requestObj.setIpAddresses(Arrays.asList("127.0.0.1", "127.0.0.2"));
        SNMPConfigDTO config1 = new SNMPConfigDTO();
        config1.setCommunityString("test");
        config1.setRetry(1);
        config1.setTimeout(300);
        config1.setSecurityLevel(1);

        SNMPConfigDTO config2 = new SNMPConfigDTO();
        config2.setCommunityString("test2");
        config2.setRetry(1);
        config2.setTimeout(300);
        config2.setSecurityLevel(1);
        requestObj.setConfigurations(Arrays.asList(config1, config2));
        String requestData = new JSONObject(requestObj).toString();
        List<SNMPFitResultDTO> result = given().body(requestData).contentType(ContentType.JSON).post()
                .then().statusCode(200).assertThat()
                .body("size()", is(4))
                .extract().body().jsonPath().getList(".", SNMPFitResultDTO.class);
    }
}
