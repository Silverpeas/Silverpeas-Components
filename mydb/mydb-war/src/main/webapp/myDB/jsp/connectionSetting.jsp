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

<%@ include file="init.jsp" %>

<html>
<head>
<%
	String login = myDBSC.getLogin();
	if (login == null)
	{
		login = "";
	}
	String password = myDBSC.getPassword();
	if (password == null)
	{
		password = "";
	}

	out.println(gef.getLookStyleSheet());
%>
	<script type="text/javascript" src="<%=applicationURL%>/util/javaScript/checkForm.js"></script>
	<script type="text/javascript">
		function Driver(driverName, driverDescription, jdbcUrls)
		{
			this.driverName = driverName;
			this.driverDescription = driverDescription;
			this.jdbcUrls = jdbcUrls;
		}
<%
	DriverManager driverManager = myDBSC.getDriverManager();
	Collection drivers = driverManager.getAvailableDriversNames();
	Collection driversDescriptions = driverManager.getDriversDescriptions();
%>
		var drivers = new Array(<%=drivers.size()%>);<%

	Iterator di = drivers.iterator();
	Iterator dd = driversDescriptions.iterator();
	int index = 0;
	Collection driversUrls;
	Iterator du;
	StringBuffer driversUrlsSb;
	while (di.hasNext())
	{
		String name = (String)di.next();
		String desc = (String)dd.next();

		driversUrls = driverManager.getJdbcUrlsForDriver(name);
		du = driversUrls.iterator();
		driversUrlsSb = new StringBuffer();
		while (du.hasNext())
		{
			if (driversUrlsSb.length() > 0)
			{
				driversUrlsSb.append(", ");
			}
			driversUrlsSb.append("\"").append((String)du.next()).append("\"");
		}
%>
		drivers[<%=index%>] = new Driver("<%=name%>", "<%=desc%>", new Array(<%=driversUrlsSb.toString()%>));<%

		index++;
	}
%>

		var form;

		function init()
		{
			form = document.forms["processForm"];
			form.elements["login"].value = "<%=login%>";<%

	if (password.equals(""))
	{
%>
			form.elements["password"].value = "";<%

	}
%>
		}

		function selectDriver()
		{
			var urlSelect = form.elements["jdbcUrlSelect"];
			var driverNameSelect = form.elements["jdbcDriverNameSelect"];
			// URLs list is cleared ...
			var i;
			for (i = urlSelect.options.length - 1; i >= 0; i--)
			{
				urlSelect.options[i] = null;
			}
			// ... and filled.
			for (i = 0; i < drivers[driverNameSelect.selectedIndex].jdbcUrls.length; i++)
			{
				urlSelect.options[i] = new Option(drivers[driverNameSelect.selectedIndex].jdbcUrls[i]);
			}
			selectUrl();
		}

		function selectUrl()
		{
			// Description update.
			form.elements["driverDescription"].value = drivers[form.elements["jdbcDriverNameSelect"].selectedIndex].driverDescription;
			form.elements["login"].value = "";
			form.elements["password"].value = "";
		}

		function processUpdate()
		{
			form.elements["jdbcDriverName"].value = form.elements["jdbcDriverNameSelect"].value;
			form.elements["jdbcUrl"].value = form.elements["jdbcUrlSelect"].options[form.elements["jdbcUrlSelect"].selectedIndex].text;
			if (!isValidTextField(form.elements["login"]))
			{
				form.elements["login"].focus();
				alert("<%=resource.getString("ErrorTooLongField")%>");
			}
			else if (!isValidTextField(form.elements["password"]))
			{
				form.elements["password"].focus();
				alert("<%=resource.getString("ErrorTooLongField")%>");
			}
			else if (!isFinite(form.elements["rowLimit"].value))
			{
				form.elements["rowLimit"].focus();
				alert("<%=resource.getString("ErrorIntFieldRequired")%>");
			}
			else
			{
				form.action = "<%=MyDBConstants.ACTION_UPDATE_CONNECTION%>";
				form.submit();
			}
		}
	</script>
