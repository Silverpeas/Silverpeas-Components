<%@ include file="checkYellowpages.jsp" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="tabManager.jsp.inc" %>

<%
	String newTopicName = (String) request.getParameter("Name");
    String newTopicDescription = (String) request.getParameter("Description");
	NodePK newNodePK = addTopic(yellowpagesScc, newTopicName, newTopicDescription,out);
%>

<HTML>
<HEAD>
<TITLE></TITLE>
<SCRIPT LANGUAGE="JavaScript">

function functionSubmit(){
<%
	if (newNodePK.getId().equals("-1")) {
%>
		self.opener.errorUpdate();
		self.close();
<%
	}
	else{
%>
		window.document.enctypeForm.submit();
<%
	}
%>
}
</SCRIPT>
</HEAD>

<BODY onLoad="functionSubmit()">
	<Form Name="enctypeForm" ACTION="modelManager.jsp" Method="POST" ENCTYPE="multipart/form-data">
		<input type="hidden" name="ContactId" VALUE="<%=newNodePK.getId()%>">
		<input type="hidden" name="Action" VALUE="ModelChoice">
	</Form>
</BODY>
</HTML>
