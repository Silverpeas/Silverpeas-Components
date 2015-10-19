<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.util.StringUtil"%>
<%@page import="org.apache.commons.io.FilenameUtils"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.Encode"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.Window"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.board.Board"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="org.silverpeas.util.DBUtil"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ include file="checkScc.jsp" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.selection.multilang.selectionBundle" />

<c:url var="checkformjs" value="/util/javaScript/checkForm.js"/>
<c:url var="mandatoryField" value="/util/icons/mandatoryField.gif"/>

<c:set var="action" value="${param.Action}"/>
<c:set var="name" value="${param.Name}"/>
<c:set var="fatherId" value="${param.Id}"/>
<c:set var="path" value="${param.Path}"/>

<fmt:message var="pageUpdateTitle" key="PageUpdateTitle"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>
<fmt:message var="okLabel" key="GML.validate"/>
<fmt:message var="theFieldTxt" key="GML.theField"/>
<fmt:message var="nameTxt" key="GML.name"/>
<fmt:message var="mustBeFilledTxt" key="GML.MustBeFilled"/>
<fmt:message var="mustNotContainSpecialCharTxt" key="MustNotContainSpecialChar"/>
<fmt:message var="mustContainFileNameTxt" key="MustContainFileName"/>
<fmt:message var="thisFormContainsTxt" key="GML.ThisFormContains"/>
<fmt:message var="errorsTxt" key="GML.errors"/>

<c:choose>
<c:when test="${action == 'View'}">
  <%
  String nameToChange = (String) pageContext.getAttribute("name");
  String extension = FilenameUtils.getExtension(nameToChange);
  if (StringUtil.isDefined(extension)) {
    nameToChange = nameToChange.replace("." + extension, "");
  }
  pageContext.setAttribute("nameToChange", nameToChange);
  %>
<HTML>
<HEAD>
  <TITLE><fmt:message key="GML.popupTitle"/></TITLE>
<view:looknfeel/>
<script type="text/javascript" src="${checkformjs}"></script>
<script type="text/javascript">

/************************************************************************************/

function isCorrect(nom) {
    
    if (nom.indexOf("\\")>-1 || nom.indexOf("/")>-1 || nom.indexOf(":")>-1 || 
        nom.indexOf("*")>-1 || nom.indexOf("?")>-1 || nom.indexOf("\"")>-1 ||
        nom.indexOf("<")>-1 || nom.indexOf(">")>-1 || nom.indexOf("|")>-1 ||
        nom.indexOf("&")>-1 || nom.indexOf(";")>-1 || nom.indexOf("+")>-1 ||
        nom.indexOf("%")>-1 || nom.indexOf("#")>-1 || 
		nom.indexOf("'")>-1 ||
        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("�")>-1 ||
        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("^")>-1 ||
        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || 
		nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || 
		nom.indexOf(" ")>-1) {
        return false;
    }
    return true;

}

/************************************************************************************/


function isCorrectForm(title) {
     var result;
     var errorMsg = "";
     var errorNb = 0;
     
     if (isWhitespace(title)) {
       errorMsg+="  - ${theFieldTxt} '${nameTxt}' ${mustBeFilledTxt}\n";
       errorNb++; 
     }
     
    if (! isCorrect(title)) {
       errorMsg+="  - ${theFieldTxt} '${nameTxt}' ${mustNotContainSpecialCharTxt}\n<%=EncodeHelper.javaStringToJsString(resources.getString("Char5"))%>\n";
       errorNb++; 
     }    

     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "${thisFormContainsTxt} 1 ${errorsTxt} : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "${thisFormContainsTxt} " + errorNb + " ${errorsTxt} :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

/************************************************************************************/

function sendData() {
      var nameFile = stripInitialWhitespace(document.topicForm.Name.value);
      if (isCorrectForm(nameFile)) {
            document.topicDetailForm.Action.value = "Update";
            document.topicDetailForm.NewName.value = nameFile;
            document.topicDetailForm.submit();
      }
}

</script>
</HEAD>

<BODY bgcolor="white" topmargin="15" leftmargin="20" onload="document.topicForm.Name.focus()">
  <view:window>
    <view:browseBar componentId="${componentLabel}" path="${pageUpdateTitle}"/>
    <view:frame>
      <view:board>

<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
  <FORM NAME="topicForm">
     <TR>
       <TD class="txtlibform">${nameTxt} : </TD>
       <TD valign="top"><input type="text" name="Name" value="<c:out value='${nameToChange}'/>" size="60" maxlength="50">&nbsp;<img border="0" src="${mandatoryField}" width="5" height="5"></TD>
     </TR>
     <TR> 
       <TD colspan="2">(<img border="0" src="${mandatoryField}" width="5" height="5"> : <fmt:message key="GML.requiredField"/>)</TD>
     </TR>      
  </FORM>
</TABLE>
         <view:buttonPane verticalPosition="center">
           <view:button action="javascript:onClick=window.close();" label="${cancelLabel}" disabled="false"/>
           <view:button action="javascript:onClick=sendData();" label="${okLabel}" disabled="false"/>
         </view:buttonPane>
     
      </view:board>
    </view:frame>
</view:window>
  
<FORM NAME="topicDetailForm" ACTION="updatePage.jsp" METHOD=POST>
  <input type="hidden" name="Action">
  <input type="hidden" name="Id" value="${fatherId}">
  <input type="hidden" name="Path" value="${path}">
  <input type="hidden" name="Name" value="${name}">
  <input type="hidden" name="NewName">
</FORM>
      
</BODY>
</HTML>

</c:when>
<c:when test="${action == 'Update'}">
  <c:set var="newName" value="${param.NewName}"/>

      <HTML>
      <HEAD>
      <script language="Javascript">
          function verifServer(id, path, name, newname) {
                window.opener.location.replace("verif.jsp?Action=renamePage&Id="+id+"&path="+path+"&name="+name+"&newName="+newname);
              window.close();
          }
      </script>
      </HEAD>
      
      <BODY onLoad="verifServer('${fatherId}', 
                    '<%=EncodeHelper.javaStringToJsString((String) pageContext.getAttribute("path"))%>', 
                    '<%=EncodeHelper.javaStringToJsString((String) pageContext.getAttribute("name"))%>', 
                    '<%=EncodeHelper.javaStringToJsString((String) pageContext.getAttribute("newName"))%>')">
      </BODY>
      </HTML>

</c:when>
</c:choose>
