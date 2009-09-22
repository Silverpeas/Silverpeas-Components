<%@ page import="java.io.IOException"
%><%@ page import="java.util.Calendar"
%><%@ page import="java.util.Collection"
%><%@ page import="java.util.Date"
%><%@ page import="java.util.Hashtable"
%><%@ page import="java.util.Iterator"
%><%@ page import="com.silverpeas.util.StringUtil"
%><%@ page import="com.stratelia.silverpeas.peasCore.URLManager"
%><%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace"
%><%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"
%><%@ page import="com.stratelia.silverpeas.util.SilverpeasSettings"
%><%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"
%><%@ page import="com.stratelia.webactiv.forums.forumsException.ForumsException"
%><%@ page import="com.stratelia.webactiv.forums.models.Category"
%><%@ page import="com.stratelia.webactiv.forums.models.Forum"
%><%@ page import="com.stratelia.webactiv.forums.models.Message"
%><%@ page import="com.stratelia.webactiv.forums.sessionController.ForumsSessionController"
%><%@ page import="com.stratelia.webactiv.forums.url.ActionUrl"
%><%@ page import="com.stratelia.webactiv.util.DBUtil"
%><%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"
%><%@ page import="com.stratelia.webactiv.util.ResourceLocator"
%><%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"
%><%@ page errorPage="../../admin/jsp/errorpage.jsp"
%>
<%!
    public static String IMAGE_UPDATE =         "../../util/icons/update.gif";
    public static String IMAGE_UNLOCK =         "../../util/icons/lock.gif";
    public static String IMAGE_LOCK =           "../../util/icons/unlock.gif";
    public static String IMAGE_DELETE =         "../../util/icons/delete.gif";
    public static String IMAGE_MOVE =           "../../util/icons/moveMessage.gif";
    public static String IMAGE_ADD_FORUM =      "../../util/icons/forums_to_add.gif";
    public static String IMAGE_ADD_CATEGORY =   "../../util/icons/folderAddBig.gif";
    public static String IMAGE_WORD =           "icons/word.gif";
    public static String IMAGE_NOTATION_OFF =   "../../util/icons/starEmpty.gif";
    public static String IMAGE_NOTATION_ON =    "../../util/icons/starFilled.gif";
    public static String IMAGE_NOTATION_EMPTY = "../../util/icons/shim.gif";
    
    public int getIntParameter(HttpServletRequest request, String name)
    {
        return getIntParameter(request, name, -1);
    }

    public int getIntParameter(HttpServletRequest request, String name, int defaultValue)
    {
        String param = request.getParameter(name);
        if (param != null)
        {
            param = param.trim();
            if (param.length() > 0)
            {
                return Integer.parseInt(param);
            }
        }
        return defaultValue;
    }
    
    public String convertDate(Date date, ResourcesWrapper resources)
    {
    	return resources.getOutputDateAndHour(date);
    }
    
    public void addBodyOnload(JspWriter out, ForumsSessionController fsc)
    {
    	addBodyOnload(out, fsc, "");
    }

    public void addBodyOnload(JspWriter out, ForumsSessionController fsc, String call)
    {
        try
        {
            if (call == null)
            {
                call = "";
            }
        	out.print("onload=\"");
        	out.print(call);
            if (fsc.isResizeFrame())
            {
                if (call.length() > 0 && !call.endsWith(";"))
                {
                	out.print(";");
                }
                out.print("resizeFrame();");
            }
            out.print("\"");
        }
        catch (IOException ioe)
        {
            SilverTrace.info("forums", "JSPmessagesListManager.addBodyOnload()",
                "root.EX_NO_MESSAGE", null, ioe);
        }
    }
    
    public void addJsResizeFrameCall(JspWriter out, ForumsSessionController fsc)
    {
        try
        {
            if (fsc.isResizeFrame())
            {
                out.print("resizeFrame();");
            }
        }
        catch (IOException ioe)
        {
            SilverTrace.info("forums", "JSPmessagesListManager.addJsResizeFrameCall()",
                "root.EX_NO_MESSAGE", null, ioe);
        }
    }
%>
<%
	ForumsSessionController fsc = (ForumsSessionController) request.getAttribute(
        "forumsSessionClientController");
	ResourcesWrapper resources = (ResourcesWrapper)request.getAttribute("resources");
    
	ResourceLocator resource = new ResourceLocator(
	    "com.stratelia.webactiv.forums.multilang.forumsBundle", fsc.getLanguage());

	if (fsc == null)
	{
	    // No forums session controller in the request -> security exception
	    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator()
	    	.getString("sessionTimeout");
	    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout)
	    	.forward(request, response);
	    return;
	}

	String context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");

	GraphicElementFactory graphicFactory = (GraphicElementFactory) session
		.getAttribute("SessionGraphicElementFactory");
	
	String[] browseContext = (String[]) request.getAttribute("browseContext");
	String spaceLabel = browseContext[0];
	String componentLabel = browseContext[1];
	String spaceId = browseContext[2];
	String instanceId = browseContext[3];
    
	String userId = fsc.getUserId();
	boolean isAdmin = false;
	boolean isReader = false;
	// Infos utilisateur
    String[] profiles = fsc.getUserRoles();
    String profile;
    if (profiles != null)
    {
        for (int i = 0; i < profiles.length; i++)
        {
            profile = profiles[i];
            if ("admin".equals(profile))
            {
            	isAdmin = true;
            }
            else if ("reader".equals(profile))
            {
            	isReader = true;
            }
        }
    }
    if (isReader)
    {
        isAdmin = false;
    }
%>