<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:setLocale value="{sessionScope.SilverSessionController.favoriteLanguage}" />

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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><fmt:message key="GML.popupTitle" /></title>
  <view:looknfeel />
  <link rel="stylesheet" type="text/css" href="css/question-reply-css.jsp" />
  <script type="text/javascript" src="<c:url value='/wysiwyg/jsp/FCKeditor/fckeditor.js'/>"></script>
<script language="JavaScript">
<!--
function isCorrectForm() {
     	var errorMsg = "";
     	var errorNb = 0;
     	
	var title = document.forms[0].title.value;
	var content = document.forms[0].content;
	var titleR = document.forms[0].titleR.value;
	var contentR = document.forms[0].contentR;

        
	if (isWhitespace(title)) {
           errorMsg+="  - '<fmt:message key="questionReply.question"/>' <fmt:message key="GML.MustBeFilled"/>\n";
           errorNb++; 
        }              
	
     	if (!isValidTextArea(content)) {
     		errorMsg+="  - '<fmt:message key="GML.description"/>'<fmt:message key="questionReply.containsTooLargeText" /><fmt:message key="questionReply.nbMaxTextArea" /><fmt:message key="questionReply.characters" />\n";
           	errorNb++;
		}
	if (isWhitespace(titleR)) {
           errorMsg+="  - '<fmt:message key="questionReply.reponse"/>'<fmt:message key="GML.MustBeFilled"/>\n";
           errorNb++; 
        }              
	
     	if (!isValidTextArea(contentR)) {
     		errorMsg+="  - '<fmt:message key="GML.description" />'<fmt:message key="questionReply.containsTooLargeText" /><fmt:message key="questionReply.nbMaxTextArea" /><fmt:message key="questionReply.characters" />\n";
           	errorNb++; 
		}  	  	
		
     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<fmt:message key="GML.ThisFormContains" /> 1<fmt:message key="GML.error" /> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<fmt:message key="GML.ThisFormContains" /> " + errorNb + "<fmt:message key="GML.errors" /> :\n" + errorMsg;
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
</script>
</head>
<body id="<%=componentId%>" class="questionReply addQR" onLoad="document.forms[0].title.focus();">
<fmt:message key="questionReply.addQR" var="currentPathLabel"/>
<view:browseBar extraInformations="${currentPathLabel}"/>
<view:window>
<view:frame>
<view:board>


  <form method="post" name="myForm" action="<%=routerUrl%>EffectiveCreateQR">
    <table cellpadding="5" width="100%">
	<tr>
	  	<td>
	  		<span class="txtlibform"><fmt:message key="questionReply.category" /> :&nbsp;</span>
	    </td>
	    <TD>
			<select name="CategoryId">
			<option value=""></option>
			<%
			if (allCategories != null)
    		{
				String selected = "";
    			Iterator it = allCategories.iterator();
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
		</td>
	</tr>
	<tr> 
		<td class="txtlibform"><fmt:message key="questionReply.question" /> :</td>
		<td><input type="text" name="title" size="120" maxlength="100" value="" />&nbsp;<img alt="<%=resource.getString("GML.requiredField")%>" src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5" /></td>
	</tr>
	<tr valign="top"> 
		<td class="txtlibform"><fmt:message key="GML.description" /> :</td>
		<td><textarea cols="120" rows="5" name="content"></textarea></td>
	</tr>
	<tr> 
		<td class="txtlibform"><fmt:message key="GML.date" /> :</td>
		<td><%=creationDate%></td>
	</tr>
	<tr> 
		<td class="txtlibform"><fmt:message key="GML.publisher" /> :</td>
		<td><%=creator%></td>
	</tr>
	<tr> 
		<td colspan="2">
			<table width="70%" align="center" border=0 cellpadding=0 cellspacing=0>
				<tr>
					<td align="center" class="intfdcolor"  height="1px"><img alt="<%=resource.getString("GML.requiredField")%>" src="<%=resource.getIcon("pdcPeas.noColorPix")%>" /></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr> 
		<td class="txtlibform"><fmt:message key="questionReply.reponse" /> :</td>
		<td><input type="text" name="titleR" size="120" maxlength="100" value="" />&nbsp;<img alt="<%=resource.getString("GML.requiredField")%>" src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5" /></td>
	</tr>
	<tr valign="top"> 
		<td class="txtlibform"><fmt:message key="GML.description" /> :</td>
		<td><textarea cols="120" rows="5" name="contentR" id="contentR"></textarea></td>
	</tr>
	<tr> 
		<td class="txtlibform"><fmt:message key="GML.date" /> :</td>
		<td><%=creationDateR%></td>
	</tr>
	<tr> 
		<td class="txtlibform"><fmt:message key="GML.publisher" /> :</td>
		<td><%=creatorR%></td>
	</tr>
	<tr>				 
		<td colspan="2"><span class="txt">(<img alt="<%=resource.getString("GML.requiredField")%>"  src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5" /> :<fmt:message key="GML.requiredField" />)</span></td>
	</tr>
</table>
  </form>
</view:board>
<br/>
<div class="buttonPane">
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:save();", false));
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "Main", false));
    out.println(buttonPane.print());
%>
</div>
            </view:frame>
</view:window>
<script type="text/javascript">
  <fmt:message key='configFile' var='configFile'/>
  <c:if test="${configFile eq '???configFile???'}">
  <c:url value="/wysiwyg/jsp/javaScript/myconfig.js" var="configFile"/>
  </c:if>
  var oFCKeditor = new FCKeditor('contentR');
  oFCKeditor.Width = "500";
  oFCKeditor.Height = "300";
  oFCKeditor.BasePath = "<c:url value='/wysiwyg/jsp/FCKeditor/'/>";
  oFCKeditor.DisplayErrors = true;
  oFCKeditor.Config["AutoDetectLanguage"] = false;
  oFCKeditor.Config["DefaultLanguage"] = "<c:out value='${language}'/>";
  oFCKeditor.Config["CustomConfigurationsPath"] = "<c:out value='${configFile}'/>"
  oFCKeditor.ToolbarSet = 'questionreply';
  oFCKeditor.Config["ToolbarStartExpanded"] = true;
  oFCKeditor.ReplaceTextarea();
</script>
</body>
</html>