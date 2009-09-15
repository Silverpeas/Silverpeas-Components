<%@ include file="imports.jsp" %>
<%@ include file="declarations.jsp.inc" %>
<%@ include file="init.jsp.inc" %>

<%

   //Icons
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

if (action==null)
	action = "Choose";

String error = null;

if (action.equals("Add"))
{
	SilverpeasMultipartParser mp = new SilverpeasMultipartParser(request);
	Part part;
	
	while ((part = mp.readNextPart()) != null) 
	{
  		String partName = part.getName();
  		if (part.isFile()) 
  		{
			FilePart filePart = (FilePart) part;
    		String logicalName = filePart.getFileName();
    		if (logicalName != null) 
    		{
	    		String type = logicalName.substring(logicalName.lastIndexOf(".")+1, logicalName.length()).toLowerCase();
	    		String physicalName = new Long(new Date().getTime()).toString() + "." +type;
	    		String mimeType = filePart.getContentType();
			    File dir = new File(FileRepositoryManager.getAbsolutePath(news.getSpaceId(), news.getComponentId())+settings.getString("imagesSubDirectory")+ File.separator +physicalName);
	    		if ((type.equals("gif")) || (type.equals("jpg")) || (type.equals("jpeg"))) 
	    		{
	      			// the part actually contained a file
	      			long size = filePart.writeTo(dir);
							if (size > 0) {
		      					news.updatePublication(pubDetail.getName(), pubDetail.getDescription(),physicalName,mimeType);
							} else {
								error = news.getString("fichierIntrouvable") + " : " +  logicalName;
								action = "Choose";
							}
								
					//out.println(settings.getString("imagesSubDirectory")+";"+physicalName+";"+mimeType);
	    		} else 
	    		{ 
								error = news.getString("pasFichierImage") + " : " +  logicalName;
								action = "Choose";
	    		}
    		}
    	}
    	//out.flush(); 			

  	}
}
%>

<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="../../util/javaScript/checkForm.js"></script>
<Script language="JavaScript">

function addPicture()
{
	if (checkString(document.EditoPictureForm.EditoPicture, "<%=news.getString("pasFichierImage")%>")) {
	    var file = document.EditoPictureForm.EditoPicture.value;
	    var indexSlash = file.lastIndexOf("\\");
	    var cheminFile = file.substring(0, indexSlash);
	    
	    if (cheminFile == "") 
	        alert("<%=news.getString("pasFichierImage")%>");
	    else {
	        var indexPoint = file.lastIndexOf(".");
	        var nomFile = file.substring(indexSlash + 1, indexPoint);
	        var ext = file.substring(indexPoint + 1);
	        if ((ext.toLowerCase() != "gif") && (ext.toLowerCase()!= "jpg") && 
	            (ext.toLowerCase() != "bmp") && (ext.toLowerCase() != "png") && 
	            (ext.toLowerCase() != "pcd") && (ext.toLowerCase() != "tga") && 
	            (ext.toLowerCase() != "tif"))
	                alert("<%=news.getString("pasFichierImage")%>");
	    
	        else {
	        	document.EditoPictureForm.submit();
	        }
	    } 
	}
}

function closeRefreshOpener()
{
	window.opener.location.replace("publication.jsp");
	window.close();

}


</Script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 <%if (action.equals("Add")) out.print("onload=\"closeRefreshOpener();\"");%>>

<FORM NAME="EditoPictureForm" ACTION="editoPictureForm.jsp?Action=Add" METHOD=POST ENCTYPE="multipart/form-data">


<%
	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(news.getComponentLabel());
	browseBar.setDomainName(news.getSpaceLabel());
	browseBar.setPath(choisirImageEditorialBB);
	//Le cadre
	Frame frame = gef.getFrame();
	
	
	out.println(window.printBefore());
	out.println(frame.printBefore());
	
	if (error != null) out.println(error + "<BR>");
		
%>
<center>
<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
  <tr>
    <td>
     <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>      
     <TR>
        <TD class="txtlibform"><%=generalMessage.getString("GML.file")%> : </TD>
        <td valign="top"><input type="file" name="EditoPicture">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
     </TR>
     <TR> 
     	<TD colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5"> 
              : <%=generalMessage.getString("GML.requiredField")%>)</TD>
        <INPUT type="hidden" name="Action">
     </TR>       
    </TABLE>
   </td>
  </tr>
</TABLE>    
</FORM>

<!--<TABLE CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor><TR><TD CLASS=intfdcolor4 NOWRAP align="center">
		<INPUT TYPE="file" name=EditoPicture>
  		<INPUT type="hidden" name="Action">
</td></tr></table>
<br>
-->
<%
			Button buttonValid = gef.getFormButton(generalMessage.getString("GML.validate"), "javaScript:onClick=addPicture()", false, settings.getString("formButtonIconUrl"));
		 	
			Button buttonCancel = gef.getFormButton(generalMessage.getString("GML.cancel"),"javaScript:onClick=window.close()", false, settings.getString("formButtonIconUrl"));
			
		  ButtonPane buttonPane = gef.getButtonPane();
		  buttonPane.addButton(buttonValid);
		  buttonPane.addButton(buttonCancel);
		  out.println(buttonPane.print());
 %>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</BODY >
</HTML>