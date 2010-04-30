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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%
      Form formUpdate = (Form) request.getAttribute("Form");
      DataRecord data = (DataRecord) request.getAttribute("Data");
      PublicationDetail pubDetail = (PublicationDetail) request.getAttribute("CurrentPublicationDetail");
      String xmlFormName = (String) request.getAttribute("XMLFormName");
      String wizardLast = (String) request.getAttribute("WizardLast");
      String wizard = (String) request.getAttribute("Wizard");
      String wizardRow = (String) request.getAttribute("WizardRow");
      String currentLang = (String) request.getAttribute("Language");

      SilverTrace.debug("kmelia", "xmlForm.jsp", "root.MSG_GEN_ENTER_METHOD", "formUpdate is null ? " + (formUpdate == null));
      SilverTrace.debug("kmelia", "xmlForm.jsp", "root.MSG_GEN_ENTER_METHOD", "data is null ? " + (data == null));
      SilverTrace.debug("kmelia", "xmlForm.jsp", "root.MSG_GEN_ENTER_METHOD", "pubDetail is null ? " + (pubDetail == null));
      SilverTrace.debug("kmelia", "xmlForm.jsp", "root.MSG_GEN_ENTER_METHOD", "xmlFormName is null ? " + (xmlFormName == null));
      SilverTrace.debug("kmelia", "xmlForm.jsp", "root.MSG_GEN_ENTER_METHOD", "wizardLast is null ? " + (wizardLast == null));
      SilverTrace.debug("kmelia", "xmlForm.jsp", "root.MSG_GEN_ENTER_METHOD", "wizard is null ? " + (wizard == null));
      SilverTrace.debug("kmelia", "xmlForm.jsp", "root.MSG_GEN_ENTER_METHOD", "wizardRow is null ? " + (wizardRow == null));
      SilverTrace.debug("kmelia", "xmlForm.jsp", "root.MSG_GEN_ENTER_METHOD", "currentLang is null ? " + (currentLang == null));

      String pubId = pubDetail.getPK().getId();
      String pubName = pubDetail.getName(currentLang);

      PagesContext context = new PagesContext("myForm", "2", resources.getLanguage(), false, componentId, kmeliaScc.getUserId());
      context.setObjectId(pubId);
      if (kmeliaMode) {
        context.setNodeId(kmeliaScc.getSessionTopic().getNodeDetail().getNodePK().getId());
      }
      context.setBorderPrinted(false);
      context.setContentLanguage(currentLang);

      String linkedPathString = kmeliaScc.getSessionPath();

      boolean isOwner = kmeliaScc.getSessionOwner();

      if (wizardRow == null) {
        wizardRow = "2";
      }
      boolean isEnd = "2".equals(wizardLast);
%>
<html>
  <head>
    <% out.println(gef.getLookStyleSheet());%>
    <script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
    <script type="text/javascript" src="<%=m_context %>/util/javaScript/jquery/jquery-1.3.2.min.js"></script>
    <% formUpdate.displayScripts(out, context);%>
    <script type="text/javascript" language="javaScript">
      function topicGoTo(id) {
        location.href="GoToTopic?Id="+id;
      }

      function B_VALIDER_ONCLICK()
      {
        if (isCorrectForm())
        {
          document.myForm.submit();
        }
      }

      function B_ANNULER_ONCLICK()
      {
        location.href = "Main";
      }

      function closeWindows() {
        if (window.publicationWindow != null)
          window.publicationWindow.close();
      }

      function showTranslation(lang)
      {
        location.href="ToPubliContent?SwitchLanguage="+lang;
      }
    </script>
  </head>
  <body class="yui-skin-sam" onUnload="closeWindows()">
    <%
          Window window = gef.getWindow();
          Frame frame = gef.getFrame();
          Board board = gef.getBoard();
          Board boardHelp = gef.getBoard();

          BrowseBar browseBar = window.getBrowseBar();
          browseBar.setDomainName(spaceLabel);
          browseBar.setComponentName(componentLabel, "Main");
          browseBar.setPath(linkedPathString);
          browseBar.setExtraInformation(pubName);
          browseBar.setI18N(pubDetail, currentLang);

          Button cancelWButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "ToPubliContent?WizardRow=" + wizardRow, false);
          Button nextButton;
          if (isEnd) {
            nextButton = (Button) gef.getFormButton(resources.getString("kmelia.End"), "javascript:onClick=B_VALIDER_ONCLICK();", false);
          } else {
            nextButton = (Button) gef.getFormButton(resources.getString("GML.next"), "javascript:onClick=B_VALIDER_ONCLICK();", false);
          }

          out.println(window.printBefore());

          if ("progress".equals(wizard)) {
            displayWizardOperations(wizardRow, pubId, kmeliaScc, gef, "ModelUpdateView", resources, out, kmaxMode);
          } else {
            if (isOwner) {
              displayAllOperations(pubId, kmeliaScc, gef, "ModelUpdateView", resources, out, kmaxMode);
            } else {
              displayUserOperations(pubId, kmeliaScc, gef, "ModelUpdateView", resources, out, kmaxMode);
            }
          }

          out.println(frame.printBefore());
          out.println(board.printBefore());
          if (("finish".equals(wizard)) || ("progress".equals(wizard))) {
            //  cadre d'aide
            out.println(boardHelp.printBefore());
            out.println("<table border=\"0\"><tr>");
            out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\"" + resources.getIcon("kmelia.info") + "\"></td>");
            out.println("<td>" + kmeliaScc.getString("kmelia.HelpContentXml") + "</td>");
            out.println("</tr></table>");
            out.println(boardHelp.printAfter());
            out.println("<BR>");
          }
    %>
    <form name="myForm" method="POST" action="UpdateXMLForm" enctype="multipart/form-data" accept-charset="UTF-8">
      <%
            formUpdate.display(out, context, data);
      %>
      <input type="hidden" name="Name" value="<%=xmlFormName%>">
    </form>
    <%
          out.println(board.printAfter());
          ButtonPane buttonPane = gef.getButtonPane();
          if (wizard.equals("progress")) {
            buttonPane.addButton(nextButton);
            buttonPane.addButton(cancelWButton);
          } else {
            buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
            buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
          }
          out.println("<br/><center>" + buttonPane.print() + "</center>");

          out.println(frame.printAfter());
          out.println(window.printAfter());%>
  </body>
  <script type="text/javascript" language="javascript">
    document.myForm.elements[1].focus();
  </script>
</html>