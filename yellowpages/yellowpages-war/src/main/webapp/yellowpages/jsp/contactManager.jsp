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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ page import="com.silverpeas.form.DataRecord" %>
<%@ page import="com.silverpeas.form.Form" %>
<%@ page import="com.silverpeas.form.PagesContext" %>
<%@ page import="com.silverpeas.yellowpages.model.Company" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ include file="checkYellowpages.jsp" %>
<%@ include file="modelUtils.jsp.inc" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="tabManager.jsp.inc" %>

<%!

    void displayUserView(GraphicElementFactory gef, CompleteContact contactComplete, List<Company> companyList, UserDetail owner, ResourcesWrapper resources, ResourceLocator contactSettings, JspWriter out) throws IOException {
        ContactDetail detail = contactComplete.getContactDetail();
        out.println("<table>");
        out.println("<tr><td class=\"txtlibform\">" + resources.getString("Contact") + " :</td>");
        out.println("<td align=\"left\" class=\"txtnav\">" + EncodeHelper.javaStringToHtmlString(detail.getFirstName()) + " " + Encode.javaStringToHtmlString(detail.getLastName()) + "</td>");
        out.println("</tr>");
        out.println("<tr><td valign=\"baseline\" align=\"left\" class=\"txtlibform\">" + resources.getString("GML.company") + " :</td>");
        displayCompanyList(out, companyList);
        out.println("</tr>");
        out.println("<tr><td valign=\"baseline\" align=\"left\" class=\"txtlibform\">" + resources.getString("GML.phoneNumber") + " :</td>");
        out.println("<td align=\"left\">" + EncodeHelper.javaStringToHtmlString(detail.getPhone()) + "</td>");
        out.println("</tr>");
        out.println("<tr><td valign=\"baseline\" align=\"left\" class=\"txtlibform\">" + resources.getString("GML.faxNumber") + " :</td>");
        out.println("<td align=\"left\">" + EncodeHelper.javaStringToHtmlString(detail.getFax()) + "</td>");
        out.println("</tr>");
        out.println("<tr><td valign=\"baseline\" align=\"left\" class=\"txtlibform\">" + resources.getString("GML.eMail") + " :</td>");
        out.println("<td align=\"left\"><a href=mailto:" + EncodeHelper.javaStringToHtmlString(detail.getEmail()) + ">" + Encode.javaStringToHtmlString(Encode.javaStringToHtmlString(detail.getEmail())) + "</A></td>");
        out.println("</tr>");
        out.println("</table>");
    }

    void displayCompanyList(JspWriter out, List<Company> companyList) throws IOException {
        if (companyList != null && companyList.size() > 0) {
            for (int i = 0; i < companyList.size(); i++) {
                Company company = companyList.get(i);
                if (i == 0) {
                    // premier element de la liste affiché sur la même ligne que le label
                    //out.println("<td align='left'>" + EncodeHelper.javaStringToHtmlString(company.getName()) + "</td>");
                    out.println("<td align='left'><ul><li>" + EncodeHelper.javaStringToHtmlString(company.getName()));
                } else {
                    // les autres éléments affichés sur une ligne complète
                    //out.println("</tr><tr><td/><td align='left'>" + EncodeHelper.javaStringToHtmlString(company.getName()) + "</td>");
                    out.println("</li><li>" + EncodeHelper.javaStringToHtmlString(company.getName()));
                }
            }
            out.println("</li></ul></td>");
        }
    }

    // TODO ajouter la classe company_list dans les CSS
    void displayCompanyListWithActionButtons(JspWriter out, List<Company> companyList, ContactDetail contactDetail, ResourcesWrapper resources) throws IOException {
        out.println("<div id='CompanyList' class='company_list'>");
        if (companyList != null && companyList.size() > 0) {

            for (int i = 0; i < companyList.size(); i++) {
                Company company = companyList.get(i);
                out.println(generateCompanyLineHtml(company.getCompanyId(), company.getName(), resources));
            }
        }
        out.println("</div>");
    }

    // TODO : duplicated with the javascript version... how to factorize this ?
    String generateCompanyLineHtml(int companyId, String companyName, ResourcesWrapper resources) {
        StringBuffer result = new StringBuffer("");
        result.append("<div id='company_").append(companyId).append("'>");
        result.append("<input type='hidden' id='companyId_").append(companyId).append("' name='companyIdList' value='").append(companyId).append("' />");
        result.append("<input readonly type='text' value='" + EncodeHelper.javaStringToHtmlString(companyName)).append("' size='50'/>");
        result.append("<span><a href='javaScript:companyRemoveFromList(").append(companyId).append(")' title='").append(resources.getString("CompanyUnlink")).append("'>");
        String iconPath = resources.getIcon("yellowpages.delete");
        result.append("<img border=0 src='").append(iconPath).append("' width=15 height=15>");
        result.append("</a></span>");
        result.append("</div>");
        return result.toString();
    }

