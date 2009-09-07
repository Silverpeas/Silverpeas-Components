<%--
******************************
*** forumsListManager.jsp  ***
*** fonctions de gestion   *** 
*** de la liste des forums ***
*** cree par F. Rageade    ***
*** 29 Septembre 2000      ***
******************************
--%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="com.stratelia.webactiv.forums.forumsException.*"%>

<%!
// Icons
String update = "../../util/icons/update.gif";
String unlock = "../../util/icons/lock.gif";
String lock = "../../util/icons/unlock.gif";
String delete = "../../util/icons/delete.gif";
String addForum = "../../util/icons/forums_to_add.gif";
String addCategory = "../../util/icons/folderAddBig.gif";

  public String actionURL(boolean admin, String page, String action, String params, String forum) {
    // String retour = "/forums/forums/jsp/" + page + ".jsp?";
    String retour = page + ".jsp?";
    if (admin) retour = retour + "admin=1";
    else retour = retour + "admin=0";
    if (action.length()>0) retour = retour + "&action=" + action;
    if (params.length()>0) retour = retour + "&params=" + params;
    if (forum.length()>0) retour = retour + "&forum=" + forum;
    return retour;
  }

  public String actionURL(boolean admin, String call, String page, String action, String params, String forum) {
    // String retour = "/forums/forums/jsp/" + page + ".jsp?";
    String retour = page + ".jsp?";
    if (admin) retour = retour + "admin=1";
    else retour = retour + "admin=0";
    if (call.length()>0) retour = retour + "&call=" + call;
    if (action.length()>0) retour = retour + "&action=" + action;
    if (params.length()>0) retour = retour + "&params=" + params;
    if (forum.length()>0) retour = retour + "&forum=" + forum;
    return retour;
  }

  public String navigationBar(boolean admin, String forumId, com.stratelia.webactiv.util.ResourceLocator message,
				com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc) throws ForumsException {
	boolean boucle = false;
	String retour = "";
	String currentId = new String(forumId);
	// String base = "\n\t<a href=\"" + actionURL(admin, "main", "", "", "") + "\">" + message.getString("accueil") + "</a> &gt; ";
	String base = "";

	if (forumId.equals("0")) {
		/* retour = "\n\t<!-- Navigation -->" + "\n\t<a href=\"" + 
			actionURL(admin, "main", "", "", "") + "\">" + message.getString("accueil") + "</a>"; */
		retour = "";
	}
	else {
		retour = "<a href=\"" + actionURL(admin, "viewForum", "", "", forumId) + "\">" + 
			Encode.javaStringToHtmlString((String)(scc.getForumInfos(Integer.parseInt(forumId)).elementAt(1))) + "</a>";
		boucle = true;
	}
	
	while (boucle) {
	    String forumParent = scc.getForumParent(currentId);
	    if (forumParent.equals("0")) {
		retour = base + retour;
	        boucle = false;
	    }
	    else {
		String parentName = (String)(scc.getForumInfos(Integer.parseInt(forumParent)).elementAt(1));
	        String line = "<a href=\"" + actionURL(admin, "viewForum", "", "", forumParent) + "\">" + Encode.javaStringToHtmlString(parentName) + "</a> &gt; ";
		retour = line + retour;
		currentId = forumParent;
	    }
	}
	return retour;
  }

  public void displayForumLine(java.util.Vector line, com.stratelia.silverpeas.util.ResourcesWrapper resources, JspWriter out, 
                            int currentPage, String call, boolean admin, boolean moderator, int depth, boolean hasChilds, 
                            boolean deployed, com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc) {
	try {
		int i;
		int forumId = ((Integer) line.elementAt(0)).intValue();
		String forumName = (String) line.elementAt(1);
		String forumDescription = (String) line.elementAt(2);
		int forumActive = ((Integer) line.elementAt(3)).intValue();
		
		String nbSubjects = Integer.toString(scc.getNbSubjects(forumId));
		String nbMessages = Integer.toString(scc.getNbMessages(forumId));
		
		String lastMessageId = "";
		Date   dateLastMessage = null;
		String lastMessageDate = "";
		String lastMessageUser = "";
		String[] lastMessage = scc.getLastMessage(Integer.toString(forumId));
		if (lastMessage[0] != null && !lastMessage[0].equals("null") && !lastMessage[0].equals(""))
		{
			lastMessageId = lastMessage[0];
			lastMessageDate = convertDate(new Date(Long.parseLong(lastMessage[1])), resources);
			lastMessageUser = lastMessage[2];
		}
		
		out.println("<tr>");

		// Cadenas de "lock"
		//premier colonne
		out.print("<td>");

		// rechercher si l'utilisateur a des messages non lu sur ce forum
		boolean isNewMessage = scc.isNewMessageByForum(scc.getUserId(), forumId);
		if (isNewMessage)
			out.print("<img src=\"icons/buletRed.gif\">");
		else
			out.print("<img src=\"icons/buletColoredGreen.gif\">");

		// Icone de deploiement
		out.print("<img src=\"icons/1px.gif\">");
		if (depth>0) {
			out.print("<img src=\"icons/1px.gif\" width=\"" + depth*10 + "\" height=\"1\">");
		}
		if (hasChilds) {
			out.print("<a href=\"");
			if (currentPage>0) {
				if (deployed) {
					out.print(actionURL(admin, call, "viewForum", "2", String.valueOf(forumId), String.valueOf(currentPage)));
					out.println("\">");
					//out.println("<img src=\"icons/fo_flechebas.gif\" width=\"11\" height=\"6\" border=\"0\"></a>");
				}
				else {
					out.print(actionURL(admin, call, "viewForum", "1", String.valueOf(forumId), String.valueOf(currentPage)));
					out.println("\"><img src=\"icons/topnav_r.gif\" width=\"6\" height=\"11\" border=\"0\"></a>");
				}
			}
			else { 
				if (deployed) {
					out.print(actionURL(admin, call, "main", "2", String.valueOf(forumId), ""));
					out.println("\">");
					//out.println("<img src=\"icons/fo_flechebas.gif\" width=\"11\" height=\"6\" border=\"0\"></a>");
				}
				else {
					out.print(actionURL(admin, call, "main", "1", String.valueOf(forumId), ""));
					out.println("\"><img src=\"icons/topnav_r.gif\" width=\"6\" height=\"11\" border=\"0\"></a>");
				}
			}
		}
		else out.print("&nbsp;");
		out.print("</td>");

		//deuxieme colonne
		// nom du Forum
		out.print("<td width=\"100%\" >");
		out.print("<span class=\"titreForum\">");
		out.print("<a href=\"" + actionURL(admin, call, "viewForum", "", "", String.valueOf(forumId)) + "\">");
		out.print(Encode.javaStringToHtmlString(forumName));
		out.println("</a></span><br />");

		// description du forum
		out.print("<span class=\"descriptionForum\">");
		out.print(Encode.javaStringToHtmlString(forumDescription));
		out.println("</span></td>");
		
		// Troisieme colonne
		// nombre de sujets dans le forum
		out.print("<td align=\"center\" class=\"fondClair\"><span class=\"txtnote\">");
		out.print(Encode.javaStringToHtmlString(nbSubjects));
		out.println("</span></td>");
		
		// quatrieme colonne
		// nombre de messages dans le forum
		out.print("<td align=\"center\" class=\"fondFonce\"><span class=\"txtnote\">");
		out.print(Encode.javaStringToHtmlString(nbMessages));
		out.println("</span></td>");
		
		//cinquième colonne
		// dernier sujet du forum
		out.print("<td nowrap=\"nowrap\" align=\"center\" class=\"fondClair\"><span class=\"txtnote\">");
		if (lastMessageDate != null)
		{
			out.print(Encode.javaStringToHtmlString(lastMessageDate) + "<br />");
			out.print("<a href=\"" + actionURL(admin, call, "viewMessage", "", lastMessageId, String.valueOf(forumId)) + "\">");
			out.print(Encode.javaStringToHtmlString(lastMessageUser));
		}
		out.println("</span></td>");

		//sixieme colonne
		// boutons d'admin
		if (admin||moderator) {
			out.print("<td align=\"middle\" nowrap class=\"fondFonce\">");
		
			// icone de modification
			out.print("<a href=\"");
			out.print(actionURL(admin, call, "editForumInfo", "2", String.valueOf(forumId), String.valueOf(currentPage)));
			out.println("\"><img src=\""+ update +"\" border=\"0\" align=\"middle\" alt=\"" + resources.getString("editForum") + "\" title=\"" + resources.getString("editForum") + "\"></a>");
		    
		    // icone de suppression
		    out.print("&nbsp;<a href=\"javascript:confirmDeleteForum('" + String.valueOf(forumId) + "');\">");
			out.print("<img src=\""+ delete +"\" border=\"0\" align=\"middle\" alt=\"" + resources.getString("deleteForum") + "\" title=\"" + resources.getString("deleteForum") + "\"></a>");
		    
		    // icone de vérouillage
		    out.print("&nbsp;<a href=\"");
			if (currentPage>0) 
			{
				if (forumActive==1) out.print(actionURL(admin, call, "viewForum", "5", String.valueOf(forumId), String.valueOf(currentPage)));
				else out.print(actionURL(admin, call, "viewForum", "6", String.valueOf(forumId), String.valueOf(currentPage)));
			}
			else 
			{
				if (forumActive==1) out.print(actionURL(admin, call, "main", "5", String.valueOf(forumId), String.valueOf(currentPage)));
				else out.print(actionURL(admin, call, "main", "6", String.valueOf(forumId), String.valueOf(currentPage)));
			}
			if (forumActive==1) 
			{
			   out.print("\">");
			   out.print("<img src=\""+ unlock +"\" border=\"0\" align=\"middle\" alt=\"");
		       out.print(resources.getString("lockForum"));
			   out.println("\" title=\"" + resources.getString("lockForum") + "\"></a>");
			}
			else 
			{
			   out.print("\">");
			   out.print("<img src=\""+ lock +"\" border=\"0\" align=\"middle\" alt=\"");
			   out.print(resources.getString("unlockForum"));
			   out.println("\" title=\"" + resources.getString("unlockForum") + "\"></a>");
		    }
		    
			out.println("</td></td></tr>");
		}
	}
	catch (java.io.IOException ioe) {
		SilverTrace.info("forums", "JSPforumsListManager.displayForumLine()", "root.EX_NO_MESSAGE", null, ioe);
	}

}

