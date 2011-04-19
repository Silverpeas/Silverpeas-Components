<%--

    Copyright (C) 2000 - 2011 Silverpeas

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

<%@ include file="checkQuizz.jsp" %>

<%

String m_context                = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String url                      = quizzScc.getComponentUrl()+"pdcPositions.jsp";

%>
<HTML>
<HEAD>
<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>
<%
        Window                  window                  = gef.getWindow();
        Frame                   frame                   = gef.getFrame();
        OperationPane			operationPane			= window.getOperationPane();

    QuestionContainerDetail quizz = (QuestionContainerDetail) session.getAttribute("currentQuizz");

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(quizzScc.getSpaceLabel());
    browseBar.setComponentName(quizzScc.getComponentLabel());
    browseBar.setPath("<a href=\"Main.jsp\">"+resources.getString("QuizzList")+"</a>");
    browseBar.setExtraInformation(quizz.getHeader().getTitle());

    operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resources.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
    operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resources.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");

    out.println(window.printBefore());

    TabbedPane tabbedPane1 = gef.getTabbedPane();
    tabbedPane1.addTab(resources.getString("GML.head"),"quizzQuestionsNew.jsp?QuizzId="+quizz.getHeader().getId()+"&Action=ViewQuizz",false);
    tabbedPane1.addTab(resources.getString("GML.PDC"),"pdcPositions.jsp",true);
    out.println(tabbedPane1.print());
    out.println(frame.printBefore());

    out.flush();
    int silverObjectId = quizzScc.getSilverObjectId(quizz.getHeader().getId()); //quizz.getHeader().getId()
    getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+ silverObjectId +"&ComponentId="+quizzScc.getComponentId()+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<FORM NAME="toComponent" ACTION="pdcPositions.jsp" METHOD=POST >
        <input type="hidden" name="Action" value="ViewPdcPositions">
</FORM>
</BODY>
</HTML>