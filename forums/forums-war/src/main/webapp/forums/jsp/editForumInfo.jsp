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
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp"%>
<%@ include file="tabManager.jsp"%>
<%!
public String navigationBar(int forumId, ResourceLocator resource, ForumsSessionController fsc)
    throws ForumsException
{
    String navigation = "";
    if (forumId != 0)
    {
    	navigation = "<a href=\"" + ActionUrl.getUrl("viewForum", -1, forumId) + "\">" +
            fsc.getForumName(forumId) + "</a>";
        int currentId = forumId;
        boolean loop = true;
        while (loop)
        {
            int forumParent = fsc.getForumParentId(currentId);
            if (forumParent == 0)
            {
            	loop = false;
            }
            else
            {
                String parentName = fsc.getForumName(forumParent);
                String line = "<a href=\"" + ActionUrl.getUrl("viewForum", -1, forumParent)
                    + "\">" + parentName + "</a> &gt; ";
                navigation = line + navigation;
                currentId = forumParent;
            }
        }
    }
    return navigation;
}

public void listFolders(JspWriter out, int rootId, int forumId, int parentId, String indent,
    ForumsSessionController fsc)
{
    try
    {
        int[] sonsIds = fsc.getForumSonsIds(rootId);
        Forum sonForum;
        for (int i = 0; i < sonsIds.length; i++)
        {
            int sonId = sonsIds[i];
            if (forumId != sonId)
            {
                sonForum = fsc.getForum(sonId);
                out.print("<option ");
                if (parentId == sonForum.getId())
                {
                	out.println("selected ");
                }
                out.print("value=\"");
                out.print(sonForum.getId());
                out.print("\">");
                out.print(indent + Encode.javaStringToHtmlString(sonForum.getName()));
                out.println("</option>");
                listFolders(out, sonId, forumId, parentId, indent + "-", fsc);
            }
        }
    }
    catch (IOException ioe)
    {
        SilverTrace.info("forums", "JSPeditForumInfo.listFolders()", "root.EX_NO_MESSAGE", null, ioe);
    }
}
%>
<%
    Collection allCategories = fsc.getAllCategories();

    int forumId = getIntParameter(request, "forumId", 0);

    String call = request.getParameter("call");
    String backURL = ActionUrl.getUrl(call, -1, forumId);

    int params = getIntParameter(request, "params");

    int action = getIntParameter(request, "action", 1);

    SilverTrace.debug("forums", "JSPeditForumInfo", "root.MSG_GEN_PARAM_VALUE",
        "isAdmin=" + isAdmin + " ; " + "forumId=" + forumId + " ; " + "call=" + call
        + " ; " + "params=" + params + " ; " + "action=" + action);

    int parentId = 0;
    boolean update = false;
	if (action == 1)
    {
		parentId = params;
        update = false;
    }
	else if (action == 2)
    {
		forumId = params;
        update = true;
    }

    boolean isModerator = (params != 0 && fsc.isModerator(userId, params));

    Forum forum = null;
    String categoryId = null;
    String keywords = null;
    if (update)
    {
        forum = fsc.getForum(forumId);
        parentId = forum.getParentId();
        categoryId = forum.getCategory();
        keywords = fsc.getForumKeywords(forumId);
    }
%>
<html>
<head>
    <title>_________________/ Silverpeas - Corporate portal organizer \_________________/</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><%
    out.println(graphicFactory.getLookStyleSheet());
%>
    <script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
    <script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript"><%

    if (isAdmin || isModerator)
    {
%>
        function submitForm()
        {
            nbr = document.forumsForm.moderators.length;
            var j;
            for (j = 0; j < nbr; j++)
            {
                document.forumsForm.moderators[j].selected = true;
            }
            if (!isValidTextArea(document.forumsForm.forumDescription))
            {
                window.alert("Le texte saisi dans le champ Description est trop long !");
            }
            else
            {
                if (document.forumsForm.forumName.value == "")
                {
                    alert("<%=resource.getString("emptyForumTitle")%>");
                }
                else
                {
                    document.forumsForm.submit();
                }
            }
        }

        function moveUsers(button)
        {
            var z = 0;
            var indexArray = new Array();
            if (button == ">")
            {
                var source = document.forumsForm.availableUsers;
                var target = document.forumsForm.moderators;
            }
            else
            {
                var target = document.forumsForm.availableUsers;
                var source = document.forumsForm.moderators;
            }
            var i;
            for (i = 0; i < source.length; i++)
            {
                if (source.options[i].selected)
                {
                    var selectedText = source.options[i].text;
                    var selectedValue = source.options[i].value;
                    target.options[target.length] = new Option(selectedText, selectedValue);
                    indexArray[z] = i;
                    z++;
                }
            }
            for (i = source.length - 1; i >= 0; i--)
            {
                source.options[indexArray[i]] = null;
            }
        }

        function moveAllUsers()
        {
            var target = document.forumsForm.availableUsers;
            var source = document.forumsForm.moderators;
            var i;
            for (i = 0; i < source.length; i++)
            {
                var selectedText = source.options[i].text;
                var selectedValue = source.options[i].value;
                target.options[target.length] = new Option(selectedText, selectedValue);
            }
            for (i = source.length - 1; i >= 0; i--)
            {
                source.options[i] = null;
            }
        }

        function removeAllUsers()
        {
            var source = document.forumsForm.availableUsers;
            var target = document.forumsForm.moderators;
            var i;
            for (i = 0; i < source.length; i++)
            {
                var selectedText = source.options[i].text;
                var selectedValue = source.options[i].value;
                target.options[target.length] = new Option(selectedText, selectedValue);
            }
            for (i = source.length - 1; i >= 0; i--)
            {
                source.options[i] = null;
            }
        }<%

    }
