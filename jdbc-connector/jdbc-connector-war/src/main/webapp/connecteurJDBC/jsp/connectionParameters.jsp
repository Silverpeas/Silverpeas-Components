<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ include file="imports.jsp" %>
<%@ include file="init.jsp.inc" %>

<%!
	String toStringList(java.util.Collection col)
	{
		Iterator i = col.iterator();
		String list = "";
		while (i.hasNext())
		{
			list +="\""+(String)i.next()+"\"";
			if (i.hasNext())
				list+=",";
		}
		return list;
	}
%>

<%
String graphicPath                            = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String m_context                              = graphicPath;

%>
<HTML>
<Head>
  <TITLE><%=connecteurJDBC.getString("windowTitleParametrageConnection")%> </TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<Script language="JavaScript">

	// constructeur d'un objet Driver
	function Driver(nameDrv,descriptionDrv,jdbcUrls)
	{
		this.nameDrv=nameDrv;
		this.descriptionDrv = descriptionDrv;
		this.jdbcUrls = jdbcUrls;
	}

	<%
		Collection drivers = connecteurJDBC.getAvailableDriversNames();
		Collection driversDescriptions = connecteurJDBC.getDriversDescriptions();
	%>

	drivers =new Array(<%= drivers.size()%>);
	<%
		Iterator di = drivers.iterator();
		Iterator dd = driversDescriptions.iterator();
		int indice = 0;
		while (di.hasNext())
		{
			String name = (String)di.next();
			String desc = (String)dd.next();
			Collection driversUrls = connecteurJDBC.getJDBCUrlsForDriver(name);

			%>
			jdbcUrls = new Array(<%=toStringList(driversUrls)%>);
			nameDrv = <%="\""+name+"\""%>;
			descriptionDrv = <%="\""+desc+"\""%>;
			drivers[<%=indice%>]=new Driver(nameDrv,descriptionDrv,jdbcUrls);
			<%
					indice ++;
			}
	%>

	function selectDriver()
	{
		// efface le select des urls
		for(i=document.processForm.JDBCUrlsSelect.options.length-1;i>=0;i--)
		{
			document.processForm.JDBCUrlsSelect.options[i]=null;
		}
		// rempli le select des urls
		for(i=0;i<drivers[document.processForm.JDBCdriverNameSelect.selectedIndex].jdbcUrls.length;i++)
			document.processForm.JDBCUrlsSelect.options[i]=new Option( drivers[document.processForm.JDBCdriverNameSelect.selectedIndex].jdbcUrls[i]);

		// mise � jour de la description
		document.processForm.DescriptionDrv.value=drivers[document.processForm.JDBCdriverNameSelect.selectedIndex].descriptionDrv;
		document.processForm.Login.value = "";
		document.processForm.Password.value = "";
	}

	function selectUrl()
	{
		// mise � jour de la description
		document.processForm.DescriptionDrv.value=drivers[document.processForm.JDBCdriverNameSelect.selectedIndex].descriptionDrv;
		document.processForm.Login.value="";
		document.processForm.Password.value="";
	}

	function processUpdate()
	{
		document.processForm.JDBCdriverName.value = document.processForm.JDBCdriverNameSelect.options[document.processForm.JDBCdriverNameSelect.selectedIndex].value;
		document.processForm.JDBCurl.value = document.processForm.JDBCUrlsSelect.options[document.processForm.JDBCUrlsSelect.selectedIndex].text;
		if (isValidTextField(document.processForm.Login)== false)
		{
			document.processForm.Login.focus();
			alert("<%= connecteurJDBC.getString("erreurChampsTropLong")%>");
		}
		else if (isValidTextField(document.processForm.Password)== false)
		{
			document.processForm.Password.focus();
			alert("<%= connecteurJDBC.getString("erreurChampsTropLong")%>");
		}
		else if (isFinite(document.processForm.RowLimit.value)== false)
		{
			document.processForm.RowLimit.focus();
			alert("<%= connecteurJDBC.getString("erreurChampsNonEntier")%>");
		}
		else
		{
			document.processForm.action = "UpdateConnection";
			document.processForm.submit();
		}
	}

