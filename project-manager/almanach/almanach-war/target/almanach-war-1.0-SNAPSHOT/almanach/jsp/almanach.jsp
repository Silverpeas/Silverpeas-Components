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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkAlmanach.jsp"%>

<%
ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(almanach.getLanguage());

String 			rssURL = (String) request.getAttribute("RSSUrl");
MonthCalendar 	monthC = (MonthCalendar) request.getAttribute("MonthCalendar");
List			accessibleInstances = (List) request.getAttribute("AccessibleInstances");

String flag = request.getParameter("flag");
if (flag == null) {
	flag = "user";
}

String checkedAll = "";
ArrayList othersAlmanachs = null;
String[] agregatedAlmanachs = null;
boolean otherAlmanachsExists = false;
if (almanach.isAgregationUsed())
{
	othersAlmanachs = almanach.getOthersAlmanachs();
	if (othersAlmanachs != null)
		otherAlmanachsExists = true;
	agregatedAlmanachs = almanach.getAgregatedAlmanachs();
	if (otherAlmanachsExists &&  agregatedAlmanachs != null)
	{
		if (othersAlmanachs.size() == agregatedAlmanachs.length) {
			checkedAll = "checked";
		}
	}
}
%>

<!-- AFFICHAGE BROWSER -->
<HTML>
<HEAD>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<% if (StringUtil.isDefined(rssURL)) { %>
	<link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resources.getString("almanach.rssNext")%>" href="<%=m_context+rssURL%>"/>
<% } %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">

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

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function viewEvent(id, date, componentId)
{
	url = "<%=m_context%>/Ralmanach/"+componentId+"/viewEventContent.jsp?Id="+id+"&Date="+date;
	window.open(url,'_self');
}

function addEvent(day)
{
    document.eventForm.Day.value = day;
    document.eventForm.submit();
}

