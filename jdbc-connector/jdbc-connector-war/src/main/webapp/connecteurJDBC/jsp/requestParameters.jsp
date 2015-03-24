<%@ page import="org.silverpeas.search.searchEngine.searchEngine.control.ejb.*" %>
<%--
  Copyright (C) 2000 - 2015 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "https://www.silverpeas.org/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="imports.jsp" %>
<%@ include file="init.jsp" %>

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

String m_context = GeneralPropertiesManager.getString("ApplicationURL");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<Head>
<title><%=connecteurJDBC.getString("windowTitleParametrageRequete")%></title>
<view:looknfeel/>
</head>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
	function processUpdate()
	{

		if( isValidTextMaxi(document.processForm.SQLReq))
		{
			document.processForm.action = "SetSQLRequest";
			document.processForm.submit();
    } else {
      var err = '<%=connecteurJDBC.getString("erreurChampsTropLong")%>';
      alert(err);
    }
	}

	function cancel()
	{
		document.processForm.action = "connecteurJDBC";
		document.processForm.submit();
	}

	function editTableColumn()
	{
		chemin = "<%=m_context%><%=connecteurJDBC.getComponentUrl()%>EditTableColumn.jsp?indiceForm=0&indiceElem=5";
		largeur = "540";
		hauteur = "450";
		SP_openWindow(chemin,"SqlRequest_Debut",largeur,hauteur,"");
	}

</script>
<body marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">

<%
	browseBar.setExtraInformation(connecteurJDBC.getString("titreParametrageRequete")) ;

	//operation Pane 
	OperationPane operationPane = window.getOperationPane();
	operationPane.addOperation( m_context + "/util/icons/connecteurJDBC_request.gif", connecteurJDBC.getString("operationPaneRequete"), "javascript:onClick=editTableColumn()");


	//Les onglets
    tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneConsultation"), "DoRequest", false);

  if (flag.equals("publisher") || flag.equals("admin")) {
    tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneRequete"), "ParameterRequest", true);
  }

  if (flag.equals("admin")) {
    tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneParametresJDBC"), "ParameterConnection",
        false);
  }



	Frame frame = gef.getFrame();


	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
	
%>
<form name="processForm" action="SetSQLRequest" >

<table cellpadding=0 cellspacing=0 border=0 width="98%" class=intfdcolor4><TR><TD><!--Cadre bleu-->
	  <table cellpadding=5 cellspacing=0 border=0 width="100%" class=contourintfdcolor>
	  	
	<tr class=intfdcolor4>
		<td align="center" valign=top><span class="txtlibform"><%=connecteurJDBC.getString("champRequete")%> : </span>
		</td>		
		<td align="center" valign=top>
			<textarea name="SQLReq" cols=70 rows="15" wrap="SOFT"  ><%
        String requete = connecteurJDBC.getCurrentConnectionInfo().getSqlRequest();
				if (requete != null )
					out.print(requete);
%></textarea>
		</td>
	</tr>

	</table>
	</td></tr></table>
	</form>
	<%
    Button validateButton = gef.getFormButton(connecteurJDBC.getString("boutonValider"),
        "javascript:onClick=processUpdate()", false);
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(
        gef.getFormButton(connecteurJDBC.getString("boutonAnnuler"), "javascript:onClick=cancel()",
            false));
    out.println(buttonPane.print());
   %>

</center>


<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<form name="navigationForm" >
</form>
</body>
</html>
