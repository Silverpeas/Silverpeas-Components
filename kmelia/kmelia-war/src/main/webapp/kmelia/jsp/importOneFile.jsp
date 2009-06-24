<%@ page import="com.stratelia.silverpeas.versioning.model.DocumentVersion"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%
//Récupération des paramètres
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
	if (fileName != "")
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
</head>

<body>
<%
	out.println(gef.getLookStyleSheet());
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(kmeliaScc.getSpaceLabel());
  browseBar.setComponentName(kmeliaScc.getComponentLabel());
  browseBar.setPath(kmeliaScc.getString("kmelia.ImportFile"));
	
	//Le cadre
  Frame frame = gef.getFrame();

  //Début code
  out.println(window.printBefore());
  out.println(frame.printBefore());
%>
<FORM NAME="frm_import" action="ImportFileUpload" Method="POST"  ENCTYPE="multipart/form-data">
				<input type="hidden" name="topicId" value="<%=topicId%>">
				<input type="hidden" name="opt_importmode" value="0">
  <TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor align="center" valign="top">
    <tr>
	    <td>
				<TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4 align="center">
			    <tr align="center">
				    <td class="txtlibform"><%=kmeliaScc.getString("kmelia.ImportModeUnitaireTitre")%>&nbsp;</td>
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
			</td>
		</tr>
	</table>
<div align="center"><br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    out.println(buttonPane.print());
%>
<br>
<%
	//fin du code
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</div>
</FORM>
</body>
</html>
