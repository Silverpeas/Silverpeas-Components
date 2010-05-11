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

<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>

<%@ include file="checkKmelia.jsp" %>

<%
//R�cup�ration des param�tres
String 		path 			= (String) request.getAttribute("Path");
String 		linkedPath 		= (String) request.getAttribute("PathLinked");
String 		translation		= (String) request.getAttribute("Translation");
List 		profiles 		= (List) request.getAttribute("Profiles");
String		rightsDependsOn = (String) request.getAttribute("RightsDependsOn");
Boolean		popup			= (Boolean) request.getAttribute("PopupDisplay");
Boolean		isLinked		= (Boolean) request.getAttribute("IsLink");
boolean 	notificationAllowed = ((Boolean) request.getAttribute("NotificationAllowed")).booleanValue();
NodeDetail  parent			= (NodeDetail) request.getAttribute("Parent");

boolean useRightsOnTopics = (profiles != null);

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=cancelData();", false);
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);

%>
<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script LANGUAGE="JavaScript" TYPE="text/javascript">
function topicGoTo(id) 
{
    location.href = "GoToTopic?Id="+id;
}

function sendData() {
    if (isCorrectForm()) {
  	  document.topicForm.submit();
    }
}

function cancelData()
{
	<% if (popup.booleanValue()) { %>
		window.close();
	<% } else { %>
		location.href = "GoToCurrentTopic";
	<% } %>
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.topicForm.Name.value);
     //var description = stripInitialWhitespace(document.topicForm.Description.value);
     if (isWhitespace(title)) {
       errorMsg+="  - '<%=resources.getString("TopicTitle")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     }
     <% if (isLinked != null && isLinked.booleanValue()) { %>
     	if (isWhitespace(stripInitialWhitespace(document.topicForm.Path.value))) {
     		errorMsg+="  - '<%=resources.getString("kmelia.Path")%>' <%=resources.getString("GML.MustContainsText")%>\n";
       		errorNb++;
       	}
     <% } %>
     /*if (isWhitespace(description)) {
       errorMsg+="  - '<%=resources.getString("TopicDescription")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     } */
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
</script>
</HEAD>

<BODY>
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    
    if (popup.booleanValue())
    {
    	browseBar.setComponentName(componentLabel);
    	browseBar.setPath(resources.getString("TopicCreationTitle"));
    }
    else
    {
    	browseBar.setComponentName(componentLabel, "Main");
    	browseBar.setPath(linkedPath);
    }
	
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();

    //D�but code
    out.print(window.printBefore());
    
    if (useRightsOnTopics)
    {
	    TabbedPane tabbedPane = gef.getTabbedPane();
	    tabbedPane.addTab(resources.getString("Theme"), "#", true);
	    
	    Iterator p = profiles.iterator();
	    ProfileInst theProfile = null;
	    while (p.hasNext()) {
	    	theProfile = (ProfileInst) p.next();
	    	
	    	tabbedPane.addTab(theProfile.getLabel(), "ViewTopicProfiles?Id="+theProfile.getId()+"&Role="+theProfile.getName(), false, false);
	    }
	    out.println(tabbedPane.print());
    }
    
    out.print(frame.printBefore());
    out.print(board.printBefore());
%>
<FORM name="topicForm" action="AddTopic" method="POST">
<TABLE CELLPADDING="5" WIDTH="100%">
  	<TR><TD class="txtlibform"><%=resources.getString("TopicPath")%> :</TD>
      <TD valign="top"><%=path%></TD>
    </TR>
	<%=I18NHelper.getFormLine(resources, null, kmeliaScc.getLanguage())%>
  	<TR>
  		<TD class="txtlibform"><%=resources.getString("TopicTitle")%> :</TD>
      	<TD><input type="text" name="Name" size="60" maxlength="60"/><input type="hidden" name="ParentId" value="<%=parent.getId()%>"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/></TD>
    </TR>
    <% if (isLinked != null && isLinked.booleanValue()) { %>
    	<TR>
      		<TD class="txtlibform"><%=resources.getString("kmelia.Path")%> :</TD>
      		<TD><input type="text" name="Path" size="60" maxlength="200"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/></TD>
        </TR>
    <% } else { %>
    	<TR>
  			<TD class="txtlibform"><%=resources.getString("TopicDescription")%> :</TD>
      		<TD><input type="text" name="Description" size="60" maxlength="200"></TD>
    	</TR>
    <% } %>
	<% if (notificationAllowed) { %>
	  	<TR>
	  		<TD class="txtlibform" valign="top"><%=resources.getString("TopicAlert")%> :</TD>
	      	<TD valign="top">
				<select name="AlertType">
					<option value="NoAlert" selected="selected"><%=resources.getString("NoAlert")%></option>
					<option value="Publisher"><%=resources.getString("OnlyPubsAlert")%></option>
					<option value="All"><%=resources.getString("AllUsersAlert")%></option>
				</select>
			</TD>
		</TR>
	<% } %>
   <% if (useRightsOnTopics) { %>
   		<TR>
   			<TD valign="top" class="txtlibform"><%=resources.getString("kmelia.WhichTopicRightsUsed")%> :</TD>
   			<TD valign="top">
   				<table width="235" cellpadding="0" cellspacing="0">
   					<tr>
   						<td width="201"><%=resources.getString("kmelia.RightsSpecific")%></td>
   						<td width="20"><input type="radio" value="dummy" name="RightsUsed"></td>
   					</tr>
   	    			<tr>
   	    				<td width="201"><%=resources.getString("kmelia.RightsInherited")%></td>
   	    				<td width="20"><input type="radio" value="father" name="RightsUsed" checked></td>
   	    			</tr>
   	    		</table>
   	    	</TD>
   	    </TR>
   <% } %>	
  	<TR>
  		<TD colspan="2">( <img border="0" src="<%=mandatoryField%>" width="5" height="5"> : <%=resources.getString("GML.requiredField")%> )</TD>
  	</TR>  	
</TABLE>
</FORM>	
<%
	out.print(board.printAfter());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	
	out.print("<br/><center>"+buttonPane.print()+"</center>");

	out.print(frame.printAfter());
	out.print(window.printAfter());
%>
</BODY>
<script language="javascript">
	document.topicForm.Name.focus();
</script>
</HTML>