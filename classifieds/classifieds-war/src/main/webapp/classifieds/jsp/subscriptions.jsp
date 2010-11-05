<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp" %>

<% 
Collection	subscribes		= (Collection) request.getAttribute("Subscribes");
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

</head>

<body>
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(resource.getString("classifieds.mySubscriptions"));
	
	// affichage des options
	operationPane.addOperation(resource.getIcon("classifieds.subscriptionsAdd"),resource.getString("classifieds.addSubscription"), "javaScript:addSubscription()");
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
		
	Board	board		 = gef.getBoard();
	%>
	<br/>
	<%
	out.println(board.printBefore());
	
	%>

    <%@include file="subscriptionManager.jsp" %>

	<table>
	<%
	if (subscribes != null && subscribes.size() > 0) {
		Subscribe subscribe;
		Iterator it = (Iterator) subscribes.iterator();
		
		while (it.hasNext()) {			
				subscribe 		= (Subscribe) it.next();
				%>
				
				<tr>
					<td>
						<p>
							&nbsp; &#149; &nbsp;&nbsp;<b><%=subscribe.getFieldName1()%> - <%=subscribe.getFieldName2()%></b> 
							<A href="DeleteSubscription?SubscribeId=<%=subscribe.getSubscribeId()%>">
							  <IMG SRC="<%=resource.getIcon("classifieds.smallDelete") %>" border="0" alt="<%=resource.getString("GML.delete")%>" title="<%=resource.getString("GML.delete")%>" align="absmiddle"/>
							</A>
						</p>
					</td>
				</tr>
			<%}
	}
	else {
		%>
		<tr>
			<td colspan="5" valign="middle" align="center" width="100%">
				<br/>
				<%out.println(resource.getString("classifieds.SubscribeEmpty"));%>
				<br/>
			</td>
		</tr>
	<% } %>
	</table>
	<%	
  	out.println(board.printAfter());
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>