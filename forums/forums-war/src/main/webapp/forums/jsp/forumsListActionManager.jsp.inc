<%--
************************************
*** forumsListActionManager.jsp  ***
*** fonctions de gestion         *** 
*** des actions liees a          ***
*** la liste des forums          ***
*** cree par F. Rageade          ***
*** 29 Septembre 2000            ***
************************************
--%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>


<%!
  public void actionManagement(javax.servlet.http.HttpServletRequest request, boolean isAdmin, boolean isModerator, String userId,
                                  com.stratelia.webactiv.util.ResourceLocator message, JspWriter out,
                                  com.stratelia.webactiv.forums.sessionController.ForumsSessionController scc) {
  String action = request.getParameter("action"); 
  String params = request.getParameter("params");
  if (action!=null) {
    if (action.equals("1")) {
      try {
        int id = Integer.parseInt(params);
        scc.deployForum(id);
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("2")) {
      try {
        int id = Integer.parseInt(params);
        scc.undeployForum(id);
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("5")) {
      try {
        int id = Integer.parseInt(params);
        if (isAdmin) scc.lockForum(id, 1);
        else if (isModerator) scc.lockForum(id, 2);
      } 
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("6")) {
      try {
        int success = 0;
        int id = Integer.parseInt(params);
        if (isAdmin) success = scc.unlockForum(id, 1);
        else if (isModerator) success = scc.unlockForum(id, 2);
        if (success==0) {
          out.println("<script language=\"Javascript\">");
          out.println("alert(\"" + message.getString("adminTopicLock") + "\");");
          out.println("</script>");
        }
      } 
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
      catch (IOException ioe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, ioe);
      }
    }
    else  if (action.equals("3")) {
      try {
        String forumName = request.getParameter("forumName").trim(); 
        String forumDescription = request.getParameter("forumDescription").trim();
        String forumFolder = request.getParameter("forumFolder").trim();
        String[] forumModerators = (String[]) request.getParameterValues("moderators");
        int forumParent = Integer.parseInt(forumFolder);
        String categoryId = request.getParameter("CategoryId").trim();
        String forumId = scc.createForum(forumName, forumDescription, userId, forumParent, categoryId);
        if (forumModerators!=null) {
          for (int i=0;i<forumModerators.length;i++) scc.addModerator(forumId, forumModerators[i].trim());
        }
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("4")) {
      try {
        int id = Integer.parseInt(params);
        scc.deleteForum(id);
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("7")) {
      try {
        String forumName = request.getParameter("forumName").trim(); 
        String forumDescription = request.getParameter("forumDescription").trim();
        String forumFolder = request.getParameter("forumFolder").trim();
        String forumId = request.getParameter("forumId").trim();
        int forumParent = Integer.parseInt(forumFolder);
        int id = Integer.parseInt(forumId);
        String[] forumModerators = (String[]) request.getParameterValues("moderators");
        scc.removeAllModerators(forumId);
        if (forumModerators!=null) {
          for (int i=0;i<forumModerators.length;i++) scc.addModerator(forumId, forumModerators[i].trim());
        }
        String categoryId = request.getParameter("CategoryId").trim();
        scc.updateForum(id, forumName, forumDescription, forumParent, categoryId);
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("8")) {
      try {
        String forumId = request.getParameter("forumId").trim(); 
        String parentId = request.getParameter("parentId");
        if (parentId==null) parentId = "0";
        else parentId = parentId.trim();
        String messageTitle = request.getParameter("messageTitle").trim();
        String messageText = request.getParameter("messageText").trim();
	String suscribe = request.getParameter("suscribeMessage");
	if ((messageTitle.length()>0)&&(messageText.length()>0)) {
		String retour = scc.createMessage(messageTitle, userId, forumId, parentId, messageText);
		if (suscribe==null) {
			suscribe = "0";
		} else {
			suscribe = "1";
			if (!retour.equals("0")) {
				scc.suscribeMessage(retour,userId);   
			}
		}
		if (Integer.parseInt(parentId)>0) scc.deployMessage(parentId);
	}
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("9")) {
      try {
       scc.deleteMessage(params);
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else if (action.equals("10")) {
      try {
        scc.deployMessage(params);
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("11")) {
      try {
        scc.undeployMessage(params);
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("12")) {
      try {
        String messageId = request.getParameter("messageId").trim(); 
        String folderId = request.getParameter("messageNewFolder").trim();
        scc.moveMessage(messageId, folderId);
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("13")) {
      try {
        scc.unsuscribeMessage(params, userId);
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
    else  if (action.equals("14")) {
      try {
        scc.suscribeMessage(params, userId);
      }
      catch (NumberFormatException nfe) {
		  SilverTrace.info("forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      }
    }
  }
}
%>
<%--
 **********************
 *** Fin du fichier ***
 **********************
--%>