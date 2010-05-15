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

<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>

<%
	// r�cup�ration des param�tres
	String		profil		= (String) request.getAttribute("Flag");
	String		userId		= (String) request.getAttribute("UserId");
	Collection 	questions 	= (Collection) request.getAttribute("questions");
	String		questionId	= (String) request.getAttribute("QuestionId");  // question en cours � ouvrir
	Collection	categories	= (Collection) request.getAttribute("Categories");
	
	profil = "user";
	browseBar.setComponentName("");
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>

<style type="text/css">
/* Layout properties for your question  */
.question{
	cursor:pointer;		/* Cursor is like a hand when someone rolls the mouse over the question */
	background-color:#B3BFD1;	
}

.answer{
	padding:3px;
	display:none;	
}

.answers{
	border-left: solid; 
	border-right: solid;
	border-width: 1px;
	border-color: #B3BFD1;   
	background-color: #EDEDED;
}

.titreCateg{
	padding:5px;
	font-size: 11px;
	font-weight: bold;
	background-image: url(/silverpeas/questionReply/jsp/icons/fondCateg.gif);
	background-repeat: repeat-x;
}
</style>

<script language="javascript">

function openSPWindow(fonction, windowName)
{
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}




function showHideAnswer()
{
	var numericID = this.id.replace(/[^\d]/g,'');
	var obj = document.getElementById('a' + numericID);
	if(obj.style.display=='block'){
		obj.style.display='none';
	}else{
		obj.style.display='block';
	}		
}

function initShowHideContent()
{
	var divs = document.getElementsByTagName('div');
	for(var no=0;no<divs.length;no++)
	{
		if(divs[no].className=='question')
		{
			divs[no].onclick = showHideAnswer;
		}
	}
	<% if (questionId != null && !questionId.equals("null") && questionId.length() > 0) { %>
		openQuestion(<%=questionId%>);
	<% } %>
}

// d�rouler les r�ponses d'une question dans la liste
function openQuestion(questionId)
{
	var obj = document.getElementById('a' + questionId);
	obj.style.display='block';
}

window.onload = initShowHideContent;

</SCRIPT>

</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	
   	boolean existToClose = existQuestionStatus(questions, 1);
	boolean existToDelete = existQuestionStatus(questions, 2);
	boolean existToBeReplied = existQuestionStatus(questions, 0);
	
	//out.println(window.printBefore());  
	out.println(frame.printBefore());	