%>
    </script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" <%addBodyOnload(out, fsc, "document.forumsForm.forumName.focus();");%>><%

    Window window = graphicFactory.getWindow();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel());
    browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main"));
    browseBar.setPath(navigationBar(forumId, resource, fsc));
    browseBar.setExtraInformation(resource.getString("creatnewForum"));

    if (!isReader)
    {
        OperationPane operationPane = window.getOperationPane();
        operationPane.addOperation(context + "/util/icons/forums_mailtoAdmin.gif",
            resource.getString("mailAdmin"),
            "javascript:notifyPopup2('" + context + "','" + fsc.getComponentId() + "','"
                + fsc.getAdminIds() + "','');");
    }

    out.println(window.printBefore());

    Frame frame = graphicFactory.getFrame();
    if (fsc.isPdcUsed() && update)
    {
    	displayTabs(params, forumId, fsc, graphicFactory, "editForumInfos", out);
    }

    out.println(frame.printBefore());
%>
    <center>
        <table class="intfdcolor4" border="0" cellpadding="0" cellspacing="0" width="98%">
            <tr>
                <td valign="top"><%

    if (isAdmin || isModerator)
    {
        String formAction = ActionUrl.getUrl("main", (update ? 7 : 3), (forumId > 0 ? forumId : -1));
%>
                    <table width="100%" cellpadding="5" cellspacing="0" border="0" class="contourintfdcolor">
                    <form name="forumsForm" action="<%=formAction%>" method="post">
                        <tr>
                            <td align="center" colspan="3">
                                <!-- SAISIE DU FORUM -->
                                <table width="100%" cellpadding="5" cellspacing="0" border="0">
                                    <tr>
                                        <td>
                                            <span class="txtlibform"><%=resource.getString("forumName")%> :</span>
                                        </td>
                                        <td>
                                            <input type="text" name="forumName" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>"
                                                <%if (update) {%>value="<%=Encode.javaStringToHtmlString(forum.getName())%>"<%}%>/>
                                            &nbsp;<img src="<%=context%>/util/icons/mandatoryField.gif" width="5" height="5">
                                        </td>
                                    </tr>
                                    <!-- Affichage de la liste des cat�gories -->
                                    <tr>
                                        <td>
                                            <span class="txtlibform"><%=resource.getString("forums.category")%> :&nbsp;</span>
                                        </td>
                                        <td>
                                            <select name="CategoryId">
                                                <option value=""></option><%

        if (allCategories != null)
        {
            Iterator it = (Iterator) allCategories.iterator();
            NodeDetail currentCategory;
            String currentCategoryId;
            String selected;
            while (it.hasNext())
            {
                currentCategory = (NodeDetail) it.next();
                currentCategoryId = currentCategory.getNodePK().getId();
                selected = ((categoryId != null && categoryId.equals(currentCategoryId))
                    ? "selected" : "");
%>
                                                <option value=<%=currentCategoryId%> <%=selected%>><%=currentCategory.getName()%></option><%

            }
        }
%>
                                            </select>
                                        </td>
                                    </tr><%

        if (fsc.forumInsideForum())
        {
%>
                                    <tr>
                                        <td><span class="txtlibform"><%=resource.getString("forumFolder")%> :&nbsp;</span></td>
                                        <td><span class="selectNS">
                                            <select name="forumFolder">
                                                <option <%if (parentId == 0) {%>selected <%}%> value="0"><%=resource.getString("racine")%></option><%

            listFolders(out, 0, forumId, parentId, "", fsc);
%>
                                            </select></span>
                                            &nbsp;<img src="<%=context%>/util/icons/mandatoryField.gif" width="5" height="5"></td>
                                    </tr><%

        }
        else
        {
%>
                                    <input type="hidden" name="forumFolder" value="0"><%

        }
%>
                                    <tr>
                                        <td valign="top"><span class="txtlibform"><%=resource.getString("forumDescription")%> :</span></td>
                                        <td><font size="1"><textarea name="forumDescription" cols="49" rows="6" wrap="VIRTUAL"><%if (update) {%><%=forum.getDescription()%><%}%></textarea></font></td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <span class="txtlibform"><%=resource.getString("forumKeywords")%> :</span>
                                        </td>
                                        <td>
                                            <input type="text" name="forumKeywords" size="50" <%if (update) {%>value="<%=keywords%>"<%}%>/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td align="left" colspan="2"><span class="txtnote">(<img src="<%=context%>/util/icons/mandatoryField.gif" width="5" height="5">&nbsp;=
                                            &nbsp;<%=resource.getString("reqchamps")%>)</span></td>
                                    </tr>
                                </table>
                            </td>
                        </tr><%

        if (update) {
%>
                        <input type="hidden" name="forumId" value="<%=forumId%>"/><%

        }
%>
                    </table>
                </td>
            </tr>
        </table>
        <br>
        <table cellpadding="5" cellspacing="2" border="0" width="98%" class="intfdcolor">
            <tr align="center">
                <td class="intfdcolor4">
                    <table width="98%" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                            <td colspan="3" align=left><span class="txtnav"><%=resource.getString("forumModerators")%> : </span></td>
                        </tr>
                        <tr>
                            <td colspan="3">&nbsp;</td>
                        </tr>
                        <tr>
                            <td nowrap align="right"><span class="txtlibform"><%=resource.getString("availableUsers")%> : </span>&nbsp;</td>
                            <td nowrap width="10%">&nbsp;</td>
                            <td nowrap align="left">&nbsp;<span class="txtlibform"><%=resource.getString("moderators")%> : </span></td>
                        </tr>
                        <tr>
                            <td nowrap align="right" valign="middle"><span class="selectNS">
                                <select name="availableUsers" multiple size="7"><%

        UserDetail[] userDetails = fsc.listUsers();
        UserDetail userDetail;
        for (int i = 0; i < userDetails.length; i++)
        {
        	userDetail = userDetails[i];
            if (params == 0)
            {
%>
                                    <option value="<%=userDetail.getId()%>"><%=userDetail.getFirstName()%> <%=userDetail.getLastName()%></option><%

            }
            else if (!fsc.isModerator(userDetail.getId(), params))
            {
%>
                                    <option value="<%=userDetail.getId()%>"><%=userDetail.getLastName()%> <%=userDetail.getFirstName()%></option><%

            }
        }
%>
                                </select></span>&nbsp;&nbsp;</td>
                            <td nowrap align="center" valign="middle">
                                <center>
                                    <table border="0" cellpadding="0" cellspacing="0" width="37">
                                        <tr>
                                            <td class="intfdcolor" width="37"><a href="javascript:moveUsers('>');"><img
                                                src="icons/bt_fleche-d.gif" width="37" height="24" border="0"></a><a href="javascript:moveUsers('<');"><img
                                                src="icons/bt_fleche-g.gif" width="37" height="24" border="0"></a><a href="javascript:removeAllUsers();"><img
                                                src="icons/bt_db-fleche-d.gif" width="37" height="24" border="0"></a><a href="javascript:moveAllUsers();"><img
                                                src="icons/bt_db-fleche-g.gif" width="37" height="24" border="0"></a></td>
                                        </tr>
                                    </table>
                                </center>
                            </td>
                            <td valign="middle" align="left">&nbsp;&nbsp;<span class="selectNS">
                                <select name="moderators" multiple size="7"><%

        if (params != 0)
        {
        	for (int i = 0; i < userDetails.length; i++)
            {
        		userDetail = userDetails[i];
                if (fsc.isModerator(userDetail.getId(), params))
                {
%>
                                    <option value="<%=userDetail.getId()%>"><%=userDetail.getFirstName()%> <%=userDetail.getLastName()%></option><%

                }
            }
        }
%>
                                </select></span></td>
                        </tr>
                    </form>
                    </table><%

    }
%>
                </td>
            </tr>
        </table>
    </center><%

    out.println(frame.printMiddle());
%>
    <br>
    <center><%

    ButtonPane buttonPane = graphicFactory.getButtonPane();
    buttonPane.addButton(graphicFactory.getFormButton(
        resource.getString("valider"), "javascript:submitForm();", false));
    buttonPane.addButton(graphicFactory.getFormButton(
        resource.getString("annuler"), backURL, false));
    buttonPane.setHorizontalPosition();
    out.println(buttonPane.print());
%>
    </center>
    <br><%

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>
