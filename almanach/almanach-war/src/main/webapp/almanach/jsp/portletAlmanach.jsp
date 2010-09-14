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

<%@ include file="checkAlmanach.jsp"%>

<% 
	MonthCalendar monthC = (MonthCalendar) request.getAttribute("MonthCalendar");
	
	String flag = request.getParameter("flag");
	if (flag == null) {
		flag = "user";
	}
%>

<!-- AFFICHAGE BROWSER -->
<HTML>
<HEAD>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
<!--
function nextMonth()
{
    document.almanachForm.Action.value = "NextMonth";
    document.almanachForm.submit();
}

function previousMonth()
{
    document.almanachForm.Action.value = "PreviousMonth";
    document.almanachForm.submit();
}
function goToDay()
{
    document.almanachForm.Action.value = "GoToday";
    document.almanachForm.submit();
}

function clickEvent(idEvent, date, componentId){
    viewEvent(idEvent, date, componentId);
}

function clickDay(day){
   flag = "<%=flag%>";
   if(flag == "publisher" || flag == "admin")
      addEvent(day);
}

function viewEvent(id, date, componentId)
{
	document.viewEventForm.action = "<%=m_context%>/Ralmanach/"+componentId+"/viewEventContent.jsp";
    document.viewEventForm.Id.value = id;
	document.viewEventForm.Date.value = date;
    document.viewEventForm.submit();
}

function addEvent(day)
{
    document.createEventForm.Day.value = day;
    document.createEventForm.submit();
}
//-->
</script>
<%
out.println(graphicFactory.getLookStyleSheet());
%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style type="text/css">
<!--
.eventCells {  padding-right: 3px; padding-left: 3px; vertical-align: top; background-color: #FFFFFF}
-->
</style>
</HEAD>
<BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5">
<% 
	Window window = graphicFactory.getWindow();
	Frame 	frame	= graphicFactory.getFrame();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
  
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>

<!-- AFFICHAGE HEADER -->
<CENTER>
  <table width="98%" border="0" cellspacing="0" cellpadding="1">
    <tr>
      <td> 
        <table cellpadding=0 cellspacing=0 border=0 width=50% bgcolor=000000>
          <tr> 
            <td> 
              <table cellpadding=2 cellspacing=1 border=0 width="100%" >
                <tr> 
                  <td class=intfdcolor align=center nowrap width="100%" height="24"><a href="javascript:onClick=goToDay()" onFocus="this.blur()" class=hrefComponentName><%=almanach.getString("auJour")%></a></td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
      <td width="100%"> 
        <table cellpadding=0 cellspacing=0 border=0 width=50% bgcolor=000000>
          <tr> 
            <td> 
              <table cellpadding=0 cellspacing=1 border=0 width="100%" >
                <tr> 
                  <td class=intfdcolor><a href="javascript:onClick=previousMonth()" onFocus="this.blur()"><img src="<%=arrLeft%>" border="0"></a></td>
                  <td class=intfdcolor align=center nowrap width="100%" height="24"><span class="txtnav"><%=almanach.getString("mois" + almanach.getCurrentDay().get(Calendar.MONTH))%> <%=String.valueOf(almanach.getCurrentDay().get(Calendar.YEAR))%></span></td>
                  <td class=intfdcolor><a href="javascript:onClick=nextMonth()" onFocus="this.blur()"><img src="<%=arrRight%>" border="0"></a></td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
  <BR>
<%=monthC.print()%>
</CENTER>

<%
		out.println(frame.printAfter());				
		out.println(window.printAfter());
%>
<form name="almanachForm" action="../..<%=almanach.getComponentUrl()+"almanach.jsp"%>"  method="POST" target="MyMain">
  <input type="hidden" name="Action">
  <input type="hidden" name="Id">
</form>

<form name="viewEventForm" action=""  method="POST" target="MyMain">
  <input type="hidden" name="Id">
  <input type="hidden" name="Date">
</form>

<form name="createEventForm" action="../..<%=almanach.getComponentUrl()+"createEvent.jsp"%>" method="POST" target="MyMain">
  <input type="hidden" name="Day">
</form>

</body>
</html>