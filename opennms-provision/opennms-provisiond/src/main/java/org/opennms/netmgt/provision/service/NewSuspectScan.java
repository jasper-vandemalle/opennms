/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static org.opennms.core.utils.InetAddressUtils.str;
import static org.opennms.netmgt.provision.service.ProvisionService.ABORT;
import static org.opennms.netmgt.provision.service.ProvisionService.ERROR;
import static org.opennms.netmgt.provision.service.ProvisionService.FOREIGN_ID;
import static org.opennms.netmgt.provision.service.ProvisionService.FOREIGN_SOURCE;
import static org.opennms.netmgt.provision.service.ProvisionService.LOCATION;
import static org.opennms.netmgt.provision.service.ProvisionService.NODE_ID;

import java.net.InetAddress;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.core.tasks.Task;
import org.opennms.core.tasks.TaskCoordinator;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.Span;

/**
 * <p>NewSuspectScan class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NewSuspectScan implements Scan {
    private static final Logger LOG = LoggerFactory.getLogger(NewSuspectScan.class);
    private String m_location;
    private InetAddress m_ipAddress;
    private ProvisionService m_provisionService;
    private EventForwarder m_eventForwarder;
    private SnmpAgentConfigFactory m_agentConfigFactory;
    private TaskCoordinator m_taskCoordinator;
	private String m_foreignSource;
    private Span m_span;
    private ProvisionMonitor monitor;
	
    /**
     * <p>Constructor for NewSuspectScan.</p>
     *
     * @param ipAddress a {@link java.net.InetAddress} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @param eventForwarder a {@link org.opennms.netmgt.events.api.EventForwarder} object.
     * @param agentConfigFactory a {@link org.opennms.netmgt.config.api.SnmpAgentConfigFactory} object.
     * @param taskCoordinator a {@link org.opennms.core.tasks.TaskCoordinator} object.
     * @param monitor a {@link org.opennms.netmgt.provision.service.operations.ProvisionMonitor} object. (optional)
     */
    public NewSuspectScan(final InetAddress ipAddress, final ProvisionService provisionService, final EventForwarder eventForwarder, final SnmpAgentConfigFactory agentConfigFactory, final TaskCoordinator taskCoordinator, String foreignSource, final String location, final ProvisionMonitor monitor) {
        m_ipAddress = ipAddress;
        m_provisionService = provisionService;
        m_eventForwarder = eventForwarder;
        m_agentConfigFactory = agentConfigFactory;
        m_taskCoordinator = taskCoordinator;
        m_foreignSource = foreignSource;
        m_location = location;
        this.monitor = monitor;
    }
    
    @Override
    public Task createTask() {
        return m_taskCoordinator.createBatch().add(this).get();
    }
    
    /** {@inheritDoc} */
    @Override
    public void run(final BatchTask phase) {
        m_span = m_provisionService.buildAndStartSpan("NewSuspectScan", null);
        scanUndiscoveredNode(phase, monitor);
    }

    /**
     * <p>scanUndiscoveredNode</p>
     *
     * @param phase a {@link org.opennms.core.tasks.BatchTask} object.
     * @param monitor a {@link org.opennms.netmgt.provision.service.operations.ProvisionMonitor} object. (optional)
     */
    protected void scanUndiscoveredNode(final BatchTask phase, final ProvisionMonitor monitor) {
    	final String addrString = str(m_ipAddress);
		LOG.info("Attempting to scan new suspect address {} for foreign source {}", addrString, m_foreignSource);

        final OnmsNode node = m_provisionService.createUndiscoveredNode(addrString, m_foreignSource, m_location, monitor != null ? monitor.getName() : null);
        if (node != null) {
            if(node.getId() != null && node.getId() > 0) {
                m_span.setTag(NODE_ID, node.getId());
                m_span.setTag(LOCATION, m_location);
            }
        	phase.getBuilder().addSequence(
        			new NodeInfoScan(node, m_ipAddress, null, node.getLocation(), createScanProgress(), m_agentConfigFactory, m_provisionService, null, m_span),
        			new IpInterfaceScan(node.getId(), m_ipAddress, m_foreignSource, node.getLocation(), m_provisionService, m_span),
				new NodeScan(node.getId(), m_foreignSource, node.getForeignId(), node.getLocation(), m_provisionService, m_eventForwarder, m_agentConfigFactory, m_taskCoordinator, m_span, monitor),
				new RunInBatch() {
					@Override
					public void run(BatchTask batch) {
						LOG.info("Done scanning scan new suspect address {} for foreign source {}", addrString, m_foreignSource);
                        DefaultProvisionService.setTag(m_span, FOREIGN_ID, node.getForeignId());
                        DefaultProvisionService.setTag(m_span, FOREIGN_SOURCE, node.getForeignSource());
                        m_span.finish();
					}
				});
        } else {
            LOG.info("A node already exists with address {} in foreign source {}. No node scan will be performed.", addrString, m_foreignSource);
        }
    }

    private ScanProgress createScanProgress() {
        return new ScanProgress() {
            private boolean m_aborted = false;
            @Override
            public void abort(final String message) {
                m_aborted = true;
                m_span.setTag(ERROR, true);
                m_span.setTag(ABORT, true);
                m_span.log(message);
                LOG.info(message);
            }

            @Override
            public boolean isAborted() {
                return m_aborted;
            }};
    }

    /**
     * <p>reparentNodes</p>
     *
     * @param batch a {@link org.opennms.core.tasks.BatchTask} object.
     * @param nodeId a {@link java.lang.Integer} object.
     */
    protected void reparentNodes(final BatchTask batch, final Integer nodeId) {
        LOG.debug("reparenting node ID {} not supported", nodeId);
    }
}
