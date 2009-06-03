<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>

<%
	Collection questions = (Collection) request.getAttribute("questions");
	Iterator it = questions.iterator();
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
 	browseBar.setPath(resource.getString("questionReply.listQ"));
	
	if (existQuestionStatus(questions, 2))
		operationPane.addOperation(resource.getIcon("questionReply.delQ"), resource.getString("questionReply.delQs"), "javascript:onClick=DeletesQ();");	
	out.println(window.printBefore());  
	out.println(frame.printBefore());	
%>
<FORM METHOD=POST ACTION="">
<%
	ArrayPane arrayPane = gef.getArrayPane("questionReply", routerUrl+"Main", request, session);

	ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);
	arrayPane.addArrayColumn(resource.getString("GML.name"));
	arrayPane.addArrayColumn(resource.getString("GML.date"));
	arrayPane.addArrayColumn(resource.getString("GML.status"));
	arrayPane.addArrayColumn(resource.getString("questionReply.nbR"));
	ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("GML.operation"));
	arrayColumn.setSortable(false);
	while(it.hasNext())
	{
		Question question = (Question) it.next();
		String title = Encode.javaStringToHtmlString(question.getTitle());
		String date = resource.getOutputDate(question.getCreationDate());
		String id = question.getPK().getId();
		int status = question.getStatus();
		int nb = question.getPrivateReplyNumber();
		ArrayLine arrayLine = arrayPane.addArrayLine();
		IconPane iconPane1 = gef.getIconPane();
		Icon debIcon = iconPane1.addIcon();
		debIcon.setProperties(resource.getIcon("questionReply.miniconeQuestion"),"", routerUrl+"ConsultQuestionQuery?questionId=" + id);
		arrayLine.addArrayCellIconPane(iconPane1);	
		arrayLine.addArrayCellLink(title, routerUrl+"ConsultQuestionQuery?questionId=" + id);
		arrayLine.addArrayCellText(date);
		if (status == 0)
			arrayLine.addArrayCellText(displayIcon(resource.getIcon("questionReply.waiting"), resource.getString("questionReply.waiting")));
		if (status == 1)
			arrayLine.addArrayCellText(displayIcon(resource.getIcon("questionReply.encours"), resource.getString("questionReply.encours")));
		if (status == 2)
			arrayLine.addArrayCellText(displayIcon(resource.getIcon("questionReply.close"), resource.getString("questionReply.close")));
		ArrayCellText cell0 = arrayLine.addArrayCellText(new Integer(nb).toString());
		cell0.setCompareOn((Integer) new Integer(nb));
		if (status == 2)
			arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkedQuestion\" value=\""+id+"\">");
		else
			arrayLine.addArrayCellText("");
	}


	out.println(arrayPane.print());
%>
</FORM>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>