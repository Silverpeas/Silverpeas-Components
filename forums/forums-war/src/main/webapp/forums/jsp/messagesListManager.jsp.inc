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
<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%!
//Icons
String deplaceMsg = "../../util/icons/moveMessage.gif";

  public String actionURL2(boolean admin, String page, String action, String params, String forum) {
    // String retour = "/forums/forums/jsp/" + page + ".jsp?";
    String retour = page + ".jsp?";
    if (admin) retour = retour + "admin=1";
    else retour = retour + "admin=0";
    if (action.length()>0) retour = retour + "&action=" + action;
    if (params.length()>0) retour = retour + "&params=" + params;
    if (forum.length()>0) retour = retour + "&forum=" + forum;
    return retour;
  }

  public String actionURL2(boolean admin, String call, String page, String action, String params, String forum) {
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

  public void displayMessageLine(java.util.Vector line, JspWriter out, com.stratelia.webactiv.util.ResourceLocator message, String userId, 
                            boolean admin, boolean moderator, boolean view, int depth, boolean hasChilds, boolean deployed, boolean forumActive,
                            String call, com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc, ResourcesWrapper resources) {
    try {
      int i;
      String messageId = (String) line.elementAt(0);
      String messageTitle = (String) line.elementAt(1);
      String messageAuthor = scc.getAuthorName((String) line.elementAt(2));
      String messageParent = (String) line.elementAt(5);
      if (messageAuthor==null) messageAuthor = message.getString("inconnu");
      String forumId = (String) line.elementAt(4);
      Date messageCreationDate = new Date(Long.parseLong( (String) line.elementAt(3) ));
      boolean isSuscriber = scc.isSuscriber(messageId, userId);

      out.println("<tr>");
			// abonnement
			out.print("<td>");
			if (isSuscriber) {
				out.print("<a href=\"");
				if (view) {
					out.print(actionURL2(admin, call, "viewForum", "13", messageId, forumId));
				}
				else {
					out.print(actionURL2(admin, call, "viewMessage", "13", messageId, forumId));
				}
				out.print("\"><img src=\"icons/abonn_message.gif\" border=\"0\" alt=\"" + message.getString("unsuscribeMessage") + "\" title=\"" + message.getString("unsuscribeMessage") + "\"></a>");
			} else {
					out.print("&nbsp;");
			}
			out.print("</td>");


			// icone de deploiement
      if (hasChilds) {
			out.print("<td>");
			if (depth>0) {
				out.print("<img src=\"icons/1px.gif\" width=\"" + depth*10 + "\" height=\"1\">");
			}
			out.print("<a href=\"");
			if (deployed) {
				if (view) {
						out.print(actionURL2(admin, call, "viewForum", "11", messageId, forumId));
				}
				else {
						out.print(actionURL2(admin, call, "viewMessage", "11", messageId, forumId));
				}
				out.print("\">");
				//out.print("<img src=\"icons/fo_flechebas.gif\" border=\"0\" width=\"11\" height=\"6\">");
			}
			else {
				if (view) {
						out.print(actionURL2(admin, call, "viewForum", "10", messageId, forumId));
				}
				else {
						out.print(actionURL2(admin, call, "viewMessage", "10", messageId, forumId));
				}
				out.print("\"><img src=\"icons/topnav_r.gif\" border=\"0\" width=\"6\" height=\"11\">");
			}
			out.print("</a>");
		}
		else {
			out.print("<td>");
			if (depth>0) {
				out.print("<img src=\"icons/1px.gif\" width=\"" + depth*10 + "\" height=\"1\">");
			}
		}
     
     // rechercher si l'utilisateur a des messages non lu sur ce sujet
     if (messageParent.equals("0"))
     {
		boolean isNewMessage = scc.isNewMessage(scc.getUserId(), forumId, messageId);
		if (isNewMessage)
			out.print("<img src=\"icons/buletRed.gif\">");
		else
			out.print("<img src=\"icons/buletColoredGreen.gif\">");
	  }
     
      // Titre du message
      out.print("&nbsp;<span class=\"txtnote\">");
      // out.print("<a href=\"javascript:ouvre('" + actionURL2(admin, "viewMessage", "1", messageId, forumId) + "');\">");
      out.print("<a href=\"" + actionURL2(admin, call, "viewMessage", "1", messageId, forumId) + "\">");
      out.print(Encode.javaStringToHtmlString(messageTitle) + "</a>");

	  // Auteur du message
      out.print("&nbsp;(" + messageAuthor);

		//try
		//{
			// Date de Creation
			String date = convertDate(messageCreationDate, resources);
      		out.print("&nbsp;-&nbsp;" + date);
		//}
		//catch (java.text.ParseException pe)
		//{
			//SilverTrace.error("forums", "JSPmessagesListManager.displayMessageLine()", "root.EX_NO_MESSAGE", "messageCreationDate = "+messageCreationDate, pe);
		//}
      
      
      out.print(")");

      out.print("</span></td>");

      // icones
      out.print("<td>");
      if (admin||moderator) {
	if (messageParent.equals("0")) {
		out.print("<a href=\"editMessage.jsp?action=3&params=");
		out.print(messageId);
		out.print("&call=" + call);
		out.print("&forum=" + forumId);
		out.print("\"><img src="+ deplaceMsg +" align=\"middle\" border=\"0\" alt=\"" + message.getString("moveMessage") + "\" title=\"" + message.getString("moveMessage") + "\"></a>");
		out.print("&nbsp;");
	}
        out.print("<a href=\"javascript:confirmDeleteMessage('" + messageId + "')\">");
	out.print("<img src="+ delete +" align=\"middle\" border=\"0\" alt=\"" + message.getString("deleteMessage") + "\" title=\"" + message.getString("deleteMessage") + "\"></a>");
      }
      else out.print("&nbsp;");
      out.print("&nbsp;");
      out.print("</td>");
      out.println("</tr>");

	out.println("<!-- SEPARATEUR DE LIGNE -->");
	out.println("<tr>");
	out.println("<td colspan=\"3\" align=\"center\" bgcolor=\"#CCCCCC\"><img src=\"icons/1px.gif\" width=\"1\" height=\"1\"></td>");
	out.println("</tr>");
    }
    catch (java.io.IOException ioe) {
		SilverTrace.info("forums", "JSPmessagesListManager.displayMessageLine()", "root.EX_NO_MESSAGE", null, ioe);
    }
  }


  public void displayMessagesList(JspWriter out, com.stratelia.webactiv.util.ResourceLocator message, String userId,
                                  boolean admin, boolean moderator, boolean view, String currentForumId, String call,  
                                  com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc, ResourcesWrapper resources) {
    try {
        java.util.Vector messages = scc.getMessagesList(currentForumId);
			out.println("<!-- TABLEAU DES MESSAGES -->");
			out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
			out.println("<!-- ENTETE TABLEAU -->");
			out.println("<tr>");
			out.println("<td colspan=\"3\" align=\"center\" bgcolor=\"#999999\"><img src=\"icons/1px.gif\" width=\"1\" height=\"1\"></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=\"3\" align=\"center\" bgcolor=\"#FFFFFF\"><img src=\"icons/1px.gif\" width=\"1\" height=\"1\"></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.print("<td colspan=\"3\" class=\"intfdcolor52\"><span class=\"txttitrecol\">");
			out.println(message.getString("forums.nbMessages") + "</span></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=\"3\" align=\"center\" bgcolor=\"#999999\"><img src=\"icons/1px.gif\" width=\"1\" height=\"1\"></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=\"3\" align=\"center\" bgcolor=\"#333333\"><img src=\"icons/1px.gif\" width=\"1\" height=\"1\"></td>");
			out.println("</tr>");
			out.println("<!-- FIN ENTETE TABLEAU -->");
			if (messages.size()>0) {
				scanMessage(messages, out, message, userId, currentForumId, admin, moderator, view, "0", 0, call, scc, resources);
			}
			else {
				out.println("<tr><td colspan=\"3\" align=center><span class=\"txtnote\">" + message.getString("noMessages") + "</span></td></tr>");
			}
			out.println("</table>");
			out.println("<!-----------------------------------------------------[ END HTML ]-->");
    }
    catch (java.io.IOException ioe) {
		SilverTrace.info("forums", "JSPmessagesListManager.displayMessagesList()", "root.EX_NO_MESSAGE", null, ioe);
	} 
  }

  public void displaySingleMessageList(JspWriter out, com.stratelia.webactiv.util.ResourceLocator message, String userId,
                                  boolean admin, boolean moderator, boolean view, String currentForumId, String messageId, String call,
                                  com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc, ResourcesWrapper resources) {
    try {
        java.util.Vector messages = scc.getMessagesList(currentForumId);
	String messageParent = (String) scc.getMessageInfos(messageId).elementAt(5);
	int parent = Integer.parseInt(messageParent);
	while (parent>0) {
	    messageId = messageParent;
	    messageParent = (String) scc.getMessageInfos(messageId).elementAt(5);
	    parent = Integer.parseInt(messageParent);
	}
        if (messages.size()>0) {
	  displayOneMessage(messages, out, message, userId, currentForumId, admin, moderator, view, messageId, 0, call, scc, resources);
          scanMessage(messages, out, message, userId, currentForumId, admin, moderator, view, messageId, 1, call, scc, resources);
        }
        else {
          out.println("<tr><td colspan=\"3\" align=center><span class=\"txtnav\">" + message.getString("noMessages") + "</span></td></tr>");
        }
    }
    catch (java.io.IOException ioe) {
		SilverTrace.info("forums", "JSPmessagesListManager.displaySingleMessageList()", "root.EX_NO_MESSAGE", null, ioe);
    }
  }

  public void scanMessage(java.util.Vector messages, JspWriter out, com.stratelia.webactiv.util.ResourceLocator message, String userId,
                          String currentPage, boolean admin, boolean moderator, boolean view, String currentMessageId, int depth, 
                          String call, com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc, ResourcesWrapper resources) {
    int i;
    
    for (i=0; i<messages.size(); i++) {
        java.util.Vector line = (java.util.Vector) messages.elementAt(i);
        String messageId = (String) line.elementAt(0);
        boolean hasChilds = hasMessagesChilds(messages, messageId);
        boolean isDeployed = scc.messageIsDeployed(messageId);
        String parentId = (String) line.elementAt(5);
        if (parentId.equals(currentMessageId)) {
          displayMessageLine(line, out, message, userId, admin, moderator, view, depth, hasChilds, isDeployed, scc.isActive(currentPage), call, scc, resources);
          if ((hasChilds)&&(isDeployed)) scanMessage(messages, out, message, userId, currentPage, admin, moderator, view, messageId, depth+1, call, scc, resources);
        } 
    } 
  }
  
  public void deployAll(java.util.Vector messages, com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc)
  {
  int i;
  	for (i=0; i<messages.size(); i++) 
  	{
        java.util.Vector line = (java.util.Vector) messages.elementAt(i);
        String messageId = (String) line.elementAt(0);
        scc.deployMessage(messageId);
    } 
  }
    
  public void displayOneMessage(java.util.Vector messages, JspWriter out, com.stratelia.webactiv.util.ResourceLocator message, String userId, 
                          String currentPage, boolean admin, boolean moderator, boolean view, String currentMessageId, int depth, 
                          String call, com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc, ResourcesWrapper resources) {
    int i=0;
    boolean boucle = true;
    while ((i<messages.size())&&(boucle)) {
        java.util.Vector line = (java.util.Vector) messages.elementAt(i);
        String messageId = (String) line.elementAt(0);
        boolean hasChilds = hasMessagesChilds(messages, messageId);
        boolean isDeployed = scc.messageIsDeployed(messageId);
        String parentId = (String) line.elementAt(5);
        if (messageId.equals(currentMessageId)) {
          displayMessageLine(line, out, message, userId, admin, moderator, view, depth, hasChilds, isDeployed, scc.isActive(currentPage), call, scc, resources);
	  boucle=false;
        }
	i++;
    }
  }

  public boolean hasMessagesChilds(java.util.Vector messages, String currentMessageId) {
    boolean retour = false;
    int i = 0;
    while ((i<messages.size())&&(!retour)) {
       java.util.Vector line = (java.util.Vector) messages.elementAt(i);
       String messageParent = (String) line.elementAt(5);
       retour = (messageParent.equals(currentMessageId));
       i++;
    }
    return retour;
  }

  public void displayMessagesAdminButtons(boolean admin, boolean moderator, int forumActive, JspWriter out, int currentFolderId, String call, com.stratelia.webactiv.util.ResourceLocator message) {
    try {
      if (forumActive==1) {
        // out.print("<a href=\"/forums/forums/jsp/editMessage.jsp?admin=");
	out.print("<a href=\"editMessage.jsp?admin=");
        if (admin) out.print("1");
        else out.print("0");
        out.print("&action=1&params=");
        out.print(String.valueOf(currentFolderId));
	out.print("&call=" + call);
        out.print("&forum=");
        out.print(String.valueOf(currentFolderId));
        out.println("\"><img src=\"icons/fo_newmessage.gif\" width=\"25\" height=\"26\" border=\"0\" alt=\"" + message.getString("newMessage") + "\" title=\"" + message.getString("newMessage") + "\"></a>");
      }
    }
    catch (java.io.IOException ioe) {
		SilverTrace.info("forums", "JSPmessagesListManager.displayMessagesAdminButtons()", "root.EX_NO_MESSAGE", null, ioe);
    }
  }

%>
<%--
 **********************
 *** Fin du fichier ***
 **********************
--%>