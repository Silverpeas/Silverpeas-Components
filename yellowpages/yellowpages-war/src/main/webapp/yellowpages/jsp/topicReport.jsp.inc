<%@page import="com.silverpeas.util.EncodeHelper"%>
<%!
String displayPath(YellowpagesSessionController Scc, Collection path, boolean linked, int beforeAfter) {
      String linkedPathString = new String();
      String pathString = new String();
	  String nodeName = "";
	  String bundleName = "";
      int nbItemInPath = path.size();
      Iterator iterator = path.iterator();
      boolean alreadyCut = false;
      int i = 0;
      while (iterator.hasNext()) {
            NodeDetail nodeInPath = (NodeDetail) iterator.next();
            if ((i <= beforeAfter) || (i + beforeAfter >= nbItemInPath - 1)){
				nodeName = EncodeHelper.javaStringToHtmlString(nodeInPath.getName());
				bundleName = Scc.getString(nodeName);
				// V�rifie que le nom n'est pas Corbeille
				if (bundleName != null){
					nodeName = bundleName;
				}
			if (nodeInPath.getNodePK().getId().equals("0"))
				nodeName = EncodeHelper.javaStringToHtmlString(Scc.getComponentLabel());
				linkedPathString += "<a href=\"javascript:onClick=topicGoTo('"+nodeInPath.getNodePK().getId()+"')\">"+nodeName+"</a>";
                pathString += EncodeHelper.javaStringToHtmlString(nodeInPath.getName());
                if (iterator.hasNext()) {
                      linkedPathString += " > ";
                      pathString += " > ";
                }
           } else {
                if (!alreadyCut) {
                      linkedPathString += " ... > ";
                      pathString += " ... > ";
                      alreadyCut = true;
                }
           }
           i++;
      }
      if (linked)
          return linkedPathString;
      else
          return pathString;
}

TopicDetail displayTopicsToAdmin(YellowpagesSessionController yellowpagesScc, String id, String separator, GraphicElementFactory gef, PageContext pageContext, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException, Exception {
	return displayTopicsToAdmin(yellowpagesScc, id, separator, gef, pageContext, request, session, resources, out, true);
}

TopicDetail displayTopicsToAdmin(YellowpagesSessionController yellowpagesScc, String id, String separator, GraphicElementFactory gef, PageContext pageContext, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, ResourcesWrapper resources, JspWriter out, boolean displayOperations) throws IOException, Exception {
      TopicDetail CurrentTopic = yellowpagesScc.getTopic(id);
      NodeDetail nodeDetail = CurrentTopic.getNodeDetail();
      Collection path = CurrentTopic.getPath();

      if (nodeDetail.getChildrenNumber() > 0 ) {
        ArrayPane arrayPane = gef.getArrayPane("topicsList", "topicManager.jsp?Action=Search&Id="+id, request, session);
        ArrayColumn arrayColumn1 = arrayPane.addArrayColumn("&nbsp;");
        arrayColumn1.setSortable(false);
        arrayPane.addArrayColumn(yellowpagesScc.getString("Theme"));
        arrayPane.addArrayColumn(yellowpagesScc.getString("Nb"));
        arrayPane.addArrayColumn(resources.getString("GML.description"));
        
        ArrayColumn arrayColumn2 = null;
        if (displayOperations)
        {
        	arrayColumn2 = arrayPane.addArrayColumn(yellowpagesScc.getString("Operations"));
        	arrayColumn2.setSortable(false);
        }

        NodeDetail node = null;
        String childId;
        String childName;
        String childDescription;
        String nbContact;
        String link;
        Collection nodes = (Collection) nodeDetail.getChildrenDetails();
        Iterator iteratorN = nodes.iterator();
        Collection nbContactByTopic = CurrentTopic.getNbContactByTopic();
        Iterator iteratorNbContact = nbContactByTopic.iterator();
        while (iteratorN.hasNext()) {
                node = (NodeDetail) iteratorN.next();
                nbContact = "?";
                if (iteratorNbContact.hasNext())
                    nbContact = ((Integer) iteratorNbContact.next()).toString();
                childId = node.getNodePK().getId();
                if (childId.equals("1") || childId.equals("2"))
                {
                	//Do not display the basket and the DZ in the list
                }
                else
                {
	                childName 			= node.getName();
	                childDescription 	= node.getDescription();
	                
	                IconPane 	actionPane 	= gef.getIconPane();
					IconPane 	folderPane 	= gef.getIconPane();
					Icon 		folder 		= folderPane.addIcon();
	                if (childId.startsWith("group_"))
	                {
	                	link = "GoToGroup?Id="+childId;
	                	folder.setProperties(resources.getIcon("yellowpages.group"), "", link);
	                	
	                	Icon expleIcon2 = actionPane.addIcon();
						expleIcon2.setProperties(resources.getIcon("yellowpages.delete"), yellowpagesScc.getString("SupprimerSousTheme")+" '"+Encode.javaStringToHtmlString(childName)+"'", "javascript:onClick=groupDeleteConfirm('"+childId+"', '"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(childName))+"')");
	                }
	                else
	                {
	                	link = "javascript:onClick=topicGoTo('"+childId+"')";
		                folder.setProperties(resources.getIcon("yellowpages.folder"), "", link);
	                	
	                	Icon expleIcon1 = actionPane.addIcon();
						expleIcon1.setProperties(resources.getIcon("yellowpages.update"), yellowpagesScc.getString("ModifierSousTheme")+" '"+Encode.javaStringToHtmlString(childName)+"'", "javascript:onClick=toAddOrUpdateFolder('ToUpdateFolder', '"+childId+"')");
						Icon expleIcon2 = actionPane.addIcon();
						expleIcon2.setProperties(resources.getIcon("yellowpages.delete"),  yellowpagesScc.getString("SupprimerSousTheme")+" '"+Encode.javaStringToHtmlString(childName)+"'", "javascript:onClick=topicDeleteConfirm('"+childId+"', '"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(childName))+"')");
	                }
	                
	                
	                ArrayLine arrayLine = arrayPane.addArrayLine();
					arrayLine.addArrayCellIconPane(folderPane);
	                arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(childName), link);
	                ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(nbContact);
	                arrayCellText1.setCompareOn(new Integer(nbContact));
	                arrayLine.addArrayCellText(Encode.javaStringToHtmlString(childDescription));
	                
	                if (displayOperations)
						arrayLine.addArrayCellIconPane(actionPane);
				}
          } //fin du while
          out.println(arrayPane.print());
      } else {
          out.println("");
      }
      return CurrentTopic;
}

