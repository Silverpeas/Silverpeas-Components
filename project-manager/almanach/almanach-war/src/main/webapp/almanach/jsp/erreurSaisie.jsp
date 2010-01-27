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
<%@ include file="checkAlmanach.jsp" %>

<%
ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(almanach.getLanguage());
%>

<HTML>
<HEAD>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<%
out.println(graphicFactory.getLookStyleSheet());
%>
</head>
<body leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<%
	Window window = graphicFactory.getWindow();
	Frame frame = graphicFactory.getFrame();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "almanach.jsp");
	browseBar.setPath(resources.getString("GML.error"));
	
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>

<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4>
<tr> 
	<td nowrap>
	  <table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%">
		<tr align=center> 
                <td align="center" class="textePetitBold">
                <%= almanach.getString(((AlmanachPrivateException)request.getAttribute("error")).getMessage())%>  
				</td>
        </tr>
      </table>
    </td>
  </tr>
</table>
<br>
      <!-- BOUTON DE FORMULAIRE--> 
  	  <%
			 Button button1 = graphicFactory.getFormButton(resources.getString("GML.back"), m_context+almanach.getComponentUrl()+"Main", false);
			 out.print(button1.print());
 	    %>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>