public String convertDate(Date date, com.stratelia.silverpeas.util.ResourcesWrapper resources)
{

	String messageDateText = "";
	
	// convertir la date du jour 
	Calendar today = Calendar.getInstance();
    today.setTime(new Date());
    today.set(Calendar.HOUR_OF_DAY, 0);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    today.set(Calendar.MILLISECOND, 0); 
                      	
    // convertir la date d'hier
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DATE,-1);
    yesterday.set(Calendar.HOUR_OF_DAY, 0);
    yesterday.set(Calendar.MINUTE, 0);
    yesterday.set(Calendar.SECOND, 0);
    yesterday.set(Calendar.MILLISECOND, 0);
                      	
    // convertir la date du message
    Calendar messageDate = Calendar.getInstance();
    messageDate.setTime(date);
    messageDate.set(Calendar.HOUR_OF_DAY, 0);
    messageDate.set(Calendar.MINUTE, 0);
                        	
    if (today.equals(messageDate))
    {
    	// message du jour
       	messageDateText = resources.getString("forums.today");
    }
    else if (yesterday.equals(messageDate))
    {
    	// message de la veille
       	messageDateText = resources.getString("forums.yesterday");    
    }
    else          
    {
    	// recherche du libellé du jour
        int day = messageDate.get(Calendar.DAY_OF_WEEK);
        String jour = "GML.jour" + day;
		// recherche du libellé du mois
        int month = messageDate.get(Calendar.MONTH);
        String mois = "GML.mois" + month;
       	messageDateText = resources.getString(jour)+ " " + messageDate.get(Calendar.DATE) +" " + resources.getString(mois) + " " + messageDate.get(Calendar.YEAR);
    }  
    return messageDateText;
}


