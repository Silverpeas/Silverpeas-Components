<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%
  String parution = (String) request.getAttribute("parution");
  String parutionTitle = (String) request.getAttribute("parutionTitle");
  String parutionContent = (String) request.getAttribute("parutionContent");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
  <view:looknfeel/>
  <view:includePlugin name="wysiwyg"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
	function goHeaders() {
		document.headerParution.submit();
	}

  function goView() {
    document.viewParution.submit();
  }

  function goFiles() {
    document.attachedFiles.submit();
  }

  function saveContentData() {
    document.contentForm.submit();
  }

  $(document).ready(function() {
    <view:wysiwyg replace="Content" language="<%=resource.getLanguage() %>" width="95%" height="500" toolbar="infoLetter"
                  spaceId="<%=spaceId%>" spaceName="<%=spaceLabel%>" componentId="<%=componentId%>" componentName="<%=componentLabel%>"
                browseInfo="<%=parutionTitle%>" objectId="<%=parution%>" />
  });
</script>
</head>
<body>
<%

	browseBar.setPath(EncodeHelper.javaStringToHtmlString(parutionTitle));

	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator
  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(resource.getString("infoLetter.headerLetter"),"javascript:goHeaders();",false);  
  tabbedPane.addTab(resource.getString("infoLetter.editionLetter"),"#",true);
  tabbedPane.addTab(resource.getString("infoLetter.previewLetter"),"javascript:goView();",false);
  tabbedPane.addTab(resource.getString("infoLetter.attachedFiles"),"javascript:goFiles();",false);

  out.println(tabbedPane.print());
    
	out.println(frame.printBefore());	
	
%>
<form name="contentForm" action="SaveContent" method="post">
  <input type="hidden" name="parution" value="<%= parution %>"/>

  <div class="field" id="contentArea">
    <div class="champs">
      <div class="container-wysiwyg wysiwyg-fileStorage">

        <viewTags:displayToolBarWysiwyg
            editorName="Content"
            componentId="<%=componentId%>"
            objectId="<%=parution%>" />
      </div>

      <textarea rows="5" cols="10" name="Content" id="Content"><%=parutionContent%></textarea>
    </div>
  </div>
</form>

<br/>
<%
  ButtonPane buttonPane = gef.getButtonPane();
  buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=saveContentData()", false));
  buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=goView()", false));
  out.println(buttonPane.print());
%>

<form name="headerParution" action="ParutionHeaders" method="post">
	<input type="hidden" name="parution" value="<%= parution %>"/>
  <input type="hidden" name="ReturnUrl" value="Preview"/>
</form>
<form name="viewParution" action="Preview" method="post">
  <input type="hidden" name="parution" value="<%= parution %>"/>
</form>
<form name="attachedFiles" action="FilesEdit" method="post">
	<input type="hidden" name="parution" value="<%= parution %>"/>
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>