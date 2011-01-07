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

<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.SearchAxis"%>
<%@ page import="com.silverpeas.whitePages.model.SearchFieldsType"%>

<%@ include file="checkWhitePages.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator"
	prefix="view"%>
	
<%
  browseBar.setDomainName(spaceLabel);
  browseBar.setPath(resource.getString("whitePages.usersList"));
%>
<HTML>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<fmt:message key="whitePages.js.nocheckfields" var="whitePagesNoCheckFields" />
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
  out.println(gef.getLookStyleSheet());
%>

<script language="JavaScript">
function confirmChoices(){
	if(testCheckBoxes()){
		document.validform.submit();
	}else{
		alert('${whitePagesNoCheckFields}')
	}	
}

function testCheckBoxes(){		
	var i = 0;
	var coche = false;	
	for (i=0;i< document.getElementsByName("checkedFields").length;i++){
		if(document.getElementsByName("checkedFields").item(i).checked){
			coche = true;
			break;
		}
	}
	return coche;
}
</script>

</HEAD>

<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5
	bgcolor="#FFFFFF">
<%
  out.println(window.printBefore());
  out.println(frame.printBefore());
%>
<br>
<br>
<FORM id="validform" name="validform" method="POST"
	action="<%=routerUrl + "comfirmFieldsChoice"%>">
<%
		Set alreadySelectedFields = (Set) request.getAttribute("alreadySelectedFields");
		String checked = "checked=\"checked\"";
		ArrayPane arrayPane = gef.getArrayPane("SilverFields", routerUrl
					+ "Main", request, session);

			ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
			arrayColumn0.setSortable(false);
			ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resource
					.getString("whitePages.fieldnamesilver"));
			arrayColumn1.setSortable(false);
			// nom silverpeas
			ArrayLine arrayLine = arrayPane.addArrayLine();
			StringBuffer text = new StringBuffer("<input type=\"checkbox\" name=\"checkedFields\" value=\"USR_name\"");
			if(alreadySelectedFields.contains("USR_name")){
			  text.append(checked);
			}
			text.append(">");
			arrayLine.addArrayCellText(text.toString());
			arrayLine.addArrayCellText(resource.getString("whitePages.silveruserlastname"));
		    // prénom silverpeas
			arrayLine = arrayPane.addArrayLine();
			text = new StringBuffer("<input type=\"checkbox\" name=\"checkedFields\" value=\"USR_surname\"");
			if(alreadySelectedFields.contains("USR_surname")){
			  text.append(checked);
			}
			text.append(">");
			arrayLine.addArrayCellText(text.toString());
			arrayLine.addArrayCellText(resource.getString("whitePages.silveruserfirstname"));
		    // email silverpeas -> pas indexé?
			arrayLine = arrayPane.addArrayLine();
			text = new StringBuffer("<input type=\"checkbox\" name=\"checkedFields\" value=\"USR_email\"");
			if(alreadySelectedFields.contains("USR_email")){
			  text.append(checked);
			}
			text.append(">");
			arrayLine.addArrayCellText(text.toString());
			arrayLine.addArrayCellText(resource.getString("whitePages.silveruseremail"));
			
			out.println(arrayPane.print());

			List xmlFields = (List) request.getAttribute("xmlFields");

			if (xmlFields != null && xmlFields.size() > 0) {
				ArrayPane arrayPaneXmlFields = gef.getArrayPane("XmlFields",
						routerUrl + "Main", request, session);
				ArrayColumn arrayColumnXml0 = arrayPaneXmlFields
						.addArrayColumn("&nbsp;");
				arrayColumnXml0.setSortable(false);
				ArrayColumn arrayColumnXml1 = arrayPaneXmlFields
						.addArrayColumn(resource
								.getString("whitePages.fieldnamexml"));
				arrayColumnXml1.setSortable(false);

				Iterator i = xmlFields.iterator();
				while (i.hasNext()) {
					String xmlField = (String) i.next();
					arrayLine = arrayPaneXmlFields.addArrayLine();
					String fieldId = SearchFieldsType.XML.getLabelType() + xmlField;
					text = new StringBuffer("<input type=\"checkbox\" name=\"checkedFields\" value=\"" + fieldId + "\"");
					if(alreadySelectedFields.contains(fieldId)){
					  text.append(checked);
					}
					text.append(">");
					arrayLine.addArrayCellText(text.toString());
					arrayLine.addArrayCellText(xmlField);
				}
				out.println(arrayPaneXmlFields.print());
			}
			
			List ldapFields = (List) request.getAttribute("ldapFields");

			if (ldapFields != null && ldapFields.size() > 0) {
				ArrayPane arrayPaneLdapFields = gef.getArrayPane("LdapFields",
						routerUrl + "Main", request, session);
				arrayPaneLdapFields.setVisibleLineNumber(50);
				ArrayColumn arrayColumnLdap0 = arrayPaneLdapFields
						.addArrayColumn("&nbsp;");
				arrayColumnLdap0.setSortable(false);
				ArrayColumn arrayColumnLdap1 = arrayPaneLdapFields
						.addArrayColumn(resource
								.getString("whitePages.fieldnameldap"));
				arrayColumnLdap1.setSortable(false);

				Iterator i = ldapFields.iterator();
				while (i.hasNext()) {
					String ldapField = (String) i.next();
					arrayLine = arrayPaneLdapFields.addArrayLine();
					String fieldId = SearchFieldsType.LDAP.getLabelType() + ldapField;
					text = new StringBuffer("<input type=\"checkbox\" name=\"checkedFields\" value=\"" + fieldId + "\"");
					if(alreadySelectedFields.contains(fieldId)){
					  text.append(checked);
					}
					text.append(">");
					arrayLine.addArrayCellText(text.toString());
					arrayLine.addArrayCellText(ldapField);
				}
				out.println(arrayPaneLdapFields.print());
			}
%>
</form>
<center><view:buttonPane>
	<fmt:message key="whitePages.button.cancel" var="cancelLabel" />
	<view:button label="${cancelLabel}" action="<%=routerUrl + "Main"%>" />

	<fmt:message key="whitePages.button.valid" var="validLabel" />
	<view:button label="${validLabel}"
		action="${'javascript:confirmChoices();'}" />

</view:buttonPane></center>
<%
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</BODY>
</HTML>