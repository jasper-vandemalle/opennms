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

package org.opennms.features.newgui.rest.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.opennms.features.newgui.rest.NodeDiscoverRestService;
import org.opennms.features.newgui.rest.model.ScanResultDTO;
import org.opennms.features.newgui.rest.model.FitRequest;
import org.opennms.features.newgui.rest.model.IPAddressScanRequestDTO;
import org.opennms.features.newgui.rest.model.IPScanResult;
import org.opennms.features.newgui.rest.model.SNMPFitRequestDTO;
import org.opennms.features.newgui.rest.model.SNMPFitResultDTO;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.icmp.proxy.PingSweepSummary;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDiscoverServiceImpl implements NodeDiscoverRestService {
    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscoverServiceImpl.class);
    private final LocationAwarePingClient locationAwarePingClient;
    private final LocationAwareSnmpClient locationAwareSnmpClient;

    private static final String DEFAULT_SYS_OBJECTID_INSTANCE = ".1.3.6.1.2.1.1.2.0";

    public NodeDiscoverServiceImpl(LocationAwarePingClient locationAwarePingClient,
                                   LocationAwareSnmpClient locationAwareSnmpClient) {
        this.locationAwarePingClient = locationAwarePingClient;
        this.locationAwareSnmpClient = locationAwareSnmpClient;
    }

    @Override
    public List<ScanResultDTO> discoverByRange(List<IPAddressScanRequestDTO> ipRangeList) {
        List<ScanResultDTO> results = new ArrayList<>();
        Map<IPAddressScanRequestDTO, CompletableFuture<PingSweepSummary>> futureMap = new HashMap<>();

        ipRangeList.forEach(ipRange -> {
            try {

                CompletableFuture<PingSweepSummary> future = locationAwarePingClient.sweep().withRange(InetAddress.getByName(ipRange.getStartIP()), InetAddress.getByName(ipRange.getEndIP()))
                        .withLocation(ipRange.getLocation())
                        .execute().handle((v, t) -> {
                            if(t != null) {
                                LOG.debug("Error happened during scan ip range from {}, {} with location {}",
                                        ipRange.getStartIP(), ipRange.getEndIP(), ipRange.getLocation());
                            }
                            return v;
                        });
                futureMap.put(ipRange, future);

            } catch (UnknownHostException e) {
                LOG.error("Invalid IP range start {}, end {}", ipRange.getStartIP(), ipRange.getEndIP());
            }
        });

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futureMap.values().toArray(new CompletableFuture[0]));
        while (true) {
            try {
                combinedFuture.get(1, TimeUnit.SECONDS);
                for (IPAddressScanRequestDTO key : futureMap.keySet()) {
                    PingSweepSummary summary = futureMap.get(key).get();
                    if (summary!=null && !summary.getResponses().isEmpty()) {
                        List<IPScanResult> scanResults = new ArrayList<>();
                        summary.getResponses().forEach((address, rtt) -> scanResults.add(new IPScanResult(address.getHostName(), address.getHostAddress(), rtt)));
                        ScanResultDTO resultDTO = new ScanResultDTO(key.getLocation(), scanResults);
                        results.add(resultDTO);
                    } else {
                        LOG.info("No response from any IP address in the range of {} to {}", key.getStartIP(), key.getEndIP());
                    }
                }
                break;
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Error happened during the IP scanning", e);
                break;
            } catch (TimeoutException e) {
                // continue
            }
        }
        return results;
    }

    @Override
    public List<SNMPFitResultDTO> fitSNMP(List<SNMPFitRequestDTO> requestList) {
        List<SNMPFitResultDTO> results = new ArrayList<>();
        SnmpObjId objId = SnmpObjId.get(DEFAULT_SYS_OBJECTID_INSTANCE);
        Map<FitRequest, CompletableFuture<SnmpValue>> futureMap = new HashMap<>();
        buildRequestFromDTO(requestList).forEach(r -> {
            try {
                InetAddress inetAddress = InetAddress.getByName(r.getIpAddress());
                SnmpAgentConfig agentConfig = new SnmpAgentConfig();
                agentConfig.setAddress(inetAddress);
                if(StringUtils.isNotEmpty(r.getConfig().getCommunityString())) {
                    agentConfig.setWriteCommunity(r.getConfig().getCommunityString());
                    agentConfig.setReadCommunity(r.getConfig().getCommunityString());
                }
                agentConfig.setSecurityLevel(SnmpAgentConfig.DEFAULT_SECURITY_LEVEL);
                agentConfig.setRetries(r.getConfig().getRetry());
                agentConfig.setTimeout(r.getConfig().getTimeout());
                CompletableFuture<SnmpValue> future = locationAwareSnmpClient.get(agentConfig, objId)
                        .withLocation(r.getLocation())
                        .execute().handle((v, t)-> {
                            if(t != null) {
                                LOG.debug("Error happened when detect SNMP: location {}, IP {}, community string {}, " +
                                        "security Leve {}", r.getLocation(), r.getIpAddress(),
                                        r.getConfig().getCommunityString(), r.getConfig().getSecurityLevel());
                            }
                            return v;
                        });
                futureMap.put(r, future);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futureMap.values().toArray(new CompletableFuture[0]));
        while (true) {
            try {
                combinedFuture.get(1, TimeUnit.SECONDS);
                for (FitRequest request : futureMap.keySet()) {
                    InetAddress inetAddress = InetAddress.getByName(request.getIpAddress());
                    SNMPFitResultDTO resultDTO = new SNMPFitResultDTO();
                    resultDTO.setHostname(inetAddress.getHostName());
                    resultDTO.setIpAddress(inetAddress.getHostAddress());
                    resultDTO.setLocation(request.getLocation());
                    resultDTO.setCommunityString(request.getConfig().getCommunityString());
                    results.add(resultDTO);
                    SnmpValue snmpValue = futureMap.get(request).get();
                    if (snmpValue != null && !snmpValue.isError()) {
                        resultDTO.setSysOID(snmpValue.toString());
                    }
                }
                break;
            } catch (InterruptedException | ExecutionException | UnknownHostException e) {
                LOG.error("Couldn't find SNMP service", e);
                break;
            } catch (TimeoutException e) {
                //continue
            }
        }
        return results;
    }

    private List<FitRequest> buildRequestFromDTO(List<SNMPFitRequestDTO> requestData) {
        List<FitRequest> list = new ArrayList<>();
        requestData.forEach(r -> r.getIpAddresses().forEach(ip -> r.getConfigurations().forEach(config -> list.add(new FitRequest(r.getLocation(), ip, config)))));
        return list;
    }
}
