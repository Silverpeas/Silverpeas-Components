<%@ include file="checkQuickInfo.jsp" %>
<%@ page import="com.stratelia.silverpeas.wysiwyg.control.WysiwygController" %>
<%@ page import="com.stratelia.silverpeas.util.SilverpeasSettings" %>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager" %>
<%@ page import="com.stratelia.webactiv.util.fileFolder.FileFolderManager" %>
<%@ page import="com.stratelia.webactiv.util.fileFolder.FileFolderManager" %>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager" %>

<%
  PublicationDetail quickInfoDetail = (PublicationDetail) request.getAttribute("info");
  String pubId   = (String)request.getAttribute("Id");
	
  String routerUrl = URLManager.getApplicationURL() + URLManager.getURL("quickinfo", quickinfo.getSpaceId(), quickinfo.getComponentId());

  boolean isNewSubscription = true;
  String codeHtml = "";
  if (pubId != null && pubId != "-1") {
       isNewSubscription = false;
			if (quickInfoDetail.getWysiwyg() != null && !"".equals(quickInfoDetail.getWysiwyg()))
	       codeHtml = quickInfoDetail.getWysiwyg();
      else if (quickInfoDetail.getDescription() != null)
	       codeHtml = Encode.javaStringToHtmlParagraphe(quickInfoDetail.getDescription());
  }

%>
<HTML>
<HEAD>
<TITLE>QuickInfo - Edition</TITLE>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>

<%@ include file="scriptClipboard_js.jsp.inc" %>
</head>

<body bgcolor="#FFFFFF" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
    Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel, "Main");

        browseBar.setPath(resources.getString("edition"));

        Frame maFrame = gef.getFrame();
				
        if (quickInfoDetail != null) {
                OperationPane operationPane = window.getOperationPane();
                operationPane.addOperation(m_context+"/util/icons/quickInfo_to_del.gif", resources.getString("suppression"), "javascript:onClick=quickInfoDeleteConfirm()");
                // CLIPBOARD
            operationPane.addOperation(m_context+"/util/icons/copy.gif", generalMessage.getString("GML.copy"), "javascript:onClick=ClipboardCopyOne()");
        }

        out.println(window.printBefore());

        TabbedPane tabbedPane = gef.getTabbedPane();
        tabbedPane.addTab(resources.getString("GML.head"), routerUrl + "quickInfoEdit.jsp?Action=changePage&Id="+pubId+"&page=1",
            quickinfo.getPageId() == QuickInfoSessionController.PAGE_HEADER, !isNewSubscription );
        if (!isNewSubscription && quickinfo.isPdcUsed()) {
            tabbedPane.addTab( resources.getString("GML.PDC") , routerUrl + "quickInfoEdit.jsp?Action=changePage&Id="+pubId
            +"&page=2", quickinfo.getPageId() != QuickInfoSessionController.PAGE_HEADER, true);
        }

        out.println(tabbedPane.print());

        out.println(maFrame.printBefore());
%>

<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4>

