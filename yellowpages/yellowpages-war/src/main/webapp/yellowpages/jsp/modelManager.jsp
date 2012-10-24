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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.webactiv.util.contact.model.ContactDetail, com.stratelia.webactiv.util.contact.model.CompleteContact"%>
<%@ page import="java.util.Iterator, java.util.ArrayList, java.util.Collection, java.util.Date"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.EJBException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="java.io.File"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>

<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@ page import="com.silverpeas.form.DataRecord"%>
<%@ page import="com.silverpeas.form.Form"%>
<%@ page import="com.silverpeas.form.RecordSet"%>
<%@ page import="com.silverpeas.form.PagesContext"%>


<%@ include file="checkYellowpages.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="modelUtils.jsp.inc" %>
<%!
String alertSrc;

void displayModelsList(YellowpagesSessionController yellowpagesScc, String selectedModel, ArrayList listTemplate, JspWriter out)  throws IOException, Exception {
	out.println("<TABLE border=0 cellPadding=1 cellSpacing=1 >");
	out.println("<FORM Name=\"models\" >");

	if (selectedModel == null || "".equals(selectedModel)) {
		out.println("<tr><td><input type=radio name=radiobutton value=\"\" checked onClick=previewModel(\"0\")>"+"<a href=javascript:onClick=previewModel(\"0\"); class=txtlibform>"+ yellowpagesScc.getString("Nomodel")+"</a></td>");
	}
	else {
		out.println("<tr><td><input type=radio name=radiobutton value=\"\" onClick=previewModel(\"0\")>"+"<a href=javascript:onClick=previewModel(\"0\"); class=txtlibform>"+ yellowpagesScc.getString("Nomodel")+"</a></td>");
	}
	out.println("<td class=field>&nbsp;</td></tr>");

	if (listTemplate != null)
	{
		PublicationTemplate xmlForm;
		int nb = 0;
    out.println("<TR>");
		for(int i = 0;i<listTemplate.size();i++)
		{
			xmlForm = (PublicationTemplate) listTemplate.get(i);
      if (nb != 0 && nb%3==0)
	      out.println("</TR><TR>");
      nb++;
      String checked = "";
			if (selectedModel != null && selectedModel.equals(xmlForm.getFileName()))
				checked = "checked";
			out.println("<tr><td><input onClick=\"previewModel('"+xmlForm.getFileName()+"')\"" + checked +" type=\"radio\" name=\"radiobutton\" value=\""+ xmlForm.getFileName() +"\"><a href=javascript:onClick=previewModel(\""+xmlForm.getFileName()+"\"); class=txtlibform alt=\""+Encode.javaStringToHtmlString(xmlForm.getDescription())+"\" title=\""+Encode.javaStringToHtmlString(xmlForm.getDescription())+"\">");
			out.println("<IMG SRC=\"" + xmlForm.getThumbnail() + "\" border=0 alt=\"" + xmlForm.getDescription() +"\">" + xmlForm.getName()+ " </a></td>");
		}
	}
	out.println("</TABLE></FORM>");
}
%>

<%
String contactId = (String) request.getAttribute("ContactId");
String modelId = (String) request.getAttribute("ModelId");
String action = (String) request.getAttribute("Action");

String linkedPathString = yellowpagesScc.getPath();

ResourceLocator contactSettings = new ResourceLocator("com.stratelia.webactiv.contact.contactSettings", "fr");

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=reallyClose();", false);
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=goToModel()", false);
Button sendNewButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendModelData('Add')", false);

%>
<script language="Javascript">
<!--
function topicAddGoTo() {
    document.topicAddLink.submit();
}
//-->
function reallyClose()
{
  window.opener.document.topicDetailForm.Action.value = "Search";
  window.opener.document.topicDetailForm.submit();
  window.close();
}

</script>
<%
if (action.equals("NewModel")) {

	Form formUpdate    = (Form) request.getAttribute("Form");
	DataRecord data    = (DataRecord) request.getAttribute("Data"); 
	PagesContext context = (PagesContext) request.getAttribute("PagesContext");
	context.setBorderPrinted(true);
%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">
function sendModelData(operation) {
	document.modelForm.Action.value = operation;
    document.modelForm.submit();
}

function topicGoTo(id) {
    document.topicDetailForm.Action.value = "Search";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}
function reallyClose()
{
  window.opener.document.topicDetailForm.Action.value = "Search";
  window.opener.document.topicDetailForm.submit();
  window.close();
}
</script>
<%
	formUpdate.displayScripts(out, context);
%>
</HEAD>
<BODY class="yui-skin-sam">
<%
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();

	BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setPath(resources.getString("Model"));

	OperationPane operationPane = window.getOperationPane();
	operationPane.addOperation(resources.getIcon("yellowpages.contactTopicLink"), yellowpagesScc.getString("TopicLink"), "javascript:topicAddGoTo();");

	out.println(window.printBefore());
	displayContactOperations(resources, contactId, gef, action, out, true);
	out.println(frame.printBefore());

%> 
<FORM NAME="modelForm" ACTION="modelManager.jsp" METHOD="POST" ENCTYPE="multipart/form-data">
<input type="hidden" name="ModelId" value="<%=modelId%>">
<input type="hidden" name="ContactId" value="<%=contactId%>">
<input type="hidden" name="Action">

<%
    formUpdate.display(out, context, data); 
%>
</FORM>

<%
	out.println(frame.printMiddle());

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(sendNewButton);
	buttonPane.addButton(cancelButton);
	buttonPane.setHorizontalPosition();
	out.println("<BR><center>"+buttonPane.print()+"</center>");

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<FORM NAME="topicDetailForm" ACTION="topicManager.jsp" METHOD=POST >
  <input type="hidden" name="Action"><input type="hidden" name="Id" value="">
</FORM>
</BODY>
</HTML>
<%
} else if (action.equals("Add")) {
    %>
    <BODY onload="reallyClose()">
    </BODY>
    <%
    
    
} else if (action.equals("UpdateTopicModel")) {
    String name = "";
    String description = "";
    NodeDetail subTopicDetail = yellowpagesScc.getSubTopicDetail(contactId);
    if (subTopicDetail != null) {
        name = subTopicDetail.getName();
        description = subTopicDetail.getDescription();
    }

    %>
    <script language="javascript">
    <!--
            window.opener.document.topicDetailForm.Action.value = "Update";
            window.opener.document.topicDetailForm.ChildId.value = '<%=contactId%>';
            window.opener.document.topicDetailForm.Name.value = '<%=Encode.javaStringToJsString(name)%>';
            window.opener.document.topicDetailForm.Description.value = '<%=Encode.javaStringToJsString(description)%>';
            window.opener.document.topicDetailForm.ModelId.value = '<%=modelId%>';
            window.opener.document.topicDetailForm.submit();
            window.close();
    //-->
    </script>
    <%  
} 

