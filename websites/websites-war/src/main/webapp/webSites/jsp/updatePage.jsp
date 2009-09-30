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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.DBUtil"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ include file="checkScc.jsp" %>

<%

//Récupération des paramètres
String fatherId = (String) request.getParameter("Id");
String path = (String) request.getParameter("Path");
String name = (String) request.getParameter("Name"); //ancien nom
String action = (String) request.getParameter("Action");

String type = "";
if (name != null) {
    int index = name.lastIndexOf(".");
    if (index != -1)
        type = name.substring(index + 1);
}
    
//CBO : REMOVE String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData('"+type+"')", false);

%>

<!-- updatePage -->

<%
if (action.equals("View")) {
%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script LANGUAGE="JavaScript" TYPE="text/javascript">



function isCorrectName(nom) {

        if (nom.indexOf(".") == 0) {
        return false;
    }
    
    return true;

}

/************************************************************************************/

function isCorrect(nom) {
    
    if (nom.indexOf("\\")>-1 || nom.indexOf("/")>-1 || nom.indexOf(":")>-1 || 
        nom.indexOf("*")>-1 || nom.indexOf("?")>-1 || nom.indexOf("\"")>-1 ||
        nom.indexOf("<")>-1 || nom.indexOf(">")>-1 || nom.indexOf("|")>-1 ||
        nom.indexOf("&")>-1 || nom.indexOf(";")>-1 || nom.indexOf("+")>-1 ||
        nom.indexOf("%")>-1 || nom.indexOf("#")>-1 || 
		nom.indexOf("'")>-1 ||
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
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("MustNotContainSpecialChar")%>\n<%=Encode.javaStringToJsString(resources.getString("Char5"))%>\n";
       errorNb++; 
     }
     
     if (! isCorrectName(title)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("MustContainFileName")%>\n";
       errorNb++; 
     }     

     switch(errorNb) {
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

function sendData(type) {
      if (isCorrectForm()) {
            document.topicDetailForm.Action.value = "Update";
            
            var nameFile = stripInitialWhitespace(document.topicForm.Name.value);
            
            if ( (type == "html") || (type == "htm") || (type == "HTML") || (type == "HTM") ) {         
                
                var indexPoint = nameFile.lastIndexOf(".");
                var ext = nameFile.substring(indexPoint + 1);
                if ( (ext != "html") && (ext != "htm") && (ext != "HTML") && (ext != "HTM") )
                    nameFile = nameFile + ".html";  
            }
            
            document.topicDetailForm.NewName.value = nameFile;
            document.topicDetailForm.submit();
      }
}

</script>
</HEAD>

<BODY bgcolor="white" topmargin="15" leftmargin="20" onload="document.topicForm.Name.focus()">
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    //CBO : UPDATE
	//browseBar.setDomainName(scc.getSpaceLabel());
	browseBar.setDomainName(spaceLabel);
    //CBO : UPDATE
//browseBar.setComponentName(scc.getComponentLabel());
browseBar.setComponentName(componentLabel);
    browseBar.setPath(resources.getString("PageUpdateTitle"));
    
    //Le cadre
    Frame frame = gef.getFrame();

	//Le board
	Board board = gef.getBoard();

    //Début code
    out.println(window.printBefore());
    out.println(frame.printBefore());
	out.print(board.printBefore());
%>

    <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
	<FORM NAME="topicForm">
     <TR>
        <TD class="txtlibform"><%=resources.getString("GML.name")%> : </TD>
            <TD valign="top"><input type="text" name="Name" value="<%=Encode.javaStringToHtmlString(name)%>" size="60" maxlength="50">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></TD>
        </TR>
        <TR> 
            <TD colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5"> 
              : <%=resources.getString("GML.requiredField")%>)</TD>
        </TR>      
    </FORM>
    </TABLE>

<%
	//fin du code
	out.print(board.printAfter());
    out.println(frame.printMiddle());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);

	out.println("<br><center>"+buttonPane.print()+"</center><br>");

    out.println(frame.printAfter());
    out.println(window.printAfter());
    
%>

<FORM NAME="topicDetailForm" ACTION="updatePage.jsp" METHOD=POST>
  <input type="hidden" name="Action">
  <input type="hidden" name="Id" value="<%=fatherId%>">
  <input type="hidden" name="Path" value="<%=path%>">
  <input type="hidden" name="Name" value="<%=name%>">
  <input type="hidden" name="NewName">
</FORM>

</BODY>
</HTML>

<% } //End View 



else if (action.equals("Update")) {

    //VERIFICATION COTE SERVEUR
    String newName = (String) request.getParameter("NewName"); //nouveau nom
%>

      <HTML>
      <HEAD>
      <script language="Javascript">
          function verifServer(id, path, name, newname) {
                window.opener.location.replace("verif.jsp?Action=renamePage&Id="+id+"&path="+path+"&name="+name+"&newName="+newname);
              window.close();
          }
      </script>
      </HEAD>
      
      <BODY onLoad="verifServer('<%=fatherId%>', 
                    '<%=Encode.javaStringToJsString(path)%>', 
                    '<%=Encode.javaStringToJsString(name)%>', 
                    '<%=Encode.javaStringToJsString(newName)%>')">
      </BODY>
      </HTML>
<%
    
}

%>

