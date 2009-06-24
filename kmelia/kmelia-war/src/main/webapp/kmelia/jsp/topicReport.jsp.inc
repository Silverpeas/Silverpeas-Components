<%@ page language="java" %>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.webactiv.kmelia.model.TopicDetail, com.stratelia.webactiv.util.node.model.NodeDetail, java.util.Collection, java.util.Iterator, com.stratelia.webactiv.kmelia.control.KmeliaSessionController"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellIconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>

<%!
String displayPath(Collection path, boolean linked, int beforeAfter, String translation) {
      StringBuffer	linkedPathString	= new StringBuffer();
      StringBuffer	pathString			= new StringBuffer();
      int			nbItemInPath		= path.size();
      Iterator		iterator			= path.iterator();
      boolean		alreadyCut			= false;
      int			i					= 0;
	  NodeDetail	nodeInPath			= null;
      while (iterator.hasNext()) {
            nodeInPath = (NodeDetail) iterator.next();
            if ((i <= beforeAfter) || (i + beforeAfter >= nbItemInPath - 1)){
				if (!nodeInPath.getNodePK().getId().equals("0")) {
					linkedPathString.append("<a href=\"javascript:onClick=topicGoTo('").append(nodeInPath.getNodePK().getId()).append("')\">").append(Encode.javaStringToHtmlString(nodeInPath.getName(translation))).append("</a>");
					pathString.append(Encode.javaStringToHtmlString(nodeInPath.getName(translation)));
					if (iterator.hasNext()) {
						  linkedPathString.append(" > ");
						  pathString.append(" > ");
					}
				}
           } else {
                if (!alreadyCut) {
                      linkedPathString.append(" ... > ");
                      pathString.append(" ... > ");
                      alreadyCut = true;
                }
           }
           i++;
      }
	  nodeInPath = null;
      if (linked)
          return linkedPathString.toString();
      else
          return pathString.toString();
}
String displayPath(Collection path, boolean linked, int beforeAfter) {
	return displayPath(path, linked, beforeAfter, "");
}

String displayPath(Collection path, int beforeAfter) {
      return displayPath(path, true, beforeAfter);
}

