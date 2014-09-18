<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@ page import="com.silverpeas.form.DataRecord"%>
<%@ page import="com.silverpeas.form.Form"%>
<%@ page import="com.silverpeas.form.RecordSet"%>
<%@ page import="com.silverpeas.form.PagesContext"%>
<%@ page import="com.silverpeas.util.StringUtil"%>

<%@ include file="checkYellowpages.jsp" %>
<%@ include file="topicReport.jsp.inc" %>

<%
String firstName = "";
String lastName = "";
String email = "";
String phone = "";
String fax = "";
String userId = "";
String creatorName = yellowpagesScc.getUserDetail().getDisplayedName();
String creationDate = resources.getOutputDate(new Date());
String topicId = "";

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
browseBar.setComponentId(componentId);

OperationPane operationPane = window.getOperationPane();
Board board = gef.getBoard();
Frame frame = gef.getFrame();

//Vrai si le user connecte est le createur de ce contact ou si il est admin
boolean isOwner = false;
boolean creation = true;

UserDetail ownerDetail = null;
CompleteContact contactComplete = null;
ContactDetail contactDetail = null;
String id = null;

String profile = (String) request.getAttribute("Profile");
UserCompleteContact userContactComplete = (UserCompleteContact) request.getAttribute("Contact");
if (userContactComplete != null) {
  contactComplete = userContactComplete.getContact();
  contactDetail = contactComplete.getContactDetail();
  ownerDetail = userContactComplete.getOwner();
  
  id = contactDetail.getPK().getId();
  if (StringUtil.isInteger(id)) {
    creation = false;
  }
  firstName = contactDetail.getFirstName();
  lastName = contactDetail.getLastName();
  email = contactDetail.getEmail();
  phone = contactDetail.getPhone();
  fax = contactDetail.getFax();
  userId = contactDetail.getUserId();
  creationDate = resources.getOutputDate(contactDetail.getCreationDate());
  if (ownerDetail != null) {
      creatorName = ownerDetail.getDisplayedName();
  } else {
      creatorName = yellowpagesScc.getString("UnknownAuthor");
  }
}

if (creation) {
  browseBar.setPath(resources.getString("ContactCreation"));
  operationPane.addOperationOfCreation(resources.getIcon("yellowpages.contactAdd2"), resources.getString("UserCreer"), "selectUser");
} else {
  browseBar.setPath(resources.getString("ContactUpdate"));
  operationPane.addOperation(resources.getIcon("yellowpages.contactTopicLink"), resources.getString("TopicLink"), "javascript:topicAddGoTo();");
	
  if (profile.equals("admin") || (ownerDetail!=null && yellowpagesScc.getUserId().equals(ownerDetail.getId()))) {
      isOwner = true;
  }
}

Form formUpdate = (Form) request.getAttribute("Form");
DataRecord data = (DataRecord) request.getAttribute("Data"); 
PagesContext context = (PagesContext) request.getAttribute("PagesContext");
if (context != null) {
	context.setBorderPrinted(false);
}

String linkedPathString = yellowpagesScc.getPath();

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "topicManager.jsp", false);
Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onclick=sendContactData();", false);

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<view:includePlugin name="wysiwyg"/>
<view:includePlugin name="popup"/>
<view:includePlugin name="preview"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function topicGoTo(id) {
    document.topicDetailForm.Action.value = "Search";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}

function topicAddGoTo() {
    document.topicAddLink.submit();
}

function sendContactData() {
    <% if (formUpdate != null) { %>
    if (isCorrectAppForm() && isCorrectForm()) {
    <% } else { %>
    if (isCorrectAppForm()) {
    <% } %>
         document.contactForm.submit();
     }
}

function isCorrectAppForm() {
     var errorMsg = "";
     var errorNb = 0;
     var lastName = stripInitialWhitespace(document.contactForm.LastName.value);

     if (isWhitespace(lastName)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }
     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}
</script>
<%
if (formUpdate != null) {
	formUpdate.displayScripts(out, context);
}
%>
</head>
<body>
<%
out.println(window.printBefore());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<form name="contactForm" action="ContactSave" method="post" enctype="multipart/form-data">
<view:board>
<table cellpadding="5" cellspacing="0" border="0">
<% if (StringUtil.isDefined(userId)) { %>
  <tr><td class="txtlibform"><%=resources.getString("GML.name")%>&nbsp;:</td>
      <td><input type="text" name="LastName" value="<%=EncodeHelper.javaStringToHtmlString(lastName)%>" size="60" maxlength="60" readonly="readonly"/>&nbsp;<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"/></td></tr>
  <tr><td class="txtlibform"><%=resources.getString("GML.surname")%>&nbsp;:</td>
      <td><input type="text" name="FirstName" value="<%=EncodeHelper.javaStringToHtmlString(firstName)%>" size="60" maxlength="60" readonly="readonly"/></td></tr>
  <tr><td class="txtlibform"><%=resources.getString("GML.eMail")%>&nbsp;:</td>
      <td><input type="text" name="Email" value="<%=EncodeHelper.javaStringToHtmlString(email)%>" size="60" maxlength="100" readonly="readonly"/></td></tr>
<% } else { %>
  <tr><td class="txtlibform"><%=resources.getString("GML.name")%>&nbsp;:</td>
      <td><input type="text" name="LastName" value="<%=EncodeHelper.javaStringToHtmlString(lastName)%>" size="60" maxlength="60"/>&nbsp;<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"/></td></tr>
  <tr><td class="txtlibform"><%=resources.getString("GML.surname")%>&nbsp;:</td>
      <td><input type="text" name="FirstName" value="<%=EncodeHelper.javaStringToHtmlString(firstName)%>" size="60" maxlength="60"/></td></tr>
  <tr><td class="txtlibform"><%=resources.getString("GML.eMail")%>&nbsp;:</td>
      <td><input type="text" name="Email" value="<%=EncodeHelper.javaStringToHtmlString(email)%>" size="60" maxlength="100"/></td></tr>
<% } %>
  <tr><td class="txtlibform"><%=resources.getString("GML.phoneNumber")%>&nbsp;:</td>
      <td><input type="text" name="Phone" value="<%=EncodeHelper.javaStringToHtmlString(phone)%>" size="20" maxlength="20"/></td></tr>
  <tr><td class="txtlibform"><%=resources.getString("GML.faxNumber")%>&nbsp;:</td>
      <td><input type="text" name="Fax" value="<%=EncodeHelper.javaStringToHtmlString(fax)%>" size="20" maxlength="20"/></td></tr>
<% if (formUpdate == null) { %>
  <tr><td colspan="2"><img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"/> : <%=resources.getString("GML.requiredField")%></td></tr>
<% } %>
  </table>
  <input type="hidden" name="ContactId" value="<%=id%>"/>
  <input type="hidden" name="UserId" value="<%=EncodeHelper.javaStringToHtmlString(userId)%>"/>

<%
if (formUpdate != null) {
    formUpdate.display(out, context, data);
}
%>
  
</view:board>
</form>

<br/>

<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    out.println(buttonPane.print());
%>
</view:frame>
<%
	out.println(window.printAfter());
%>
<form name="topicDetailForm" action="topicManager.jsp" method="post">
  <input type="hidden" name="Action"/><input type="hidden" name="Id" value=""/>
</form>

<form name="topicAddLink" action="TopicLink.jsp" method="post">
	<input type=hidden name="ContactId" value="<%=id%>"/>
</form>
</body>
</html>