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
<%@ page import="com.oreilly.servlet.multipart.*"%>
<%@ page import="com.oreilly.servlet.MultipartRequest"%>
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
//CBO : REMOVE 
/*<jsp:useBean id="thePath" scope="session" class="java.lang.String"/>
<jsp:useBean id="prems" scope="session" class="java.lang.String"/>*/


	//CBO : ADD
	String thePath = (String) request.getParameter("path");
	Boolean uploadOk = (Boolean) request.getAttribute("UploadOk");

    //CBO : REMOVE String language;
    //CBO : REMOVE FilePart filePart;
    //CBO : REMOVE boolean uploadFile = false;
    //CBO : REMOVEboolean uploadOk = true;

    //CBO : REMOVE String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    //Icons
    String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
    //CBO : REMOVE
/*	try {
         if (! ((String) session.getValue("prems")).equals("premiere fois")) { /* deuxieme fois */
/*                SilverpeasMultipartParser mp = new SilverpeasMultipartParser(request);
                Part part;
                while ((part = mp.readNextPart()) != null) {
                    String name = part.getName();
                    if (part.isParam()) {
                        SilverpeasParamPart paramPart = (SilverpeasParamPart) part;
                    }
                    else if (part.isFile()) {
                        filePart = (FilePart) part;
                        /* creation du fichier sur le serveur */
/*                        String fichierName = filePart.getFileName();
                        String type = fichierName.substring(fichierName.lastIndexOf(".")+1, fichierName.length());

                        File fichier = new File(thePath, fichierName);
                        long size = filePart.writeTo(fichier);

						//CBO : UPDATE
                        /*if (size <= 0) 
							uploadOk = false;*/
 /*                       if (size > 0) {
							uploadOk = true;
						}

                        if (! uploadOk) {//le fichier n'est pas bon, on supprime le fichier
							scc.deleteFile(thePath+"/"+fichierName);
						}
		                else uploadFile = true;
                   } //partFile
               } //ferme le while
         }
   }
   catch(Exception e) {
        SilverTrace.warn("websites", "JSPuploadFile", "webSites.EXE_UPLOAD_FILE_FAILED",null, e);
   }
   finally {   
*/
//CBO : FIN REMOVE
%>          
      <!-- uploadFile -->
      <HTML>
      <HEAD>
      <TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
      <%
	    out.println(gef.getLookStyleSheet());
       %>
      <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
	  <% out.println(gef.getLookStyleSheet()); %>
      <Script language="JavaScript">
      <%
			//CBO : UPDATE
			/*if (! uploadFile && ! uploadOk) {
				out.println("alert(\""+resources.getString("FileToUploadNotCorrect")+"\");");
			}
			else if (uploadFile && uploadOk) {
				out.println("window.opener.location.replace(\"design.jsp?Action=view&path="+thePath+"\");");
						out.println("window.close();");
			}*/
			if (uploadOk != null && uploadOk.equals(Boolean.FALSE)) {
				out.println("alert(\""+resources.getString("FileToUploadNotCorrect")+"\");");
			}
			else if (uploadOk != null && uploadOk.equals(Boolean.TRUE)) {
				out.println("window.opener.location.replace(\"design.jsp?Action=view&path="+thePath+"\");");
				out.println("window.close();");
			}
			//CBO : FIN UPDATE
       %>
       
		function isCorrect(nom) {
	    	if (nom.indexOf("&")>-1 || nom.indexOf(";")>-1 || nom.indexOf("+")>-1 ||
		        nom.indexOf("%")>-1 || nom.indexOf("#")>-1 || 
				nom.indexOf("'")>-1 ||
		        nom.indexOf("²")>-1 || nom.indexOf("é")>-1 || nom.indexOf("è")>-1 ||
		        nom.indexOf("ç")>-1 || nom.indexOf("à")>-1 || nom.indexOf("^")>-1 ||
		        nom.indexOf("ù")>-1 || nom.indexOf("°")>-1 || nom.indexOf("£")>-1 || 
				nom.indexOf("µ")>-1 || nom.indexOf("§")>-1 || nom.indexOf("¤")>-1) {
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

				//CBO : REMOVE
                /*var indexSlash = file.lastIndexOf("\\");
				var cheminFile = file.substring(0, indexSlash);

				if (cheminFile == "") 
                    alert("<%=resources.getString("ErreurFichierUpload")%>");
                else {*/
				var indexPoint = file.lastIndexOf(".");
				
				//CBO : ADD
				if(indexPoint == -1) {
					alert("<%=resources.getString("ErreurFichierUpload")%>");
				} else {

					//CBO : UPDATE
					//var nomFile = file.substring(indexSlash + 1, indexPoint);
					var nomFile = file.substring(0, indexPoint);

					if (! isCorrect(nomFile)) {// verif caractères speciaux contenus dans le nom du fichier
						alert("<%=resources.getString("NameFile")%> <%=resources.getString("MustNotContainSpecialChar")%>\n<%=Encode.javaStringToJsString(resources.getString("Char7"))%>\n");
					}
					else {
						<%
							//CBO : REMOVE session.putValue("prems", "deuxieme fois");
						%>
						document.descriptionFile.submit(); 
					} 
                } 
	       }
	    }
      </Script>
      </HEAD>

      <BODY bgcolor="white" topmargin="15" leftmargin="20">
      <%
        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
		//CBO : UPDATE
		//browseBar.setDomainName(scc.getSpaceLabel());
		browseBar.setDomainName(spaceLabel);
        //CBO : UPDATE
		//browseBar.setComponentName(scc.getComponentLabel());
		browseBar.setComponentName(componentLabel);
        browseBar.setPath(resources.getString("UploadTitle"));
    
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
	 <!--CBO : UPDATE -->
	 <!--<FORM NAME="descriptionFile" ACTION="uploadFile.jsp" METHOD="POST" ENCTYPE="multipart/form-data">-->
		 <FORM NAME="descriptionFile" ACTION="EffectiveUploadFile" METHOD="POST" ENCTYPE="multipart/form-data">
		<!-- CBO : ADD -->
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


  <%
	//fin du code
    out.print(board.printAfter());
	out.println(frame.printMiddle());
	
	ButtonPane buttonPane = gef.getButtonPane();
	Button validerButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false);
	buttonPane.addButton(validerButton);
	Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
    buttonPane.addButton(cancelButton);
            
    out.println("<br><center>"+buttonPane.print()+"</center><br>");

	out.println(frame.printAfter());
	out.println(window.printAfter());
 %>
      </BODY>       
      </HTML>
<% 
//CBO : REMOVE }
%>