%>

<SCRIPT LANGUAGE="JavaScript">
    <!--
    function reallyClose() {
        window.opener.document.topicDetailForm.Action.value = "Search";
        window.opener.document.topicDetailForm.submit();
        window.close();
    }
    //-->
</SCRIPT>
<%
    out.println(gef.getLookStyleSheet());
%>
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

    String[] companyIdList = null;
    List<Company> companyList = null;

//R�cup�ration des param�tres
    String action = (String) request.getAttribute("Action"); //Delete || Add || Update ||
    // ViewContactInTopic || View ||
    // UpdateView || ViewContact ||
    // SaveUser || New || SelectUser

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

    Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=reallyClose();", false);
    Button validateButton = null;

    if (action.equals("Delete") == false) {
%>

<HTML>
<HEAD>
    <TITLE><%=resources.getString("GML.popupTitle")%>
    </TITLE>
    <%
        out.println(gef.getLookStyleSheet());
    %>
    <link href="<%=m_context%>/util/styleSheets/jquery.autocomplete.css" rel="stylesheet" type="text/css"
          media="screen"/>
    <script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery-1.7.1.min.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.autocomplete.js"></script>

    <script language="javascript">

        function contactDeleteConfirm(id) {
            if (window.confirm("<%=yellowpagesScc.getString("ConfirmDeleteContact")%> ?")) {
                document.contactForm.Action.value = "Delete";
                document.contactForm.ContactId.value = id;
                document.contactForm.submit();
            }
        }
        ;

        function topicGoTo(id) {
            document.topicDetailForm.Action.value = "Search";
            document.topicDetailForm.Id.value = id;
            document.topicDetailForm.submit();
        }
        ;

        function topicAddGoTo() {
            document.topicAddLink.submit();
        }
        ;

        function contactGoTo(id, action) {
            document.contactForm.Action.value = "ViewContact";
            document.contactForm.CheckPath.value = "1";
            document.contactForm.ContactId.value = id;
            document.contactForm.submit();
        }
        ;

        function sendContactData(operation) {
            if (isCorrectForm()) {
                document.contactForm.Action.value = operation;
                document.contactForm.submit();
            }
        }
        ;

        function isCorrectForm() {
            var errorMsg = "";
            var errorNb = 0;
            var firstName = stripInitialWhitespace(document.contactForm.FirstName.value);
            var lastName = stripInitialWhitespace(document.contactForm.LastName.value);

            if (isWhitespace(firstName)) {
                errorMsg += "  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.surname")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
                errorNb++;
            }
            if (isWhitespace(lastName)) {
                errorMsg += "  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
                errorNb++;
            }
            switch (errorNb) {
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
        ;

        function init() {
        <% if (!action.equals("SelectUser")) { %>
            document.contactForm.LastName.focus();
        <%	} %>
        }
        ;

        function selectUser() {
            document.contactForm.Action.value = "SelectUser";
            document.contactForm.submit();
        }
        ;

        function autoSubmit() {
            document.enctypeForm.submit();
        }
        ;

        function companyRemoveFromList(companyId) {
            $("#company_" + companyId).remove();

        }
        ;

        function createCompanyLineHtml(companyId, companyName) {
            result = "<div id='company_" + companyId + "'>";
            result = result + "<input type='hidden' id='companyId_" + companyId + "' name='companyIdList' value='" + companyId + "' />";
            result = result + "<input readonly type='text' value='" + companyName + "' size='50'/>";
            result = result + "<span><a href='javaScript:companyRemoveFromList(" + companyId + ")'";
            result = result + " title='<%= resources.getString("CompanyUnlink")%>'>";
            result = result + "<img border=0 src='<%=resources.getIcon("yellowpages.delete")%>' width=15 height=15>";
            result = result + "</a></span></div>";
            return result;
        }
        ;

        $(document).ready(function() {
            $('#CompanyName').autocomplete("<%=m_context%>/CompanyAutocompleteServlet", {
                minChars: 1,
                max: 50,
                autoFill: false,
                mustMatch: true,
                matchContains: false,
                scrollHeight: 220,
                formatItem: function(rowdata) {
                    var company = rowdata[0].split(":");
                    return company[1];
                },
                formatResult: function (rowdata) {
                    var company = rowdata[0].split(":");
                    //$('#CompanyId').val(company[0]);
                    return company[1];
                }
            }).result(function(e, rowdata) {
                        var company = rowdata[0].split(":");
                        companyId = company[0];
                        companyName = company[1];
                        if (!$("#company_" + companyId).length > 0) {
                            // not already in the list
                            $('#CompanyList').append(createCompanyLineHtml(companyId, companyName));
                        }
                        // empty the search field
                        $('#CompanyName').val("");
                    });
        });

    </script>
</HEAD>
<% } // fin action != Delete


/* Add */
    if (action.equals("Add")) {
        //Ajout du contact
        firstName = request.getParameter("FirstName");
        lastName = request.getParameter("LastName");
        companyIdList = request.getParameterValues("companyIdList");
        email = request.getParameter("Email");
        phone = request.getParameter("Phone");
        fax = request.getParameter("Fax");
        userId = request.getParameter("UserId");

        if (!userId.equals("")) {
            contactDetail = new ContactDetail("X", firstName, lastName, email, phone, fax, userId, null, null);
        } else {
            contactDetail = new ContactDetail("X", firstName, lastName, email, phone, fax, null, null, null);
        }
        newContactId = yellowpagesScc.createContact(contactDetail);
        userContactComplete = yellowpagesScc.getCompleteContact(newContactId);
        yellowpagesScc.setCurrentContact(userContactComplete);

        // Update company info for contact
        yellowpagesScc.updateCompanyListForContact(companyIdList, newContactId);

        if (yellowpagesScc.useForm()) {
%>
<BODY onload="autoSubmit()"><%
	}
	else
	{
		%>
<BODY onload="reallyClose()"><%
        }
    }


/* Update */
    else if (action.equals("Update")) {
        //Mise a jour du contact
        firstName = request.getParameter("FirstName");
        lastName = request.getParameter("LastName");
        companyIdList = request.getParameterValues("companyIdList");
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

        // Update company info for contact if needed
        yellowpagesScc.updateCompanyListForContact(companyIdList, id);

    } else if (action.equals("ViewContactInTopic")) {
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
        // get company list if any
        companyList = yellowpagesScc.getCompanyListForUserId(id);

        if ((profile.equals("admin")) || ((ownerDetail != null) && (yellowpagesScc.getUserId().equals(ownerDetail.getId()))))
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
        companyList = yellowpagesScc.getCompanyListForUserId(id);
        firstName = contactDetail.getFirstName();
        lastName = contactDetail.getLastName();
        email = contactDetail.getEmail();
        phone = contactDetail.getPhone();
        fax = contactDetail.getFax();
        userId = contactDetail.getUserId();

        isOwner = true;

        action = "New";
        nextAction = "Add";
    }

/* New || SelectUser */
    else if (action.equals("New") || action.equals("SelectUser")) {
        firstName = (String) request.getParameter("FirstName");
        lastName = (String) request.getParameter("LastName");
        companyIdList = request.getParameterValues("companyIdList");
        email = (String) request.getParameter("Email");
        phone = (String) request.getParameter("Phone");
        fax = (String) request.getParameter("Fax");
        userId = (String) request.getParameter("UserId");

        id = null;
        TopicDetail currentTopic = yellowpagesScc.getCurrentTopic();
        Collection pathColl = currentTopic.getPath();
        linkedPathString = displayPath(yellowpagesScc, pathColl, true, 3);
        yellowpagesScc.setPath(linkedPathString);

        isOwner = true;
        nextAction = "Add";

        if (action.equals("SelectUser")) {
            action = "New";
            contactDetail = new ContactDetail("X", firstName, lastName, email, phone, fax, null, null, null);
            userContactComplete = new UserCompleteContact(null, new CompleteContact(contactDetail, null));
            yellowpagesScc.setCurrentContact(userContactComplete);

            //routage vers le UserPanel
%>
<Script language="JavaScript">
    SP_openWindow('selectUser.jsp', 'selectUser', '750', '650', 'scrollbars=yes, resizable, alwaysRaised');
</Script>
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

    validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendContactData('" + nextAction + "');", false);
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);

    OperationPane operationPane = window.getOperationPane();
    Board board = gef.getBoard();
    Frame frame = gef.getFrame();

/* Update */
    if (action.equals("Update"))
        out.println("<BODY onload = \"reallyClose()\">");

/* New || UpdateView */
    else if (action.equals("New") || action.equals("UpdateView")) {
        /* New */
        if (action.equals("New")) {
            out.println("<BODY onLoad=\"init()\">");
        }

        /* UpdateView */
        else {
            out.println("<BODY onLoad=\"document.contactForm.LastName.focus();\">");
        }

        /* New */
        if (action.equals("New")) {
            browseBar.setPath(resources.getString("ContactCreation"));
            operationPane.addOperation(resources.getIcon("yellowpages.contactAdd2"), resources.getString("UserCreer"), "javascript:selectUser();");
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

        out.println(frame.printBefore());
        out.println(board.printBefore());
%>
<center>
    <FORM Name="contactForm" Action="contactManager.jsp" Method="POST">
        <table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%">
            <tr>
                <td NOWRAP>
                    <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
                        <%
                            if ((userId != null) && (!userId.equals(""))) {
                        %>
                        <TR>
                            <td valign="baseline" align=left class="txtlibform"><%=resources.getString("GML.name")%>
                                &nbsp;:
                            </TD>
                            <TD><input type="text" name="LastName" value="<%=Encode.javaStringToHtmlString(lastName)%>"
                                       size="60" maxlength="60" readonly>&nbsp;<img border="0"
                                                                                    src="<%=resources.getIcon("yellowpages.mandatory")%>"
                                                                                    width="5" height="5"></TD>
                        </TR>
                        <TR>
                            <td valign="baseline" align=left class="txtlibform"><%=resources.getString("GML.surname")%>
                                &nbsp;:
                            </TD>
                            <TD><input type="text" name="FirstName"
                                       value="<%=Encode.javaStringToHtmlString(firstName)%>" size="60" maxlength="60"
                                       readonly>&nbsp;<img border="0"
                                                           src="<%=resources.getIcon("yellowpages.mandatory")%>"
                                                           width="5" height="5"></TD>
                        </TR>
                        <TR>
                            <td valign="baseline" align=left class="txtlibform"><%=resources.getString("GML.eMail")%>
                                &nbsp;:
                            </TD>
                            <TD><input type="text" name="Email" value="<%=Encode.javaStringToHtmlString(email)%>"
                                       size="60" maxlength="100" readonly></TD>
                        </TR>
                        <%
                        } else {
                        %>
                        <TR>
                            <td valign="baseline" align=left class="txtlibform"><%=resources.getString("GML.name")%>
                                &nbsp;:
                            </TD>
                            <TD><input type="text" name="LastName" value="<%=Encode.javaStringToHtmlString(lastName)%>"
                                       size="60" maxlength="60">&nbsp;<img border="0"
                                                                           src="<%=resources.getIcon("yellowpages.mandatory")%>"
                                                                           width="5" height="5"></TD>
                        </TR>
                        <TR>
                            <td valign="baseline" align=left class="txtlibform"><%=resources.getString("GML.surname")%>
                                &nbsp;:
                            </TD>
                            <TD><input type="text" name="FirstName"
                                       value="<%=Encode.javaStringToHtmlString(firstName)%>" size="60" maxlength="60">&nbsp;<img
                                    border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5"
                                    height="5"></TD>
                        </TR>
                        <TR>
                            <td valign="baseline" align=left class="txtlibform"><%=resources.getString("GML.eMail")%>
                                &nbsp;:
                            </TD>
                            <TD><input type="text" name="Email" value="<%=Encode.javaStringToHtmlString(email)%>"
                                       size="60" maxlength="100"></TD>
                        </TR>
                        <%
                            }
                        %>
                        <TR>
                            <td valign="baseline" align=left
                                class="txtlibform"><%=resources.getString("GML.phoneNumber")%>&nbsp;:
                            </TD>
                            <TD><input type="text" name="Phone" value="<%=Encode.javaStringToHtmlString(phone)%>"
                                       size="20" maxlength="20"></TD>
                        </TR>
                        <TR>
                            <td valign="baseline" align=left
                                class="txtlibform"><%=resources.getString("GML.faxNumber")%>&nbsp;:
                            </TD>
                            <TD><input type="text" name="Fax" value="<%=Encode.javaStringToHtmlString(fax)%>" size="20"
                                       maxlength="20"></TD>
                        </TR>
                        <TR>
                            <td valign="baseline" align=left class="txtlibform"><%=resources.getString("GML.company")%>
                                &nbsp;:
                            </TD>
                            <TD><input type="text" id="CompanyName" name="CompanyName" value="" size="60"
                                       maxlength="100"></TD>
                        </TR>
                        <tr>
                            <td/>
                            <td>
                                <%
                                    // Liste des companies déjà affectées au contact
                                    displayCompanyListWithActionButtons(out, companyList, contactDetail, resources);
                                %>
                            </td>
                        </tr>
                        <TR>
                            <td valign="baseline" align=left
                                class="txtlibform"><%=resources.getString("GML.publisher")%>&nbsp;:
                            </TD>
                            <TD><%=creatorName%>
                            </TD>
                        </TR>
                        <TR>
                            <td valign="baseline" align=left
                                class="txtlibform"><%=resources.getString("ContactDateCreation")%>&nbsp;:
                            </TD>
                            <TD><%=creationDate%>
                            </TD>
                        </TR>
                        <tr>
                            <td colspan="2">(<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>"
                                                  width="5" height="5"> : <%=resources.getString("GML.requiredField")%>)
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        <input type="hidden" name="Action"><input type="hidden" name="ContactId" value="<%=id%>">
        <input type="hidden" name="UserId" value="<%=Encode.javaStringToHtmlString(userId)%>">
    </FORM>
</center>
<br>
<%
    out.println(board.printAfter());
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    buttonPane.setHorizontalPosition();
    out.println("<br><center>" + buttonPane.print() + "</center><br>");
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
<FORM NAME="topicDetailForm" ACTION="topicManager.jsp" METHOD=POST>
    <input type="hidden" name="Action"><input type="hidden" name="Id" value="<%=id%>">
</FORM>
<% } // fin du action = "New" or "UpdateView"

/* ViewContact */
else if (action.equals("ViewContact")) {
    Form formView = (Form) request.getAttribute("Form");
    DataRecord data = (DataRecord) request.getAttribute("Data");
    PagesContext context = (PagesContext) request.getAttribute("PagesContext");

    out.println(window.printBefore());

    browseBar.setPath(resources.getString("BBarconsultManager"));
    browseBar.setClickable(false);


    operationPane = window.getOperationPane();
    operationPane.addOperation(resources.getIcon("yellowpages.contactPrint"), resources.getString("GML.print"), "javaScript:window.print();");

    out.println(frame.printBefore());
    out.println(board.printBefore());

    displayUserView(gef, contactComplete, companyList, ownerDetail, resources, contactSettings, out);

    if (formView != null)
        formView.display(out, context, data);

    out.println(board.printAfter());
%>
<view:comments userId="<%= yellowpagesScc.getUserId() %>" componentId="<%= yellowpagesScc.getComponentId() %>"
               resourceId="<%= id %>" indexed="false"/>
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>

<FORM NAME="contactForm" ACTION="contactManager.jsp" METHOD="POST">
    <input type="hidden" name="Action">
    <input type="hidden" name="ContactId">
    <input type="hidden" name="CheckPath">
</FORM>

<FORM NAME="topicDetailForm" ACTION="topicManager.jsp" METHOD=POST>
    <input type="hidden" name="Action">
    <input type="hidden" name="Id" value="<%=id%>">
</FORM>
<% } // fin du if action == "ViewContact" %>

<FORM NAME="topicAddLink" ACTION="TopicLink.jsp" METHOD=POST>
    <input type=hidden name="ContactId" value="<%=id%>">
</FORM>
<Form Name="enctypeForm" ACTION="modelManager.jsp" Method="POST" ENCTYPE="multipart/form-data">
    <input type="hidden" name="ContactId" VALUE="<%=newContactId%>">
    <input type="hidden"    name="Action" VALUE="NewModel">
</Form>
</BODY>
</HTML>