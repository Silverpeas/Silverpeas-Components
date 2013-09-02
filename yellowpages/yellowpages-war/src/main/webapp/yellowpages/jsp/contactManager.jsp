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

<%@ include file="checkYellowpages.jsp" %>
<%@ include file="modelUtils.jsp.inc" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="tabManager.jsp.inc" %>

<%!

void displayUserView(GraphicElementFactory gef, CompleteContact contactComplete, UserDetail owner, ResourcesWrapper resources, ResourceLocator contactSettings, JspWriter out) throws IOException
{
	ContactDetail detail = contactComplete.getContactDetail();
   out.println("<table>");
   out.println("<tr><td class=\"txtlibform\">"+resources.getString("Contact")+" :</td>");
   out.println("<td align=\"left\" class=\"txtnav\">"+EncodeHelper.javaStringToHtmlString(detail.getFirstName())+" "+EncodeHelper.javaStringToHtmlString(detail.getLastName())+"</td>");
   out.println("</tr>");
   out.println("<tr><td valign=\"baseline\" align=\"left\" class=\"txtlibform\">"+resources.getString("GML.phoneNumber")+" :</td>");
   out.println("<td align=\"left\">"+EncodeHelper.javaStringToHtmlString(detail.getPhone())+"</td>");
   out.println("</tr>");
   out.println("<tr><td valign=\"baseline\" align=\"left\" class=\"txtlibform\">"+resources.getString("GML.faxNumber")+" :</td>");
   out.println("<td align=\"left\">"+EncodeHelper.javaStringToHtmlString(detail.getFax())+"</td>");
   out.println("</tr>");
   out.println("<tr><td valign=\"baseline\" align=\"left\" class=\"txtlibform\">"+resources.getString("GML.eMail")+" :</td>");
   out.println("<td align=\"left\"><a href=mailto:"+EncodeHelper.javaStringToHtmlString(detail.getEmail())+">"+EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToHtmlString(detail.getEmail()))+"</A></td>");
   out.println("</tr>");
   out.println("</table>");
}

%>

<SCRIPT LANGUAGE="JavaScript">
<!--
function reallyClose()
{
  window.opener.document.topicDetailForm.Action.value = "Search";
  window.opener.document.topicDetailForm.submit();
  window.close();
}
//-->
</SCRIPT>

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

  String newContactId = ""; //result of Contact creation
  String nextAction = "";

  UserCompleteContact userContactComplete = null;
  UserDetail ownerDetail = null;

  CompleteContact contactComplete = null;
  ContactDetail contactDetail = null;

//Recuperation des parametres
String action = (String) request.getAttribute("Action"); //Delete || Add || Update ||
// ViewContactInTopic || View || UpdateView || ViewContact || SaveUser || New || SelectUser

String id = (String) request.getAttribute("ContactId");
String profile = (String) request.getAttribute("Profile");

//Vrai si le user connecte est le createur de ce contact ou si il est admin
boolean isOwner = false;

String linkedPathString = yellowpagesScc.getPath();

//Chaines de caracteres lues dans des fichiers de properties
ResourceLocator contactSettings = new ResourceLocator("com.stratelia.webactiv.contact.contactSettings", yellowpagesScc.getLanguage());

TopicDetail CurrentTopic = yellowpagesScc.getCurrentTopic();
UserCompleteContact CurrentContact = yellowpagesScc.getCurrentContact();
String owner = yellowpagesScc.getOwner();
String Path = yellowpagesScc.getPath();

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=reallyClose();", false);
Button validateButton = null;