public void displayForumsList(JspWriter out, com.stratelia.silverpeas.util.ResourcesWrapper resources,  
                                  boolean admin, boolean moderator, int currentForumId, String call, 
                                  com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc,
                                  String categoryId, String nom, String description) 
{
    try 
    {
	    java.util.Vector forums = null;
	    forums = scc.getForumsListByCategory(categoryId);
	    
	   if (forums != null)
	   {
		   	out.println("<tr>");
		   	out.println("<td colspan=\"5\" class=\"titreCateg\">"+nom+"</td>");
		   	
		   	//sixieme colonne
			// boutons d'admin
			if (admin||moderator) 
			{
				out.println("<td class=\"titreCateg\">");
				if (categoryId != null)
		   		{
		   			out.print(" <a href=\"EditCategory?CategoryId=" + categoryId + "\"> ");
	
					out.println("<img src=\""+ update +"\" border=\"0\" align=\"middle\" alt=\"" + resources.getString("forums.editCategory") + "\" title=\"" + resources.getString("forums.editCategory") + "\"></a>");
					out.print("&nbsp;<a href=\"javascript:confirmDeleteCategory('" + String.valueOf(categoryId) + "');\">");
					out.print("<img src=\""+ delete +"\" border=\"0\" align=\"middle\" alt=\"" + resources.getString("forums.deleteCategory") + "\" title=\"" + resources.getString("forums.deleteCategory") + "\"></a>");
					out.print("</td>");
				}
				out.println("</td></tr>");
			}
			out.println("</tr>");  
					
			scanForum(forums, resources, out, currentForumId, call, admin, moderator, currentForumId, 0, scc);
		}
	}
     catch (java.io.IOException ioe) 
     {
			SilverTrace.info("forums", "JSPforumsListManager.displayForumsList()", "root.EX_NO_MESSAGE", null, ioe);
     }
}
  