TopicDetail displaySessionTopicsToAdmin(KmeliaSessionController kmeliaScc, String folderSrc, String topicUpdateIcon, String topicDeleteIcon, String infoIcon, String upIconSrc, String downIconSrc, String topicVisibleSrc, String topicInvisibleSrc, GraphicElementFactory gef, String profile, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException, RemoteException {

    	TopicDetail	currentTopic 	= kmeliaScc.getSessionTopic();
    	NodeDetail 	nodeDetail 		= currentTopic.getNodeDetail();
    	boolean		displayLinks	= URLManager.displayUniversalLinks();
		String 		translation 	= kmeliaScc.getCurrentLanguage();
		String		linkIcon		= resources.getIcon("kmelia.link");
		
		Iterator iteratorN = null;

		//Trick to not display special nodes
		Collection nodes = nodeDetail.getChildrenDetails();
		boolean haveGotChildren = (nodes.size() > 0);
		if (nodeDetail.getNodePK().getId().equals("0"))
		{
			List filteredNodes = new ArrayList();
			NodeDetail node2Filter = null;
			iteratorN = nodes.iterator();
			while (iteratorN.hasNext())
			{
				node2Filter = (NodeDetail) iteratorN.next();
				if (!node2Filter.getNodePK().getId().equals("1") && !(node2Filter.getNodePK().getId().equals("2") && node2Filter.getName().equals("Déclassées")))
					filteredNodes.add(node2Filter);
			}		
			nodes.clear();
			nodes.addAll(filteredNodes);
		}
			
      if (nodes.size() > 0) {
        ArrayPane arrayPane = gef.getArrayPane("topicsList", "GoToCurrentTopic", request, session);
        ArrayColumn arrayColumn1 = arrayPane.addArrayColumn("&nbsp;");
        arrayColumn1.setSortable(false);
		arrayColumn1.setWidth("30");
        arrayPane.addArrayColumn(kmeliaScc.getString("Theme"));
        arrayPane.addArrayColumn(kmeliaScc.getString("Description"));
             
        ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(kmeliaScc.getString("Operations"));
        arrayColumn2.setSortable(false);

		if (kmeliaScc.isOrientedWebContent() && !profile.equals("writer"))
		{
			ArrayColumn arrayColumnWeb = arrayPane.addArrayColumn(kmeliaScc.getString("OperationsWeb"));
			arrayColumnWeb.setSortable(false);
		}

		if (kmeliaScc.isSortedTopicsEnabled() && !profile.equals("writer"))
		{
			ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("GML.order"));
			arrayColumn3.setSortable(false);
		}

        NodeDetail node = null;
        String childId;
        String childName;
        String childDescription;
        String nbPublis;
        iteratorN = nodes.iterator();
		int nbChild = 0;
		int nbDisplayedNodes = nodes.size();

        while (iteratorN.hasNext()) {
                node = (NodeDetail) iteratorN.next();
                childId = node.getNodePK().getId(); 
                childName = node.getName(translation);
                childDescription = node.getDescription(translation);
                nbPublis = "";
                if (node.getNbObjects() != -1)
                	nbPublis = " ("+node.getNbObjects()+")";

                switch (new Integer(childId).intValue()){
                  case 1 : //Basket -- Do not display the basket in the list
                            break;
				  case 2 : //DZ -- Do Not display the DZ in the list
                            break;
          		default : //Others
							
							IconPane folderPane1 = gef.getIconPane();
							Icon folder1 = folderPane1.addIcon();
							folder1.setProperties(folderSrc, "");
							
							ArrayLine arrayLine = arrayPane.addArrayLine();
                            ArrayCellIconPane acip = arrayLine.addArrayCellIconPane(folderPane1);
							acip.setAlignement("center");
							
							String universalLink = "";
							if (displayLinks)
							{
								String link = URLManager.getSimpleURL(URLManager.URL_TOPIC, childId, node.getNodePK().getInstanceId());
								universalLink = "&nbsp;<a href=\""+link+"\"><img src=\""+linkIcon+"\" border=\"0\" align=\"bottom\" alt=\""+resources.getString("kmelia.CopyTopicLink")+"\" title=\""+resources.getString("kmelia.CopyTopicLink")+"\"></a>";
							}
													
							ArrayCellText cellName = null; 
							if (StringUtil.isDefined(node.getType()) && node.getType().equals(NodeDetail.FILE_LINK_TYPE))
								cellName = arrayLine.addArrayCellText("<a href=\"javascript:onClick=dirGoTo('"+childId+"')\">"+childName+"</a>");
							else
								cellName = arrayLine.addArrayCellText("<a href=\"javascript:onClick=topicGoTo('"+childId+"')\">"+childName+"</a>"+nbPublis+universalLink);
                            cellName.setCompareOn(childName.toLowerCase());
                            
                            arrayLine.addArrayCellText(Encode.javaStringToHtmlString(childDescription));

                            IconPane iconPane = gef.getIconPane();
                            if (!profile.equals("writer") || (profile.equals("writer") && node.getCreatorId().equals(kmeliaScc.getUserId())))
                            {
		                        Icon updateIcon = iconPane.addIcon();
		                        updateIcon.setProperties(topicUpdateIcon, kmeliaScc.getString("ModifierSousTheme")+" '"+Encode.javaStringToHtmlString(childName)+"'", "javascript:onClick=topicUpdate('"+childId+"')");
		                        Icon deleteIcon = iconPane.addIcon();
		                        deleteIcon.setProperties(topicDeleteIcon, kmeliaScc.getString("SupprimerSousTheme")+" '"+Encode.javaStringToHtmlString(childName)+"'", "javascript:onClick=topicDeleteConfirm('"+childId+"', '"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(childName))+"')");
		                        iconPane.setSpacing("30px");
							}
                            arrayLine.addArrayCellIconPane(iconPane);            			

							if (kmeliaScc.isOrientedWebContent() && !profile.equals("writer"))
							{
								IconPane webOperationsPane1 = gef.getIconPane();

								Icon wysiwygIcon = webOperationsPane1.addIcon();
		                        wysiwygIcon.setProperties(infoIcon, kmeliaScc.getString("TopicWysiwyg")+" '"+Encode.javaStringToHtmlString(childName)+"'", "javascript:onClick=topicWysiwyg('"+childId+"')");

								Icon statusIcon = webOperationsPane1.addIcon();

								if (node.getStatus() != null && "Visible".equals(node.getStatus()))
			                        statusIcon.setProperties(topicVisibleSrc, kmeliaScc.getString("TopicVisible2Invisible")+" '"+Encode.javaStringToHtmlString(childName)+"' "+kmeliaScc.getString("OnTheWeb"), "javascript:onClick=changeTopicStatus('"+childId+"','Invisible')");
								else
									statusIcon.setProperties(topicInvisibleSrc, kmeliaScc.getString("TopicInvisible2Visible")+" '"+Encode.javaStringToHtmlString(childName)+"' "+kmeliaScc.getString("OnTheWeb"), "javascript:onClick=changeTopicStatus('"+childId+"','Visible')");
								
								webOperationsPane1.setSpacing("30px");

                				arrayLine.addArrayCellIconPane(webOperationsPane1);
							}
							
							if (kmeliaScc.isSortedTopicsEnabled() && !profile.equals("writer"))
							{
								IconPane sortPane = gef.getIconPane();
								if (nbChild != 0) {
									Icon upIcon = sortPane.addIcon();
			                        upIcon.setProperties(upIconSrc, kmeliaScc.getString("TopicUp")+" '"+Encode.javaStringToHtmlString(childName)+"'", "javascript:onClick=topicUp('"+childId+"')");
								} else {
									Icon upIcon = sortPane.addEmptyIcon();
								}

								if (nbChild < nbDisplayedNodes-1) {
									Icon downIcon = sortPane.addIcon();
			                        downIcon.setProperties(downIconSrc, kmeliaScc.getString("TopicDown")+" '"+Encode.javaStringToHtmlString(childName)+"'", "javascript:onClick=topicDown('"+childId+"')");
								} else {
									Icon downIcon = sortPane.addEmptyIcon();
								}
								arrayLine.addArrayCellIconPane(sortPane);
							}
							
							nbChild++;

                            break;
                } //fin du switch
          } //fin du while
          out.println(arrayPane.print());
      } else {
          out.println("");
      }
      return currentTopic;
}