%>
<FORM METHOD="POST" ACTION="">
<%
	// lecture des cat�gories
	out.println("<table width=\"100%\">");
	Iterator itC = categories.iterator();
	while (itC.hasNext())
	{
		NodeDetail uneCategory = (NodeDetail) itC.next();
		String categoryId = Integer.toString(uneCategory.getId());
		String nom = uneCategory.getName();
		String description = uneCategory.getDescription();
		// affichage de la cat�gorie 
		out.println("<tr>");	
		out.println("<td colspan=\"2\" width=\"91%\" class=\"titreCateg\">"+nom+"</td>");
		out.println("</tr>");
		
		out.println("<tr><td colspan=\"2\">");
		Collection questionsByCategory = scc.getQuestionsByCategory(categoryId);
		// lecture de toutes les questions de la cat�gorie
		Iterator it = questionsByCategory.iterator();
		while(it.hasNext())
		{
			Question question = (Question) it.next();
			String title = Encode.javaStringToHtmlString(question.getTitle());
			String content = Encode.javaStringToHtmlString(question.getContent());
			String creator = Encode.javaStringToHtmlString(question.readCreatorName());
			String date = resource.getOutputDate(question.getCreationDate());
			String id = question.getPK().getId();
			String link = question._getPermalink();
			int status = question.getStatus();
			
			// on n'affiche pas les questions en attente pour les lecteurs
			if (!profil.equals("user") || (profil.equals("user") && status != 0))
			{
				// recherche de l'icone de l'�tat
				String etat = "";
				if (status == 0)
					etat = resource.getIcon("questionReply.waiting");
				if (status == 1)
					etat = resource.getIcon("questionReply.encours");
				if (status == 2)
					etat = resource.getIcon("questionReply.close");
				
				// affichage de la question 
				// ------------------------
				String qId = "q" + id;
				%>
				<table cellpadding="0" cellspacing="0" border="0" width="98%" align="center" class="question"><tr><td>
				<!-- <div id="<%=qId%>" class="question"> -->
					<table cellpadding="0" cellspacing="2" width="100%">
						<tr>
							
							<td><img src="<%=etat%>"></td>
							<td class="titreQuestionReponse" width="100%">
								<div id="<%=qId%>" class="question">
									<%=Encode.javaStringToHtmlParagraphe(title)%>
								</div>
							</td>
							<td>
							<a href="<%=link%>"><img border="0" src="<%=resource.getIcon("questionReply.link")%>" alt='<%=resource.getString("questionReply.CopyQuestionLink")%>' title='<%=resource.getString("questionReply.CopyQuestionLink")%>' /></a>
							</td>
							
						</tr>
						<tr>
							<td>&nbsp;</td>
							<td colspan="2">
								<span class="txtBaseline"><%=resource.getString("questionReply.questionOf")%> <%=creator%> <%=resource.getString("questionReply.replyBy")%> <%=date%></span>
							</td>
						</tr>
					</table>
				<!-- </div> -->
				</td></tr></table>
				<%
				
				// affichage des r�ponses 
				// ----------------------
				String aId = "a" + id;
				Collection replies = question.readReplies();
				Iterator itR = replies.iterator();
				boolean existe = false;
				if (itR.hasNext())
					existe = true;
				// MODIF A VALIDER : toujours afficher la zone des r�ponses, pour avoir une s�paration entre les questions
				existe = true;
				// FIN MODIF A VALIDER
				if (existe)
				{
					// il y a au moins une r�ponse, on peut cr�er la zone des r�ponses 
					%>
					<table cellpadding="0" cellspacing="0" width="98%" align="center">
					<tr>
						<td class="answers">
							<div id="<%=aId%>" class="answer"><br/>
							<% if (content != null && content.length() > 0) 
							{ %>
								<table><tr><td>
									<%=Encode.javaStringToHtmlParagraphe(content)%>
								</td></tr></table>				
								<br/>
							<% } 
				}
				
				// lecture de toutes les r�ponses de la question en cours
				while (itR.hasNext())
				{
					Reply reply = (Reply) itR.next();
					String creatorR = Encode.javaStringToHtmlString(reply.readCreatorName());
					String contentR = Encode.javaStringToHtmlString(reply.getContent());
					String dateR = resource.getOutputDate(reply.getCreationDate());
					String titleR = reply.getTitle();
					String idR = reply.getPK().getId();
					
					// recherche du type de la r�ponse (publique ou priv�e) pour l'ic�ne � afficher
					int statusR = reply.getPublicReply();
					String typeReply = "";
					if (statusR == 1)
						typeReply = resource.getIcon("questionReply.minicone"); 
					else
						typeReply = resource.getIcon("questionReply.miniconeReponse");
					
					// dans le cas du demandeur, regarder si la question est la sienne pour afficher ou non les r�ponses priv�es
					boolean isPublisherQuestion = true;
					if (profil.equals("publisher") && statusR == 0)
					{
						if (!question.getCreatorId().equals(userId))
							isPublisherQuestion = false;
						else
							isPublisherQuestion = true;
					}
	
					if ( (statusR == 0 && profil.equals("user")) || (!isPublisherQuestion) )
					{
						// on n'affiche pas cette r�ponse priv�e car :
						// soit c'est un lecteur (il ne voit jamais les r�ponses priv�es)
						// soit c'est un publieur et ce n'est pas sa question (il ne voit pas les r�ponses priv�es des autres demandeurs)
					}
					else
					{ 
						out.println(board.printBefore());
						%>
						<table cellpadding="0" cellspacing="2" width="100%">
						<tr>
							<td><img src="<%=typeReply%>"></td>
							<td class="titreQuestionReponse" width="100%">
								<span class="titreQuestionReponse"><%=Encode.javaStringToHtmlParagraphe(titleR)%></span>
							</td>
							<td nowrap>
								<%
								if (profil.equals("admin") || profil.equals("writer"))
								{ %>
									<a href="UpdateR?replyId=<%=idR%>&QuestionId=<%=id%>"><img border="0" src="<%=resource.getIcon("questionReply.update")%>" title='<%=resource.getString("questionReply.modifR")%>'/></a>
									<a href="javaScript:deleteConfirmR('<%=idR%>', '<%=id%>')"><img border="0" src="<%=resource.getIcon("questionReply.delete")%>" title='<%=resource.getString("questionReply.delR")%>'/></a>
							    <% } %>
						    </td>
						</tr>
						</table>
						<% if (contentR != null && !contentR.equals(""))
						{ %>
							<br>
							<table><tr><td>
								<%=Encode.javaStringToHtmlParagraphe(contentR)%>
							</td></tr></table>
						<% }%> 
						<br>
						<span class="txtBaseline">
							<%=resource.getString("questionReply.replyOf")%> <%=creatorR%> <%=resource.getString("questionReply.replyBy")%> <%=dateR%>
						</span>
						<%
						out.println(board.printAfter());
						out.println("<br>");
					}
				} // fin while (lecture des r�ponses)
				if (existe)
				{ %>
					</div>
					</td></tr>
					</table>
				<% } 
			}   // fin "if (!profil.equals("user") || (profil.equals("user") && status != 0))"
		}  // fin while (lecture des questions)

		out.println("</td></tr>"); 
	}


	// les questions sans cat�gories
	Collection questionsByCategory = scc.getQuestionsByCategory(null);
	if (questionsByCategory != null)
	{
		String nom = "  ";
		out.println("<tr>");	
		out.println("<td colspan=\"2\" class=\"titreCateg\">"+nom+"</td>");
		out.println("</tr>");
		out.println("<tr><td colspan=\"2\">");
		// lecture de toutes les questions de la cat�gorie
		Iterator it = questionsByCategory.iterator();
		while(it.hasNext())
		{
			Question question = (Question) it.next();
			String title = Encode.javaStringToHtmlString(question.getTitle());
			String content = Encode.javaStringToHtmlString(question.getContent());
			String creator = Encode.javaStringToHtmlString(question.readCreatorName());
			String date = resource.getOutputDate(question.getCreationDate());
			String id = question.getPK().getId();
			String link = question._getPermalink();
			int status = question.getStatus();
			// recherche si le profil peut modifier la question
			// le demandeur ne peut modifier que ses questions sans r�ponse (en attente)
			
			// on n'affiche pas les questions en attente pour les lecteurs
			if (!profil.equals("user") || (profil.equals("user") && status != 0))
			{
				// recherche de l'icone de l'�tat
				String etat = "";
				if (status == 0)
					etat = resource.getIcon("questionReply.waiting");
				if (status == 1)
					etat = resource.getIcon("questionReply.encours");
				if (status == 2)
					etat = resource.getIcon("questionReply.close");
				
				// affichage de la question 
				// ------------------------
				String qId = "q" + id;
				%>
				<table cellpadding="0" cellspacing="0" border="0" width="98%" align="center" class="question"><tr><td>
				<!-- <div id="<%=qId%>" class="question"> -->
					<table cellpadding="0" cellspacing="2" width="100%">
						<tr>
							
							<td><img src="<%=etat%>"></td>
							<td class="titreQuestionReponse" width="100%">
								<div id="<%=qId%>" class="question">
									<%=Encode.javaStringToHtmlParagraphe(title)%>
								</div>
							</td>
							<td>
							<a href="<%=link%>"><img border="0" src="<%=resource.getIcon("questionReply.link")%>" alt='<%=resource.getString("questionReply.CopyQuestionLink")%>' title='<%=resource.getString("questionReply.CopyQuestionLink")%>' /></a>
							</td>
	
						</tr>
						<tr>
							<td>&nbsp;</td>
							<td colspan="2">
								<span class="txtBaseline"><%=resource.getString("questionReply.questionOf")%> <%=creator%> <%=resource.getString("questionReply.replyBy")%> <%=date%></span>
							</td>
						</tr>
					</table>
				<!-- </div> -->
				</td></tr></table>
				<%
				
				// affichage des r�ponses 
				// ----------------------
				String aId = "a" + id;
				Collection replies = question.readReplies();
				Iterator itR = replies.iterator();
				boolean existe = false;
				if (itR.hasNext())
					existe = true;
				// MODIF A VALIDER : toujours afficher la zone des r�ponses, pour avoir une s�paration entre les questions
				existe = true;
				// FIN MODIF A VALIDER
				if (existe)
				{
					// il y a au moins une r�ponse, on peut cr�er la zone des r�ponses 
					%>
					<table cellpadding="0" cellspacing="0" width="98%" align="center">
					<tr>
						<td class="answers">
							<div id="<%=aId%>" class="answer"><br/>
							<% if (content != null && content.length() > 0) 
							{ %>
								<table><tr><td>
									<%=Encode.javaStringToHtmlParagraphe(content)%>
								</td></tr></table>				
								<br/>
							<% } 
				}
				
				// lecture de toutes les r�ponses de la question en cours
				while (itR.hasNext())
				{
					Reply reply = (Reply) itR.next();
					String creatorR = Encode.javaStringToHtmlString(reply.readCreatorName());
					String contentR = Encode.javaStringToHtmlString(reply.getContent());
					String dateR = resource.getOutputDate(reply.getCreationDate());
					String titleR = reply.getTitle();
					String idR = reply.getPK().getId();
					
					// recherche du type de la r�ponse (publique ou priv�e) pour l'ic�ne � afficher
					int statusR = reply.getPublicReply();
					String typeReply = "";
					if (statusR == 1)
						typeReply = resource.getIcon("questionReply.minicone"); 
					else
						typeReply = resource.getIcon("questionReply.miniconeReponse");
					
					// dans le cas du demandeur, regarder si la question est la sienne pour afficher ou non les r�ponses priv�es
					boolean isPublisherQuestion = true;
					if (profil.equals("publisher") && statusR == 0)
					{
						if (!question.getCreatorId().equals(userId))
							isPublisherQuestion = false;
						else
							isPublisherQuestion = true;
					}
	
					if ( (statusR == 0 && profil.equals("user")) || (!isPublisherQuestion) )
					{
						// on n'affiche pas cette r�ponse priv�e car :
						// soit c'est un lecteur (il ne voit jamais les r�ponses priv�es)
						// soit c'est un publieur et ce n'est pas sa question (il ne voit pas les r�ponses priv�es des autres demandeurs)
					}
					else
					{ 
						out.println(board.printBefore());
						%>
						<table cellpadding="0" cellspacing="2" width="100%">
						<tr>
							<td><img src="<%=typeReply%>"></td>
							<td class="titreQuestionReponse" width="100%">
								<span class="titreQuestionReponse"><%=Encode.javaStringToHtmlParagraphe(titleR)%></span>
							</td>
							<td nowrap>
								<%
								if (profil.equals("admin") || profil.equals("writer"))
								{ %>
									<a href="UpdateR?replyId=<%=idR%>&QuestionId=<%=id%>"><img border="0" src="<%=resource.getIcon("questionReply.update")%>" title='<%=resource.getString("questionReply.modifR")%>'/></a>
									<a href="javaScript:deleteConfirmR('<%=idR%>', '<%=id%>')"><img border="0" src="<%=resource.getIcon("questionReply.delete")%>" title='<%=resource.getString("questionReply.delR")%>'/></a>
							    <% } %>
						    </td>
						</tr>
						</table>
						<% if (contentR != null && !contentR.equals(""))
						{ %>
							<br>
							<table><tr><td>
								<%=Encode.javaStringToHtmlParagraphe(contentR)%>
							</td></tr></table>
						<% }%> 
						<br>
						<span class="txtBaseline">
							<%=resource.getString("questionReply.replyOf")%> <%=creatorR%> <%=resource.getString("questionReply.replyBy")%> <%=dateR%>
						</span>
						<%
						out.println(board.printAfter());
						out.println("<br>");
					}
				} // fin while (lecture des r�ponses)
				if (existe)
				{ %>
					</div>
					</td></tr>
					</table>
				<% } 
			}   // fin "if (!profil.equals("user") || (profil.equals("user") && status != 0))"
		}  // fin while (lecture des questions)
		out.println("</td></tr>");
	}


	out.println("</table>");

%>
</FORM>

<form name="QForm" action="" Method="POST">
<input type="hidden" name="Id">
</form>

<form name="RForm" action="" Method="POST">
<input type="hidden" name="replyId">
<input type="hidden" name="QuestionId">
</form>
<%
out.println(frame.printAfter());
//out.println(window.printAfter());
%>
</BODY>
</HTML>