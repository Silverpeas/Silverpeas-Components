<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>

<%
	Collection 	questions 	= (Collection) request.getAttribute("questions");
	String 		profil 		= (String) request.getAttribute("Flag");
	String		userId		= (String) request.getAttribute("UserId");
	Iterator it = questions.iterator();
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="javascript">

function openSPWindow(fonction, windowName)
{
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function deleteConfirm(id)
{
	//confirmation de suppression de la question
	if(window.confirm("<%=resource.getString("MessageSuppressionQ")%>"))
	{
			document.QForm.action = "DeleteQ";
			document.QForm.Id.value = id;
			document.QForm.submit();
	}
}

function closeQ(id)
{
	//confirmation de cloture de la question
	if(window.confirm("<%=resource.getString("MessageCloseQ")%>"))
	{
			document.QForm.action = "CloseQ";
			document.QForm.Id.value = id;
			document.QForm.submit();
	}
}

function DeletesAdmin()
{
	if (existSelected())
	{
		if (existStatusError('2', '0')) {
			alert("<%=resource.getString("questionReply.delStatusErr")%>");	
		}
		else
		{
			if (window.confirm("<%=resource.getString("MessageSuppressionsQ")%>")) 
			{ 
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
			if (window.confirm("<%=resource.getString("MessageSuppressionsQ")%>")) 
			{ 
				document.forms[0].action = "<%=routerUrl%>DeleteQuestions";
				document.forms[0].submit();
			}
		}
	}
}

function DeletesPublisher()
{
	if (existSelected())
	{
		if (existStatusError('0')) {
			alert("<%=resource.getString("questionReply.delStatusErrPublisher")%>");	
		}
		else
		{
			if (window.confirm("<%=resource.getString("MessageSuppressionsQ")%>")) 
			{ 
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



</SCRIPT>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%

	browseBar.setDomainName(spaceLabel);
   	browseBar.setPath("");
	
	boolean existToClose = existQuestionStatus(questions, 1);
	boolean existToDelete = existQuestionStatus(questions, 2);
	boolean existToBeReplied = existQuestionStatus(questions, 0);
	
	if (profil.equals("admin"))
	{
		operationPane.addOperation(resource.getIcon("questionReply.pdcUtilizationSrc"), resource.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+componentId+"','utilizationPdc1')");
		operationPane.addLine();
	}
	operationPane.addOperation(resource.getIcon("questionReply.addQ"), resource.getString("questionReply.addQ"), "CreateQQuery");	
	if (profil.equals("admin") || profil.equals("writer"))
	{
		operationPane.addOperation(resource.getIcon("questionReply.addQR"), resource.getString("questionReply.addQR"), "CreateQueryQR");
		if (existToDelete || existToBeReplied)  
		{
			operationPane.addLine();
			operationPane.addOperation(resource.getIcon("questionReply.delQ"), resource.getString("questionReply.delQs"), "javascript:onClick=DeletesAdmin();");
		}
		if (existToClose)
			operationPane.addOperation(resource.getIcon("questionReply.cloreQ"), resource.getString("questionReply.cloreQs"), "javascript:onClick=Closes();");	
	}
	
	out.println(window.printBefore());  
	out.println(frame.printBefore());	
%>
<FORM METHOD=POST ACTION="">
<%
	ArrayPane arrayPane = gef.getArrayPane("questionReply", routerUrl + "Main", request, session);
	
	arrayPane.addArrayColumn(resource.getString("GML.status"));
	arrayPane.addArrayColumn(resource.getString("GML.name"));
	arrayPane.addArrayColumn(resource.getString("GML.date"));
	arrayPane.addArrayColumn(resource.getString("questionReply.nbR"));
	if (!profil.equals("user"))
	{
		ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resource.getString("GML.operation"));
		arrayColumn1.setSortable(false);
		if (!profil.equals("publisher"))
		{
			ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resource.getString(""));
			arrayColumn2.setSortable(false);
		}
	}
	while(it.hasNext())
	{
		
		Question question = (Question) it.next();
		String title = Encode.javaStringToHtmlString(question.getTitle());
		String date = resource.getOutputDate(question.getCreationDate());
		String id = question.getPK().getId();
		int status = question.getStatus();
		// récupération du nombre de réponses (suivant le profil)
		int nb = question.getReplyNumber();
		int nbPublic = question.getPublicReplyNumber();
		int nbPrive = question.getPrivateReplyNumber();
		if (profil.equals("user"))
			nb = nbPublic;
		if (!profil.equals("user") || (profil.equals("user") && status != 0))
		{
			ArrayLine arrayLine = arrayPane.addArrayLine();
			if (status == 0)
				arrayLine.addArrayCellText(displayIcon(resource.getIcon("questionReply.waiting"), resource.getString("questionReply.waiting")));
			if (status == 1)
				arrayLine.addArrayCellText(displayIcon(resource.getIcon("questionReply.encours"), resource.getString("questionReply.encours")));
			if (status == 2)
				arrayLine.addArrayCellText(displayIcon(resource.getIcon("questionReply.close"), resource.getString("questionReply.close")));
			
			arrayLine.addArrayCellLink(title, routerUrl+"ConsultQuestionQuery?questionId=" + id);
			arrayLine.addArrayCellText(date);
			ArrayCellText cell0 = arrayLine.addArrayCellText(new Integer(nb).toString());
			cell0.setCompareOn((Integer) new Integer(nb));
	
			//création de la colonne des icônes
			IconPane iconPane = gef.getIconPane();
			if (!profil.equals("user"))
			{
				if (!profil.equals("publisher"))
				{
					// icône "supprimer"
					if (status == 0 || status == 2)
					{
						Icon deleteIcon = iconPane.addIcon();
						deleteIcon.setProperties(resource.getIcon("questionReply.delete"), resource.getString("questionReply.delQ"),"javaScript:deleteConfirm('"+id+"')");
					}
					//icône "clore"
					if (status == 1)
					{
						Icon closeIcon = iconPane.addIcon();
						closeIcon.setProperties(resource.getIcon("questionReply.close"), resource.getString("questionReply.cloreQ"),"javaScript:closeQ('"+id+"')");
					}
					if (status == 0 || status == 1)
					{
						//icône "répondre"
						Icon repIcon = iconPane.addIcon();
						repIcon.setProperties(resource.getIcon("questionReply.miniconeReponse"), resource.getString("questionReply.ajoutR"),"CreateRQuery?QuestionId="+ id);
					}
					iconPane.setSpacing("30px");
					arrayLine.addArrayCellIconPane(iconPane);
					
					arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkedQuestion\" value=\"" + id + "\"><INPUT TYPE=\"hidden\" NAME=\"status\" value=\""+status+"\">");
				}
				else 
					// pour le demandeur (publisher)
					// icône "supprimer" que si la question est la sienne et si elle n'a pas encore de réponses
					if (status == 0 && question.getCreatorId().equals(userId))
					{
						Icon deleteIcon = iconPane.addIcon();
						deleteIcon.setProperties(resource.getIcon("questionReply.delete"), resource.getString("questionReply.delQ"),"javaScript:deleteConfirm('"+id+"')");
						
						iconPane.setSpacing("30px");
						arrayLine.addArrayCellIconPane(iconPane);
					}
			}
		}
	}

	out.println(arrayPane.print());
%>
</FORM>

<form name="QForm" action="" Method="POST">
	<input type="hidden" name="Id">
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>