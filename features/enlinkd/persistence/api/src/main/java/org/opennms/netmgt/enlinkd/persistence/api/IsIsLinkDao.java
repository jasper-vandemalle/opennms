/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.persistence.api;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.model.OnmsNode;

public interface IsIsLinkDao extends OnmsDao<IsIsLink, Integer> {

    IsIsLink get(OnmsNode node, Integer isisCircIndex, Integer isisISAdjIndex);

    IsIsLink get(Integer nodeId, Integer isisCircIndex, Integer isisISAdjIndex);
    
    List<IsIsLink> findByNodeId(Integer nodeId);

    /**
     * Returns all IsIsLinks related by the sysId of an intermediary IsIsElement and the
     * adj/circ indexes of some other IsIsLink that is directly related to the given node.
     * Used to retrieve all IsIsLinks that need to be accessed when finding IsIsLinks of a node.
     */
    List<IsIsLink> findBySysIdAndAdjAndCircIndex(int nodeId);

    void deleteByNodeIdOlderThen(Integer nodeiId, Date now);
    
    void deleteByNodeId(Integer nodeId);

}