if (action.equals("ModelChoice")) { 

	ArrayList listTemplate = (ArrayList) request.getAttribute("XMLForms");
		
	Form formUpdate    = (Form) request.getAttribute("Form");
	DataRecord data    = (DataRecord) request.getAttribute("Data"); 
	PagesContext context = (PagesContext) request.getAttribute("PagesContext"); 
    
	%>
	<HTML>
	<HEAD>
	<view:looknfeel/>
	<view:includePlugin name="wysiwyg"/>
	<script language="javaScript">
	    function previewModel(id)
	    {
			document.modelForm.Action.value = "ModelChoice";
			document.modelForm.action = "modelManager.jsp";
			document.modelForm.ModelId.value = id;
			document.modelForm.submit();
	    }
	    
	    function sendData() {
			if (document.models.radiobutton.length == 1)
			{
				document.modelForm.Action.value = "UpdateTopicModel";
				document.modelForm.action = "modelManager.jsp";
				document.modelForm.ModelId.value = document.models.radiobutton.value;
				document.modelForm.submit();
			}
			else
			{
				for (i=0;i<document.models.radiobutton.length;i++)
				{
					if (document.models.radiobutton[i].checked==true)
					{
						document.modelForm.Action.value = "UpdateTopicModel";
						document.modelForm.action = "modelManager.jsp";
						document.modelForm.ModelId.value = document.models.radiobutton[i].value;
						document.modelForm.submit();
						return;
					}
				}
			}
	    }
	
	    function goToModel() {
	        document.modelForm.submit();
	    } 
	
		function topicGoTo(id) {
			document.topicDetailForm.Action.value = "Search";
			document.topicDetailForm.Id.value = id;
			document.topicDetailForm.submit();
		}
	
		function reallyClose()
		{
		  window.opener.document.topicDetailForm.Action.value = "Search";
		  window.opener.document.topicDetailForm.submit();
		  window.close();
		}
	</script>
	</HEAD>
	<BODY class="yui-skin-sam">
	<% 
	    Window window = gef.getWindow();
	    Frame frame = gef.getFrame();
			Board board = gef.getBoard();
			
	    BrowseBar browseBar = window.getBrowseBar();
	    browseBar.setDomainName(spaceLabel);
			browseBar.setComponentName(componentLabel);
			browseBar.setPath(resources.getString("ModelChoiceTitle"));
			
		  out.println(window.printBefore());
		  displayAllOperations(resources, contactId, gef, action, out);
		  out.println(frame.printBefore());
		  out.println(board.printBefore());
%>
	
		<TABLE border=0 width="98%">
			<TR>
				<TD><% displayModelsList(yellowpagesScc, modelId, listTemplate, out); %></td> 
				<TD width="70%" valign="top">
					<% if (modelId != null && ! "".equals(modelId)) { %>
					<TABLE border="0" cellPadding="0" cellSpacing="0" class="contourbleufondblanc" width="100%" height="100">
						<TR><TD><%formUpdate.display(out, context, data);%></td></tr>
					</table>
				 	<% } %>
				</TD>
			</TR>
		</TABLE>
	
	<FORM NAME="modelForm" ACTION="modelManager.jsp" METHOD="POST" ENCTYPE="multipart/form-data">
	<input type="hidden" name="ModelId" value="<%=modelId%>">
	<input type="hidden" name="ContactId" value="<%=contactId%>">
	<input type="hidden" name="Action">
	</FORM>
	<FORM NAME="topicDetailForm" ACTION="topicManager.jsp" METHOD=POST >
	  <input type="hidden" name="Action">
	  <input type="hidden" name="Id" value="">
	</FORM>
	<%
		validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
	    ButtonPane buttonPane = gef.getButtonPane();
	    buttonPane.addButton(validateButton);
	    buttonPane.addButton(cancelButton);
	    out.println(board.printAfter());
	    out.println("<br><CENTER>"+buttonPane.print()+"</CENTER><br>");
	    out.println(frame.printAfter());
	%>
	</BODY>
	</HTML>
<% } 
%>
<FORM NAME="topicAddLink" ACTION="TopicLink.jsp" METHOD=POST >
<input type=hidden name=ContactId value="<%=contactId%>">
</FORM>