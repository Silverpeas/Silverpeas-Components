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
Collection	classifieds		= (Collection) request.getAttribute("Classifieds");
Form 		formSearch 		= (Form) request.getAttribute("Form");
DataRecord	data 			= (DataRecord) request.getAttribute("Data"); 
String		instanceId		= (String) request.getAttribute("InstanceId");
String 		nbTotal			= (String) request.getAttribute("NbTotal");

//déclaration des boutons
Button validateButton = (Button) gef.getFormButton(resource.getString("GML.search")+" dans <b>"+nbTotal+"</b> "+resource.getString("classifieds.classifieds"), "javascript:onClick=sendData();", false);

%>

<html>
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

function sendData() {
	document.classifiedForm.submit();
}


</script>

</head>

<body>

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(resource.getString("classifieds.classifiedsResult"));
	
	out.println(window.printBefore());
    out.println(frame.printBefore());

    // afficher les petites annonces
	Board	board		 = gef.getBoard();
	%>
	<br/>
	<FORM Name="classifiedForm" action="SearchClassifieds" Method="POST" ENCTYPE="multipart/form-data">
		<% if (formSearch != null) { %>
			<center>
			<div id="search">
				<!-- AFFICHAGE du formulaire -->
					<%=board.printBefore()%>
					<%
						PagesContext xmlContext = new PagesContext("myForm", "0", resource.getLanguage(), false, instanceId, null);
						xmlContext.setBorderPrinted(false);
						xmlContext.setIgnoreDefaultValues(true);
						
						formSearch.display(out, xmlContext, data);
				    
				    // bouton valider
					ButtonPane buttonPane = gef.getButtonPane();
					buttonPane.addButton(validateButton);
					out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
			
					out.println(board.printAfter());
					%>
			</div>
			</center>
		<% } %>	
	</FORM>
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
			while (it.hasNext()) {			
				classified 		= (ClassifiedDetail) it.next();
				%>
				<tr>
					<td>
						<p>
							&nbsp; &#149; &nbsp;&nbsp;<b><a href="ViewClassified?ClassifiedId=<%=classified.getClassifiedId()%>"><%=classified.getTitle()%></a></b>
							
							<br/>
							&nbsp;&nbsp;&nbsp;<%=classified.getCreatorName()%> - <%=resource.getOutputDate(classified.getCreationDate())%>
						</p>
					</td>
				</tr>
			<%}
		}
	}
	else {
		%>
		<tr>
			<td colspan="5" valign="middle" align="center" width="100%">
				<br/>
				<% out.println(resource.getString("classifieds.noResult"));%>
				<br/>
			</td>
		</tr>
	<%
	}
	%>
	</table>
	<%
		
  	out.println(board.printAfter());
	
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>