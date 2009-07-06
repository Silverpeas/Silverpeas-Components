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