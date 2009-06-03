<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%
	Question 	question 	= (Question) request.getAttribute("question");
	String		profil		= (String) request.getAttribute("Flag");
	Collection allCategories = (Collection) request.getAttribute("AllCategories");
	
	String categoryId = question.getCategoryId();
	
	String title = Encode.javaStringToHtmlString(question.getTitle());
	String content = Encode.javaStringToHtmlString(question.getContent());
	String date = resource.getOutputDate(question.getCreationDate());
	String id = question.getPK().getId();
	int status = question.getStatus();
	String creator = Encode.javaStringToHtmlString(question.readCreatorName());
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
function cancel(id)
{
	document.QForm.action = "ConsultQuestionQuery";
	document.QForm.questionId.value = id;
	document.QForm.submit();
}
//-->
</SCRIPT>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF" onLoad="document.forms[0].title.focus();">

<%
	browseBar.setDomainName(spaceLabel);
   	browseBar.setPath("<a href="+routerUrl+"Main></a>" + title);
   	
	out.println(window.printBefore());
	
	boolean pdc = true;
	if (profil.equals("publisher"))
		pdc = false;
	if (!profil.equals("user"))
		displayTabs(true, pdc, id, resource, gef, "CreateQQuery", routerUrl, out);
	
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>

<center>
<table CELLPADDING=5 width="100%">
	<FORM METHOD=POST NAME="myForm" ACTION="EffectiveUpdateQ">
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
		<td><input type="text" name="title" size="120" maxlength="100" value="<%=title%>">&nbsp;<img src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5"></td>
		<input type="hidden" name="questionId" value="<%=id%>">
	</tr>
	<tr valign="top"> 
		<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
		<td><textarea cols="120" rows="5" name="content"><%=content%></textarea></td>
	</tr>
	<tr> 
		<td class="txtlibform"><%=resource.getString("GML.date")%> :</td>
		<td><%=date%></td>
	</tr>
	<tr> 
		<td class="txtlibform"><%=resource.getString("GML.publisher")%> :</td>
		<td><%=creator%></td>
	</tr>
	<tr align=center>				 
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
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancel('"+id+"');", false));
    out.println(buttonPane.print());
%>
</CENTER>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

<form name="QForm" action="" Method="POST">
<input type="hidden" name="questionId">
</form>

</BODY>
</HTML>