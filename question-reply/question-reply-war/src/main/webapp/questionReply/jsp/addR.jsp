<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%
	Reply reply = (Reply) request.getAttribute("reply");
	Boolean isUsedPrivateReplies = (Boolean) request.getAttribute("UsedPrivateReplies");

	String creationDate = resource.getOutputDate(reply.getCreationDate());
	String creator = reply.readCreatorName();
	boolean usedPrivateReplies = isUsedPrivateReplies.booleanValue();

%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<SCRIPT LANGUAGE="JavaScript">
<!--
function isCorrectForm() {
     	var errorMsg = "";
     	var errorNb = 0;
     	
	var title = document.forms[0].title.value;
	var content = document.forms[0].content;
        
	if (isWhitespace(title)) {
           errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
        }              
	
     	if (!isValidTextArea(content)) {
     		errorMsg+="  - '<%=resource.getString("GML.description")%>' <%=resource.getString("questionReply.containsTooLargeText")+resource.getString("questionReply.nbMaxTextArea")+resource.getString("questionReply.characters")%>\n";
           	errorNb++; 
		}  	  	
		
     switch(errorNb)
     {
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
function save()
{
	if (isCorrectForm())
		document.forms[0].submit();
}
//-->
</SCRIPT>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF" onLoad="document.forms[0].title.focus();">

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setExtraInformation(resource.getString("questionReply.reponse"));

	tabbedPane.addTab(resource.getString("GML.head"), "#", true, false);
    tabbedPane.addTab(resource.getString("GML.attachments"), "#", false);
	
	out.println(window.printBefore());
	out.println(tabbedPane.print());
 	out.println(frame.printBefore());
 	out.println(board.printBefore());
%>

<table CELLPADDING=5 width="100%">
	<FORM METHOD=POST NAME="myForm" ACTION="<%=routerUrl%>EffectiveCreateR">
	<tr> 
		<td class="txtlibform"><%=resource.getString("questionReply.reponse")%> :</td>
		<td><input type="text" name="title" size="120" maxlength="100" value="">&nbsp;<img src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5"></td>
	</tr>
	<tr valign="top"> 
		<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
		<td><textarea cols="120" rows="5" name="content"></textarea></td>
	</tr>
	<tr> 
		<td class="txtlibform"><%=resource.getString("GML.date")%> :</td>
		<td><%=creationDate%></td>
	</tr>
	<tr> 
		<td class="txtlibform"><%=resource.getString("GML.publisher")%> :</td>
		<td><%=creator%></td>
	</tr>
	<% if (usedPrivateReplies)
	{ %>
		<tr> 
			<td class="txtlibform"><%=resource.getString("questionReply.Rprivee")%> :</td>
			<td><input type="radio" name="publicReply" value="0" checked></td>
		</tr>
		<tr> 
			<td class="txtlibform"><%=resource.getString("questionReply.Rpublique")%> :</td>
			<td><input type="radio" name="publicReply" value="1"></td>
		</tr>
	<% } 
	else
	{%>
		<td><input type="hidden" name="publicReply" value="1"></td>
	<% } %>
	<tr>				 
		<td colspan=2><span class="txt">(<img src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</span></td>
	</tr>
	</FORM>
</table>
<% out.println(board.printAfter()); %>
<br>
<CENTER>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:save();", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "ConsultQuestionQuery", false));
    out.println(buttonPane.print());
%>
</CENTER>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</BODY>
</HTML>