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
	boolean				isDraftEnabled 	= ((Boolean) request.getAttribute("IsDraftEnabled")).booleanValue();
	ClassifiedDetail	classified		= (ClassifiedDetail) request.getAttribute("Classified");
	String 				profile			= (String) request.getAttribute("Profile");
	String 				userId			= (String) request.getAttribute("UserId");

	Collection 			comments		= (Collection) request.getAttribute("AllComments");
	
	// paramètres du formulaire
	Form		xmlForm 				= (Form) request.getAttribute("Form");
	DataRecord	xmlData					= (DataRecord) request.getAttribute("Data");
	
	// déclaration des variables :
	String 		classifiedId 			= Integer.toString(classified.getClassifiedId());
	String 		title 					= classified.getTitle();
	String 		instanceId				= classified.getInstanceId();
	String 		creatorId				= classified.getCreatorId();
	String 		creatorName				= classified.getCreatorName();
	String		creatorEmail			= classified.getCreatorEmail();
	String 		creationDate			= null;
	if (classified.getCreationDate() != null){
		creationDate = resource.getOutputDateAndHour(classified.getCreationDate());
	}
  	else {
  		creationDate = "";
  	}
	String 		updateDate		= null;
	if (classified.getUpdateDate() != null){
		updateDate = resource.getOutputDateAndHour(classified.getUpdateDate());
	}
  	else {
  		updateDate = "";
  	}
	String 		status				= classified.getStatus();
	String 		validatorId			= classified.getValidatorId();
	String 		validatorName		= classified.getValidatorName();
	String 		validateDate		= null;
	if (classified.getValidateDate() != null) {
		validateDate = resource.getOutputDateAndHour(classified.getValidateDate());
	}
  	else {
  		validateDate = "";
  	}
	
	boolean isAutorized = userId.equals(creatorId) || profile.equals("admin");
	
	//déclaration des boutons
	Button validateComment 	= (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
	Button cancelButton 	= (Button) gef.getFormButton(resource.getString("GML.cancel"), "Main", false);


	Board board	= gef.getBoard();
%>

<%@page import="com.silverpeas.util.StringUtil"%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

	var refusalMotiveWindow = window;
	
	function deleteConfirm(id) 
	{
		// confirmation de suppression de l'annonce
		if(window.confirm("<%=resource.getString("classifieds.confirmDeleteClassified")%>"))
		{
  			document.classifiedForm.action = "DeleteClassified";
  			document.classifiedForm.ClassifiedId.value = id;
  			document.classifiedForm.submit();
		}
	}

	function updateClassified(id) 
	{
		document.classifiedForm.action = "EditClassified";
		document.classifiedForm.ClassifiedId.value = id;
		document.classifiedForm.submit();
	}
	function sendData() 
	{
		//if (isCorrectForm()) 
		//{
			document.commentForm.action = "AddComment";
			document.commentForm.submit();
		//}
	}

	function isCorrectForm() 
	{
     	var errorMsg = "";
     	var errorNb = 0;
     	var message = stripInitialWhitespace(document.commentForm.Message.value);
		window.alert("message = " + message);
     	if (message == "") 
     	{ 
			errorMsg+="  - '<%=resource.getString("classifieds.comment")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
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

	function updateComment(id, classifiedId)
	{
	    SP_openWindow("<%=m_context%>/comment/jsp/newComment.jsp?id="+id+"&IndexIt=1", "blank", "600", "250","scrollbars=no, resizable, alwaysRaised");
	    document.commentForm.action = "UpdateComment";
	   	document.commentForm.ClassifiedId.value = classifiedId;
		document.commentForm.submit();
	}
	
	function removeComment(id)
	{
	    if (window.confirm("<%=resource.getString("classifieds.confirmDeleteComment")%>"))
	    {
	    	document.commentForm.action = "DeleteComment";
	    	document.commentForm.CommentId.value = id;
			document.commentForm.submit();
	    }
	}

	function commentCallBack()
	{
		location.href="<%=m_context+URLManager.getURL("useless", instanceId)%>ViewClassified?ClassifiedId=<%=classifiedId%>";
	}
	
	function draftIn(id) {
		location.href = "<%=m_context+URLManager.getURL("useless", instanceId)%>DraftIn?ClassifiedId="+id;
	}

	function draftOut(id) {
		location.href = "<%=m_context+URLManager.getURL("useless", instanceId)%>DraftOut?ClassifiedId="+id;
	}
	
	function validate(id) {
		location.href = "<%=m_context+URLManager.getURL("useless", instanceId)%>ValidateClassified?ClassifiedId="+id;
	}

	function refused(id) {
		url = "WantToRefuseClassified?ClassifiedId="+id;
	    windowName = "refusalMotiveWindow";
		larg = "550";
		haut = "350";
	    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
	    if (!refusalMotiveWindow.closed && refusalMotiveWindow.name== "refusalMotiveWindow")
	        refusalMotiveWindow.close();
	    refusalMotiveWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
	}
	
	
</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(resource.getString("classifieds.classified"));
	
	// affichage des options
	if (isAutorized) {
		operationPane.addOperation(resource.getIcon("classifieds.update"),resource.getString("classifieds.updateClassified"), "javaScript:updateClassified('"+classifiedId+"')");
		operationPane.addOperation(resource.getIcon("classifieds.delete"),resource.getString("classifieds.deleteClassified"), "javaScript:deleteConfirm('"+classifiedId+"')");
		// opérations du mode brouillon si option activée et si pas admin
		if (isDraftEnabled) {
			operationPane.addLine();
			if ((ClassifiedDetail.DRAFT).equals(classified.getStatus()))
				operationPane.addOperation(resource.getIcon("classifieds.draftOut"), resource.getString("classifieds.draftOut"), "javaScript:draftOut('"+classifiedId+"')");
			else
				operationPane.addOperation(resource.getIcon("classifieds.draftIn"), resource.getString("classifieds.draftIn"), "javaScript:draftIn('"+classifiedId+"')");
		}
		// opérations de validation (ou refus)
		if ("admin".equals(profile) && (ClassifiedDetail.TO_VALIDATE).equals(classified.getStatus())) {
			operationPane.addLine();
			operationPane.addOperation(resource.getIcon("classifieds.validate"), resource.getString("classifieds.validate"), "javaScript:validate('"+classifiedId+"')");
			operationPane.addOperation(resource.getIcon("classifieds.refused"), resource.getString("classifieds.refused"), "javaScript:refused('"+classifiedId+"')");
		}
	}

	out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<table CELLPADDING=5 WIDTH="100%">
<tr>
	<td>
	<%=board.printBefore()%>
	<table width="600" align="center">
		<%if ( title != null ) {	%>
			<tr>
				<td class="txtlibform" colspan="2" align="center"><%=title%></td>
			</tr>
		<%	} %>
		<tr>
			<td class="txtlibform"><%=resource.getString("classifieds.annonceur")%> : </td><td><%=creatorName%> (<%=Encode.javaStringToHtmlString(creatorEmail)%>)</td>
		</tr>
		<tr>
			<td class="txtlibform"><%=resource.getString("classifieds.parutionDate")%> : </td><td><%=creationDate%></td>
		</tr>
		<tr>
			<% if (StringUtil.isDefined(updateDate)) { %>
				<td class="txtlibform"><%=resource.getString("classifieds.updateDate")%> : </td><td><%=updateDate%></td>
			<% } %>
		</tr>
		<% if (StringUtil.isDefined(validateDate) && StringUtil.isDefined(validatorName)) { %>
			<tr>
				<td class="txtlibform"><%=resource.getString("classifieds.validateDate")%> :</td>
				<TD><%=validateDate%>&nbsp;<span class="txtlibform"><%=resource.getString("classifieds.by")%></span>&nbsp;<%=validatorName%></TD>
			</tr>
		<% } %>
	</table>
	<%=board.printAfter()%>	
	<% if (xmlForm != null) { %>
		<br/>
		<%=board.printBefore()%>
		<table border="0" width="50%">
			<!-- AFFICHAGE du formulaire -->
			<tr>
				<td colspan="2">
				<%
					PagesContext xmlContext = new PagesContext("myForm", "0", resource.getLanguage(), false, instanceId, null);
					xmlContext.setBorderPrinted(false);
					xmlContext.setIgnoreDefaultValues(true);
					
			    	xmlForm.display(out, xmlContext, xmlData);
			    %>
				</td>	
			</tr>
		</table>
		<%=board.printAfter()%>
	<% } %>	
	</td>
</tr>
<tr>
	<td>
		<!--Afficher les commentaires-->
		<table width="98%" align="center" border="0" cellspacing="0" cellpadding="0">
			<% if (comments != null) {
				Iterator itCom = (Iterator) comments.iterator();
				while (itCom.hasNext()) {
					Comment unComment = (Comment) itCom.next();
					String commentDate = resource.getOutputDate(unComment.getCreationDate());
					String commentAuthor = unComment.getOwner();
					String ownerId = Integer.toString(unComment.getOwnerId());
					%>
					<tr>
						<td><%=resource.getString("classifieds.from")%> <%=commentAuthor%> <%=resource.getString("classifieds.postOn")%> <%=commentDate%>  
							<% if ("admin".equals(profile) || ownerId.equals(userId) ) { %>
								<A href="javascript:updateComment(<%=unComment.getCommentPK().getId()%>,<%=classifiedId%>)"><IMG SRC="<%=resource.getIcon("classifieds.smallUpdate") %>" border="0" alt="<%=resource.getString("GML.update")%>" title="<%=resource.getString("GML.update")%>" align="absmiddle"/></A>
								<A href="javascript:removeComment(<%=unComment.getCommentPK().getId()%>)"><IMG SRC="<%=resource.getIcon("classifieds.smallDelete") %>" border="0" alt="<%=resource.getString("GML.delete")%>" title="<%=resource.getString("GML.delete")%>" align="absmiddle"/></A>
							<% } %>
						</td>
					</tr>
					<tr>
						<td><%=Encode.javaStringToHtmlParagraphe(unComment.getMessage())%></td>
					</tr>
					<tr>
						<td class="separateur">&nbsp;</td>
					</tr>
					<%
				}
			}
			%>
			<tr>
				<td>
					<form Name="commentForm" action="AddComment" Method="POST">	
						<table width="100%" border="0" cellspacing="0" cellpadding="0">
					    	<tr>
					    		<td class="txtlibform"><%=resource.getString("classifieds.addComment")%></td>
					    	</tr>
							<tr>
								<td><TEXTAREA ROWS="8" COLS="100" name="Message"></TEXTAREA>
									<input type="hidden" name="ClassifiedId" value="<%=classifiedId%>">
									<input type="hidden" name="CommentId" value="">
								</td>
					    	</tr>
				    	</table>
				   	</form>
				</td>
			</tr>
			<tr>
		   		<td>
		    		<%
				   	ButtonPane buttonPaneComment = gef.getButtonPane();
		    		buttonPaneComment.addButton(validateComment);
		    		buttonPaneComment.addButton(cancelButton);
					out.println("<BR><center>"+buttonPaneComment.print()+"</center><BR>");
					%>
		   		</td>
			</tr>
			<tr>
		 	  	<td class="separateur">&nbsp;</td>
			</tr>
		</table>
	</td>
</tr>
</table>
<% 
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>	

<form name="classifiedForm" action="" Method="POST">
	<input type="hidden" name="ClassifiedId">
</form>
</body>
</html>