</Script>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
  	//browse bar
	browseBar.setExtraInformation(connecteurJDBC.getString("titreParametrageConnection")) ;

	//Les onglets
    tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneConsultation"), "DoRequest", false);

    if (flag.equals("publisher") || flag.equals("admin"))
    	tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneRequete"), "ParameterRequest",false );

	if (flag.equals("admin"))
		tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneParametresJDBC"), "ParameterConnection", true);

	Frame frame = gef.getFrame();

	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());

%>
<form name="processForm">
		<input name="JDBCdriverName" type="hidden" >
		<input name="JDBCurl" type="hidden" >

<%
	String JDBCdriverNameSelectString = "<select name=\"JDBCdriverNameSelect\" onChange=selectDriver()>";
	String currentDriver = connecteurJDBC.getJDBCdriverName();

	Iterator i = connecteurJDBC.getAvailableDriversNames().iterator();
	Iterator iDisplayNames = connecteurJDBC.getAvailableDriversDisplayNames().iterator();

	while (i.hasNext())
	{
		String optionName = (String) i.next();
		String optionDisplayName=(String) iDisplayNames.next();

		if (currentDriver==null || currentDriver.length()==0)
			currentDriver=optionName;

		if (!optionName.equals(currentDriver))
			JDBCdriverNameSelectString+="<option value=\""+optionName+"\">"+optionDisplayName;
  		else
  			JDBCdriverNameSelectString+="<option value=\""+optionName+"\" selected>"+optionDisplayName;
  	}

	JDBCdriverNameSelectString+="</select>";

	String JDBCUrlsSelectString = "<select name=\"JDBCUrlsSelect\" onChange=selectUrl()>>";
	String currentUrl = connecteurJDBC.getJDBCurl();
	if (currentDriver != null )
		i = connecteurJDBC.getJDBCUrlsForDriver(currentDriver).iterator();
	while (i.hasNext())
	{	String option = (String) i.next();
		if (!option.equals(currentUrl))
			JDBCUrlsSelectString+="<option >"+option;
  		else
  			JDBCUrlsSelectString+="<option selected>"+option;
  	}
	JDBCUrlsSelectString+="</select>";

    Button validateButton = (Button) gef.getFormButton(connecteurJDBC.getString("boutonValider"), "javascript:onClick=processUpdate()", false);
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonAnnuler"), "javascript:history.back()", false));
   %>

<center>
<TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor><TR><TD>
	  <TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
	<TR>
		<TD class="txtlibform"><%=connecteurJDBC.getString("champNomDriver")%> :</TD>
		<TD><%=JDBCdriverNameSelectString%></TD>
	</TR>
	<TR>
		<TD class="txtlibform"><%=connecteurJDBC.getString("champUrlJDBC")%> :</TD>
		<TD><%=JDBCUrlsSelectString%></TD>
	</TR>
	<TR>
		<TD class="txtlibform"><%=connecteurJDBC.getString("champsDescription")%> :</TD>
		<TD><input type="text" name="DescriptionDrv" size="50" disabled value = "<% if (currentDriver!=null) out.print(connecteurJDBC.getDescriptionForDriver(currentDriver));%>"></TD>
	</TR>

	<TR>
		<TD class="txtlibform"><%=connecteurJDBC.getString("champIdentifiant")%> :</TD>
		<TD><input type="text" name="Login" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%if (connecteurJDBC.getLogin()!=null) out.print(connecteurJDBC.getLogin());%>"></TD>
	</TR>
	<TR>
		<TD class="txtlibform"><%=connecteurJDBC.getString("champMotDePasse")%> :</TD>
		<TD><input type="password" name="Password" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%if (connecteurJDBC.getPassword()!=null) out.print(connecteurJDBC.getPassword());%>"></TD>
	</TR>
	<TR>
		<TD class="txtlibform"><%=connecteurJDBC.getString("champLignesMax")%> :</TD>
		<TD><input type="text" name="RowLimit" size="50" maxlength="<%=String.valueOf(Integer.MAX_VALUE).length()%>" value="<%if (connecteurJDBC.getRowLimit()!= -1) out.print(String.valueOf(connecteurJDBC.getRowLimit()));%>"> <i><%=connecteurJDBC.getString("champLignesMaxExplanation")%></i></TD>
	</TR>
</TABLE>
</TD></TR></TABLE>
</form><%out.print(buttonPane.print());%>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());

%>
<form name="navigationForm"  >
</form>
</Body>
</HTML>
