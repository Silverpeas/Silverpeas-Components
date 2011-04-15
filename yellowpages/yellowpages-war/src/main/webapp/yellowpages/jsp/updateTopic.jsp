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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkYellowpages.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%

//Récupération des paramètres
String id = (String) request.getParameter("ChildId");
String action = (String) request.getParameter("Action");
String name = "";
String description = "";
String modelId = "";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=reallyClose();", false);
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);

NodeDetail subTopicDetail = yellowpagesScc.getSubTopicDetail(id);
if (subTopicDetail != null) {
    name = subTopicDetail.getName();
    description = subTopicDetail.getDescription();
    modelId = subTopicDetail.getModelId();
}

%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script LANGUAGE="JavaScript" TYPE="text/javascript">
function sendData() {
      if (isCorrectForm()) {
            window.opener.document.topicDetailForm.Action.value = "Update";
            window.opener.document.topicDetailForm.ChildId.value = document.topicForm.ChildId.value;
            window.opener.document.topicDetailForm.Name.value = stripInitialWhitespace(document.topicForm.Name.value);
            window.opener.document.topicDetailForm.Description.value = stripInitialWhitespace(document.topicForm.Description.value);
            window.opener.document.topicDetailForm.ModelId.value = '<%=modelId%>';
            window.opener.document.topicDetailForm.submit();
            window.close();
      }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.topicForm.Name.value);
     if (isWhitespace(title)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=yellowpagesScc.getString("TopicTitle")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     }
     switch(errorNb)
     {
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
function reallyClose()
{
  window.opener.document.forms[0].elements[0].value = "Search";
  window.opener.document.forms[0].submit();
  window.close();
}
</script>
</HEAD>

<BODY>
<%
String linkedPathString = yellowpagesScc.getPath();
Window window = gef.getWindow();
Frame frame = gef.getFrame();

BrowseBar browseBar = window.getBrowseBar();
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel);
browseBar.setPath(resources.getString("TopicUpdateTitle"));

out.println(window.printBefore());
displayAllOperations(resources, id, gef, "ViewDesc", out);
out.println(frame.printBefore());

%>
<center>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
<FORM NAME="topicForm">
<input type="hidden" name="ChildId" value="<%=id%>">
    <tr>
        <td CLASS=intfdcolor4 NOWRAP>
            <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
                <tr>            
                    <td valign="baseline" align=left  class="txtlibform">
                        <%=yellowpagesScc.getString("TopicTitle")%> :
                    </td>
                    <td align=left valign="baseline">
                        <input type="text" name="Name" value="<%=Encode.javaStringToHtmlString(name)%>" size="60" maxlength="60">&nbsp;<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"> 
                    </td>
                </tr>
                <tr>            
                    <td valign="baseline" align=left  class="txtlibform">
                        <%=resources.getString("GML.description")%> :
                    </td>
                    <td align=left valign="baseline">
                        <input type="text" name="Description" value="<%=Encode.javaStringToHtmlString(description)%>" size="60" maxlength="200">
                    </td>
                </tr>
                <tr> 
                    <td colspan="2">(<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"> 
              : <%=resources.getString("GML.requiredField")%>)
          </td>
                </tr>
            </table>
        </td>
    </tr>
</FORM>
</table>
<br>
<!-- BUTTONS -->
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    out.println(buttonPane.print());
    out.println(frame.printAfter());
%>
</center>
<% out.println(window.printAfter()); %>
</BODY>
</HTML>