<form name="quickInfoEditForm" action="quickInfoEdit" method="post">
  <tr>
    <td valign="top" align="center">
                <table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%">
         <tr>
            <td>
                    <table width="100%" border="0" cellspacing="0" cellpadding="3" class="intfdcolor4">
                      <tr>
                        <td class="intfdcolor4" nowrap>
                        
                        
                          <span class="txtlibform"><%=resources.getString("GML.title")%>
                          :</span></td>
                          <td><input type="text" size="68" name="Name" maxlength="<%=DBUtil.TextFieldLength%>" <%
                            if (quickInfoDetail != null)
                              out.println("value=\""+Encode.javaStringToHtmlString(quickInfoDetail.getName())+"\"");
                          %>>
                          &nbsp;<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5"></td></tr>
                        <tr><td class="intfdcolor4" nowrap valign=top><span class="txtlibform"><%= resources.getString("GML.description")%>
                          :</span></td>
                          <td><font size=1>
						<textarea name="Description" id="Description"><%=codeHtml%></textarea>
                          </font></td>
                      </tr>
					<tr>
						<td>  
					  </td>
					 </tr>              
                      
                      <tr>
                        <td class="intfdcolor4" nowrap><span class="txtlibform"><%=resources.getString("dateDebut")%> :</span></td>
                          <td><input type="text" name="BeginDate" size="14" maxlength="<%=DBUtil.DateFieldLength%>" <%
                            if (quickInfoDetail != null)
                              if (quickInfoDetail.getBeginDate() != null)
                                out.println("value=\""+resources.getInputDate(quickInfoDetail.getBeginDate())+"\"");
                          %>>
                          <a href="javascript:selectBeginDay('BeginDate')"><img src="icons/calendrier.gif" width="13" height="15" border="0" alt="Afficher le calendrier" title="Afficher le calendrier"></a>
                          <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span></td></tr>

                        <tr><td class="intfdcolor4" nowrap><span class="txtlibform"><%=resources.getString("dateFin")%> :</span></td>

                          <td><input type="text" name="EndDate" size="14" maxlength="<%=DBUtil.DateFieldLength%>" <%
                            if (quickInfoDetail != null)
                              if (quickInfoDetail.getEndDate() != null)
                                out.println("value=\""+resources.getInputDate(quickInfoDetail.getEndDate())+"\"");
                          %>>
                          <a href="javascript:selectEndDay('EndDate')"><img src="icons/calendrier.gif" width="13" height="15" border="0" alt="Afficher le calendrier" title="Afficher le calendrier"></a>
                          <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span></td>
                      </tr>
                                          <tr><td colspan=2 align=left><span class="txtnote">(<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5"> = <%=resources.getString("GML.requiredField")%></span>)</td></tr>
                    </table>
         </td>
      </tr>
     </table>
<%
	out.println(maFrame.printMiddle());
	ButtonPane	buttonPane		= gef.getButtonPane();

%>
  
        <table width="100%" border="0" cellspacing="0" cellpadding="5">
          <tr>
            <td align="right">
              <%
                String link = null;
                if (quickInfoDetail == null)
                  link = "javascript:onClick=reallyAddQuickInfo()";
                else
                  link = "javascript:onClick=updateQuickInfo()";
                Button button = gef.getFormButton(resources.getString("GML.validate"), link, false);
								buttonPane.addButton(button);
                button = gef.getFormButton(resources.getString("GML.cancel"), "Main", false);
								buttonPane.addButton(button);
              %>
            <BR><center><%=buttonPane.print()%></center><BR>
          </tr>
        </table>
    </td>
  </tr>
    <input type="hidden" name="Action">
  <%
        if (quickInfoDetail != null) {
  %>
                <input type="hidden" name="Id" value="<%=quickInfoDetail.getPK().getId()%>">
  <%
        }
  %>
</form>
</table>
<%
        out.println(maFrame.printAfter());
        out.println(window.printAfter());
%>

<form name="quickInfoForm" action="quickInfoEdit.jsp" method="post">
  <input type="hidden" name="Action">
  <% if (quickInfoDetail != null) { %>
      <input type="hidden" name="Id" value="<%=quickInfoDetail.getPK().getId()%>">
  <% } %>
</form>

</BODY>
</HTML>
<%                    
out.println("<script language=\"JavaScript\">");
out.println("var oFCKeditor = new FCKeditor('Description');");
out.println("oFCKeditor.Width = \"500\";");
out.println("oFCKeditor.Height = \"300\";");
out.println("oFCKeditor.BasePath = \""+URLManager.getApplicationURL()+"/wysiwyg/jsp/FCKeditor/\" ;");
out.println("oFCKeditor.DisplayErrors = true;");
out.println("oFCKeditor.Config[\"AutoDetectLanguage\"] = false");
out.println("oFCKeditor.Config[\"DefaultLanguage\"] = \""+quickinfo.getLanguage()+"\";");
String configFile = SilverpeasSettings.readString(settings, "configFile", URLManager.getApplicationURL() +"/wysiwyg/jsp/javaScript/myconfig.js");
out.println("oFCKeditor.Config[\"CustomConfigurationsPath\"] = \""+configFile+"\";");
out.println("oFCKeditor.ToolbarSet = 'quickinfo';");
out.println("oFCKeditor.Config[\"ToolbarStartExpanded\"] = true;");
out.println("oFCKeditor.ReplaceTextarea();");
out.println("</script>");
%>
