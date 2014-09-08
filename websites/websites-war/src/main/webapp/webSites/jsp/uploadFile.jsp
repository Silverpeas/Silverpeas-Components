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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%> 

<%@ page import="java.util.Collection"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.lang.Boolean"%>
<%@ page import="javax.naming.Context"%>
<%@ page import="javax.naming.InitialContext"%>
<%@ page import="javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.SiteDetail"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.webSites.control.WebSiteSessionController"%>
<%@ page import="java.io.File"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ include file="util.jsp" %>
<%@ include file="checkScc.jsp" %>

<% 
	String thePath = (String) request.getParameter("path");
	Boolean uploadOk = (Boolean) request.getAttribute("UploadOk");

    //Icons
    String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
%>          
      <!-- uploadFile -->
      <HTML>
      <HEAD>
      <TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
      <view:looknfeel/>
      <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
      <Script language="JavaScript">
      <%
			if (uploadOk != null && uploadOk.equals(Boolean.FALSE)) {
				out.println("alert(\""+resources.getString("FileToUploadNotCorrect")+"\");");
			}
			else if (uploadOk != null && uploadOk.equals(Boolean.TRUE)) {
				out.println("window.opener.location.replace(\"design.jsp?Action=view&path="+thePath+"\");");
				out.println("window.close();");
			}
       %>
       
		function isCorrect(nom) {
	    	if (nom.indexOf("&")>-1 || nom.indexOf(";")>-1 || nom.indexOf("+")>-1 ||
		        nom.indexOf("%")>-1 || nom.indexOf("#")>-1 || 
				nom.indexOf("'")>-1 ||
		        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("�")>-1 ||
		        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("^")>-1 ||
		        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || 
				nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("�")>-1) {
	        return false;
	    }
	    return true;
      }
      
      /*******************************************************************************/

        function B_VALIDER_ONCLICK() {          
			
			var file = stripInitialWhitespace(document.descriptionFile.fichier.value);

			if (isWhitespace(file)) {
				alert("<%=resources.getString("GML.theField")%> '<%=resources.getString("FichierUpload")%>' <%=resources.getString("GML.MustBeFilled")%>");
			} else {

				var indexPoint = file.lastIndexOf(".");
				
				if(indexPoint == -1) {
					alert("<%=resources.getString("ErreurFichierUpload")%>");
				} else {
					var nomFile = file.substring(0, indexPoint);

					if (! isCorrect(nomFile)) {// verif caract�res speciaux contenus dans le nom du fichier
						alert("<%=resources.getString("NameFile")%> <%=resources.getString("MustNotContainSpecialChar")%>\n<%=Encode.javaStringToJsString(resources.getString("Char7"))%>\n");
					}
					else {
						document.descriptionFile.submit(); 
					} 
                } 
	       }
	    }
      </Script>
      </HEAD>

      <BODY>
      <view:browseBar path='<%=resources.getString("UploadTitle")%>'/>
	<view:window popup="true">
	<view:frame>
	<view:board>      

     <TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">      
		 <FORM NAME="descriptionFile" ACTION="EffectiveUploadFile" METHOD="POST" ENCTYPE="multipart/form-data">
		 <input type="hidden" name="path" value="<%=thePath%>">
	    <TR>
	        <TD class="txtlibform"><%=resources.getString("FichierUpload")%> : </TD>
	        <td valign="top"><input type="file" name="fichier">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
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
	Button validerButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false);
	buttonPane.addButton(validerButton);
	Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
    buttonPane.addButton(cancelButton);
    out.println(buttonPane.print());
 %>
 </view:frame>
 </view:window>
      </BODY>       
      </HTML>