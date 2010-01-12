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

<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	ClassifiedDetail 	classified		= (ClassifiedDetail) request.getAttribute("Classified");
	String 				userName		= (String) request.getAttribute("UserName");
	String 				userEmail		= (String) request.getAttribute("UserEmail");
	String 				userId			= (String) request.getAttribute("UserId");

	// paramètres pour le formulaire
	Form 			formUpdate 			= (Form) request.getAttribute("Form");
	DataRecord 		data 				= (DataRecord) request.getAttribute("Data"); 
	
	String 			fieldKey			= (String) request.getAttribute("FieldKey");
	String 			fieldName			= (String) request.getAttribute("FieldName");
	
	// déclaration des variables :
	String 		classifiedId 			= "";
	String 		title 					= "";
	String 		instanceId				= "";
	String 		creatorId				= "";
	String 		creatorName				= userName;
	String		creatorEmail			= userEmail;
	String 		creationDate			= resource.getOutputDateAndHour(new Date());
	String 		updateDate				= "";
	String 		status					= "";
	String 		validatorId				= "";
	String 		validatorName			= null;
	String 		validateDate			= "";
	String 		action 					= "CreateClassified";
	
	PagesContext 		context 		= new PagesContext("myForm", "0", resource.getLanguage(), false, instanceId, null);
	context.setBorderPrinted(false);
	context.setCurrentFieldIndex("11");
	context.setIgnoreDefaultValues(true);
	
	boolean isCreator = userId.equals(creatorId);

	if (classified != null)
	{
		classifiedId 		= Integer.toString(classified.getClassifiedId());
		title 				= classified.getTitle();
		instanceId			= classified.getInstanceId();
		creatorId			= classified.getCreatorId();
		creatorName			= classified.getCreatorName();
		creatorEmail		= classified.getCreatorEmail();
		action				= "UpdateClassified";
		status				= classified.getStatus();
		validatorId			= classified.getValidatorId();
		validatorName 		= classified.getValidatorName();
		if (classified.getCreationDate() != null){
			creationDate = resource.getOutputDateAndHour(classified.getCreationDate());
		}
      	else {
      		creationDate = "";
      	}
		if (classified.getValidateDate() != null) {
			validateDate = resource.getOutputDateAndHour(classified.getValidateDate());
		}
      	else {
      		validateDate = "";
      	}
		if (classified.getUpdateDate() != null) {
			updateDate = resource.getOutputDateAndHour(classified.getUpdateDate());
		}
      	else {
      		updateDate = "";
      	}
	}
	
	// déclaration des boutons
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javaScript:onClick=sendData();", false);
	Button cancelButton   = (Button) gef.getFormButton(resource.getString("GML.cancel"), "Main", false);
	
%>
<%@page import="com.silverpeas.util.StringUtil"%>
<html>
<head>
<%
out.println(gef.getLookStyleSheet());
if (formUpdate != null)
	formUpdate.displayScripts(out, context); 
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">
		
	// fonctions de contrôle des zones des formulaires avant validation
	function sendData() 
	{
		<% if (formUpdate != null) { %>
			if (isCorrectForm() && isCorrectLocalForm()) {
		    	document.classifiedForm.submit();
		    }
		<% } else { %>
				if (isCorrectLocalForm()) {
					document.classifiedForm.submit();
		    	}
		<% } %>
	}
			
	function isCorrectLocalForm() 
	{
	   	var errorMsg = "";
	   	var errorNb = 0;
	   	var title = stripInitialWhitespace(document.classifiedForm.Title.value);
	   	var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
			
		if (title == "") 
		{ 
			errorMsg+="  - '<%=resource.getString("GML.title")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
		    errorNb++;
		}     	
	   	if (title.length > 255) 
	   	{ 
			errorMsg+="  - '<%=resource.getString("GML.title")%>'  <%=resource.getString("classifieds.msgSize")%>\n";
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
	
	function setData()
	{
		<% if (StringUtil.isDefined(fieldName)) { %>
	      document.classifiedForm.<%=fieldName%>.value = <%=fieldKey%>;
	    <% } %>
	}
	
</script>
		
</head>
<body onload="setData()">
<%

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	if (action.equals("CreateClassified")) {
		browseBar.setPath(resource.getString("classifieds.addClassified"));
	}
	else {
		browseBar.setPath(resource.getString("classifieds.updateClassified"));
	}
		
	Board board	= gef.getBoard();
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
%>

<FORM Name="classifiedForm" action="<%=action%>" Method="POST" ENCTYPE="multipart/form-data" onsubmit="sendData();return false;">
<table CELLPADDING="5" WIDTH="100%">
<tr> 
	<td> 
		<%=board.printBefore()%>		
		<table cellpadding="5">
			<% if (action.equals("UpdateClassified")) { %>
				<tr>
					<td class="txtlibform"><%=resource.getString("classifieds.number")%> :</td>
					<TD><%=Encode.javaStringToHtmlString(classifiedId)%></TD>
				</tr>
			<% } %> 
			<tr>
				<td class="txtlibform"><%=resource.getString("GML.title")%> :</td>
				<TD><input type="text" name="Title" size="60" maxlength="150" value="<%=Encode.javaStringToHtmlString(title)%>">
					<IMG src="<%=resource.getIcon("classifieds.mandatory")%>" width="5" height="5" border="0">
					<input type="hidden" name="ClassifiedId" value="<%=Encode.javaStringToHtmlString(classifiedId)%>">
				</TD>
			</tr>
			<tr>
				<td class="txtlibform"><%=resource.getString("classifieds.creationDate")%> :</td>
				<TD><%=creationDate%>&nbsp;<span class="txtlibform"><%=resource.getString("classifieds.by")%></span>&nbsp;<%=creatorName%> (<%=Encode.javaStringToHtmlString(creatorEmail)%> )</TD>
			</tr>
			<% if (StringUtil.isDefined(updateDate)) { %>
				<tr>
					<td class="txtlibform"><%=resource.getString("classifieds.updateDate")%> :</td>
					<TD><%=updateDate%></TD>
				</tr>
			<% } %>
			<% if (validateDate != null && validatorName != null) { %>
				<tr>
					<td class="txtlibform"><%=resource.getString("classifieds.validateDate")%> :</td>
					<TD><%=validateDate%>&nbsp;<span class="txtlibform"><%=resource.getString("classifieds.by")%></span>&nbsp;<%=validatorName%></TD>
				</tr>
			<% } %>
			<tr><td colspan="2">( <img border="0" src=<%=resource.getIcon("classifieds.mandatory")%> width="5" height="5"> : <%=resource.getString("classifieds.mandatory")%> )</td></tr>
		</table>
		<%=board.printAfter()%>
		<br/>
			<% if (formUpdate != null) { %>
	  				<%=board.printBefore()%>
					<!-- AFFICHAGE du formulaire -->
					<table>
					<tr>
						<td>
							<% 
								formUpdate.display(out, context, data); 
							%>
						</td>	
					</tr>
					</table>
					<%=board.printAfter()%>
			<% } %>	
	</td>
</tr>
</table>	
</form>
<% 
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
 	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>