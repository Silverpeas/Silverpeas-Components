<%--

    Copyright (C) 2000 - 2009 Silverpeas

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

<%@ page import="com.stratelia.silverpeas.versioning.model.DocumentVersion"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%
//R�cup�ration des param�tres
	String topicId = (String) request.getParameter("TopicId");
	String message = "";
	if (request.getAttribute("Message") != null)
		message = (String) request.getAttribute("Message");

//Icons
String hLineSrc = m_context + "/util/icons/colorPix/1px.gif";
Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.close"), "javascript:onClick=window.close();", false);
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=validateForm();", false);
%>
<html>
<head><title><%=resources.getString("GML.popupTitle")%></title>

<script language="javaScript">
function validateForm() {
	formValid = false;
	fileName = document.frm_import.file_name.value;
	if (document.frm_import.opt_importmode[0].checked || document.frm_import.opt_importmode[1].checked)
	{
		if (fileName.indexOf(".zip")== -1)
				alert("<%=kmeliaScc.getString("kmelia.FileNotZip")%>");
		else
				formValid = true;			
	}
	else if (fileName != "")
				formValid = true;			
	if (formValid)
	{
				var obj = document.getElementById("Processing");
				if (obj != null)
								obj.style.visibility = "visible";
				obj = document.getElementById("ImgProcessing");
				if (obj != null)
								obj.style.visibility = "visible";
				document.frm_import.submit();	
	}
}
</script>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>

<body>
<%
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(kmeliaScc.getSpaceLabel());
  browseBar.setComponentName(kmeliaScc.getComponentLabel());
  browseBar.setPath(kmeliaScc.getString("kmelia.ImportFiles"));
	
  Frame frame = gef.getFrame();
  Board board = gef.getBoard();

  out.println(window.printBefore());
  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
<FORM NAME="frm_import" action="ImportFilesUpload" Method="POST"  ENCTYPE="multipart/form-data">
<input type="hidden" name="topicId" value="<%=topicId%>">
  
	<TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4 align="center">
    <tr align="center">
	    <td class="txtlibform"><%=kmeliaScc.getString("kmelia.ImportModeMassifTitre")%>&nbsp;</td>
	  </tr>
    <tr>
	    <td><input type="radio" name="opt_importmode" value="1" checked>&nbsp;<%=kmeliaScc.getString("kmelia.ImportMode2")%></td>
	  </tr>
    <tr>
	    <td><input type="radio" name="opt_importmode" value="2">&nbsp;<%=kmeliaScc.getString("kmelia.ImportMode3")%></td>
	  </tr>
    <tr>
			<td><hr></td>
	  </tr>
	  <% if (kmeliaScc.isDraftEnabled()) { %>
	    <tr>
		    <td><%=kmeliaScc.getString("kmelia.DraftMode")%>&nbsp;<input type="checkbox" name="chk_draft" value="true"></td>
		  </tr>
		<% } %>
	  <% if (kmeliaScc.isVersionControlled()) { %>
	    <tr>
		    <td><%=kmeliaScc.getString("kmelia.TypeVersion")%>&nbsp;
						    <input type="radio" name="opt_versiontype" value="<%=DocumentVersion.TYPE_DEFAULT_VERSION%>" checked><%=kmeliaScc.getString("kmelia.PrivateVersion")%>&nbsp;
						    <input type="radio" name="opt_versiontype" value="<%=DocumentVersion.TYPE_PUBLIC_VERSION%>"><%=kmeliaScc.getString("kmelia.PublicVersion")%>
		    </td>
		  </tr>
		<% } %>
    <tr>
	    <td><%=kmeliaScc.getString("kmelia.FileToImport")%>&nbsp;<input type="file" name="file_name" size="50" value=""></td>
	  </tr>
		<% if (!message.equals(""))	{ %>
		    <tr>
			    <td align="center"><span class="MessageReadHighPriority"><%=message%></span></td>
			  </tr>
		<% } %>
		<tr>
			<td>
								<div align="center" id="ImgProcessing" style="visibility:hidden"><img src="<%=resources.getIcon("kmelia.progress")%>" width="58" height="40" border="0"></div>
			</td>
		</tr>
		<tr>
					<td>
									<div align="center" id="Processing" style="visibility:hidden"><b><%=kmeliaScc.getString("kmelia.Processing")%></b></div>
					</td>				
		</tr>
	</table>

<%
	out.println(board.printAfter());
	out.println("<div align=\"center\"><br/>");
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    out.println(buttonPane.print());
    out.println("<br/></div>");

    //fin du code
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</FORM>
</body>
</html>