TopicDetail displayTopicsToUsers(YellowpagesSessionController yellowpagesScc, String id, String separator,String profile,GraphicElementFactory gef, PageContext pageContext, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException, Exception {
      return displayTopicsToAdmin(yellowpagesScc, id, separator, gef, pageContext, request, session, resources, out, false);
}

TopicDetail displaySimpleTopicsToUsers(YellowpagesSessionController yellowpagesScc, String id, JspWriter out) throws IOException, Exception  {
      TopicDetail CurrentTopic = yellowpagesScc.getTopic(id);
      NodeDetail nodeDetail = CurrentTopic.getNodeDetail();
      String nodeName = Encode.javaStringToHtmlString(nodeDetail.getName());

        out.println("<select name=\"selectSimpleTopic\" onChange=\"simpleTopicGoToSelected()\">");
        out.println("<option value=\""+nodeDetail.getNodePK().getId()+"\" selected>"+nodeName+"</option>");
    if (nodeDetail.getChildrenNumber() > 0 ) {
        boolean peer = true;
        NodeDetail node = null;
        String childId;
        String childName;
        int nbContact;
        Collection nodes = (Collection) nodeDetail.getChildrenDetails();
        Iterator iteratorN = nodes.iterator();
        Collection nbContactByTopic = CurrentTopic.getNbContactByTopic();
        Iterator iteratorNbContact = nbContactByTopic.iterator();
        while (iteratorN.hasNext()) {
            node = (NodeDetail) iteratorN.next();
            nbContact = -1;
            if (iteratorNbContact.hasNext())
                nbContact = ((Integer) iteratorNbContact.next()).intValue();
            childId = node.getNodePK().getId(); 
            childName = node.getName();
            switch (new Integer(childId).intValue()){
              case 1 : //Basket -- Do not display the basket in the list
                    break;
              case 2 : //DZ -- Do Not display the DZ in the list
                    break;
              default : //Others
                out.println("<option value=\""+childId+"\">&nbsp;-&nbsp;"+Encode.javaStringToHtmlString(childName)+"</option>");
                    break;
            } //fin du switch
          } //fin du while   
    }
        out.println("</select>");
        return null;
}
%>