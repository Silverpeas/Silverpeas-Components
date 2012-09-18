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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />


<%@ include file="checkScc.jsp" %>

<%

// Retrieve parameters
String fatherId = (String) request.getParameter("Id");
String path = (String) request.getParameter("Path");
String action = (String) request.getParameter("Action");

//CBO : REMOVE String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
%>

<!-- addRep -->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
if (action.equals("View")) {
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><fmt:message key="GML.popupTitle" /></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script type="text/javascript">
function isCorrect(nom) {
  if (nom.indexOf("\\")>-1 || nom.indexOf("/")>-1 || nom.indexOf(":")>-1 || 
      nom.indexOf("*")>-1 || nom.indexOf("?")>-1 || nom.indexOf("\"")>-1 ||
      nom.indexOf("<")>-1 || nom.indexOf(">")>-1 || nom.indexOf("|")>-1 ||
      nom.indexOf("&")>-1 || nom.indexOf(";")>-1 || nom.indexOf("+")>-1 ||
      nom.indexOf("%")>-1 || nom.indexOf("#")>-1 || 
      nom.indexOf(".")>-1 || nom.indexOf("'")>-1 || 
      nom.indexOf("²")>-1 || nom.indexOf("é")>-1 || nom.indexOf("è")>-1 || 
      nom.indexOf("ç")>-1 || nom.indexOf("à")>-1 || nom.indexOf("^")>-1 || 
      nom.indexOf("ù")>-1 || nom.indexOf("°")>-1 || nom.indexOf("£")>-1 || 
      nom.indexOf("µ")>-1 || nom.indexOf("§")>-1 || nom.indexOf("¤")>-1 || 
      nom.indexOf(" ")>-1) {
      return false;
  }
  return true;
}

/************************************************************************************/

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.topicForm.Name.value);
  
     if (isWhitespace(title)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     }
     
     if (! isCorrect(title)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("MustNotContainSpecialChar")%>\n<%=EncodeHelper.javaStringToJsString(resources.getString("Char2"))%>\n";
       errorNb++; 
     }
     
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

/************************************************************************************/
function sendData() {
  if (isCorrectForm()) {
    document.topicDetailForm.Action.value = "Add";
    document.topicDetailForm.Name.value = stripInitialWhitespace(document.topicForm.Name.value);
    document.topicDetailForm.submit();
  }
}
</script>
</head>


<body class="websites" onload="document.topicForm.Name.focus()">
<%
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  //CBO : UPDATE
	//browseBar.setDomainName(scc.getSpaceLabel());
	browseBar.setDomainName(spaceLabel);
    //CBO : UPDATE
  //browseBar.setComponentName(scc.getComponentLabel());
  browseBar.setComponentName(componentLabel);
  browseBar.setPath(resources.getString("RepCreationTitle"));
  
  //Le cadre
  Frame frame = gef.getFrame();

	//Le board
	Board board = gef.getBoard();

  //Start code
  out.println(window.printBefore());
  out.println(frame.printBefore());
	out.print(board.printBefore());

%>
  <form name="topicForm">
    <table align=center cellpadding=5 cellspacing=0 border=0 width="100%" class=intfdcolor4>
    <tr>
        <td class="txtlibform"><%=resources.getString("GML.name")%> : </TD>
            <td valign="top"><input type="text" name="Name" value="" size="60" maxlength="50">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
      </tr>
      
      <tr> 
            <td colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5"> 
              : <%=resources.getString("GML.requiredField")%>)</td>
      </tr>      
    </table>
  </form>
<%
  //end of code
  out.print(board.printAfter());
  out.println(frame.printMiddle());

  ButtonPane buttonPane = gef.getButtonPane();
  buttonPane.addButton(validateButton);
  buttonPane.addButton(cancelButton);
    
	out.println("<br><center>"+buttonPane.print()+"</center><br>");
  out.println(frame.printAfter());
  out.println(window.printAfter());
    
%>

<form name="topicDetailForm" action="addRep.jsp" method=post>
  <input type="hidden" name="Action">
  <input type="hidden" name="Id" value="<%=fatherId%>">
  <input type="hidden" name="Path" value="<%=path%>">
  <input type="hidden" name="Name">
</form>
</body>
</html>
<% } //End View 

else if (action.equals("Add")) {

    //VERIFICATION COTE SERVEUR
    String name = (String) request.getParameter("Name");
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<script type="text/javascript">
function verifServer(id, path, name) {
  window.opener.location.replace("verif.jsp?Action=addFolder&Id="+id+"&path="+path+"&name="+name);
  window.close();
}
</script>
</head>

<body onLoad="verifServer('<%=fatherId%>', '<%=EncodeHelper.javaStringToJsString(path)%>', '<%=EncodeHelper.javaStringToJsString(name)%>')">
</body>
</html>
<%
}
%>
