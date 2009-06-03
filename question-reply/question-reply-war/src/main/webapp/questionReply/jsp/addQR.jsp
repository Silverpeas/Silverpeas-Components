<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%
	Question question = (Question) request.getAttribute("question");
	String creationDate = resource.getOutputDate(question.getCreationDate());
	String creator = question.readCreatorName();
	Reply reply = (Reply) request.getAttribute("reply");
	String creationDateR = resource.getOutputDate(reply.getCreationDate());
	String creatorR = reply.readCreatorName();
	
	Collection allCategories = (Collection) request.getAttribute("AllCategories");
	String categoryId = null;
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
	var titleR = document.forms[0].titleR.value;
	var contentR = document.forms[0].contentR;

        
	if (isWhitespace(title)) {
           errorMsg+="  - '<%=resource.getString("questionReply.question")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
        }              
	
     	if (!isValidTextArea(content)) {
     		errorMsg+="  - '<%=resource.getString("GML.description")%>' <%=resource.getString("questionReply.containsTooLargeText")+resource.getString("questionReply.nbMaxTextArea")+resource.getString("questionReply.characters")%>\n";
           	errorNb++; 
		}  	  	
	if (isWhitespace(titleR)) {
           errorMsg+="  - '<%=resource.getString("questionReply.reponse")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
        }              
	
     	if (!isValidTextArea(contentR)) {
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
	browseBar.setPath(resource.getString("questionReply.addQR"));

	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>

<center>
<table CELLPADDING=5 width="100%">
	<FORM METHOD=POST NAME="myForm" ACTION="<%=routerUrl%>EffectiveCreateQR">
	
	<!-- Affichage de la liste des catégories -->
	<tr>
	  	<td>
	  		<span class="txtlibform"><%= resource.getString("questionReply.category") %> :&nbsp;</span>
	    </td>
	    <TD>
			<select name="CategoryId">
			<option value=""></option>
			<%
			if (allCategories != null)
    		{
				String selected = "";
    			Iterator it = (Iterator) allCategories.iterator();
    			while (it.hasNext()) 
		  		{
    				NodeDetail uneCategory = (NodeDetail) it.next();
    				if (categoryId != null && categoryId.equals(uneCategory.getNodePK().getId()))
    					selected = "selected";
    				%>
    				<option value=<%=uneCategory.getNodePK().getId()%> <%=selected%>><%=uneCategory.getName()%></option>
    				<%
    				selected = "";
		  		}
    		}
			%>
			</select>
		</TD>
	</tr>
	<tr> 
		<td class="txtlibform"><%=resource.getString("questionReply.question")%> :</td>
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
	<tr> 
		<td colspan="2">
			<table width="70%" align="center" border=0 cellpadding=0 cellspacing=0>
				<tr>
					<td align="center" class="intfdcolor"  height="1px"><img src="<%=resource.getIcon("pdcPeas.noColorPix")%>"></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr> 
		<td class="txtlibform"><%=resource.getString("questionReply.reponse")%> :</td>
		<td><input type="text" name="titleR" size="120" maxlength="100" value="">&nbsp;<img src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5"></td>
	</tr>
	<tr valign="top"> 
		<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
		<td><textarea cols="120" rows="5" name="contentR"></textarea></td>
	</tr>
	<tr> 
		<td class="txtlibform"><%=resource.getString("GML.date")%> :</td>
		<td><%=creationDateR%></td>
	</tr>
	<tr> 
		<td class="txtlibform"><%=resource.getString("GML.publisher")%> :</td>
		<td><%=creatorR%></td>
	</tr>
	<tr>				 
		<td colspan="2"><span class="txt">(<img src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</span></td>
	</tr>
	</FORM>
</table>
<% out.println(board.printAfter()); %>
<br>
<CENTER>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:save();", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "Main", false));
    out.println(buttonPane.print());
%>
</CENTER>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</BODY>
</HTML>