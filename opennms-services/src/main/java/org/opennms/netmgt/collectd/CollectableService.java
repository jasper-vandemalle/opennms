/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.File;
import java.net.InetAddress;
import java.util.Date;

import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collectd.Collectd.SchedulingCompletedFlag;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionFailed;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.CollectionTimedOut;
import org.opennms.netmgt.collection.api.CollectionUnknown;
import org.opennms.netmgt.collection.api.CollectionWarning;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.core.CollectionSpecification;
import org.opennms.netmgt.collection.support.AttributeGroupWrapper;
import org.opennms.netmgt.collection.support.CollectionAttributeWrapper;
import org.opennms.netmgt.collection.support.CollectionResourceWrapper;
import org.opennms.netmgt.collection.support.CollectionSetVisitorWrapper;
import org.opennms.netmgt.collection.support.ConstantTimeKeeper;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * <P>
 * The CollectableService class ...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
class CollectableService implements ReadyRunnable {

    private static final Logger LOG = LoggerFactory.getLogger(CollectableService.class);

    protected static final String STRICT_INTERVAL_SYS_PROP = "org.opennms.netmgt.collectd.strictInterval";

    protected static final String USE_COLLECTION_START_TIME_SYS_PROP = "org.opennms.netmgt.collectd.useCollectionStartTime";

    private final boolean m_usingStrictInterval = !System.getProperties().containsKey(STRICT_INTERVAL_SYS_PROP) || Boolean.getBoolean(STRICT_INTERVAL_SYS_PROP);

    /**
     * Interface's parent node identifier
     */
    private volatile int m_nodeId;

    /**
     * Last known/current status
     */
    private volatile CollectionStatus m_status;

    /**
     * The last time the collector was scheduled for collection.
     */
    private volatile long m_lastScheduledCollectionTime;

    /**
     * The scheduler for collectd
     */
    private final Scheduler m_scheduler;

    /**
     * Service updates
     */
    private final CollectorUpdates m_updates;

    private static final boolean ABORT_COLLECTION = true;

	private final CollectionSpecification m_spec;

	private final SchedulingCompletedFlag m_schedulingCompletedFlag;

	private volatile CollectionAgent m_agent;

	private final PlatformTransactionManager m_transMgr;

    private final IpInterfaceDao m_ifaceDao;

    private final ServiceParameters m_params;
    
    private final RrdRepository m_repository;

    private final PersisterFactory m_persisterFactory;

    private ThresholdingSession m_thresholdingSession;

