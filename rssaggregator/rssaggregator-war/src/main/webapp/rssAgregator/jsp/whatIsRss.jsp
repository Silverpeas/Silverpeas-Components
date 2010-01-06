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
<%@ include file="check.jsp" %>

<%
	String 	role 		= (String) request.getAttribute("Role");
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
<script language="JavaScript1.2">

var addChannelWindow = window;

function addChannel() {
    windowName = "addChannelWindow";
	larg = "600";
	haut = "350";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!addChannelWindow.closed && addChannelWindow.name== "addChannelWindow")
        addChannelWindow.close();
    addChannelWindow = SP_openWindow("ToCreateChannel", windowName, larg, haut, windowParams);
}

</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	
	if (role.equals("admin"))
	{
		operationPane.addOperation(resource.getIcon("rss.addChannel"), resource.getString("rss.addChannel"), "javascript:onClick=addChannel()");
	}

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
	<table>
	<tr>
		<td>
			<div class="txtlibform"><%=resource.getString("rss.explanation1")%></div><br>
<%=resource.getString("rss.explanation2")%><br>
<%=resource.getString("rss.explanation3")%><br>
<%=resource.getString("rss.explanation4")%><br>
<%=resource.getString("rss.explanation5")%>.<br><br>
<%=resource.getString("rss.explanation6")%> <img src="<%=resource.getIcon("rss.logoRSS")%>" border="0">&nbsp;<img src="<%=resource.getIcon("rss.logoXML")%>" border="0">.<br>
<%=resource.getString("rss.explanation7")%><br><br>

<%=resource.getString("rss.explanation8")%><br>
<%=resource.getString("rss.explanation9")%> <a href="http://www.newsisfree.com/" target=_blank>NewsIsFree</a>, <a href="http://www.syndic8.com/" target=_blank>Syndic8</a>.<br><br>

<div class="txtlibform"><%=resource.getString("rss.explanation10")%></div>
		</td>
	</tr>
	</table>
	<center>
	<table border="0">
	<tr><td colspan="2" align="center"><b><%=resource.getString("rss.explanation11")%></b></td><td colspan="2" align="center"><b><%=resource.getString("rss.explanation12")%></b></td></tr>
	<tr><td>Le monde diplomatique</td><td><a href="http://www.monde-diplomatique.fr/recents.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td><td>ZDNet</td><td><a href="http://www.zdnet.fr/feeds/rss/actualites/" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td></tr>
	<tr><td>Liberation.fr - A la une</td><td><a href="http://www.liberation.fr/rss.php" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td><td>ALL HTML - Le Portail dédié aux Webmasters</td><td><a href="http://www.allhtml.com/news/news.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td></tr>
	<tr><td>NouvelObs.com : La une </td><td><a href="http://permanent.nouvelobs.com/cgi/rss/permanent_une" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td><td>BRANCHEZ-VOUS.com - Nouvelles Technologies</td><td><a href="http://manchettes.branchez-vous.com/branchez-vous.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td></tr>
	<tr><td>RTL Info</td><td><a href="http://www.rtl.fr/referencement/rtlinfo.asp" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td><td>Journal du Net Développeurs</td><td><a href="http://developpeur.journaldunet.com/jdnetdev.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td></tr>
	<tr><td>Yahoo! News - Top Stories</td><td><a href="http://rss.news.yahoo.com/rss/topstories/" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td><td>NTIC.org</td><td><a href="http://ntic.org/nouvelles/nouvelles_rss.php" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td></tr>
	
	<tr><td colspan="2" align="center">&nbsp;</td><td colspan="2" align="center"></td></tr>
	
	<tr><td colspan="2" align="center"><b>Sciences</b></td><td colspan="2" align="center"><b>Sport</b></td></tr>
	<tr><td>Cité des Sciences</td><td><a href="http://www.cite-sciences.fr/rss/sciences_actu_fr_20.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td><td>RTL Sport</td><td><a href="http://www.rtl.fr/referencement/rtlsport.asp" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td></tr>
	<tr><td>Futura-Sciences.com</td><td><a href="http://www.futura-sciences.com/services/rss/actu10.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td><td>Reuters: Sports</td><td><a href="http://www.microsite.reuters.com/rss/sportsNews" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td></tr>
	<tr><td>Science Actualités : le Biomagazine</td><td><a href="http://www.cite-sciences.fr/rss/biomagazine_fr_20.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td><td>washingtonpost.com - Sports</td><td><a href="http://www.washingtonpost.com/wp-srv/sports/rssheadlines.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td></tr>
	<tr><td>Science Actualités : le journal de l'astronomie</td><td><a href="http://www.cite-sciences.fr/rss/astro_fr_20.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td><td>Yahoo! Sport - Suite Football Actualités</td><td><a href="http://hyperlinkextractor.free.fr/rssfiles/yahoo_football.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td></tr>
	<tr><td>BBC News | Science/Nature | World Edition</td><td><a href="http://news.bbc.co.uk/rss/newsonline_world_edition/science/nature/rss091.xml" target=_blank><img src="<%=resource.getIcon("rss.logoRSS")%>" border="0"></a></td><td></td><td></td></tr>
	</table>
	</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="refresh" Action="Main" method="POST"></form>
</body>
</html>