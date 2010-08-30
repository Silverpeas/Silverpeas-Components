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

String navigationLabel = almanach.getString("mois" + almanach.getCurrentDay().get(Calendar.MONTH))+" "+String.valueOf(almanach.getCurrentDay().get(Calendar.YEAR));
%>

<!-- AFFICHAGE BROWSER -->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><%=generalMessage.getString("GML.popupTitle")%></title>
<% if (StringUtil.isDefined(rssURL)) { %>
	<link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resources.getString("almanach.rssNext")%>" href="<%=m_context+rssURL%>"/>
<% } %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

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
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '450','scrollbars=yes, resizable, alwaysRaised');
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

function printPdf(view) 
{
    window.open(view, "PdfGeneration", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
}

function viewEvents() {
	pdcUtilizationWindow = SP_openWindow("ViewYearEventsPOPUP", "allEvents", '600', '400','scrollbars=yes,resizable,alwaysRaised');
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
</head>
<body>
<% 
	Window 	window 	= graphicFactory.getWindow();
	Frame 	frame	= graphicFactory.getFrame();

  	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);

  	OperationPane operationPane = window.getOperationPane();
   	if (flag.equals("admin") && almanach.isPdcUsed()) {
    	operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+instanceId+"','utilizationPdc1')");
   	}
   	if (flag.equals("publisher") || flag.equals("admin")) {
    	operationPane.addOperation(addEventSrc, resources.getString("creerEvenement"), "javascript:onClick=addEvent('')");
    	operationPane.addLine();
	}
   	
   	operationPane.addOperation(printSrc, resources.getString("almanach.action.monthEvents"), "ViewMonthEvents");
   	operationPane.addOperation("",resources.getString("almanach.action.yearEvents"), "ViewYearEvents");
   	operationPane.addOperation("",resources.getString("almanach.action.yearEvents")+" "+resources.getString("almanach.popup"), "javascript:onClick=viewEvents()");
   	operationPane.addLine();
  	operationPane.addOperation(calendarPdfSrc,resources.getString("genererPdfMoisComplet"), "javascript:onClick=printPdf('"+AlmanachPdfGenerator.PDF_MONTH_ALLDAYS+"')");
  	operationPane.addOperation(pdfSrc,resources.getString("genererPdfJourEvenement"), "javascript:onClick=printPdf('"+AlmanachPdfGenerator.PDF_MONTH_EVENTSONLY+"')");
  	operationPane.addOperation(calendarPdfSrc,resources.getString("almanach.genererPdfAnnee"), "javascript:onClick=printPdf('"+AlmanachPdfGenerator.PDF_YEAR_EVENTSONLY+"')");
  
  	out.println(window.printBefore());
	out.println(frame.printBefore());
%>

<!-- AFFICHAGE HEADER -->

<div id="navigation">
	<div id="currentScope">
		<a href="javascript:onClick=previousMonth()"><img src="<%=arrLeft %>" border="0" alt="" align="top"/></a>
		<span class="txtnav"><%=navigationLabel %></span>
		<a href="javascript:onClick=nextMonth()"><img src="<%=arrRight %>" border="0" alt="" align="top"/></a>
	</div>
	<div id="today">
		<a href="javascript:onClick=goToDay()"><%=resources.getString("auJour")%></a>
	</div>
	<% if (accessibleInstances != null) { %>
		<div id="others">
			<select name="select" onchange="window.open(this.options[this.selectedIndex].value,'_self')" class="selectNS">
            <% 
			for (int i = 0; i < accessibleInstances.size(); i++) {
				List instance = (List) accessibleInstances.get(i);
				String compLabel = (String) instance.get(1);
				String spaceName = (String) instance.get(2);
				String id		 = (String) instance.get(0);
				
				String selected = "";
				if (id.equals(instanceId))
					selected = "selected=\"selected\"";
                     %>
                       <option value="<%=m_context+URLManager.getURL(null,"useless",id)+"Main"%>" <%=selected%>><%=spaceName%> - <%=compLabel%></option>
			<% } %>
            </select>
		</div>		
	<% } %>
</div>
<div id="almanachView">
<%=monthC.print()%>
</div>
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
							<td><input onclick="updateAgregation(<%=i%>)" type="checkbox" name="chk_almanach" <%=checked%> value="<%=otherAlmanach.get(0)%>"/></td>
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
	   					<td><a href="<%=m_context+rssURL%>"><img src="icons/rss.gif" border="0" alt="RSS"/></a></td>
	   				</tr>
					</table>
	   				<link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resources.getString("almanach.rssNext")%>" href="<%=m_context+rssURL%>"/>
	   			<% } %>
<%		
		out.println(frame.printAfter());				
		out.println(window.printAfter());
%>
<form name="almanachForm" action="almanach.jsp" method="post">
  <input type="hidden" name="Action"/>
  <input type="hidden" name="Id"/>
</form>

<form name="eventForm" action="createEvent.jsp" method="post">
  <input type="hidden" name="Day"/>
</form>
</body>
</html>