</head>

<body onload="init()" marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setExtraInformation(resource.getString("PageTitleConnectionSetting")) ;

	out.println(window.printBefore());

	// Current page tab index.
	int tabIndex = 3;
%>
	<%@ include file="tabs.jsp" %>
<%
	out.println(frame.printBefore());
%>
	<form name="processForm">
		<input name="jdbcDriverName" type="hidden" value=""/>
		<input name="jdbcUrl" type="hidden" value=""/>
		<center>
			<table cellpadding="2" cellspacing="0" border="0" width="98%" class="intfdcolor">
				<tr>
					<td>
						<table cellpadding="5" cellspacing="0" border="0" width="100%" class="intfdcolor4">
							<tr>
								<td class="txtlibform"><%=resource.getString("Driver")%> :</td>
								<td><select name="jdbcDriverNameSelect" onchange="selectDriver();"><%

	String currentDriver = myDBSC.getJdbcDriverName();

	Iterator driversNamesIter = driverManager.getAvailableDriversNames().iterator();
	Iterator driversDisplayNamesIter = driverManager.getAvailableDriversDisplayNames().iterator();
	String optionName;
	String optionDisplayName;
	while (driversNamesIter.hasNext())
	{
		optionName = (String)driversNamesIter.next();
		optionDisplayName = (String)driversDisplayNamesIter.next();
		if (currentDriver == null || currentDriver.length() == 0)
		{
			currentDriver = optionName;
		}
%>
									<option value="<%=optionName%>" <%if (optionName.equals(currentDriver)) {%>selected<%}%>><%=optionDisplayName%></option><%

	}
							  %></select></td>
							</tr>
							<tr>
								<td class="txtlibform"><%=resource.getString("Url")%> :</td>
								<td><select name="jdbcUrlSelect" onchange="selectUrl();"><%

	String currentUrl = myDBSC.getJdbcUrl();
	if (currentDriver != null)
	{
		Iterator iter = driverManager.getJdbcUrlsForDriver(currentDriver).iterator();
		while (iter.hasNext())
		{
			String option = (String)iter.next();%>
									<option <%if (option.equals(currentUrl)) {%>selected<%}%>><%=option%></option><%

	  	}
	}
							  %></select></td>
							</tr>
							<tr>
								<td class="txtlibform"><%=resource.getString("Description")%> :</td>
								<td><input type="text" name="driverDescription" size="50" disabled value="<%if (currentDriver != null) {%><%=driverManager.getDescriptionForDriver(currentDriver)%><%}%>"></td>
							</tr>
							<tr>
								<td class="txtlibform"><%=resource.getString("Login")%> :</td>
								<td><input type="text" name="login" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%=login%>"></td>
							</tr>
							<tr>
								<td class="txtlibform"><%=resource.getString("Password")%> :</td>
								<td><input type="password" name="password" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%=password%>"></td>
							</tr>
							<tr>
								<td class="txtlibform"><%=resource.getString("MaxRows")%> :</td>
								<td><input type="text" name="rowLimit" size="50" maxlength="<%=String.valueOf(Integer.MAX_VALUE).length()%>" value="<%if (myDBSC.getRowLimit()!= -1) {%><%=myDBSC.getRowLimit()%><%}%>"> <i><%=resource.getString("MaxRowsExplanation")%></i></td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</center>
	</form>
<%
	String errorMessage = (String)request.getAttribute("errorMessage");
	if (errorMessage != null)
	{
%>
	<br>
	<center>
		<span class="MessageReadHighPriority"><%=errorMessage%></span>
	</center>
	<br>
<%
	}
%>
	<center>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("ButtonValidate"), "javascript:processUpdate();", false));
	buttonPane.addButton(gef.getFormButton(resource.getString("ButtonCancel"), "javascript:history.back();", false));
	out.print(buttonPane.print());
%>
	</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>