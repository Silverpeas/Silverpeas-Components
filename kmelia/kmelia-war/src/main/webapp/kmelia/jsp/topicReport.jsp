<%@ page language="java" %>

<%@ page import="java.io.IOException"%>

<%@ page import="org.silverpeas.components.kmelia.model.TopicDetail, org.silverpeas.core.node.model.NodeDetail, java.util.Collection, java.util.Iterator, org.silverpeas.components.kmelia.control.KmeliaSessionController"%>
<%@ page import="org.silverpeas.kernel.bundle.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellIconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.navigationlist.NavigationList"%>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.owasp.encoder.Encode" %>

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
					linkedPathString.append("<a href=\"javascript:onClick=topicGoTo('").append(nodeInPath.getNodePK().getId()).append("')\">").append(Encode.forHtml(nodeInPath.getName(translation))).append("</a>");
					pathString.append(Encode.forHtml(nodeInPath.getName(translation)));
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

TopicDetail displaySessionTopicsToUsers(KmeliaSessionController kmeliaScc, TopicDetail currentTopic, GraphicElementFactory gef, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, MultiSilverpeasBundle resources, JspWriter out) throws IOException {

	NodeDetail nodeDetail = currentTopic.getNodeDetail();
      
      String folderSrc = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL") + "/util/icons/component/kmeliaSmall.gif";
      
	if ("array".equals(resources.getSetting("userTopicAppearance")))
		displayTopicsToUsersAsArray(kmeliaScc, nodeDetail, folderSrc, gef, request, session, resources, out);
	else
      	displayTopicsToUsersAsNavlist(kmeliaScc, nodeDetail, folderSrc, gef, request, session, resources, out);

    return currentTopic;
}

void displayTopicsToUsersAsNavlist(KmeliaSessionController kmeliaScc, NodeDetail nodeDetail, String folderSrc, GraphicElementFactory gef, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, MultiSilverpeasBundle resources, JspWriter out) throws IOException {

		boolean		displayLinks	= URLUtil.displayUniversalLinks();
    	String		linkIcon		= resources.getIcon("kmelia.link");
		String 		translation 	= kmeliaScc.getCurrentLanguage();
	if (translation == null)
		translation = kmeliaScc.getLanguage();
      // Liste de navigation dans les th�mes
      NavigationList navList = gef.getNavigationList();
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
								String link = URLUtil.getSimpleURL(URLUtil.URL_TOPIC, childId, node.getNodePK().getInstanceId());
								universalLink = "<a class=\"sp-permalink\" href=\""+link+"\"><img src=\""+linkIcon+"\" border=\"0\" align=\"absmiddle\" alt=\""+resources.getString("kmelia.CopyTopicLink")+"\" title=\""+resources.getString("kmelia.CopyTopicLink")+"\"></a>";
							}
                            navList.addItem(Encode.forHtml(childName+nbPublis),"javascript:onClick=topicGoTo('"+childId+"')",-1,childDescription, universalLink);
                            break;
                } //fin du switch
          } //fin du while   
          out.println(navList.print());
      } else {
          out.println("");
      }
}

void displayTopicsToUsersAsArray(KmeliaSessionController kmeliaScc, NodeDetail nodeDetail, String folderSrc, GraphicElementFactory gef, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, MultiSilverpeasBundle resources, JspWriter out) throws IOException {

		boolean		displayLinks	= URLUtil.displayUniversalLinks();
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

							String universalLink = "";
							if (displayLinks)
							{
								String link = URLUtil.getSimpleURL(URLUtil.URL_TOPIC, childId, node.getNodePK().getInstanceId());
								universalLink = "&nbsp;<a class=\"sp-permalink\" href=\""+link+"\"><img src=\""+linkIcon+"\" border=\"0\" align=\"bottom\" alt=\""+resources.getString("kmelia.CopyTopicLink")+"\" title=\""+resources.getString("kmelia.CopyTopicLink")+"\"></a>";
							}
                            arrayLine.addArrayCellText("<a href=\"javascript:onClick=topicGoTo('"+childId+"')\">"+childName+"</a>"+universalLink);
							
                            arrayLine.addArrayCellText(Encode.forHtml(childDescription));
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