<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored ="false" %> 
<%@ page errorPage="/Error.jsp" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<script language="JavaScript">
  function SubmitOutcomeIfSelected(selectId) 
  {
    if ( selectId.selectedIndex > 0 )
    {
      // alert(selectId.selectedIndex);
      selectId.form.submit();
    }
  }
</script>
<%
  int i = 0;
  String evenOdd;
%>
<wiki:TabbedSection defaultTab='${param.tab} %>' >

<wiki:Tab id="pagecontent" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "workflow.tab")%>' >

<h3><fmt:message key="workflow.heading" /></h3>
<p><fmt:message key="workflow.instructions"/></p>

<!-- Pending Decisions -->
<h4><fmt:message key="workflow.decisions.heading" /></h4>

<c:if test="${empty decisions}">
  <div class="information">
    <fmt:message key="workflow.noinstructions"/>
  </div>
</c:if>

<c:if test="${!empty decisions}">
  <div class="formhelp">
    <fmt:message key="workflow.actor.instructions"/>
  </div>
  <table class="wikitable">
    <thead>
      <th width="5%"  align="center"><fmt:message key="workflow.id"/></th>
      <th width="45%" align="left"><fmt:message key="workflow.item"/></th>
      <th width="15%" align="left"><fmt:message key="workflow.actions"/></th>
      <th width="15%" align="left"><fmt:message key="workflow.requester"/></th>
      <th width="20%" align="left"><fmt:message key="workflow.startTime"/></th>
    </thead>
    <tbody>
      <% i = 1; %>
      <c:forEach var="decision" items="${decisions}">
        <% evenOdd = (i % 2 == 0) ? "even" : "odd"; %>
        <tr class="<%=evenOdd%>">
          <!-- Workflow ID -->
          <td align="center"><c:out value="${decision.workflow.id}"/></td>
          <!-- Name of item -->
          <td align="left">
            <fmt:message key="${decision.messageKey}">
              <c:forEach var="messageArg" items="${decision.messageArguments}">
                <fmt:param><c:out value="${messageArg}"/></fmt:param>
              </c:forEach>
            </fmt:message>
          </td>
          <!-- Possible actions (outcomes) -->
          <td align="left">
            <form id="<c:out value='decision.${decision.id}'/>" 
              action="<wiki:Link jsp='Workflow.jsp' format='url'/>" method="POST" accept-charset="UTF-8">
              <input type="hidden" name="action" value="decide" />
              <input type="hidden" name="id" value="<c:out value='${decision.id}' />" />
              <select name="outcome" onchange="SubmitOutcomeIfSelected(this)">
                <option value="-"><fmt:message key="select.one"/></option>
                <c:forEach var="outcome" items="${decision.availableOutcomes}"><option value="${outcome.messageKey}"><fmt:message key="${outcome.messageKey}"/></option>
                </c:forEach>
              </select>
            </form>
          </td>
          <!-- Requester -->
          <td align="left"><c:out value="${decision.owner.name}"/></td>
          <!-- When did the actor start this step? -->
          <td align="left">
            <fmt:formatDate value="${decision.startTime}" pattern="${prefs['DateFormat']}" />
		  </td>
        </tr>
        <!-- Hidden row with Decision details, if there are any -->
        <c:if test="${!empty decision.facts}">
          <tr class="<%=evenOdd%>" class="hideDiv">
            <td>&nbsp;</td>
          <c:set var="decisionJSObject">$('decision.<c:out value="${decision.workflow.id}"/>')</c:set>
            <td colspan="4" class="split">
              <a href="#" 
                title="Show or hide details"
              onclick="${decisionJSObject}.toggle();" >
                <fmt:message key="workflow.details" />
              </a>
              <div class="hideDiv" id="<c:out value='decision.${decision.workflow.id}'/>">
                <c:forEach var="fact" items="${decision.facts}">
                  <h5><fmt:message key="${fact.messageKey}" /></h5>
                  <p><c:out escapeXml="false" value="${fact.value}"/></p>
                </c:forEach>
              </div>
            </td>
          </tr>
        </c:if>
        
        <% i++; %>
      </c:forEach>
    </tbody>
  </table>
</c:if>

<!-- Running workflows for which current user is the owner -->
<h4><fmt:message key="workflow.workflows.heading" /></h4>

<c:if test="${empty workflows}">
  <div class="information">
    <fmt:message key="workflow.noinstructions"/>
  </div>
</c:if>

<c:if test="${!empty workflows}">
  <div class="formhelp">
    <fmt:message key="workflow.owner.instructions"/>
  </div>
  <table class="wikitable">
    <thead>
      <th width="5%"  align="center"><fmt:message key="workflow.id"/></th>
      <th width="45%" align="left"><fmt:message key="workflow.item"/></th>
      <th width="15%" align="left"><fmt:message key="workflow.actions"/></th>
      <th width="15%" align="left"><fmt:message key="workflow.actor"/></th>
      <th width="20%" align="left"><fmt:message key="workflow.startTime"/></th>
    </thead>
    <% i = 1; %>
    <tbody>
      <c:forEach var="workflow" items="${workflows}">
        <% evenOdd = (i % 2 == 0) ? "even" : "odd"; %>
        <tr class="<%=evenOdd%>">
          <!-- Workflow ID -->
          <td  align="center"><c:out value="${workflow.id}"/></td>
          <!-- Name of item -->
          <td align="left">
            <fmt:message key="${workflow.messageKey}">
              <c:forEach var="messageArg" items="${workflow.messageArguments}">
                <fmt:param><c:out value="${messageArg}"/></fmt:param>
              </c:forEach>
            </fmt:message>
          </td >
          <!-- Actions -->
          <td align="left">
          <c:set var="abort"><fmt:message key="outcome.step.abort" /></c:set>
            <form id="<c:out value='workflow.${workflow.id}'/>" action="<wiki:Link jsp='Workflow.jsp' format='url'/>" method="POST" accept-charset="UTF-8">
              <input type="submit" name="submit" value="${abort}" />
              <input type="hidden" name="action" value="abort" />
              <input type="hidden" name="id" value=""${workflow.id} />
            </form>
          </td>
          <!-- Current actor -->
          <td align="left"><c:out value="${workflow.currentActor.name}"/></td>
          <!-- When did the actor start this step? -->
          <td align="left">
            <fmt:formatDate value="${workflow.currentStep.startTime}" pattern="${prefs['DateFormat']}" />
          </td>
        </tr>
        <% i++; %>
      </c:forEach>
    </tbody>
  </table>
</c:if>

</wiki:Tab>
</wiki:TabbedSection>