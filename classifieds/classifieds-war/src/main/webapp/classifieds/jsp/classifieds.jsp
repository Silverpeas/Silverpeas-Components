<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
Collection	classifieds		= (Collection) request.getAttribute("Classifieds");
String      title         = (String) request.getAttribute("TitlePath");
String      extra         = (String) request.getAttribute("Extra");
ClassifiedsRole	profile		= (ClassifiedsRole) request.getAttribute("Profile");

if (!StringUtil.isDefined(title)) {
  title = "classifieds.myClassifieds";
}
%>


<%@page import="com.silverpeas.util.StringUtil"%><html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script language="javascript">
var classifiedWindow = window;

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

</script>
</head>

<body>

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	if (StringUtil.isDefined(extra)) {
	  browseBar.setPath(resource.getString(title) + " '" + extra + "'");
	}
	else {
	 browseBar.setPath(resource.getString(title));
	}

	// affichage des options
	if ( (profile==ClassifiedsRole.MANAGER) || (profile==ClassifiedsRole.PUBLISHER) ) {
		operationPane.addOperation(resource.getIcon("classifieds.addClassified"),resource.getString("classifieds.addClassified"), "NewClassified");
	}

	out.println(window.printBefore());
    out.println(frame.printBefore());

	// afficher les petites annonces
	Board	board		 = gef.getBoard();
	%>
	<br/>
	<%
	out.println(board.printBefore());

	%>
	<table>
	<%
	if (classifieds != null && classifieds.size() > 0) {
		ClassifiedDetail classified;
		Iterator it = (Iterator) classifieds.iterator();

		while (it.hasNext()) {
			// affichage de l'annonce
				classified 		= (ClassifiedDetail) it.next();
				String status 	= classified.getStatus();
				%>

				<tr>
					<td>
						<p>
							&nbsp; &#149; &nbsp;&nbsp;<b><a href="ViewClassified?ClassifiedId=<%=classified.getClassifiedId()%>"><%=classified.getTitle()%></a></b>
							<%if (status.equals(ClassifiedDetail.DRAFT)) { %>
								(<%=resource.getString("classifieds.draft") %>)
							<%}
							else if (status.equals(ClassifiedDetail.TO_VALIDATE)) { %>
								(<%=resource.getString("classifieds.toValidate") %>)
							<%}
							else if (status.equals(ClassifiedDetail.REFUSED)) { %>
								(<%=resource.getString("classifieds.refuse") %>)
							<%} %>
							<br/>
							&nbsp;&nbsp;&nbsp;<%=resource.getOutputDate(classified.getCreationDate())%>
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
				<%out.println(resource.getString("classifieds.CategoryEmpty"));%>
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