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