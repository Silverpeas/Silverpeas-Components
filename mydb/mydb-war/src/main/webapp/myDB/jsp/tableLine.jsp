<%@ include file="init.jsp" %>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());

	Form form = (Form)request.getAttribute("form");
	PagesContext context = (PagesContext)request.getAttribute("context");
	DataRecord data = (DataRecord)request.getAttribute("data");
	
	String command = (String)request.getAttribute("command");
	
	form.displayScripts(out, context);
	
	boolean consultation = ("true".equals((String)request.getAttribute("consultation")));
	if (!consultation)
	{
%>
	<script type="text/javascript">
		function processUpdate()
		{
			if (isCorrectForm())
			{
				document.forms["processForm"].submit();
			}
		}

		function updateField(formIndex, fieldName, value)
		{
			var field = document.forms[formIndex].elements[fieldName];
			field.value = value
			field.focus();
		}
	</script>
<%
	}
%>
</head>

<body marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	String pageTitle = new StringBuffer(30)
		.append(resource.getString(consultation ? "PageTitleDataDetail" : "PageTitleDataModification"))
		.append(" (").append(myDBSC.getTableName()).append(")").toString();
	browseBar.setExtraInformation(pageTitle);

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
	<br>
	<form name="processForm" action="<%=MyDBConstants.ACTION_UPDATE_LINE%>" method="post">
		<input name="command" type="hidden" value="<%=("create".equals(command) ? "create" : "update")%>"/>
		<center><%form.display(out, context, data);%></center>
	</form>
	<br>
	<center>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	if (consultation)
	{
		buttonPane.addButton(gef.getFormButton(resource.getString("ButtonBack"), MyDBConstants.ACTION_MAIN, false));
	}
	else
	{
		buttonPane.addButton(gef.getFormButton(resource.getString("ButtonValidate"), "javascript:processUpdate();", false));
		buttonPane.addButton(gef.getFormButton(resource.getString("ButtonCancel"), MyDBConstants.ACTION_MAIN, false));		
	}
	out.print(buttonPane.print());
%>
	</center>
<%
	String errorMessage = (String)request.getAttribute("errorMessage");
	if (errorMessage != null)
	{
%>
	<br>
	<br>
	<center>
		<a name="err"></a><span class="MessageReadHighPriority"><%=errorMessage%></span>
	</center>
	<script>window.location = "#err";</script>
<%
	}
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>