public void displayForums(JspWriter out, com.stratelia.silverpeas.util.ResourcesWrapper resources,
                                  boolean admin, boolean moderator, int currentForumId, String call, 
                                  com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc, String categoryId) 
{
	    java.util.Vector forums = null;
	    forums = scc.getForumsListByCategory(categoryId);
	      				
		scanForum(forums, resources, out, currentForumId, call, admin, moderator, currentForumId, 0, scc);
}

public void aide(JspWriter out, com.stratelia.silverpeas.util.ResourcesWrapper resources)
{
  	try
  	{
 		out.println("<img src=\"icons/buletColoredGreen.gif\">" + resources.getString("forums.notNewMessageVisite") );
		out.println("<br/>");
		out.println("<img src=\"icons/buletRed.gif\">" + resources.getString("forums.newMessageVisite"));
	}
    catch (java.io.IOException ioe) {
		SilverTrace.info("forums", "JSPforumsListManager.aide()", "root.EX_NO_MESSAGE", null, ioe);
    }
}

public void scanForum(java.util.Vector forums, com.stratelia.silverpeas.util.ResourcesWrapper resources,JspWriter out, 
                          int currentPage, String call, boolean admin, boolean moderator, int currentForumId, int depth, 
                          com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc) {
    int i;
    for (i=0; i<forums.size(); i++) 
    {
      java.util.Vector line = (java.util.Vector) forums.elementAt(i);
      int forumParent = ((Integer) line.elementAt(4)).intValue();
      if (forumParent==currentForumId) {
        int forumId = ((Integer) line.elementAt(0)).intValue();
        boolean hasChilds = hasChilds(forums, forumId);
        boolean isDeployed = scc.forumIsDeployed(forumId);
        
        displayForumLine(line, resources, out, currentPage, call, admin, moderator, depth, hasChilds, isDeployed, scc);
        if ((hasChilds)&&(isDeployed)) scanForum(forums, resources, out, currentPage, call, admin, moderator, forumId, depth+1, scc);
      }
    }
}

public boolean hasChilds(java.util.Vector forums, int currentForumId) {
    boolean retour = false;
    int i = 0;
    while ((i<forums.size())&&(!retour)) {
       java.util.Vector line = (java.util.Vector) forums.elementAt(i);
       int forumParent = ((Integer) line.elementAt(4)).intValue();
       retour = (forumParent == currentForumId);
       i++;
    }
    return retour;
}

public void displayForumsAdminButtons(boolean admin, boolean moderator,
	com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane operationPane, String currentFolderId, 
	String call, com.stratelia.webactiv.util.ResourceLocator message) 
{
      operationPane.addOperation(addForum, message.getString("newForum") , actionURL(admin, call, "editForumInfo", "1", currentFolderId, currentFolderId));
}
  
public void displayForumsAdminButtonsMain(boolean admin, boolean moderator,
	com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane operationPane, String currentFolderId, 
	String call, com.stratelia.webactiv.util.ResourceLocator message) 
{
      operationPane.addOperation(addForum, message.getString("newForum") , actionURL(admin, call, "editForumInfo", "1", currentFolderId, currentFolderId));
      operationPane.addOperation(addCategory, message.getString("forums.addCategory") , "NewCategory");
}

%>
<%--
 **********************
 *** Fin du fichier ***
 **********************
--%>