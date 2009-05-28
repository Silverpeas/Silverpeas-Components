<%@ include file="checkQuickInfo.jsp" %>

<%
	Iterator infosI = (Iterator) request.getAttribute("infos");
%>
<html>
<head>
<title>QuickInfo - Portlet</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript" src="../../util/javaScript/formUtil.js"></script>

<% out.println(gef.getLookStyleSheet()); %>

</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" class="txtlist">
<form name="quickInfoForm" method="post">
  <%
	ArrayPane arrayPane = gef.getArrayPane(quickinfo.getComponentId() + "QuickinfoList", pageContext);
	arrayPane.addArrayColumn(null);

	String 	st 		= null;
	ArrayCellText cellText = null;
	String description = "";
	while (infosI.hasNext()) {
		PublicationDetail pub = (PublicationDetail) infosI.next();
		ArrayLine line = arrayPane.addArrayLine();

		st = "<B>"+pub.getName()+"</B>";
		if (pub.getWysiwyg() != null && !"".equals(pub.getWysiwyg()))
    	description = pub.getWysiwyg();
		else if (pub.getDescription() != null && !pub.getDescription().equals(""))
			description = Encode.javaStringToHtmlParagraphe(pub.getDescription());
		st = st + "<BR>"+description;
		cellText = line.addArrayCellText(st);
		cellText.setValignement("top");
	}
	out.println(arrayPane.print());
  %>
</form>
</body>
</html>