if (action.equals("Delete") == false) {
%>

<%@page import="com.silverpeas.util.StringUtil"%>
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
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
function contactDeleteConfirm(id) {
    if(window.confirm("<%=yellowpagesScc.getString("ConfirmDeleteContact")%> ?")){
          document.contactForm.Action.value = "Delete";
          document.contactForm.ContactId.value = id;
          document.contactForm.submit();
    }
}

function topicGoTo(id) {
    document.topicDetailForm.Action.value = "Search";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}

function topicAddGoTo() {
    document.topicAddLink.submit();
}

function contactGoTo(id, action){
    document.contactForm.Action.value = "ViewContact";
    document.contactForm.CheckPath.value = "1";
    document.contactForm.ContactId.value = id;
    document.contactForm.submit();
}

function sendContactData(operation) {
    if (isCorrectForm()) {
         document.contactForm.Action.value = operation;
         document.contactForm.submit();
     }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var firstName = stripInitialWhitespace(document.contactForm.FirstName.value);
     var lastName = stripInitialWhitespace(document.contactForm.LastName.value);

     if (isWhitespace(firstName)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.surname")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }
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

function init()
{
	<% if (!action.equals("SelectUser")) { %>
				document.contactForm.LastName.focus();
	<%	} %>
}

function selectUser()
{
	document.contactForm.Action.value = "SelectUser";
	document.contactForm.submit();
}

function autoSubmit(){
	document.enctypeForm.submit();
}
</script>
</head>
<% } // fin action != Delete


/* Add */
if (action.equals("Add")) {
	//Ajout du contact
	firstName = request.getParameter("FirstName");
	lastName = request.getParameter("LastName");
	email = request.getParameter("Email");
	phone = request.getParameter("Phone");
	fax = request.getParameter("Fax");
	userId = request.getParameter("UserId");

	if (! userId.equals("")) {
	  contactDetail = new ContactDetail("X",firstName, lastName, email, phone, fax, userId, null, null);
	}
	else {
	  contactDetail = new ContactDetail("X", firstName, lastName, email, phone, fax, null, null, null);
	}
	newContactId = yellowpagesScc.createContact(contactDetail);
    userContactComplete = yellowpagesScc.getCompleteContact(newContactId);
    yellowpagesScc.setCurrentContact(userContactComplete);


	if (yellowpagesScc.useForm())
	{
		%><body onload = "autoSubmit()"><%
	}
	else
	{
		%><body onload = "reallyClose()"><%
   }
}


/* Update */
else if (action.equals("Update")) {
      //Mise a jour du contact
      firstName = request.getParameter("FirstName");
      lastName = request.getParameter("LastName");
      email = request.getParameter("Email");
      phone = request.getParameter("Phone");
      fax = request.getParameter("Fax");
      userId = request.getParameter("UserId");
      if (!userId.equals("")) {
        contactDetail = new ContactDetail(id, firstName, lastName, email, phone, fax, userId, null,
              null);
      } else {
        contactDetail = new ContactDetail(id, firstName, lastName, email, phone, fax, null, null,
              null);
      }
      yellowpagesScc.updateContact(contactDetail);
      userContactComplete = yellowpagesScc.getCompleteContact(id);
      yellowpagesScc.setCurrentContact(userContactComplete);
}
else if (action.equals("ViewContactInTopic"))
{
    topicId = (String) request.getAttribute("TopicId");
    CurrentTopic = yellowpagesScc.getTopic(topicId);
    action = "ViewContact";
}
if (action.equals("View") || action.equals("UpdateView") || action.equals("ViewContact")) {
      //Recuperation des parametres du contact
      if (StringUtil.isDefined(topicId))
    	  userContactComplete = yellowpagesScc.getCompleteContactInNode(id, topicId);
      else
      	  userContactComplete = yellowpagesScc.getCompleteContact(id);
      yellowpagesScc.setCurrentContact(userContactComplete);
      contactComplete = userContactComplete.getContact();
      contactDetail = contactComplete.getContactDetail();
      ownerDetail = userContactComplete.getOwner();

      if ((profile.equals("admin")) || ((ownerDetail!=null)&&(yellowpagesScc.getUserId().equals(ownerDetail.getId()))))
          isOwner = true;

      if (isOwner) {
            yellowpagesScc.setOwner("true");
            if (action.equals("View"))
                action = "UpdateView";
      } else {
            yellowpagesScc.setOwner("false");
            if (action.equals("View"))
                action = "ViewContact";
      }

      firstName = contactDetail.getFirstName();
      lastName = contactDetail.getLastName();
      email = contactDetail.getEmail();
      phone = contactDetail.getPhone();
      fax = contactDetail.getFax();
      userId = contactDetail.getUserId();
      creationDate = resources.getOutputDate(contactDetail.getCreationDate());
      if (ownerDetail != null)
          creatorName = ownerDetail.getDisplayedName();
      else
          creatorName = yellowpagesScc.getString("UnknownAuthor");
      nextAction = "Update";
}

/* SaveUser : retour du UserPanel */
else if (action.equals("SaveUser")) {
	  //infos saisies par l'utilisateur : Bouton Annuler du UserPanel
	  //ou infos recuperees a partir de la selection UserPanel : Bouton Valider du UserPanel
	  userContactComplete = yellowpagesScc.getCurrentContact();
      contactComplete = userContactComplete.getContact();
      contactDetail = contactComplete.getContactDetail();

      firstName = contactDetail.getFirstName();
      lastName = contactDetail.getLastName();
      email = contactDetail.getEmail();
      phone = contactDetail.getPhone();
      fax = contactDetail.getFax();
      userId = contactDetail.getUserId();

      isOwner=true;

      action = "New";
      nextAction = "Add";
}

/* New || SelectUser */
else if (action.equals("New") || action.equals("SelectUser")) {
      firstName = request.getParameter("FirstName");
      lastName = request.getParameter("LastName");
      email = request.getParameter("Email");
      phone = request.getParameter("Phone");
      fax = request.getParameter("Fax");
      userId = request.getParameter("UserId");

		  id = null;
      TopicDetail currentTopic = yellowpagesScc.getCurrentTopic();
      Collection pathColl = currentTopic.getPath();
      linkedPathString = displayPath(yellowpagesScc,pathColl, true, 3);
      yellowpagesScc.setPath(linkedPathString);

      isOwner=true;
      nextAction = "Add";

      if (action.equals("SelectUser")) {
	    	action = "New";
       	contactDetail = new ContactDetail("X", firstName, lastName, email, phone, fax, null, null, null);
     		userContactComplete = new UserCompleteContact(null, new CompleteContact(contactDetail, null));
     		yellowpagesScc.setCurrentContact(userContactComplete);

     		//routage vers le UserPanel
	      %>
				<script language="JavaScript">
					SP_openWindow('selectUser.jsp','selectUser','750','650','scrollbars=yes, resizable, alwaysRaised');
				</script>
	      <%
      }
}

/* Delete */
else if (action.equals("Delete")) {
      //Suppression du contact
      yellowpagesScc.deleteContact(id);
      %>
		  <jsp:forward page="<%=yellowpagesScc.getComponentUrl()+\"topicManager.jsp?Action=Search&Id=1\"%>"/>
      <%
      return;
}

validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendContactData('"+nextAction+"');", false);
Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel);

OperationPane operationPane = window.getOperationPane();
Board board = gef.getBoard();
Frame frame = gef.getFrame();

/* Update */
if (action.equals("Update"))
	out.println("<body onload = \"reallyClose()\">");

/* New || UpdateView */
else if (action.equals("New") || action.equals("UpdateView")) {
	/* New */
	if (action.equals("New")) {
		out.println("<body onload=\"init()\">");
	}

	/* UpdateView */
	else {
		out.println("<body onload=\"document.contactForm.LastName.focus();\">");
	}

	/* New */
	if (action.equals("New"))
	{
		browseBar.setPath(resources.getString("ContactCreation"));
		operationPane.addOperationOfCreation(resources.getIcon("yellowpages.contactAdd2"), resources.getString("UserCreer"), "javascript:selectUser();");
	}

	/* UpdateView */
	if (action.equals("UpdateView")) {
		browseBar.setPath(resources.getString("ContactUpdate"));
		operationPane.addOperation(resources.getIcon("yellowpages.contactTopicLink"), resources.getString("TopicLink"), "javascript:topicAddGoTo();");
	}

	TopicDetail currentTopic = yellowpagesScc.getCurrentTopic();

	out.println(window.printBefore());

	if (isOwner && yellowpagesScc.useForm()) {
		boolean useModel = yellowpagesScc.getCurrentTopic().getNodeDetail().getId() != ROOT_TOPIC;
		displayContactOperations(resources, id, gef, action, out, useModel);
	}
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<view:board>
<center>
<form name="contactForm" action="contactManager.jsp" method="post">
<table cellpadding="0" cellspacing="2" border="0" width="98%">
    <tr><td nowrap="nowrap">
            <table cellpadding="5" cellspacing="0" border="0" width="100%">
<% if ((userId != null) && (!userId.equals(""))) { %>
  <tr><td valign="baseline" align=left  class="txtlibform"><%=resources.getString("GML.name")%>&nbsp;:</td>
      <td><input type="text" name="LastName" value="<%=EncodeHelper.javaStringToHtmlString(lastName)%>" size="60" maxlength="60" readonly="readonly"/>&nbsp;<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"/></td></tr>
  <tr><td valign="baseline" align=left  class="txtlibform"><%=resources.getString("GML.surname")%>&nbsp;:</td>
      <td><input type="text" name="FirstName" value="<%=EncodeHelper.javaStringToHtmlString(firstName)%>" size="60" maxlength="60" readonly="readonly"/>&nbsp;<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"/></td></tr>
  <tr><td valign="baseline" align=left  class="txtlibform"><%=resources.getString("GML.eMail")%>&nbsp;:</td>
      <td><input type="text" name="Email" value="<%=EncodeHelper.javaStringToHtmlString(email)%>" size="60" maxlength="100" readonly="readonly"/></td></tr>
<% } else { %>
  <tr><td valign="baseline" align=left  class="txtlibform"><%=resources.getString("GML.name")%>&nbsp;:</td>
      <td><input type="text" name="LastName" value="<%=EncodeHelper.javaStringToHtmlString(lastName)%>" size="60" maxlength="60"/>&nbsp;<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"/></td></tr>
  <tr><td valign="baseline" align=left  class="txtlibform"><%=resources.getString("GML.surname")%>&nbsp;:</td>
      <td><input type="text" name="FirstName" value="<%=EncodeHelper.javaStringToHtmlString(firstName)%>" size="60" maxlength="60"/>&nbsp;<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"/></td></tr>
  <tr><td valign="baseline" align=left  class="txtlibform"><%=resources.getString("GML.eMail")%>&nbsp;:</td>
      <td><input type="text" name="Email" value="<%=EncodeHelper.javaStringToHtmlString(email)%>" size="60" maxlength="100"/></td></tr>
<% } %>
  <tr><td valign="baseline" align=left  class="txtlibform"><%=resources.getString("GML.phoneNumber")%>&nbsp;:</td>
      <td><input type="text" name="Phone" value="<%=EncodeHelper.javaStringToHtmlString(phone)%>" size="20" maxlength="20"/></td></tr>
  <tr><td valign="baseline" align=left  class="txtlibform"><%=resources.getString("GML.faxNumber")%>&nbsp;:</td>
      <td><input type="text" name="Fax" value="<%=EncodeHelper.javaStringToHtmlString(fax)%>" size="20" maxlength="20"/></td></tr>
  <tr><td valign="baseline" align=left  class="txtlibform"><%=resources.getString("GML.publisher")%>&nbsp;:</td>
      <td><%=creatorName%></td></tr>
  <tr><td valign="baseline" align=left  class="txtlibform"><%=resources.getString("ContactDateCreation")%>&nbsp;:</td>
      <td><%=creationDate%></td></tr>
  <tr><td colspan="2">(<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"/> : <%=resources.getString("GML.requiredField")%>)</td></tr>
  </table></td></tr></table>
  <input type="hidden" name="Action"/><input type="hidden" name="ContactId" value="<%=id%>"/>
	<input type="hidden" name="UserId" value="<%=EncodeHelper.javaStringToHtmlString(userId)%>"/>
  </form>
</center>
</view:board>
<br/>
  <%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    buttonPane.setHorizontalPosition();
    out.println(buttonPane.print());
%>
</view:frame>
<%
	out.println(window.printAfter());
%>
<form name="topicDetailForm" action="topicManager.jsp" method="post">
  <input type="hidden" name="Action"/><input type="hidden" name="Id" value="<%=id%>"/>
</form>
<% } // fin du action = "New" or "UpdateView"

/* ViewContact */
else if (action.equals("ViewContact")) {
		Form formView    = (Form) request.getAttribute("Form");
		DataRecord data    = (DataRecord) request.getAttribute("Data");
		PagesContext context = (PagesContext) request.getAttribute("PagesContext");

		out.println(window.printBefore());

		browseBar.setPath(resources.getString("BBarconsultManager"));
		browseBar.setClickable(false);


		operationPane = window.getOperationPane();
		operationPane.addOperation(resources.getIcon("yellowpages.contactPrint"), resources.getString("GML.print"), "javaScript:window.print();");

		out.println(frame.printBefore());
		out.println(board.printBefore());

		displayUserView(gef, contactComplete, ownerDetail, resources, contactSettings, out);

		if (formView != null)
			formView.display(out, context, data);

		out.println(board.printAfter());
		out.println(frame.printAfter());
		out.println(window.printAfter());
		%>

		<form name="contactForm" action="contactManager.jsp" method="post">
			<input type="hidden" name="Action"/>
			<input type="hidden" name="ContactId"/>
			<input type="hidden" name="CheckPath"/>
		</form>

		<form name="topicDetailForm" action="topicManager.jsp" method="post">
		  <input type="hidden" name="Action"/>
		  <input type="hidden" name="Id" value="<%=id%>"/>
		</form>
<% } // fin du if action == "ViewContact" %>

<form name="topicAddLink" action="TopicLink.jsp" method="post">
	<input type=hidden name="ContactId" value="<%=id%>"/>
</form>
<form name="enctypeForm" action="modelManager.jsp" method="post" enctype="multipart/form-data">
	<input type="hidden" name="ContactId" value="<%=newContactId%>"/>
	<input type="hidden" name="Action" value="NewModel"/>
</form>
</body>
</html>