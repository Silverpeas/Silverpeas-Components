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

<%@ include file="imports.jsp" %>
<%@ include file="init.jsp.inc" %>

<%
String spaceId = connecteurJDBC.getSpaceId();
String componentId =  connecteurJDBC.getComponentId();
%>

<HTML>
<Head>
  <TITLE><%=connecteurJDBC.getString("windowTitleMain")%></TITLE>
  <link rel="stylesheet" href="../../util/styleSheets/globalSP.css">
</head>
<Script language="JavaScript">

	function doRequest()
	{
		document.navigationForm.action = "../../RconnecteurJDBC/jsp/connecteurJDBC.jsp?Space=<%=spaceId%>&Component=<%=componentId%>";
		document.navigationForm.submit();
	}

<%
	if (flag.equals("admin")||flag.equals("publisher"))
	{%>
	function doParameterRequest()
	{
		document.navigationForm.action = "../../RconnecteurJDBC/jsp/requestParameters.jsp?Space=<%=spaceId%>&Component=<%=componentId%>";
		document.navigationForm.submit();
	}

<%
		if (flag.equals("admin"))
		{%>
	function doParameterConnection()
	{
		document.navigationForm.action = "../../RconnecteurJDBC/jsp/connectionParameters.jsp?Space=<%=spaceId%>&Component=<%=componentId%>";
		document.navigationForm.submit();
	}
<%		
		}
	}
%>
function goto_jsp(jsp, param)
{	
	window.open("../../RconnecteurJDBC/<%=spaceId%>_<%=componentId%>/"+jsp+"?"+param, "MyMain");	
}
</Script>
<Body >

<%

  //browse bar
  //BrowseBar browseBar = window.getBrowseBar();
  //browseBar.setExtraInformation(messages.getString("Donn�es entreprise"));
  browseBar.setComponentName(connecteurJDBC.getComponentLabel(), "javascript:goto_jsp('Main', '')");
  browseBar.setExtraInformation(connecteurJDBC.getString("titreExecution"));
  

	//Les onglets
    tabbedPane = gef.getTabbedPane();
	//tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneConsultation"), "javaScript:doRequest()", true);
	tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneConsultation"), "javaScript:goto_jsp('connecteurJDBC.jsp', '')", true);
	
    
    if (flag.equals("publisher") || flag.equals("admin"))
    	//tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneRequete"), "javaScript:doParameterRequest()",false );
    	tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneRequete"), "javaScript:goto_jsp('requestParameters.jsp', '')",false );
	
	if (flag.equals("admin"))
		//tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneParametresJDBC"), "javaScript:doParameterConnection()", false);
		tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneParametresJDBC"), "javaScript:goto_jsp('connectionParameters.jsp', '')", false);

	Frame frame = gef.getFrame();


	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());



	 //Tableau
	  ArrayPane arrayPane = gef.getArrayPane("ResultSet","",request,session);
	  arrayPane.setSortable(true);
	
	if (connecteurJDBC.getSQLreq()!=null)
	{	
	  connecteurJDBC.startConnection();
	
	  // Add the columns titles
	  for (int i=1 ; i<=connecteurJDBC.getColumnCount() ; i++) {
	    arrayPane.addArrayColumn(connecteurJDBC.getColumnName(i)) ;
	  }
	
	  while (connecteurJDBC.getNext()) {
	    ArrayLine arrayLine = arrayPane.addArrayLine() ;
	    for (int i=1 ; i<=connecteurJDBC.getColumnCount() ; i++) {
	      ArrayCellText champ = arrayLine.addArrayCellText(connecteurJDBC.getString(i)) ;
	      champ.setCompareOn(new String(""));
	    }
	  }
	
	  connecteurJDBC.closeConnection();
}

 
  
  out.println(arrayPane.print());
 


	out.println(frame.printAfter());
	out.println(window.printAfter());

%>
</Body>
<form name="processForm" action="processForm.jsp" >
	<input name="Sender" type="hidden" value = "connecteurJDBC.jsp" >
	<input name="Action" type="hidden" >

</form>

<form name="navigationForm" METHOD="POST" target="MyMain">
</form>


</HTML>
