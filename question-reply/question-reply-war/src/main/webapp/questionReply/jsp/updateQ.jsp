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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:setLocale value="{sessionScope.SilverSessionController.favoriteLanguage}" />



<%@ include file="checkQuestionReply.jsp" %>
<%
	Question 	question 	= (Question) request.getAttribute("question");
	String		profil		= (String) request.getAttribute("Flag");
	Collection allCategories = (Collection) request.getAttribute("AllCategories");
	
	String categoryId = question.getCategoryId();
	
	String title = EncodeHelper.javaStringToHtmlString(question.getTitle());
	String content = EncodeHelper.javaStringToHtmlString(question.getContent());
	String date = resource.getOutputDate(question.getCreationDate());
	String id = question.getPK().getId();
	int status = question.getStatus();
	String creator = EncodeHelper.javaStringToHtmlString(question.readCreatorName());
%>
<c:set var="question" value="${requestScope['question']}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><fmt:message key="GML.popupTitle"/></title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel />
<link rel="stylesheet" type="text/css" href="css/question-reply-css.jsp" />
<script type="text/javascript" >
<!--
function isCorrectForm() {
 	var errorMsg = "";
 	var errorNb = 0;
     	
	var title = $("#title").val();
	var content = $("#content").val(); 
        
	if (isWhitespace(title)) {
    errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
    errorNb++; 
  }              
	
  if (!isValidTextArea(content)) {
    errorMsg+="  - '<%=resource.getString("GML.description")%>' <%=resource.getString("questionReply.containsTooLargeText")+resource.getString("questionReply.nbMaxTextArea")+resource.getString("questionReply.characters")%>\n";
    errorNb++; 
  }

  <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>

  switch(errorNb) {
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
	if (isCorrectForm()) {
    document.forms[0].submit();
	}
}
function cancel(id)
{
	document.QForm.action = "ConsultQuestionQuery";
	document.QForm.questionId.value = id;
	document.QForm.submit();
}
//-->
</script>
</head>
<body id="<%=componentId%>" class="questionReply updateQ" onload="document.forms[0].title.focus();">

<%
	browseBar.setDomainName(spaceLabel);
 	browseBar.setPath("<a href="+routerUrl+"Main></a>" + title);
	out.println(window.printBefore());
%>

<form method="post" name="myForm" action="EffectiveUpdateQ">
  <input type="hidden" name="questionId" value="<%=id%>" />

<fieldset id="questionFieldset" class="skinFieldset">
  <legend><fmt:message key="questionReply.fieldset.question" /></legend>
  <div class="fields">
    <div class="field" id="categoryArea">
      <label class="txtlibform" for="CategoryId"><fmt:message key="questionReply.category" /> </label>
      <div class="champs">
        <select name="CategoryId">
        <option value=""></option>
      <%
      if (allCategories != null) {
        String selected = "";
          Iterator<NodeDetail> it = allCategories.iterator();
          while (it.hasNext()) {
            NodeDetail uneCategory = it.next();
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
      </div>
    </div>

    <div class="field" id="questionArea">
      <label class="txtlibform" for="title"><fmt:message key="questionReply.question" /> </label>
      <div class="champs">
        <input type="text" name="title" size="100" id="title" maxlength="100" value="${question.title}" />
        &nbsp;<img alt="<%=resource.getString("GML.requiredField")%>" src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5" />
      </div>
    </div>

    <div class="field" id="contentArea">
      <label class="txtlibform" for="content"><fmt:message key="GML.description" /> </label>
      <div class="champs">
        <textarea name="content" id="content" cols="120" rows="5" >${question.content}</textarea>
      </div>
    </div>
  </div>
</fieldset>

<view:pdcClassification componentId="<%= question.getInstanceId() %>" contentId="<%=id%>" editable="true" />

<div class="legend">
  <fmt:message key="GML.requiredField" /> : <img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" />
</div>

</form>

<br />
<%
  ButtonPane buttonPane = gef.getButtonPane();
  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:save();", false));
  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancel('"+id+"');", false));
  out.println(buttonPane.print());
	out.println(window.printAfter());
%>

<form name="QForm" action="" method="post">
	<input type="hidden" name="questionId" />
</form>

</body>
</html>