    /**
     * Constructs a new instance of a CollectableService object.
     *
     * @param iface The interface on which to collect data
     * @param spec
     *            The package containing parms for this collectable service.
     * @param ifaceDao a {@link org.opennms.netmgt.dao.api.IpInterfaceDao} object.
     * @param scheduler a {@link org.opennms.netmgt.scheduler.Scheduler} object.
     * @param schedulingCompletedFlag a {@link org.opennms.netmgt.collectd.Collectd.SchedulingCompletedFlag} object.
     * @param transMgr a {@link org.springframework.transaction.PlatformTransactionManager} object.
     */
    protected CollectableService(OnmsIpInterface iface, IpInterfaceDao ifaceDao, CollectionSpecification spec,
            Scheduler scheduler, SchedulingCompletedFlag schedulingCompletedFlag, PlatformTransactionManager transMgr,
            PersisterFactory persisterFactory, ThresholdingService thresholdingService) throws CollectionInitializationException {

        m_agent = DefaultSnmpCollectionAgent.create(iface.getId(), ifaceDao, transMgr);
        m_spec = spec;
        m_scheduler = scheduler;
        m_schedulingCompletedFlag = schedulingCompletedFlag;
        m_ifaceDao = ifaceDao;
        m_transMgr = transMgr;
        m_persisterFactory = persisterFactory;

        m_nodeId = iface.getNode().getId().intValue();
        m_status = CollectionStatus.SUCCEEDED;

        m_updates = new CollectorUpdates();

        m_lastScheduledCollectionTime = 0L;

        m_spec.initialize(m_agent);

        m_params = m_spec.getServiceParameters();
        m_repository=m_spec.getRrdRepository(m_params.getCollectionName());

        try {
            m_thresholdingSession = thresholdingService.createSession(m_nodeId, getHostAddress(), m_spec.getServiceName(), m_params);
        } catch (ThresholdInitializationException e) {
            LOG.error("Error when initializing Thresholding. No Thresholding will be performed on this service.", e);
        }
    }
    
    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
    	return m_agent.getAddress();
    }
    
    /**
     * <p>getSpecification</p>
     *
     * @return a {@link org.opennms.netmgt.collection.core.CollectionSpecification} object.
     */
    public CollectionSpecification getSpecification() {
    	return m_spec;
    }

    /**
     * Returns node identifier
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * Returns the service name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_spec.getServiceName();
    }

    /**
     * Returns the package name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPackageName() {
        return m_spec.getPackageName();
    }

    /**
     * Returns updates object
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectorUpdates} object.
     */
    public CollectorUpdates getCollectorUpdates() {
        return m_updates;
    }

    /**
     * Uses the existing package name to try and re-obtain the package from the collectd config factory.
     * Should be called when the collect config has been reloaded.
     *
     * @param collectorConfigDao a {@link org.opennms.netmgt.config.CollectdConfigFactory} object.
     * @throws CollectionInitializationException 
     */
    public void refreshPackage(CollectdConfigFactory collectorConfigDao) throws CollectionInitializationException {
        m_spec.refresh(collectorConfigDao);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "CollectableService for service "+m_nodeId+':'+getAddress()+':'+getServiceName();
    }

    /**
     * This method is used to evaluate the status of this interface and service
     * pair. If it is time to run the collection again then a value of true is
     * returned. If the interface is not ready then a value of false is
     * returned.
     *
     * @return a boolean.
     */
    @Override
    public boolean isReady() {
        boolean ready = false;

        if (!isSchedulingComplete())
            return false;

        if (m_spec.getInterval() < 1) {
            ready = true;
        } else {
            ready = ((m_spec.getInterval() - (System.currentTimeMillis() - m_lastScheduledCollectionTime)) < 1);
        }

        return ready;
    }

	private boolean isSchedulingComplete() {
		return m_schedulingCompletedFlag.isSchedulingCompleted();
    }

    /**
     * Generate event and send it to eventd via the event proxy.
     * 
     * uei Universal event identifier of event to generate.
     */
    private void sendEvent(String uei, String reason) {
        EventBuilder builder = new EventBuilder(uei, "OpenNMS.Collectd");
        builder.setNodeid(m_nodeId);
        builder.setInterface(m_agent.getAddress());
        builder.setService(m_spec.getServiceName());
        builder.setHost(InetAddressUtils.getLocalHostName());
        
        if (reason != null) {
            builder.addParam("reason", reason);
        }

        // Send the event
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(builder.getEvent());

            LOG.debug("sendEvent: Sent event {} for {}/{}/{}", uei, m_nodeId, getHostAddress(), getServiceName());
        } catch (Throwable e) {
            LOG.error("Failed to send the event {} for interface {}", uei, getHostAddress(), e);
        }
    }

    private String getHostAddress() {
        return m_agent.getHostAddress();
    }

    /**
     * This is the main method of the class. An instance is normally enqueued on
     * the scheduler which checks its <code>isReady</code> method to determine
     * execution. If the instance is ready for execution then it is started with
     * it's own thread context to execute the query. The last step in the method
     * before it exits is to reschedule the interface.
     */
    @Override
    public void run() {
        Logging.withPrefix(Collectd.LOG4J_CATEGORY, () -> {
            Logging.putThreadContext("service", m_spec.getServiceName());
            Logging.putThreadContext("ipAddress", m_agent.getAddress().getHostAddress());
            Logging.putThreadContext("nodeId", Integer.toString(m_agent.getNodeId()));
            Logging.putThreadContext("nodeLabel", m_agent.getNodeLabel());
            Logging.putThreadContext("foreignSource", m_agent.getForeignSource());
            Logging.putThreadContext("foreignId", m_agent.getForeignId());
            doRun();
        });
    }

    private void doRun() {
        // Process any outstanding updates.
        if (processUpdates() == ABORT_COLLECTION) {
            LOG.debug("run: Aborting because processUpdates returned ABORT_COLLECTION (probably marked for deletion) for {}", this);
            return;
        }

        // Update last scheduled poll time; if we are not doing strict interval,
        // it is the current time; if we are, it is the previous time plus the
        // interval
        if (m_lastScheduledCollectionTime == 0 || !m_usingStrictInterval) {
            m_lastScheduledCollectionTime = System.currentTimeMillis();
        } else {
            m_lastScheduledCollectionTime += m_spec.getInterval();
        }

        /*
         * Check scheduled outages to see if any apply indicating
         * that the collection should be skipped.
         */
        if (!m_spec.scheduledOutage(m_agent)) {
            try {
                doCollection();
                updateStatus(CollectionStatus.SUCCEEDED, null);
            } catch (CollectionTimedOut e) {
                LOG.info(e.getMessage());
                updateStatus(CollectionStatus.FAILED, e);
            } catch (CollectionWarning e) {
                LOG.warn(e.getMessage(), e);
                updateStatus(CollectionStatus.FAILED, e);
            } catch (CollectionUnknown e) {
                LOG.warn(e.getMessage(), e);
                // Omit any status updates
            } catch (CollectionException e) {
                LOG.error(e.getMessage(), e);
                updateStatus(CollectionStatus.FAILED, e);
            } catch (Throwable e) {
                LOG.error(e.getMessage(), e);
                updateStatus(CollectionStatus.FAILED, new CollectionException("Collection failed unexpectedly: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e));
            }
        }

        // If we are doing strict interval, determine how long the collection
        // has taken, so we can cut that off of the service interval
        long diff = 0;
        if (m_usingStrictInterval) {
            diff = System.currentTimeMillis() - m_lastScheduledCollectionTime;
            diff = Math.min(diff, m_spec.getInterval());
        }
    	// Reschedule the service
        m_scheduler.schedule(m_spec.getInterval() - diff, getReadyRunnable());
    }

    private void updateStatus(CollectionStatus status, CollectionException e) {
        // Any change in status?
        if (!status.equals(m_status)) {
            // Generate data collection transition events
            LOG.debug("run: change in collection status, generating event.");
            
            String reason = null;
            if (e != null) {
                reason = e.getMessage();
            }

            // Send the appropriate event
            switch (status) {
            case SUCCEEDED:
                sendEvent(EventConstants.DATA_COLLECTION_SUCCEEDED_EVENT_UEI, null);
                break;

            case FAILED:
                sendEvent(EventConstants.DATA_COLLECTION_FAILED_EVENT_UEI, reason);
                break;

            default:
                break;
            }
        }

        // Set the new status
        m_status = status;
    }

    /**
     * Perform data collection.
     */
    private void doCollection() throws CollectionException {
        LOG.info("run: starting new collection for {}/{}/{}/{}", m_nodeId, getHostAddress(), m_spec.getServiceName(), m_spec.getPackageName());
        CollectionSet result = null;
        try {
            result = m_spec.collect(m_agent);
            if (result != null) {
                Collectd.instrumentation().beginPersistingServiceData(m_spec.getPackageName(), m_nodeId, getHostAddress(), m_spec.getServiceName());
                try {
                    CollectionSetVisitor persister = m_persisterFactory.createPersister(m_params, m_repository, result.ignorePersist(), false, false);
                    if (Boolean.getBoolean(USE_COLLECTION_START_TIME_SYS_PROP)) {
                        final ConstantTimeKeeper timeKeeper = new ConstantTimeKeeper(new Date(m_lastScheduledCollectionTime));
                        // Wrap the persister visitor such that calls to CollectionResource.getTimeKeeper() return the given timeKeeper
                        persister = wrapResourcesWithTimekeeper(persister, timeKeeper);
                    }
                    result.visit(persister);
                } finally {
                    Collectd.instrumentation().endPersistingServiceData(m_spec.getPackageName(), m_nodeId, getHostAddress(), m_spec.getServiceName());
                }

                // Do thresholding
                if (m_thresholdingSession != null) {
                    try {
                        m_thresholdingSession.accept(result);
                    } catch (ThresholdInitializationException e) {
                        LOG.warn("ThresholdInitializationException for {}. Thresholding skipped.", this, e);
                    }
                } else {
                    LOG.warn("No thresholding session for {}. Thresholding skipped.", this);
                }

                if (!CollectionStatus.SUCCEEDED.equals(result.getStatus())) {
                    throw new CollectionFailed(result.getStatus());
                }
            }
        } catch (IllegalArgumentException e) {
            LOG.warn("run: failed collection for {}/{}/{}/{}", m_nodeId, getHostAddress(), m_spec.getServiceName(), m_spec.getPackageName());
            throw new CollectionException("Illegal Argument Exception was caught during data collection for interface " + m_nodeId + "/" + getHostAddress() + "/" + m_spec.getServiceName()
                    + " with message: " + e.getMessage(), e);
        } catch (CollectionException e) {
            LOG.warn("run: failed collection for {}/{}/{}/{}", m_nodeId, getHostAddress(), m_spec.getServiceName(), m_spec.getPackageName());
            throw e;
        } catch (Throwable t) {
            LOG.warn("run: failed collection for {}/{}/{}/{}", m_nodeId, getHostAddress(), m_spec.getServiceName(), m_spec.getPackageName());
            throw new CollectionException("An undeclared throwable was caught during data collection for interface " + m_nodeId + "/" + getHostAddress() + "/" + m_spec.getServiceName(), t);
        }
        LOG.info("run: finished collection for {}/{}/{}/{}", m_nodeId, getHostAddress(), m_spec.getServiceName(), m_spec.getPackageName());
    }

	/**
     * Process any outstanding updates.
     * 
     * @return true if update indicates that collection should be aborted (for
     *         example due to deletion flag being set), false otherwise.
     */
    private boolean processUpdates() {
        // All update processing takes place within synchronized block
        // to ensure that no updates are missed.
        //
        synchronized (this) {
            if (!m_updates.hasUpdates())
                return !ABORT_COLLECTION;

            // Update: deletion flag
            //
            if (m_updates.isDeletionFlagSet()) {
                // Deletion flag is set, simply return without polling
                // or rescheduling this collector.
                //
                LOG.debug("Collector for  {} is marked for deletion...skipping collection, will not reschedule.", getHostAddress());

                return ABORT_COLLECTION;
            }

            OnmsIpInterface newIface = m_updates.isReinitializationNeeded();
			// Update: reinitialization flag
            //
            if (newIface != null) {
                // Reinitialization flag is set, call initialize() to
                // reinit the collector for this interface
                //
                LOG.debug("ReinitializationFlag set for {}", getHostAddress());

                try {
                    reinitialize(newIface);
                    LOG.debug("Completed reinitializing {} collector for {}/{}/{}", this.getServiceName(), m_nodeId, getHostAddress(), m_spec.getServiceName());
                } catch (CollectionInitializationException rE) {
                    LOG.warn("Unable to initialize {}/{} for {} collection, reason: {}", m_nodeId, getHostAddress(), m_spec.getServiceName(), rE.getMessage());
                } catch (Throwable t) {
                    LOG.error("Uncaught exception, failed to intialize interface {}/{} for {} data collection", m_nodeId, getHostAddress(), m_spec.getServiceName(), t);
                }
            }

            // Update: reparenting flag
            //
            if (m_updates.isReparentingFlagSet()) {
                LOG.debug("ReparentingFlag set for {}", getHostAddress());

                // The interface has been reparented under a different node
                // (with
                // a different nodeId).
                //
                // If the new directory doesn't already exist simply need to
                // rename the old
                // directory:
                // /opt/OpenNMS/share/rrd/snmp/<oldNodeId>
                // to the new directory:
                // /opt/OpenNMS/share/rrd/snmp/<newNodeId>
                //
                // Otherwise must iterate over each of the files/dirs in the
                // <oldNodeId>
                // directory and move/rename them under the <newNodeId>
                // directory.

                // Get path to RRD repository
                //
                String rrdPath = DataCollectionConfigFactory.getInstance().getRrdPath();

                // Does the <newNodeId> directory already exist?
                File newNodeDir = new File(rrdPath + File.separator + m_updates.getReparentNewNodeId());
                if (!newNodeDir.isDirectory()) {
                    // New directory does not exist yet so simply rename the old
                    // directory to
                    // the new directory.
                    //

                    // <oldNodeId> directory
                    File oldNodeDir = new File(rrdPath + File.separator + m_updates.getReparentOldNodeId());

                    try {
                        // Rename <oldNodeId> dir to <newNodeId> dir.
                        LOG.debug("Attempting to rename {} to {}", oldNodeDir, newNodeDir);
                        if(!oldNodeDir.renameTo(newNodeDir)) {
                        	LOG.warn("Could not rename file: {}", oldNodeDir.getPath());
                        }
                        LOG.debug("Rename successful!!");
                    } catch (SecurityException se) {
                        LOG.error("Insufficient authority to rename RRD directory.", se);
                    } catch (Throwable t) {
                        LOG.error("Unexpected exception while attempting to rename RRD directory.", t);
                    }
                } else {
                    // New node directory already exists so we must move/rename
                    // each of the
                    // old node directory contents under the new node directory.
                    //

                    // Get list of files to be renamed/moved
                    File oldNodeDir = new File(rrdPath + File.separator + m_updates.getReparentOldNodeId());
                    String[] filesToMove = oldNodeDir.list();

                    if (filesToMove != null) {
                        // Iterate over the file list and rename/move each one
                        for (int i = 0; i < filesToMove.length; i++) {
                            File srcFile = new File(oldNodeDir.toString() + File.separator + filesToMove[i]);
                            File destFile = new File(newNodeDir.toString() + File.separator + filesToMove[i]);
                            try {
                                LOG.debug("Attempting to move {} to {}", srcFile, destFile);
                                srcFile.renameTo(destFile);
                            } catch (SecurityException se) {
                                LOG.error("Insufficient authority to move RRD files.", se);
                                break;
                            } catch (Throwable t) {
                                LOG.warn("Unexpected exception while attempting to move {} to {}", srcFile, destFile, t);
                            }
                        }
                    }
                }

                // Convert new nodeId to integer value
                int newNodeId = -1;
                try {
                    newNodeId = Integer.parseInt(m_updates.getReparentNewNodeId());
                } catch (NumberFormatException nfE) {
                    LOG.warn("Unable to convert new nodeId value to an int while processing reparenting update: {}", m_updates.getReparentNewNodeId());
                }

                // Set this collector's nodeId to the value of the interface's
                // new parent nodeid.
                m_nodeId = newNodeId;

                // We must now reinitialize the collector for this interface,
                // in order to update the NodeInfo object to reflect changes
                // to the interface's parent node among other things.
                //
                try {
                    LOG.debug("Reinitializing collector for {}/{}/{}", m_nodeId, getHostAddress(), m_spec.getServiceName());
                    reinitialize(m_updates.getUpdatedInterface());
                    LOG.debug("Completed reinitializing collector for {}/{}/{}", m_nodeId, getHostAddress(), m_spec.getServiceName());
                } catch (CollectionInitializationException rE) {
                    LOG.warn("Unable to initialize {}/{} for {} collection, reason: {}", m_nodeId, getHostAddress(), m_spec.getServiceName(), rE.getMessage());
                } catch (Throwable t) {
                    LOG.error("Uncaught exception, failed to initialize interface {}/{} for {} data collection", m_nodeId, getHostAddress(), m_spec.getServiceName(), t);
                }
            }

            // Updates have been applied. Reset CollectorUpdates object.
            // .
            m_updates.reset();
        } // end synchronized

        return !ABORT_COLLECTION;
    }

    private void reinitialize(OnmsIpInterface newIface) throws CollectionInitializationException {
        m_spec.release(m_agent);
        m_agent = DefaultSnmpCollectionAgent.create(newIface.getId(), m_ifaceDao,
                                                m_transMgr);
        m_spec.initialize(m_agent);
    }

    /**
     * <p>getReadyRunnable</p>
     *
     * @return a {@link org.opennms.netmgt.scheduler.ReadyRunnable} object.
     */
    public ReadyRunnable getReadyRunnable() {
        return this;
    }

    public static CollectionSetVisitor wrapResourcesWithTimekeeper(CollectionSetVisitor visitor, TimeKeeper timeKeeper) {
        // Wrap the given visitor and intercept the calls to visit the resources
        final CollectionSetVisitor wrappedVisitor = new CollectionSetVisitorWrapper(visitor) {
            private CollectionResource wrappedResource;
            private CollectionAttribute wrappedAttribute;
            private AttributeGroup wrappedGroup;

            @Override
            public void visitResource(CollectionResource resource) {
                // Wrap the given resource and return the custom timekeeper
                wrappedResource = new CollectionResourceWrapper(resource) {
                    @Override
                    public TimeKeeper getTimeKeeper() {
                        return timeKeeper;
                    }
                };
                visitor.visitResource(wrappedResource);
            }

            @Override
            public void completeResource(CollectionResource resource) {
                visitor.completeResource(wrappedResource);
            }

            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                // Wrap the given attribute and return the custom resource
                wrappedAttribute = new CollectionAttributeWrapper(attribute) {
                    @Override
                    public CollectionResource getResource() {
                        return wrappedResource;
                    }
                };
                visitor.visitAttribute(wrappedAttribute);
            }

            @Override
            public void completeAttribute(CollectionAttribute attribute) {
                visitor.completeAttribute(wrappedAttribute);
            }

            @Override
            public void visitGroup(AttributeGroup group) {
                // Wrap the given attribute group and return the custom resource
                wrappedGroup = new AttributeGroupWrapper(group) {
                    @Override
                    public CollectionResource getResource() {
                        return wrappedResource;
                    }
                };
                visitor.visitGroup(wrappedGroup);
            }

            @Override
            public void completeGroup(AttributeGroup group) {
                visitor.completeGroup(wrappedGroup);
            }
        };
        return wrappedVisitor;
    }
}
