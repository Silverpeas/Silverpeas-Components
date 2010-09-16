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

<%@ include file="checkProcessManager.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
	boolean isSaveButtonEnabled = ((Boolean) request.getAttribute("isSaveButtonEnabled")).booleanValue();
   ProcessInstance process = (ProcessInstance) request.getAttribute("process");
	State state = (State) request.getAttribute("state");
	Action action = (Action) request.getAttribute("action");

   com.silverpeas.form.Form form
	   = (com.silverpeas.form.Form) request.getAttribute("form");
   PagesContext context = (PagesContext) request.getAttribute("context");
   DataRecord data = (DataRecord) request.getAttribute("data");
   String isFirstTimeSaved = (String) request.getAttribute("isFirstTimeSaved");
   
   boolean 		isProcessIdVisible 		= ((Boolean) request.getAttribute("isProcessIdVisible")).booleanValue();

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	
	String processId = "";
	if (isProcessIdVisible)
		processId = "#"+process.getInstanceId()+" > ";
	browseBar.setPath(processId+process.getTitle(currentRole, language));

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(
	   generalMessage.getString("GML.validate"),
		"javascript:onClick=B_VALIDER_ONCLICK();",
		false));
	if (isSaveButtonEnabled) {
		buttonPane.addButton((Button) gef.getFormButton(
			   generalMessage.getString("GML.saveDraft"),
				"javascript:onClick=B_SAUVEGARDER_ONCLICK();",
				false));
	}
	buttonPane.addButton((Button) gef.getFormButton(
	   generalMessage.getString("GML.cancel"),
		"javascript:onClick=B_ANNULER_ONCLICK();",
		false));
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<%
	if (form != null) form.displayScripts(out, context);
%>

<SCRIPT language="JavaScript">
	function B_VALIDER_ONCLICK()
	{
	<% if (form != null) { %>
		if (isCorrectForm())
		{
			$.progressMessage();
	    	setTimeout("document.<%=context.getFormName()%>.submit();", 500);
		}
	<% } else { %>
		$.progressMessage();
		setTimeout("document.<%=context.getFormName()%>.submit();", 500);
	<% } %>
	}
	
	function B_ANNULER_ONCLICK() {
		location.href = "cancelAction?state=<%=state.getName()%>";
	}

	function B_SAUVEGARDER_ONCLICK()
	{
		$.progressMessage();
		var field = document.getElementById("isDraft");
		field.value = "yes";
		
    	setTimeout("document.<%=context.getFormName()%>.submit();", 500);
	}
</SCRIPT>
</HEAD>
<BODY class="yui-skin-sam">
<%
   out.println(window.printBefore());
	out.println(tabbedPane.print());
   out.println(frame.printBefore());
%>
<FORM NAME="<%=context.getFormName()%>" METHOD="POST" ACTION="saveAction" ENCTYPE="multipart/form-data">
<input type="hidden" id="isDraft" name="isDraft" value="No"/>
<input type="hidden" id="isFirstTimeSaved" name="isFirstTimeSaved" value="<%=isFirstTimeSaved%>"/>
<CENTER>
<%
   if (form != null) form.display(out, context, data);
	else
	{ %>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
         <table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="100%" CLASS=intfdcolor4><tr><td>&nbsp;</td></tr><tr>
			   <td width="80%">
			      <img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
					<span class="textePetitBold">
	               <%=action.getLabel(currentRole, language)%>
			      </span>
				</td>
			</tr><tr><td>&nbsp;</td></tr></table>
		</td>
	</tr>	
</table>
<% } %>
   <INPUT type="hidden" name="state" value="<%=state.getName()%>">
   <INPUT type="hidden" name="action" value="<%=action.getName()%>">
   <BR>
<%
	out.println(buttonPane.print());
%>
</CENTER>
</FORM>
<%
   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
<view:progressMessage/>
</BODY>
</HTML>