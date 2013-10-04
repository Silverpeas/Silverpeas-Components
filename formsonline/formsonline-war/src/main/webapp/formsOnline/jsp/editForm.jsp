<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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
<%@ page import="org.silverpeas.core.admin.OrganisationController" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;

     var name = stripInitialWhitespace(document.creationForm.name.value);
     var description = stripInitialWhitespace(document.creationForm.description.value);
	 var templateSelectedIndex = document.creationForm.template.selectedIndex;
     var title = stripInitialWhitespace(document.creationForm.title.value);

     if (isWhitespace(name)) {
           errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }

     if (isWhitespace(description)) {
         errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("GML.description")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
         errorNb++;
   	 }

     if (templateSelectedIndex < 1) {
         errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("formsOnline.Template")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
         errorNb++;
     }

     if (isWhitespace(title)) {
         errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("GML.title")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
         errorNb++;
     }

     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

function valider() {
	if (isCorrectForm()) {
		document.creationForm.submit();
	}
}
</script>

</head>
<body>

<%
	DateFormat formatter = new SimpleDateFormat(resource.getString("GML.dateFormat"));
    FormDetail form = (FormDetail) request.getAttribute("currentForm");
	List templates = (List) request.getAttribute("availableTemplates");

    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);

    TabbedPane tabbedPane = gef.getTabbedPane(1);
    tabbedPane.addTab(resource.getString("formsOnline.Form"), "EditForm", true,1);
	if (form.getId() != -1) {
	    tabbedPane.addTab(resource.getString("formsOnline.SendersReceivers"), "SendersReceivers", false,1);
	    tabbedPane.addTab(resource.getString("formsOnline.Preview"), "Preview", false,1);
	}

	OrganisationController controller = new OrganizationController();
	UserDetail userDetail = controller.getUserDetail(form.getCreatorId());
%>

	<%=window.printBefore()%>
	<%=tabbedPane.print()%>
	<%=frame.printBefore()%>
	<%=board.printBefore()%>

	<form name="creationForm" action="SaveForm" method="post">
	<table class="intfdcolor4" width="100%" cellspacing="0" cellpadding="5" border="0">
	<tr>
		<td ><span class="txtlibform"><%=resource.getString("formsOnline.Template")%>  : </span></td>
		<td >
           	<select size="1" name="template" <%=(form.getId() != -1) ? "disabled" : "" %>>
			<option value="">---------------------</option>
			<%
			Iterator it = templates.iterator();
			while (it.hasNext()) {
				PublicationTemplate template = (PublicationTemplate) it.next();
				boolean selected = (form.getXmlFormName() != null) && (form.getXmlFormName().equals(template.getFileName()));
				%>
				<option <%=(selected) ? "selected":""%> value="<%=template.getFileName()%>"><%=template.getName()%></option>
				<%
			}
			%>
			</select>
		</td>
	</tr>
  	<tr>
		<td width="30%"><span class="txtlibform"><%=resource.getString("GML.name")%> : </span></td>
        <td colspan="4"><span class="txtlibform"><input type="text" size="40" maxlength="40" name="name" value="<%=form.getName()%>"/></span></td>
	</tr>
  	<tr>
		<td width="30%"><span class="txtlibform"><%=resource.getString("GML.description")%> : </span></td>
        <td colspan="4"><span class="txtlibform"><input type="text" size="80" maxlength="80" name="description" value="<%=form.getDescription()%>"/></span></td>
	</tr>
  	<tr>
		<td width="30%"><span class="txtlibform"><%=resource.getString("GML.date")%> : </span></td>
        <td colspan="4"><span class="txtlibform"><%=formatter.format(form.getCreationDate())%></span></td>
	</tr>
   	<tr>
		<td width="30%"><span class="txtlibform"><%=resource.getString("GML.publisher")%> : </span></td>
        <td colspan="4"><span class="txtlibform"><view:username userId="<%=userDetail.getId()%>" /></span></td>
	</tr>
  	<tr>
		<td width="30%"><span class="txtlibform"><%=resource.getString("GML.title")%> : </span></td>
        <td colspan="4"><span class="txtlibform"><input type="text" size="80" maxlength="200" name="title" value="<%=form.getTitle()%>"/></span></td>
	</tr>
	</table>
	</form>

	<%=board.printAfter()%>
    <%=frame.printAfter()%>
  	<%=window.printAfter()%>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    Button validerButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=valider();", false);
    buttonPane.addButton(validerButton);

    Button annulerButton = gef.getFormButton(resource.getString("GML.cancel"), "Main", false);
    buttonPane.addButton(annulerButton);
    buttonPane.setHorizontalPosition();
%>
    <center>
    <%=buttonPane.print()%>
    </center>

</body>
</html>