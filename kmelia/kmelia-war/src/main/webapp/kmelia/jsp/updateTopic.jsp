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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%
//R�cup�ration des param�tres
String id = (String) request.getParameter("ChildId");
String path = (String) request.getParameter("Path");
String name = "";
String description = "";
String translation	= (String) request.getParameter("Translation");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);

NodeDetail subTopicDetail = kmeliaScc.getSubTopicDetail(id);

if (subTopicDetail != null) {
    name = subTopicDetail.getName(translation);
    description = subTopicDetail.getDescription(translation); 
}

%>
<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script LANGUAGE="JavaScript" TYPE="text/javascript">
function sendData() {
      if (isCorrectForm()) {
					window.opener.document.topicDetailForm.action = "UpdateTopic";
            window.opener.document.topicDetailForm.ChildId.value = document.topicForm.ChildId.value;
            window.opener.document.topicDetailForm.Name.value = stripInitialWhitespace(document.topicForm.Name.value);
            window.opener.document.topicDetailForm.Description.value = stripInitialWhitespace(document.topicForm.Description.value);
            if (document.topicForm.AlertType[0].checked)
                window.opener.document.topicDetailForm.AlertType.value = document.topicForm.AlertType[0].value;
            if (document.topicForm.AlertType[1].checked)
                window.opener.document.topicDetailForm.AlertType.value = document.topicForm.AlertType[1].value;
            if (document.topicForm.AlertType[2].checked)
                window.opener.document.topicDetailForm.AlertType.value = document.topicForm.AlertType[2].value;
            window.opener.document.topicDetailForm.I18NLanguage.value = document.topicForm.I18NLanguage[document.topicForm.I18NLanguage.selectedIndex].value;
						window.opener.document.getElementById('TranslationRemoveIt').value = document.getElementById('TranslationRemoveIt').value;
            window.opener.document.topicDetailForm.submit();
            window.close();
      }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.topicForm.Name.value);
     //var description = stripInitialWhitespace(document.topicForm.Description.value);
     if (isWhitespace(title)) {
       errorMsg+="  - '<%=kmeliaScc.getString("TopicTitle")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
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

	<%
	String lang = "";
	Iterator codes = subTopicDetail.getTranslations().keySet().iterator();
	while (codes.hasNext())
	{
		lang = (String) codes.next();
		out.println("var name_"+lang+" = \""+Encode.javaStringToJsString(subTopicDetail.getName(lang))+"\";\n");
		out.println("var desc_"+lang+" = \""+Encode.javaStringToJsString(subTopicDetail.getDescription(lang))+"\";\n");
	}
	%>


	function showTranslation(lang)
 	{
 		showFieldTranslation('nodeName', 'name_'+lang);
 		showFieldTranslation('nodeDesc', 'desc_'+lang);
 	}

	function removeTranslation()
	{
		sendData();
	}

</script>
</HEAD>
<BODY>
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
    browseBar.setPath(kmeliaScc.getString("TopicUpdateTitle"));
	
		//Le cadre
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.print(board.printBefore());
%>
<TABLE CELLPADDING=5 WIDTH="100%">
<FORM NAME="topicForm">
  <TR><TD class="txtlibform"><%=kmeliaScc.getString("TopicPath")%> :</TD>
      <TD valign="top"><%=Encode.javaStringToHtmlString(path)%><input type="hidden" name="ChildId" value="<%=id%>"></TD>

	<%=I18NHelper.getFormLine(resources, subTopicDetail, translation)%>

 <TR><TD class="txtlibform"><%=kmeliaScc.getString("TopicTitle")%> :</TD>
      <TD><input type="text" name="Name" id="nodeName" value="<%=Encode.javaStringToHtmlString(name)%>" size="60" maxlength="50">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></TD></TR>
  <TR><TD class="txtlibform"><%=kmeliaScc.getString("TopicDescription")%> :</TD>
      <TD><input type="text" name="Description" id="nodeDesc" value="<%=Encode.javaStringToHtmlString(description)%>" size="60" maxlength="200"></TD></TR>
  <TR><TD valign="top" class="txtlibform"><%=kmeliaScc.getString("TopicAlert")%> :</TD>
      <TD valign="top">
    	<table width="235"><tr><td width="201"><%=kmeliaScc.getString("AllUsersAlert")%></td><td width="20"><input type="radio" value="All" name="AlertType"></td></tr>
    	                   <tr><td width="201"><%=kmeliaScc.getString("OnlyPubsAlert")%></td><td width="20"><input type="radio" value="Publisher" name="AlertType"></td></tr>
		           <tr><td width="201"><%=kmeliaScc.getString("NoAlert")%></td><td width="20"><input type="radio" value="None" name="AlertType" checked></td></tr></table></TD></TR>
  <TR><TD colspan="2">( <img border="0" src="<%=mandatoryField%>" width="5" height="5"> : <%=resources.getString("GML.requiredField")%> )</TD></TR>
  </FORM>
</TABLE>
<%
	out.print(board.printAfter());
    out.println(frame.printMiddle());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);    
    out.println("<br><center>"+buttonPane.print()+"</center><br>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>