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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.almanach.model.*"%>
<%@ page import="com.stratelia.webactiv.almanach.control.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>

<%@ include file="checkAlmanach.jsp" %>

<%
  ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(almanach.getLanguage());

  String form = request.getParameter("indiceForm");
  String elem = (String) request.getParameter("indiceElem");
  String nameElem 	= (String) request.getParameter("nameElem");
  String jsFunction = (String) request.getParameter("JSCallback");
  String action = (String) request.getParameter("Action");

  if (action != null) {
    if (action.equals("NextMonth")) {
      almanach.nextMonth();
    }
    else if (action.equals("PreviousMonth")) {
      almanach.previousMonth();
    }
  }

Frame frame = graphicFactory.getFrame();
Window window = graphicFactory.getWindow();
%>
<html>
<head>
<%
out.println(graphicFactory.getLookStyleSheet());
%>
<title><%=generalMessage.getString("GML.popupTitle")%></title>
<script language="javascript" src="js/globalScript.js"></script>
<script language="JavaScript">

function selectDay(day) 
{
  var indiceF = "<%=form%>";
  var indiceE = "<%=elem%>";
  <% if (StringUtil.isDefined(nameElem)) { %>
  	  nameElement = '<%=nameElem%>';
	  window.opener.document.forms[indiceF].elements[nameElement].value = day;
  <% } else if (StringUtil.isDefined(jsFunction)){ 
	  out.println("window.opener."+jsFunction+"(day);");
  } else { %>
	  window.opener.document.forms[indiceF].elements[indiceE].value = day;
  <% } %>
  window.close();
}

function nextMonth()
{
    document.calendarForm.Action.value = "NextMonth";
    document.calendarForm.submit();
}

function previousMonth()
{
    document.calendarForm.Action.value = "PreviousMonth";
    document.calendarForm.submit();
}

</script>
</head>
<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="3" class=contourintfdcolor align="center"><!--tablcontour-->

  <tr align="center">
   <td class="intfdcolor4">
   		<table border="0" cellspacing="0" cellpadding="0" class="intfdcolor4" width="100%" align="center"><!--tabl1-->
                    <tr> 
                      <td class="intfdcolor3" nowrap align="center"><a href="javascript:onClick=previousMonth()" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('fle-211','','icons/cal_fle-gon.gif',1)"><img name="fle-211" border="0" src="icons/cal_fle-goff.gif" width="8" height="14"></a></td>
                      <td class="intfdcolor3" nowrap align="center" colspan="5"><span class="txtnav4"><%=
				almanach.getString("mois" + almanach.getCurrentDay().get(Calendar.MONTH)) + 
				" " +
				almanach.getCurrentDay().get(Calendar.YEAR)%></span></td>
                      <td class="intfdcolor3" nowrap align="center"><a href="javascript:onClick=nextMonth()" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('fle-111','','icons/cal_fle-don.gif',1)"><img name="fle-111" border="0" src="icons/cal_fle-doff.gif" width="8" height="14"></a></td>
                    </tr>
                   <%
                    CalendarDisplayer displayer = new CalendarDisplayer();
                    out.print(displayer.displayCalendar(almanach, resources));
                   %>
                  </table>
    </td>
  </tr>
</table>
<br>
<%
  Button button = null;
  button = graphicFactory.getFormButton(resources.getString("GML.close"), "javascript:onClick=window.close();", false);
  out.println(button.print());
%>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<FORM NAME="calendarForm" ACTION="calendar.jsp" METHOD=POST >
  <input type="hidden" name="Action">
  <input type="hidden" name="indiceForm" value="<%=form%>">
  <input type="hidden" name="indiceElem" value="<%=elem%>">
  <input type="hidden" name="nameElem" value="<%=nameElem%>">
  <input type="hidden" name="JSCallback" value="<%=jsFunction%>">
</FORM>
</BODY>
</HTML>