function printPdf(completeMonth) 
{
    if (completeMonth)
    	window.open("MonthPdf", "PdfGeneration", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
    else
    	window.open("EventPdf", "PdfGeneration", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
}

<%	if (almanach.isAgregationUsed()) { %>
var actionAll = false;
function updateAgregation(i) 
{
				if (document.agregateAlmanachs.chk_allalmanach)
				{
							newState = document.agregateAlmanachs.chk_allalmanach.checked;
							//Avoid too much submit for each checkbox (trigger onClick) if click on all checkbox
							if (!actionAll)
							{
									document.agregateAlmanachs.action = "UpdateAgregation";
									document.agregateAlmanachs.submit();
							}												
				}
				else
				{
						document.agregateAlmanachs.action = "UpdateAgregation";
						document.agregateAlmanachs.submit();
				}
}

	function agregateAll() 
	{
		myForm = document.agregateAlmanachs;
		var newState = true;
		if (myForm.chk_allalmanach)
				newState = myForm.chk_allalmanach.checked;
	
		if (myForm.chk_almanach.length == null)
		{
			myForm.chk_almanach.checked = true;
		}	
		else
		{
			for (i=0; i<myForm.chk_almanach.length; i++)
			{
				if (newState && !myForm.chk_almanach[i].checked)
						myForm.chk_almanach[i].checked = true;
				else if (!newState && myForm.chk_almanach[i].checked)
						myForm.chk_almanach[i].checked = false;
			}
		}
			document.agregateAlmanachs.action = "UpdateAgregation";
			document.agregateAlmanachs.submit();
	}
<% } %>
</script>
<%
out.println(graphicFactory.getLookStyleSheet());
%>

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<style type="text/css">
<!--
.eventCells {  padding-right: 3px; padding-left: 3px; vertical-align: top; background-color: #FFFFFF}
-->
</style>
</HEAD>
<BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5">
<% 
	Window 	window 	= graphicFactory.getWindow();
	Frame 	frame	= graphicFactory.getFrame();

  	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);

  	OperationPane operationPane = window.getOperationPane();
   	if (flag.equals("admin") && almanach.isPdcUsed()) {
    	operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+instanceId+"','utilizationPdc1')");
        operationPane.addLine();
   	}
  	operationPane.addOperation(calendarPdfSrc,almanach.getString("genererPdfMoisComplet"), "javascript:onClick=printPdf(true)");
  	operationPane.addOperation(pdfSrc,almanach.getString("genererPdfJourEvenement"), "javascript:onClick=printPdf(false)");
  	operationPane.addOperation(printSrc, resources.getString("GML.print"), "printAlmanach.jsp");

  	if (flag.equals("publisher") || flag.equals("admin")) {
    	operationPane.addOperation(addEventSrc, almanach.getString("creerEvenement"), "javascript:onClick=addEvent('')");
	}
  
  	out.println(window.printBefore());
	out.println(frame.printBefore());
%>

<!-- AFFICHAGE HEADER -->
<CENTER>
  <table width="98%" border="0" cellspacing="0" cellpadding="1">
    <tr>
    	<% if (accessibleInstances != null) { %>
      <td>
        <table cellpadding="0" cellspacing="0" border="0" width="50%" bgcolor="000000">
          <tr> 
            <td>
              
              <table cellpadding="2" cellspacing="1" border="0" width="100%">
				  <form name="form1">
                  <tr> 
                    <td class="intfdcolor" align="center" nowrap width="100%" height="24"> 
                      <select name="select" onChange="window.open(this.options[this.selectedIndex].value,'_self')" class="selectNS">
                      <% 
                      	List instance;
						for (int i = 0; i < accessibleInstances.size(); i++) {
							instance = (List) accessibleInstances.get(i);
							String compLabel = (String) instance.get(1);
							String spaceName = (String) instance.get(2);
							String id		 = (String) instance.get(0);
							
							String selected = "";
							if (id.equals(instanceId))
								selected = "selected";
                        %>
                          <option value="<%=m_context+URLManager.getURL(null,"useless",id)+"Main"%>" <%=selected%>><%=spaceName%> - <%=compLabel%></option>
						<% } %>
                      </select>
                    </td>
                  </tr>
				</form>
              </table>
            </td>
          </tr>
        </table>
      </td>
      	<%} %>
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
<%
				if (almanach.isAgregationUsed())
				{				
				%><form name="agregateAlmanachs"><%
					if (othersAlmanachs != null)
					{
						if (othersAlmanachs.size() > 0)
							out.println("<table><tr><td>"+resources.getString("otherAlmanachEvents")+"</td>");
						int nbChecked = 0;					
						int i = 0;							
						for (Iterator iterator = othersAlmanachs.iterator(); iterator.hasNext();)
						{
							ArrayList otherAlmanach = (ArrayList) iterator.next();
							String checked = "";
							if (almanach.isAlmanachAgregated((String) otherAlmanach.get(0)))
							{
								checked = "checked";
								nbChecked++;
							}
							if ((i % 5)==0 && i>=5)
								out.println("</tr><tr><td>&nbsp;</td>");
							 %>
							<td><input onClick="updateAgregation(<%=i%>)" type="checkbox" name="chk_almanach" <%=checked%> value="<%=otherAlmanach.get(0)%>"></td>
							<td>
							<a href="<%=m_context%>/Ralmanach/<%=otherAlmanach.get(0)%>/Main"><span style="color: <%=otherAlmanach.get(2)%>"><b><%=otherAlmanach.get(1)%></b></span></a></td>
							<td>&nbsp;</td>
							<%
							i++;			
						}

						if (i > 1)
						{
								out.println("<td><input onClick=agregateAll() "+checkedAll+" name=chk_allalmanach type=checkbox></td><td><b>"+resources.getString("allAlmanachs")+"</b></td></tr>");
								out.println("</tr>");
								out.println("</table>");
						}
					}
				%></form><%
				} 

				if (StringUtil.isDefined(rssURL)) {%>
					<table>
	   				<tr>
	   					<td><a href="<%=m_context+rssURL%>"><img src="icons/rss.gif" border="0"></a></td>
	   				</tr>
					</table>
	   				<link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resources.getString("almanach.rssNext")%>" href="<%=m_context+rssURL%>">
	   			<% } %>
</CENTER>

<%		
		out.println(frame.printAfter());				
		out.println(window.printAfter());
%>
<form name="almanachForm" action="almanach.jsp" method="POST">
  <input type="hidden" name="Action">
  <input type="hidden" name="Id">
  <!--<input type="hidden" name="Day">-->
</form>

<form name="eventForm" action="createEvent.jsp" method="POST">
  <input type="hidden" name="Day">
</form>
</body>
</html>
