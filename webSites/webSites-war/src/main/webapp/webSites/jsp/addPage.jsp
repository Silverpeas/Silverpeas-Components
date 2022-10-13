<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkScc.jsp" %>

<%

//Recuperation des parametres
String nameSite = (String) request.getParameter("nameSite");
String path = (String) request.getParameter("Path");
String action = (String) request.getParameter("Action");
String id = org.owasp.encoder.Encode.forUriComponent(request.getParameter("id"));

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);

if (action.equals("View")) {
%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
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

function isCorrectExtension(filename){
    var isCorrect = true;
    var indexPoint = filename.lastIndexOf(".");
    // on verifie qu'il existe une extension au nom du fichier propose
    if (indexPoint != -1){
        // le fichier contient une extension. On recupere l'extension
        var ext = filename.substring(indexPoint + 1);
        // on verifie que c'est une extension HTML
        if ( (ext != "html") && (ext != "htm") && (ext != "HTML") && (ext != "HTM") ){
            isCorrect = false;
        }
    }
    return isCorrect;
}

/************************************************************************************/

function ifCorrectFormExecute(callback) {
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

     // verify the extension
    if ( ! isCorrectExtension(title) ){
        errorMsg += "<%=resources.getString("HTMLExtensionRequired")%>";
        errorNb++;
    }

     switch(errorNb)
     {
        case 0 :
            callback.call(this);
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
     }
}

/************************************************************************************/

function sendData() {

      ifCorrectFormExecute(function() {
            var nameFile = stripInitialWhitespace(document.topicForm.Name.value);
            var indexPoint = nameFile.lastIndexOf(".");
            // on verifie qu'il existe une extension au nom du fichier propos�
            if (indexPoint == -1){
                nameFile += ".html" ; // le nom du fichier ne contient pas d'extension donc on ajoute l'extension html
            }
            document.topicDetailForm.nomPage.value = nameFile;
            document.topicDetailForm.submit();
      });
}
</script>
</HEAD>
<BODY onload="document.topicForm.Name.focus()">
<view:browseBar path='<%=resources.getString("NomPage")%>'/>
<view:window popup="true">
<view:frame>
<view:board>

	<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
	<FORM NAME="topicForm">
    <TR>
        <TD class="txtlibform"><%=resources.getString("GML.name")+" "+resources.getString("Ex")%> : </TD>
            <TD valign="top"><input type="text" name="Name" value="" size="60" maxlength="50">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></TD>
        </TR>
        <TR>
            <TD colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5">
              : <%=resources.getString("GML.requiredField")%>)</TD>
        </TR>
	</FORM>
    </TABLE>
</view:board>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	out.println(buttonPane.print());
%>
</view:frame>
</view:window>

<FORM NAME="topicDetailForm" ACTION="addPage.jsp" METHOD=POST>
  <input type="hidden" name="Action" value="verif">
  <input type="hidden" name="Path" value="<%=path%>">
  <input type="hidden" name="nomPage">
  <input type="hidden" name="nameSite" value="<%=nameSite%>">
	<input type="hidden" name="id" value="<%=id%>">
</FORM>
</BODY>
</HTML>
<% } //End View

else if (action.equals("verif")) { //vient de addPage

    //VERIFICATION COTE SERVEUR
    String name = (String) request.getParameter("nomPage");
%>
      <HTML>
      <HEAD>
      <script type="text/javascript">
          function verifServer(path, name, nameSite, id) {
              window.opener.location.replace("verifAjoutPage.jsp?Path="+URLENCODE(path)+"&nomPage="+URLENCODE(name)+"&nameSite="+URLENCODE(nameSite)+"&id="+id);
              window.close();
          }
      </script>
      </HEAD>
      <BODY onLoad="verifServer('<%=Encode.javaStringToJsString(path)%>', '<%=Encode.javaStringToJsString(name)%>', '<%=Encode.javaStringToJsString(nameSite)%>',<%=id%>)">
      </BODY>
      </HTML>
<%

}

else if (action.equals("addPage")) { //vient de verifAjoutPage

    //SERVER OK, AJOUT PAGE
    String name = (String) request.getParameter("nomPage"); //vient de verifAjoutPage
    /* Creer une nouvelle page sur le serveur */
    scc.createFile(path, name, code);
%>

      <HTML>
      <BODY>
	      <form name="frm_addpage" action="ToWysiwyg">
		<input type="hidden" name="Path" value="<%=path%>">
		<input type="hidden" name="name" value="<%=name%>">
		<input type="hidden" name="nameSite" value="<%=nameSite%>">
		<input type="hidden" name="id" value="<%=id%>">
	      </form>
      </BODY>
      <script type="text/javascript">
		document.frm_addpage.submit();
      </script>
      </HTML>
<%

}

%>

	<script type="text/javascript">
       function URLENCODE(URL){
         return encodeURIComponent(URL);
        }
	</script>