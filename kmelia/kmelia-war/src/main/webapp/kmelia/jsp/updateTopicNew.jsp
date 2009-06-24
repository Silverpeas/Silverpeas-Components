<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>

<%@ include file="checkKmelia.jsp" %>

<%
//Récupération des paramètres
NodeDetail 	node 			= (NodeDetail) request.getAttribute("NodeDetail");
String 		path 			= (String) request.getAttribute("Path");
String 		linkedPath 		= (String) request.getAttribute("PathLinked");
String 		translation		= (String) request.getAttribute("Translation");
List 		profiles 		= (List) request.getAttribute("Profiles");
String		rightsDependsOn = (String) request.getAttribute("RightsDependsOn");
Boolean		popup			= (Boolean) request.getAttribute("PopupDisplay");
String		language		= (String) request.getAttribute("Language");

boolean useRightsOnTopics = (profiles != null);

String rightsSpecificChecked = "";
String rightsInheritedChecked = "checked";
if (node.haveLocalRights())
{
	rightsSpecificChecked = "checked";
	rightsInheritedChecked = "";
}

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton 	= (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=cancelData();", false);
Button validateButton 	= (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);

String id 			= "";
String name 		= "";
String description 	= "";
boolean isLinked	= false;
if (node != null) {
	id 			= node.getNodePK().getId();
    name 		= node.getName(language);
    description = node.getDescription(language);
    isLinked	= node.getType().equalsIgnoreCase(NodeDetail.FILE_LINK_TYPE);
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
     if (isWhitespace(title)) {
       errorMsg+="  - '<%=resources.getString("TopicTitle")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     }
     <% if (isLinked) { %>
     	if (isWhitespace(stripInitialWhitespace(document.topicForm.Path.value))) {
     		errorMsg+="  - '<%=resources.getString("kmelia.Path")%>' <%=resources.getString("GML.MustContainsText")%>\n";
       		errorNb++;
       	}
     <% } %>
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
Iterator codes = node.getTranslations().keySet().iterator();
while (codes.hasNext())
{
	lang = (String) codes.next();
	out.println("var name_"+lang+" = \""+Encode.javaStringToJsString(node.getName(lang))+"\";\n");
	out.println("var desc_"+lang+" = \""+Encode.javaStringToJsString(node.getDescription(lang))+"\";\n");
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
    
    if (popup.booleanValue())
    {
    	browseBar.setComponentName(componentLabel);
    	browseBar.setPath(resources.getString("TopicUpdateTitle"));
    }
    else
    {
    	browseBar.setComponentName(componentLabel, "Main");
    	browseBar.setPath(linkedPath);
    	browseBar.setExtraInformation(name);
    }
	
	//Le cadre
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();

    //Début code
    out.println(window.printBefore());
    
    if (useRightsOnTopics)
    {
	    TabbedPane tabbedPane = gef.getTabbedPane();
	    tabbedPane.addTab(resources.getString("Theme"), "#", true);
	    
	    Iterator p = profiles.iterator();
	    ProfileInst theProfile = null;
	    while (p.hasNext()) {
	    	theProfile = (ProfileInst) p.next();
	    	
	    	tabbedPane.addTab(resources.getString("kmelia.Role"+theProfile.getName()), "ViewTopicProfiles?Id="+theProfile.getId()+"&Role="+theProfile.getName()+"&NodeId="+node.getNodePK().getId(), false);
	    }
	    out.println(tabbedPane.print());
    }
    
    out.println(frame.printBefore());
    out.print(board.printBefore());
%>
<FORM name="topicForm" action="UpdateTopic" method="POST">
<TABLE CELLPADDING="5" WIDTH="100%">
	<TR>
		<TD class="txtlibform"><%=resources.getString("TopicPath")%> :</TD>
      	<TD valign="top"><%=Encode.javaStringToHtmlString(path)%><input type="hidden" name="ChildId" value="<%=id%>"></TD>
    </TR>
    <%=I18NHelper.getFormLine(resources, node, translation)%>
  	<TR>
  		<TD class="txtlibform"><%=resources.getString("TopicTitle")%> :</TD>
      	<TD><input type="text" name="Name" id="nodeName" value="<%=Encode.javaStringToHtmlString(name)%>" size="60" maxlength="50">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/></TD>
    </TR>
    <% if (isLinked) { %>
	  	<TR>
	  		<TD class="txtlibform"><%=resources.getString("kmelia.Path")%> :</TD>
	      	<TD><input type="text" name="Path" value="<%=Encode.javaStringToHtmlString(node.getPath())%>" size="60" maxlength="200">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/></TD>
	    </TR>
    <% } else { %>
    	<TR>
	  		<TD class="txtlibform"><%=resources.getString("TopicDescription")%> :</TD>
	      	<TD><input type="text" name="Description" id="nodeDesc" value="<%=Encode.javaStringToHtmlString(description)%>" size="60" maxlength="200"></TD>
	    </TR>
    <% } %>
  	<TR>
  		<TD valign="top" class="txtlibform"><%=resources.getString("TopicAlert")%> :</TD>
      	<TD valign="top">
      		<select name="AlertType">
				<option value="NoAlert" selected="selected"><%=resources.getString("NoAlert")%></option>
				<option value="Publisher"><%=resources.getString("OnlyPubsAlert")%></option>
				<option value="All"><%=resources.getString("AllUsersAlert")%></option>
			</select>
    	</TD>
   	</TR>
<% if (useRightsOnTopics) { %>
	<TR>
		<TD valign="top" class="txtlibform"><%=resources.getString("kmelia.WhichTopicRightsUsed")%> :</TD>
		<TD valign="top">
			<table width="235" cellpadding="0" cellspacing="0">
				<tr>
					<td width="201"><%=resources.getString("kmelia.RightsSpecific")%></td>
					<td width="20"><input type="radio" value="<%=id%>" name="RightsUsed" <%=rightsSpecificChecked%>></td>
				</tr>
	            <tr>
	            	<td width="201"><%=resources.getString("kmelia.RightsInherited")%></td>
	            	<td width="20"><input type="radio" value="-1" name="RightsUsed" <%=rightsInheritedChecked%>></td>
	            </tr>
	        </table>
	   	</TD>
	</TR>
<% } %>		           
	<TR>
  		<TD colspan="2"><img border="0" src="<%=mandatoryField%>" width="5" height="5" align="absmiddle"> : <%=resources.getString("GML.requiredField")%></TD>
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
</HTML>