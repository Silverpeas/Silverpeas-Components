<%--

    Copyright (C) 2000 - 2009 Silverpeas

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

<%@ include file="check.jsp" %>

<%
	String 	role = (String) request.getAttribute("Role");
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

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
<body>
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	
	if (role.equals("admin")) {
		operationPane.addOperation(resource.getIcon("rss.addChannel"), resource.getString("rss.addChannel"), "javascript:onClick=addChannel()");
	}

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>

<h1 class="titreFenetre"><%=resource.getString("rss.explanation1")%></h1>
<p>
<%=resource.getString("rss.explanation2")%> <%=resource.getString("rss.explanation3")%><br />
<%=resource.getString("rss.explanation5")%><br />
<br />
<%=resource.getString("rss.explanation4")%><br />
</p>

<div class="inlineMessage">
    <p>
    <strong><%=resource.getString("rss.explanation6")%> <img border="0" src="<%=context%>/util/icons/rss.gif" alt="RSS" /></strong><br />
    <%=resource.getString("rss.explanation7")%>
  </p>
</div>

<p>
<%=resource.getString("rss.explanation8")%><br />
<%=resource.getString("rss.explanation9")%> <a target="_blank" href="http://www.newsisfree.com/">NewsIsFree</a>, <a target="_blank" href="http://www.syndic8.com/">Syndic8</a>.<br /><br />
</p>

<div class="txtlibform"><%=resource.getString("rss.explanation10")%></div>


<div class="rss_exemple">
	<h3 class="txttitrecol"><%=resource.getString("rss.explanation11")%></h3>
    <ul>
    	<li><a target="_blank" href="http://www.monde-diplomatique.fr/recents.xml">Le monde diplomatique</a></li>
        <li><a target="_blank" href="http://www.liberation.fr/rss.php">Liberation.fr - A la une</a></li>
        <li><a target="_blank" href="http://permanent.nouvelobs.com/cgi/rss/permanent_une">NouvelObs.com : La une</a></li>
        <li><a target="_blank" href="http://www.rtl.fr/referencement/rtlinfo.asp">RTL Info</a></li>
        <li><a target="_blank" href="http://rss.news.yahoo.com/rss/topstories">Yahoo! News - Top Stories</a></li>
    </ul>
</div>
<div class="rss_exemple">
	<h3 class="txttitrecol"><%=resource.getString("rss.explanation12")%></h3>
    <ul>
    	<li><a target="_blank" href="http://www.zdnet.fr/feeds/rss/actualites/">ZDNet</a></li>
        <li><a target="_blank" href="http://www.allhtml.com/news/news.xml">ALL HTML - Le Portail dédié aux Webmasters</a></li>
        <li><a target="_blank" href="http://manchettes.branchez-vous.com/branchez-vous.xml">BRANCHEZ-VOUS.com - Nouvelles Technologies</a></li>
        <li><a target="_blank" href="http://developpeur.journaldunet.com/jdnetdev.xml">Journal du Net Développeurs</a></li>
        <li><a target="_blank" href="http://ntic.org/nouvelles/nouvelles_rss.php">NTIC.org</a></li>
    </ul>
</div>
<div class="rss_exemple">
	<h3 class="txttitrecol">Sciences</h3>
    <ul>
    	<li><a target="_blank" href="http://www.cite-sciences.fr/rss/sciences_actu_fr_20.xml">Cité des Sciences</a></li>
        <li><a target="_blank" href="http://www.futura-sciences.com/services/rss/actu10.xml">Futura-Sciences.com</a></li>
        <li><a target="_blank" href="http://www.cite-sciences.fr/rss/biomagazine_fr_20.xml">Science Actualités : le Biomagazine</a></li>
        <li><a target="_blank" href="http://www.cite-sciences.fr/rss/astro_fr_20.xml">Science Actualités : le journal de l'astronomie</a></li>
        <li><a target="_blank" href="http://news.bbc.co.uk/rss/newsonline_world_edition/science/nature/rss091.xml">BBC News | Science/Nature | World Edition</a></li>
    </ul>
</div>
<div class="rss_exemple">
	<h3 class="txttitrecol">Sport</h3>
    <ul>
    	<li><a target="_blank" href="http://www.lequipe.fr/Xml/actu_rss.xml">L'équipe</a></li>
    	<li><a target="_blank" href="http://www.rtl.fr/referencement/rtlsport.asp">RTL Sport</a></li>
        <li><a target="_blank" href="http://www.microsite.reuters.com/rss/sportsNews">Reuters</a></li>
    </ul>
</div>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="refresh" action="Main" method="post"></form>
</body>
</html>