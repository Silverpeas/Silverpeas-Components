<%@page import="com.silverpeas.component.kmelia.KmeliaCopyDetail"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.kmelia.multilang.kmeliaBundle"/>

<p id="kmelia">
<fmt:message key="kmelia.app.copy.info"/>
<input type="checkbox" id="<%=KmeliaCopyDetail.PUBLICATION_HEADER%>" name="<%=KmeliaCopyDetail.PUBLICATION_HEADER%>" value="true" checked="checked" onclick="javascript:checkHeaderClick(this)" /> <fmt:message key="kmelia.app.copy.option.header"/><br/>
<input type="checkbox" id="<%=KmeliaCopyDetail.PUBLICATION_CONTENT%>" name="<%=KmeliaCopyDetail.PUBLICATION_CONTENT%>" value="true" checked="checked" onclick="javascript:checkClick(this)" /> <fmt:message key="kmelia.app.copy.option.content"/><br />
<input type="checkbox" id="<%=KmeliaCopyDetail.PUBLICATION_FILES%>" name="<%=KmeliaCopyDetail.PUBLICATION_FILES%>" value="true" checked="checked" onclick="javascript:checkClick(this)" /> <fmt:message key="kmelia.app.copy.option.files"/>
</p>

<script type="text/javascript">
function checkClick(input) {
	if ($(input).attr("checked") == "checked"){
		if ($(input).attr("id") == "<%=KmeliaCopyDetail.PUBLICATION_CONTENT%>" || 
			$(input).attr("id") == "<%=KmeliaCopyDetail.PUBLICATION_FILES%>") {
			$("#kmelia #<%=KmeliaCopyDetail.PUBLICATION_HEADER%>").prop("checked",true);
		}
	}
}

function checkHeaderClick(input) {
	if ($(input).is(':checked')) {
		// do nothing
	} else {
		if ($("#kmelia #<%=KmeliaCopyDetail.PUBLICATION_CONTENT%>").is(':checked') || 
			$("#kmelia #<%=KmeliaCopyDetail.PUBLICATION_FILES%>").is(':checked')) {
			$(input).prop("checked",true);
		}
	}
}
</script>