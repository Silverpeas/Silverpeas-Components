<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="imports.jsp" %>
<%@ include file="init.jsp.inc" %>


<%
String[] allTables = null;
String[] allColumns = null;
String[] tables = null;
String[] columns = null;
String column = null;
String action = request.getParameter("Action");

String value = "";
String label = "";
String selected = "";

String m_context = GeneralPropertiesManager..getString("ApplicationURL");
%>

<HTML>
<Head>
<TITLE><%=connecteurJDBC.getString("windowTitleParametrageRequete")%></TITLE>
<view:looknfeel/>
</head>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">
	function processUpdate()
	{

		if( isValidTextMaxi(document.processForm.SQLReq))
		{
			document.processForm.Action.value = "setSQLReq";
			document.processForm.submit();
		}else 
			alert("<%=connecteurJDBC.getString("erreurChampsTropLong")%>");
	}

	function annule()
	{
		document.processForm.Action.value = "cancelSQLReq";
		document.processForm.submit();
	}
	
	function doRequest()
	{
		document.navigationForm.action = "connecteurJDBC.jsp";
		document.navigationForm.submit();
	}

	function writeTableName()
	{
		document.processForm.Action.value = "writeTable";
		document.processForm.submit();
	}

	function writeColumnName()
	{
		document.processForm.Action.value = "writeColumn";
		document.processForm.submit();
	}

	function editTableColumn() 
	{
		chemin = "<%=m_context%><%=connecteurJDBC.getComponentUrl()%>EditTableColumn.jsp?indiceForm=0&indiceElem=5";
		largeur = "540";
		hauteur = "450";
		SP_openWindow(chemin,"SqlRequest_Debut",largeur,hauteur,"");
	}



<%
	if (flag.equals("admin")||flag.equals("publisher"))
	{%>
	function doParameterRequest()
	{
		document.navigationForm.action = "requestParameters.jsp";
		document.navigationForm.submit();
	}

<%
		if (flag.equals("admin"))
		{%>
	function doParameterConnection()
	{
		document.navigationForm.action = "connectionParameters.jsp";
		document.navigationForm.submit();
	}
<%		
		}
	}
%>
</Script>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%



  	//browse bar
	//BrowseBar browseBar = window.getBrowseBar();
	browseBar.setExtraInformation(connecteurJDBC.getString("titreParametrageRequete")) ;

	//operation Pane 
	OperationPane operationPane = window.getOperationPane();
	operationPane.addOperation( m_context + "/util/icons/connecteurJDBC_request.gif", connecteurJDBC.getString("operationPaneRequete"), "javascript:onClick=editTableColumn()");


	//Les onglets
    tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneConsultation"), "javaScript:doRequest()", false);
    
    if (flag.equals("publisher") || flag.equals("admin"))
    	tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneRequete"), "javaScript:doParameterRequest()",true );
	
	if (flag.equals("admin"))
		tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneParametresJDBC"), "javaScript:doParameterConnection()", false);



	Frame frame = gef.getFrame();


	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
	
%>
<form name="processForm" action="processForm.jsp" >
	<input name="Sender" type="hidden" value = "requestParameters.jsp" >
	<input name="Action" type="hidden" >
	<input name="SqlReq" type="hidden" >

 <CENTER>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor4><TR><TD><!--Cadre bleu-->
	  <TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=contourintfdcolor>
	  	
	<TR CLASS=intfdcolor4>
		<td align="center" valign=top><span class="txtlibform"><%=connecteurJDBC.getString("champRequete")%> : </span>
		</td>		
		<TD align="center" valign=top>
			<INPUT TYPE='hidden' name='table'>
			<TEXTAREA NAME="SQLReq" COLS=70 ROWS="15" WRAP="SOFT"  ><%
				String requete = connecteurJDBC.getSQLreq();
				if (requete != null )
					out.print(requete);
%></TEXTAREA >
		</TD>
		
	</TR>
	
	</TABLE>
	</TD></TR></TABLE>	
	</form>
	<%
	Button validateButton = (Button) gef.getFormButton(connecteurJDBC.getString("boutonValider"), "javascript:onClick=processUpdate()", false);
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonAnnuler"), "javascript:onClick=annule()", false));
    out.println(buttonPane.print());
   %>
   

</center>


<%
	out.println(frame.printAfter());
	out.println(window.printAfter());

%>

<form name="navigationForm" >
</form>


</BODY>
</HTML>
