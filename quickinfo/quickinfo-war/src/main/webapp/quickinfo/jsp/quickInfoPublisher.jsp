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
<%@ include file="checkQuickInfo.jsp" %>

<%
  //Collection infos = Tous les quickInfos
  Collection infos = (Collection) request.getAttribute("infos");
  String strAdmin = (String)  request.getAttribute("isAdmin");
  String pdcUtilizationSrc  = m_context + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";

  boolean isAdmin = false;

  if (strAdmin != null && strAdmin.equals("true") ) {
      isAdmin = true;
  }

%>

<HTML>
<HEAD>
<TITLE>Quick Info - Publieur</TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="javascript" src="<%=m_context%>/util/javaScript/formUtil.js"></script>
<%@ include file="scriptClipboard_js.jsp.inc" %>

<script language="JavaScript1.2">

  function openSPWindow(fonction, windowName){
        pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
  }

</script>

</head>
<body bgcolor="#FFFFFF" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<form name="quickInfoForm" action="quickInfoPublisher.jsp" method="post">

  <input type="hidden" name="Action">

<%
        Window window = gef.getWindow();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel);

        Frame maFrame = gef.getFrame();

        OperationPane operationPane = window.getOperationPane();
        if (isAdmin && quickinfo.isPdcUsed()) {
            operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId=" + quickinfo.getComponentId() + "','utilizationPdc1')");
            operationPane.addLine();
        }

        operationPane.addOperation(m_context+"/util/icons/quickInfo_to_add.gif", resources.getString("creation"), "javascript:onClick=addQuickInfo()");

        // Clipboard
        operationPane.addOperation(m_context+"/util/icons/copy.gif", generalMessage.getString("GML.copy"), "javascript:onClick=ClipboardCopyAll()");

        operationPane.addOperation(m_context+"/util/icons/paste.gif", generalMessage.getString("GML.paste"),    "javascript:onClick=ClipboardPaste()");

        out.println(window.printBefore());
        out.println(maFrame.printBefore());

          ArrayPane arrayPane = gef.getArrayPane("quickinfoList", pageContext);
          arrayPane.addArrayColumn(null);
          arrayPane.addArrayColumn(resources.getString("GML.title"));
          arrayPane.addArrayColumn(resources.getString("GML.publisher"));
          arrayPane.addArrayColumn(resources.getString("dateDebut"));
          arrayPane.addArrayColumn(resources.getString("dateFin"));
          ArrayColumn arrayColumnOp = arrayPane.addArrayColumn("<A HREF=\"javascript:void(0)\" onMouseDown=\"return SwitchSelection(quickInfoForm, 'selectItem', event)\" onClick=\"return false\">"+resources.getString("GML.selection")+"</A>");
                    arrayColumnOp.setSortable(false);

          Iterator infosI = infos.iterator();
                         int index = 0;									
          while (infosI.hasNext()) {
            PublicationDetail pub = (PublicationDetail) infosI.next();

			ArrayLine line = arrayPane.addArrayLine();
		IconPane iconPane1 = gef.getIconPane();
		Icon debIcon = iconPane1.addIcon();
		debIcon.setProperties(m_context+"/util/icons/quickInfoLittleIcon.gif", "", "");
		line.addArrayCellIconPane(iconPane1);	
            line.addArrayCellLink(Encode.javaStringToHtmlString(pub.getName()), "javascript:onClick=editQuickInfo('"+pub.getPK().getId()+"')");
                                                try {
                                                        UserDetail detail = quickinfo.getUserDetail(pub.getCreatorId());
                                                        line.addArrayCellText(detail.getLastName() + " " + detail.getFirstName());
                                                } catch (Exception e) {
                                                        SilverTrace.error("quickinfo", "quickInfoPublisher.jsp", "admin.EX_ERR_GET_USER_DETAILS", e);
                                                        line.addArrayEmptyCell();
                                                }

            if (pub.getBeginDate() == null)
              line.addArrayEmptyCell();
            else {
              ArrayCellText text = line.addArrayCellText(resources.getOutputDate(pub.getBeginDate()));
              text.setCompareOn(pub.getBeginDate());
            }
            if (pub.getEndDate() == null)
              line.addArrayEmptyCell();
            else {
              ArrayCellText text = line.addArrayCellText(resources.getOutputDate(pub.getEndDate()));
              text.setCompareOn(pub.getEndDate());
            }
                                line.addArrayCellText("<input type=checkbox name='selectItem"+index+"' value='"+pub.getPK().getId()+"'>");
                                index++;
          }
          out.println(arrayPane.print());

        out.println(maFrame.printAfter());
        out.println(window.printAfter());
%>
</form>
<FORM NAME="quickInfoEditForm" ACTION="quickInfoEdit.jsp" METHOD=POST >
  <input type="hidden" name="Id">
  <input type="hidden" name="Action">
</FORM>
</BODY>
</HTML>