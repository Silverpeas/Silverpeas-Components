<%@ include file="checkQuickInfo.jsp" %>

<%
	//Collection infos = Les quickInfos visibles
	Iterator infos = (Iterator) request.getAttribute("infos");
%>

<html>
<head>
<title>QuickInfo - User</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript" src="<%=m_context%>/util/javaScript/formUtil.js"></script>
<%@ include file="scriptClipboard_js.jsp.inc" %>
<sp:styleSheet/>
</head>
<sp:window class="txtlist">
	<sp:browseBar/>
	<sp:operationPane>
		<sp:operation name="Copy" url="javascript:onClick=ClipboardCopy()"/>
	</sp:operationPane>
	<sp:tabbedPane>
		<sp:tab/>
		<sp:tab/>
	</sp:tabbedPane>
	<sp:windowBody>
	<form name="quickInfoForm" method="post">
	<sp:arrayPane name="quickinfoList">
		<sp:arrayHeader>
			<sp:arrayColumn/>
			<sp:arrayColumn name="Select" sortable="no" action="SwitchSelection(quickInfoForm, 'selectItem', event)"/>
		</sp:arrayHeader>
		<sp:arrayBody>
<%
	int index = 0;
	while (infos.hasNext()) {
		PublicationDetail pub = (PublicationDetail) infos.next();
%>
		<sp:arrayLine>
			<sp:arrayCell>
				<sp:text style="txtbold">
					<%=pub.getName()%>
				</sp:text>
				<sp:newLine/>
				<sp:text>
					<%=pub.getDescription()%>
				</sp:text>
			</sp:arrayCell>
			<sp:arrayCell>
				<sp:checkBox name="selectItem<%=index%>" value="<%=pub.getPK().getId()%>"/>
			</sp:arrayCell>
		</sp:arrayLine>
<%
		index++;
	}
%>
		</sp:arrayBody>
	</sp:arrayPane>
	</form>
	</sp:windowBody>
</sp:window>
</html>