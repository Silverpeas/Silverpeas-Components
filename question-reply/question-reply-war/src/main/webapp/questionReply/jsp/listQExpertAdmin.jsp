<%--

    Copyright (C) 2000 - 2011 Silverpeas

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

<%@ include file="checkQuestionReply.jsp" %>

<%
	Collection questions = (Collection) request.getAttribute("questions");
	Iterator it = questions.iterator();
	System.out.println("entree");
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<SCRIPT LANGUAGE="JavaScript">
<!--
function DeletesAdmin()
{
	if (existSelected())
	{
		if (existStatusError('2', '0')) {
			alert("<%=resource.getString("questionReply.delStatusErr")%>");	
		}
		else
		{
			if (window.confirm("<%=resource.getString("MessageSuppressionsQ")%>")) { 
				document.forms[0].action = "<%=routerUrl%>DeleteQuestions";
				document.forms[0].submit();
			}
		}
	}
}
function DeletesExpert()
{
	if (existSelected())
	{
		if (existStatusError('2')) {
			alert("<%=resource.getString("questionReply.delStatusErrExpert")%>");	
		}
		else
		{
			if (window.confirm("<%=resource.getString("MessageSuppressionsQ")%>")) { 
				document.forms[0].action = "<%=routerUrl%>DeleteQuestions";
				document.forms[0].submit();
			}
		}
	}
}
function Closes()
{
	if (existSelect())
	{
		if (existStatusError('1'))
			alert("<%=resource.getString("questionReply.closeStatusErr")%>");
		else 
		{
			if (window.confirm("<%=resource.getString("MessageClosesQ")%>")) { 
				document.forms[0].action = "<%=routerUrl%>CloseQuestions";
				document.forms[0].submit();
			}
		}
	}
}
function existStatusError(status)
{
	var err = false;
	if (document.forms[0].status != null)
	{
		if (document.forms[0].status.length != null)
		{
			var i = 0;
			while (i < document.forms[0].status.length) 
			{
				 var statusQ = document.forms[0].status[i].value;
				 if ((document.forms[0].checkedQuestion[i] != null)&&(document.forms[0].checkedQuestion[i].checked))
				 {
					if (statusQ != status)
					{
						err = true;			
						document.forms[0].checkedQuestion[i].checked = false;
					}
				 }
				i++;
			}
		}
	}
	return err;
}
function existStatusError(status1, status2)
{
	var err = false;
	if (document.forms[0].status != null)
	{
		if (document.forms[0].status.length != null)
		{
			var i = 0;
			while (i < document.forms[0].status.length) 
			{
				 var statusQ = document.forms[0].status[i].value;
				 if ((document.forms[0].checkedQuestion[i] != null)&&(document.forms[0].checkedQuestion[i].checked))
				 {
					if (statusQ != status1 && statusQ != status2)
					{
						err = true;			
						document.forms[0].checkedQuestion[i].checked = false;
					}
				 }
				i++;
			}
		}
	}
	return err;
}
function existSelect()
{
	if (document.forms[0].checkedQuestion != null)
	{
		if (document.forms[0].checkedQuestion.length != null)
		{
			var i = 0;
			while (i < document.forms[0].checkedQuestion.length) 
			{
				 if (document.forms[0].checkedQuestion[i].checked)
					return true;	
				i ++;
			}
		}
		else
		{
			 if (document.forms[0].checkedQuestion.checked)
				return true;	

		}
	}
	return false;
}
//-->
</SCRIPT>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%

	browseBar.setDomainName(spaceLabel);
   	browseBar.setPath(resource.getString("questionReply.listQ"));
	
	boolean existToClose = existQuestionStatus(questions, 1);
	boolean existToDelete = existQuestionStatus(questions, 2);
	boolean existToBeReplied = existQuestionStatus(questions, 0);
	
	if (existToClose)
		operationPane.addOperation(resource.getIcon("questionReply.cloreQ"), resource.getString("questionReply.cloreQs"), "javascript:onClick=Closes();");	
	if ((existToClose)&&(existToDelete))
		operationPane.addLine();
	if (scc.getUserProfil().equals("admin"))  
	{
		if (existToDelete || existToBeReplied)  
		{//closed question || in line question with no reply
			operationPane.addOperation(resource.getIcon("questionReply.delQ"), resource.getString("questionReply.delQs"), "javascript:onClick=DeletesAdmin();");
		}
	}
	else if (scc.getUserProfil().equals("writer"))  
	{
		if (existToDelete)  //closed question
			operationPane.addOperation(resource.getIcon("questionReply.delQ"), resource.getString("questionReply.delQs"), "javascript:onClick=DeletesExpert();");	
	}
	out.println(window.printBefore());  
	

	tabbedPane.addTab(resource.getString("questionReply.Qrecues"), routerUrl+"ConsultReceiveQuestions",true, false);  
	tabbedPane.addTab(resource.getString("questionReply.Qremises"), routerUrl+"ConsultSendQuestions",false, true);  

	out.println(tabbedPane.print());
	out.println(frame.printBefore());	
%>
<FORM METHOD=POST ACTION="">
<%
	ArrayPane arrayPane = gef.getArrayPane("questionReply", routerUrl + "Main", request, session);

	ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);
	arrayPane.addArrayColumn(resource.getString("GML.name"));
	arrayPane.addArrayColumn(resource.getString("GML.date"));
	arrayPane.addArrayColumn(resource.getString("GML.publisher"));
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
		int nb = question.getReplyNumber();
		String creator = question.readCreatorName();
		ArrayLine arrayLine = arrayPane.addArrayLine();
		IconPane iconPane1 = gef.getIconPane();
		Icon debIcon = iconPane1.addIcon();
		debIcon.setProperties(resource.getIcon("questionReply.miniconeQuestion"),"", routerUrl+"ConsultQuestionQuery?questionId=" + id);
		arrayLine.addArrayCellIconPane(iconPane1);	
		arrayLine.addArrayCellLink(title, routerUrl+"ConsultQuestionQuery?questionId=" + id);
		arrayLine.addArrayCellText(date);
		arrayLine.addArrayCellText(creator);
		if (status == 0)
			arrayLine.addArrayCellText(displayIcon(resource.getIcon("questionReply.waiting"), resource.getString("questionReply.waiting")));
		if (status == 1)
			arrayLine.addArrayCellText(displayIcon(resource.getIcon("questionReply.encours"), resource.getString("questionReply.encours")));
		if (status == 2)
			arrayLine.addArrayCellText(displayIcon(resource.getIcon("questionReply.close"), resource.getString("questionReply.close")));
		ArrayCellText cell0 = arrayLine.addArrayCellText(new Integer(nb).toString());
		cell0.setCompareOn(Integer.valueOf(nb));
		arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkedQuestion\" value=\"" + id + "\"><INPUT TYPE=\"hidden\" NAME=\"status\" value=\""+status+"\">");	
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