<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ include file="check.jsp" %>

<%@page import="java.util.List"%>
<%@page import="com.silverpeas.formsonline.model.FormDetail"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.stratelia.webactiv.beans.admin.Group"%>
<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.DataRecord"%>
<%@page import="com.silverpeas.form.PagesContext"%>

<%!	
	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang("");
%>

<%
	Form formUpdate    		= (Form) request.getAttribute("Form");
	DataRecord data    		= (DataRecord) request.getAttribute("Data"); 
	String xmlFormName 		= (String) request.getAttribute("XMLFormName");
	String title = (String) request.getAttribute("title");
	String titleClassName 	= resource.getSetting("titleClassName");

	// cr�ation du context
	PagesContext context = new PagesContext("myForm", "0", resource.getLanguage(), false, "", null);
	context.setBorderPrinted(false);
%>

<html>

<head>
<%=gef.getLookStyleSheet()%>
<% formUpdate.displayScripts(out, context); %>
</head>

<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<%
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
    
    TabbedPane tabbedPane = gef.getTabbedPane(1);
    tabbedPane.addTab(resource.getString("formsOnline.Form"), "Main", false,1);  
    tabbedPane.addTab(resource.getString("formsOnline.SendersReceivers"), "SendersReceivers", false,1);
    tabbedPane.addTab(resource.getString("formsOnline.Preview"), "Preview", true,1);
%>	

	<%=window.printBefore()%>
	<%=tabbedPane.print()%>
	<%=frame.printBefore()%>

	<FORM NAME="myForm" METHOD="POST" ACTION="UpdateXMLForm" 
                    ENCTYPE="multipart/form-data">
	<span class="<%=titleClassName%>"><%=title%></span>
	<% 
	formUpdate.display(out, context, data); 
	%>
	</FORM>
	
    <%=frame.printAfter()%>
  	<%=window.printAfter()%>  

</body>
</html>