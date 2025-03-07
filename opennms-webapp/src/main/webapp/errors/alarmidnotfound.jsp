<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
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

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	isErrorPage="true"
	import="org.opennms.web.alarm.*, org.opennms.web.utils.ExceptionUtils"
%>

<%
    AlarmIdNotFoundException einfe = ExceptionUtils.getRootCause(exception, AlarmIdNotFoundException.class);
%>


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="Alarm ID Not Found" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error" />
</jsp:include>

<h1>Alarm Cleared or Not Found</h1>

<h1>Alarm Cleared or Not Found</h1>

<p>
  <%=einfe.getMessage()%>. The alarm has been cleared or has an invalid alarm ID.
  <br/>
  Re-enter the alarm ID below or <a href="alarm/list.htm?acktyp=unack">browse all
   alarms</a> to find the alarm you are looking for.
   <br /> If you get the same error message,
   you can assume that the alarm has been cleared.
</p>

<form role="form" method="get" action="alarm/detail.htm" class="form mb-4">
  <div class="row">
    <div class="form-group col-md-2">
      <label for="input_id">Get&nbsp;details&nbsp;for&nbsp;Alarm&nbsp;ID</label>
      <input type="text" class="form-control" id="input_id" name="id"/>
    </div>
  </div>
  <button type="submit" class="btn btn-secondary">Search</button>
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