TopicDetail displaySessionTopicsToUsers(KmeliaSessionController kmeliaScc, TopicDetail currentTopic, GraphicElementFactory gef, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException, RemoteException {

	NodeDetail nodeDetail = currentTopic.getNodeDetail();
      
      String folderSrc = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL") + "/util/icons/component/kmeliaSmall.gif";
      
	if ("array".equals(resources.getSetting("userTopicAppearance")))
		displayTopicsToUsersAsArray(kmeliaScc, nodeDetail, folderSrc, gef, request, session, resources, out);
	else
      	displayTopicsToUsersAsNavlist(kmeliaScc, nodeDetail, folderSrc, gef, request, session, resources, out);

    return currentTopic;
}

void displayTopicsToUsersAsNavlist(KmeliaSessionController kmeliaScc, NodeDetail nodeDetail, String folderSrc, GraphicElementFactory gef, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException, RemoteException {

		boolean		displayLinks	= URLManager.displayUniversalLinks();
    	String		linkIcon		= resources.getIcon("kmelia.link");
		String 		translation 	= kmeliaScc.getCurrentLanguage();
	if (translation == null)
		translation = kmeliaScc.getLanguage();
      // Liste de navigation dans les thèmes
      NavigationList navList = gef.getNavigationList();
      navList.setTitle(nodeDetail.getName(translation));
      
      if (nodeDetail.getChildrenNumber() > 0 && !(nodeDetail.getChildrenNumber() < 3 && nodeDetail.getNodePK().getId().equals("0"))) {
        NodeDetail node = null;
        String childId;
        String childName;
        String childDescription;
        String nbPublis;
        Collection nodes = (Collection) nodeDetail.getChildrenDetails();
        Iterator iteratorN = nodes.iterator();
        while (iteratorN.hasNext()) {
                node = (NodeDetail) iteratorN.next();
                childId = node.getNodePK().getId(); 
                childName = node.getName(translation);
                childDescription = node.getDescription(translation);
                nbPublis = "";
                if (node.getNbObjects() != -1)
                	nbPublis = " ("+node.getNbObjects()+")";
                	
                switch (new Integer(childId).intValue()){
                  case 1 : //Basket -- Do not display the basket in the list
                            break;
                  case 2 : //DZ -- Do Not display the DZ in the list
                            break;
                  default : //Others
                  			String universalLink = null;
                  			if (displayLinks)
							{
								String link = URLManager.getSimpleURL(URLManager.URL_TOPIC, childId, node.getNodePK().getInstanceId());
								universalLink = "<a href=\""+link+"\"><img src=\""+linkIcon+"\" border=\"0\" align=\"absmiddle\" alt=\""+resources.getString("kmelia.CopyTopicLink")+"\" title=\""+resources.getString("kmelia.CopyTopicLink")+"\"></a>";
							}
                            navList.addItem(Encode.javaStringToHtmlString(childName+nbPublis),"javascript:onClick=topicGoTo('"+childId+"')",-1,childDescription, universalLink);
                            break;
                } //fin du switch
          } //fin du while   
          out.println(navList.print());
      } else {
          out.println("");
      }
}

void displayTopicsToUsersAsArray(KmeliaSessionController kmeliaScc, NodeDetail nodeDetail, String folderSrc, GraphicElementFactory gef, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException, RemoteException {

		boolean		displayLinks	= URLManager.displayUniversalLinks();
		String		linkIcon		= resources.getIcon("kmelia.link");
		String 		translation 	= kmeliaScc.getCurrentLanguage();
		
      if ( (nodeDetail.getChildrenNumber() > 0) && !( (nodeDetail.getChildrenNumber() < 3) && (nodeDetail.getNodePK().getId().equals("0")) ) ) {
        ArrayPane arrayPane = gef.getArrayPane("topicsList", "GoToCurrentTopic", request, session);
        ArrayColumn arrayColumn1 = arrayPane.addArrayColumn("&nbsp;");
        arrayColumn1.setSortable(false);
		arrayColumn1.setWidth("30");
        arrayPane.addArrayColumn(kmeliaScc.getString("Theme"));
        arrayPane.addArrayColumn(kmeliaScc.getString("Description"));

        NodeDetail node = null;
        String childId;
        String childName;
        String childDescription;
        Collection nodes = (Collection) nodeDetail.getChildrenDetails();
        Iterator iteratorN = nodes.iterator();
		int nbChild = 0;
		int nbDisplayedNodes = 0;

		if ("0".equals(nodeDetail.getNodePK().getId()))
			nbDisplayedNodes = nodes.size()-2;
		else
			nbDisplayedNodes = nodes.size();

        while (iteratorN.hasNext()) {
                node = (NodeDetail) iteratorN.next();
                childId = node.getNodePK().getId(); 
                childName = node.getName(translation);
                childDescription = node.getDescription(translation);
                switch (new Integer(childId).intValue()){
                  case 1 : //Basket -- Do not display the basket in the list
                            break;
				  case 2 : //DZ -- Do Not display the DZ in the list
                            break;
                  default : //Others
							
							IconPane folderPane1 = gef.getIconPane();
							Icon folder1 = folderPane1.addIcon();
							folder1.setProperties(folderSrc, "");
							
							ArrayLine arrayLine = arrayPane.addArrayLine();
                            ArrayCellIconPane acip = arrayLine.addArrayCellIconPane(folderPane1);
							acip.setAlignement("center");
							
							String universalLink = "";
							if (displayLinks)
							{
								//String link = kmeliaScc.getAutoRedirectURL()+URLEncoder.encode(node.getDefaultUrl("kmelia"));
								String link = URLManager.getSimpleURL(URLManager.URL_TOPIC, childId, node.getNodePK().getInstanceId());
								universalLink = "&nbsp;<a href=\""+link+"\"><img src=\""+linkIcon+"\" border=\"0\" align=\"bottom\" alt=\""+resources.getString("kmelia.CopyTopicLink")+"\" title=\""+resources.getString("kmelia.CopyTopicLink")+"\"></a>";
							}
                            arrayLine.addArrayCellText("<a href=\"javascript:onClick=topicGoTo('"+childId+"')\">"+childName+"</a>"+universalLink);
							
                            arrayLine.addArrayCellText(Encode.javaStringToHtmlString(childDescription));
							nbChild++;
                            break;
                } //fin du switch
          } //fin du while
          out.println(arrayPane.print());
      } else {
          out.println("");
      }
}
%>