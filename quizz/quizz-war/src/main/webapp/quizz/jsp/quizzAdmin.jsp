<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
			response.setHeader("Pragma", "no-cache"); //HTTP 1.0
			response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkQuizz.jsp"%>

<jsp:useBean id="currentQuizz" scope="session"
	class="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail" />

<%
  String m_context = GeneralPropertiesManager
					.getGeneralResourceLocator().getString("ApplicationURL");
			String iconsPath = GeneralPropertiesManager
					.getGeneralResourceLocator().getString("ApplicationURL");

			//Icons
			String folderSrc = iconsPath + "/util/icons/delete.gif";
			String linkIcon = iconsPath + "/util/icons/link.gif";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>___/ Silverpeas - Corporate Portal Organizer\__________________________________________</title>
<view:looknfeel/>
<%
  ResourceLocator settings = quizzScc.getSettings();
			String space = quizzScc.getSpaceLabel();
			String component = quizzScc.getComponentLabel();
			session.removeAttribute("currentQuizz");

			String pdcUtilizationSrc = m_context
					+ "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";

			boolean isAdmin = false;

			if ("admin".equals(quizzScc.getUserRoleLevel())) {
				isAdmin = true;
			}
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/formUtil.js"></script>
<script type="text/javascript">
function SP_openWindow(page,nom,largeur,hauteur,options) {
	var top=(screen.height-hauteur)/2;
	var left=(screen.width-largeur)/2;
	fenetre=window.open(page,nom,"top="+top+",left="+left+",width="+largeur+",height="+hauteur+","+options);
	fenetre.focus();
	return fenetre;
}

function openSPWindow(fonction, windowName){
    pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function deleteQuizz(quizz_id)
{
  var rep = confirm('<%=resources.getString("QuizzDeleteThisQuizz")%>');
  if (rep==true)
    self.location="deleteQuizz.jsp?quizz_id="+quizz_id
}

function clipboardPaste() { 
  top.IdleFrame.document.location.replace('../..<%=URLManager.getURL(URLManager.CMP_CLIPBOARD)%>paste?compR=Rquizz&SpaceFrom=<%=quizzScc.getSpaceId()%>&ComponentFrom=<%=quizzScc.getComponentId()%>&JSPPage=<%=response.encodeURL(URLEncoder.encode("Main"))%>&TargetFrame=MyMain&message=REFRESH');
  // forcer le rafraichissmeent de la page
  document.location.reload();   
}
</script>
</head>
<body>
<%
  //objet window
			Window window = gef.getWindow();
			window.setWidth("100%");

			//browse bar
			BrowseBar browseBar = window.getBrowseBar();
			browseBar.setDomainName(space);
			browseBar.setComponentName(component, "Main");
			browseBar.setExtraInformation(resources.getString("QuizzList"));

			OperationPane operationPane = window.getOperationPane();
			if (isAdmin && quizzScc.isPdcUsed()) {
				operationPane.addOperation(pdcUtilizationSrc, resources
						.getString("GML.PDC"),
						"javascript:onClick=openSPWindow('" + m_context
								+ "/RpdcUtilization/jsp/Main?ComponentId="
								+ quizzScc.getComponentId()
								+ "','utilizationPdc1')");
				operationPane.addLine();
			}
			operationPane.addOperationOfCreation(m_context
					+ "/util/icons/create-action/add-quizz.png", resources
					.getString("QuizzNewQuizz"), "quizzCreator.jsp");
			if (isAdmin) {
				operationPane.addOperation(resources.getIcon("quizz.paste"),
						resources.getString("GML.paste"),
						"javascript:onClick=clipboardPaste()");
			}
			out.println(window.printBefore());

			//onglets
			TabbedPane tabbedPane1 = gef.getTabbedPane();
			tabbedPane1.addTab(resources.getString("QuizzOnglet1"),
					"quizzAdmin.jsp", true);
			tabbedPane1.addTab(resources.getString("QuizzSeeResult"),
					"quizzResultAdmin.jsp", false);

			out.println(tabbedPane1.print());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%			
			//Tableau
			ArrayPane arrayPane = gef.getArrayPane("QuizzList",
					"quizzAdmin.jsp", request, session);

			ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
			arrayColumn0.setSortable(false);
			arrayPane.addArrayColumn(resources.getString("GML.name"));
			arrayPane.addArrayColumn(resources.getString("GML.description"));
			arrayPane.addArrayColumn(resources.getString("QuizzCreationDate"));
			arrayPane.addArrayColumn(resources.getString("GML.operation"));

			Collection quizzList = quizzScc.getAdminQuizzList();
			Iterator i = quizzList.iterator();
			while (i.hasNext()) {
				QuestionContainerHeader quizzHeader = (QuestionContainerHeader) i
						.next();
				// gestion des permaliens sur les quizz
				String permalink = quizzHeader.getPermalink();
				String link = "&nbsp;<a href=\"" + permalink + "\"><img src=\""
						+ linkIcon + "\" border=\"0\" align=\"bottom\" alt=\""
						+ resources.getString("quizz.CopyQuizzLink")
						+ "\" title=\""
						+ resources.getString("quizz.CopyQuizzLink")
						+ "\"></a>";
				String name = "<a href=\"quizzQuestionsNew.jsp?QuizzId="
						+ quizzHeader.getPK().getId() + "&Action=ViewQuizz"
						+ "\">" + quizzHeader.getTitle() + "</a>";

				IconPane folderPane1 = gef.getIconPane();
				Icon folder1 = folderPane1.addIcon();
				folder1.setProperties(folderSrc, "", "javascript:deleteQuizz("
						+ quizzHeader.getPK().getId() + ");");
				ArrayLine arrayLine = arrayPane.addArrayLine();
				arrayLine.addArrayCellLink(
						"<img src=\"icons/palmares_30x15.gif\" border=0>",
						"palmaresAdmin.jsp?quizz_id="
								+ quizzHeader.getPK().getId());
				//arrayLine.addArrayCellLink(quizzHeader.getTitle(),"quizzQuestionsNew.jsp?QuizzId="+quizzHeader.getPK().getId()+"&Action=ViewQuizz");
				arrayLine.addArrayCellText(name + link);
				arrayLine.addArrayCellText(Encode
						.javaStringToHtmlParagraphe(quizzHeader
								.getDescription()));

				Date creationDate = DateUtil.parse(quizzHeader
						.getCreationDate());
				ArrayCellText arrayCellText = arrayLine
						.addArrayCellText(resources.getOutputDate(creationDate));
				arrayCellText.setCompareOn(creationDate);

				arrayLine.addArrayCellIconPane(folderPane1);
			}
			out.println(arrayPane.print());
%>
</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>