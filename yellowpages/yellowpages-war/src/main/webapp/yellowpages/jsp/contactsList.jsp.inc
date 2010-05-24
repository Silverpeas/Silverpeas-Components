<%!

void displayContactsAdmin(String contactCard, YellowpagesSessionController yellowpagesScc,String profile ,Collection contacts, boolean subtopicsExist, String contactDeleteIcon, GraphicElementFactory gef, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException {
    ContactDetail contact;
    UserContact userContact;
    UserDetail user;
    int nbContact = 0;
    int indexLastNameColumn = 1;
    boolean peer = true;
    Iterator iterator = contacts.iterator();
      
    ArrayPane arrayPane = gef.getArrayPane("tableau1", "topicManager.jsp", request, session);
    if (!"no".equalsIgnoreCase(resources.getSetting("showContactIcon")))
    {
	    ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
		arrayColumn0.setSortable(false);
		indexLastNameColumn = 2;
	}
    ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("GML.name"));
    ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("GML.surname"));
    ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("GML.eMail"));
	if (resources.getSetting("columns").contains("phone"))
    {
		ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("GML.phoneNumber"));
		arrayColumn4.setSortable(false);
	}
    if (resources.getSetting("columns").contains("fax"))
    {
	    ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resources.getString("GML.faxNumber"));
	    arrayColumn5.setSortable(false);
	}
    ArrayColumn arrayColumn6 = arrayPane.addArrayColumn(resources.getString("Operations"));
    arrayColumn6.setSortable(false);

    while (iterator.hasNext()) {
        userContact = (UserContact) iterator.next();
        contact = userContact.getContact();
        ArrayLine ligne1 = arrayPane.addArrayLine();
	    if (!"No".equalsIgnoreCase(resources.getSetting("showContactIcon")))
	    {
	        IconPane iconPane1 = gef.getIconPane();
	        Icon carte = iconPane1.addIcon();
	        carte.setProperties(contactCard, "" , "javascript:onClick=contactGoTo('"+contact.getPK().getId()+"')");    
        	ligne1.addArrayCellIconPane(iconPane1);
	    }
        ligne1.addArrayCellLink(Encode.javaStringToHtmlString(contact.getLastName()),"javascript:onClick=contactGoTo('"+contact.getPK().getId()+"')");
        ligne1.addArrayCellText(Encode.javaStringToHtmlString(contact.getFirstName()));
        ligne1.addArrayCellText(Encode.javaStringToHtmlString(contact.getEmail()));
		if (resources.getSetting("columns").contains("phone"))
		{
			ArrayCellText phoneCell = ligne1.addArrayCellText(Encode.javaStringToHtmlString(contact.getPhone()));
			phoneCell.setNoWrap(true);
		}
		if (resources.getSetting("columns").contains("fax"))
		{
			ligne1.addArrayCellText(Encode.javaStringToHtmlString(contact.getFax()));
		}

        UserDetail ownerDetail = userContact.getOwner();

        if ((profile.equals("admin")) || ((ownerDetail != null)&&(yellowpagesScc.getUserId().equals(ownerDetail.getId()))))
        {       
			IconPane iconPane = gef.getIconPane();
			Icon deleteIcon = iconPane.addIcon();
			deleteIcon.setProperties(contactDeleteIcon, resources.getString("ContactSupprimer"), "javaScript:contactDeleteConfirm('"+contact.getPK().getId()+"')");
			iconPane.setSpacing("30px");
			ligne1.addArrayCellIconPane(iconPane);
		}			   
    }
    if (arrayPane.getColumnToSort() == 0)
        arrayPane.setColumnToSort( indexLastNameColumn );       
    out.println(arrayPane.print());

}
void displayContactsUser(YellowpagesSessionController yellowpagesScc,
      Collection contacts, String id, String componentLabel,
      GraphicElementFactory gef, javax.servlet.ServletRequest request,
      javax.servlet.http.HttpSession session, ResourcesWrapper resources,
      JspWriter out)
      throws IOException {

    // fonction initialement dans contactsList.jsp.inc
    //                            --------------------

    Iterator iterator = contacts.iterator();
    int indexLastNameColumn = 1;

    ArrayPane arrayPane = null;
    if (id != null) {
      arrayPane = gef.getArrayPane("tableau1", "GoTo?Id=" + id, request,
          session);
      arrayPane
          .setVisibleLineNumber(yellowpagesScc.getNbContactPerPage());
    } else {
      arrayPane = gef.getArrayPane("tableau1", "PrintList", request,
          session);
      arrayPane.setVisibleLineNumber(-1);
    }

    // recherche des colonnes à afficher
    List arrayHeaders = yellowpagesScc.getArrayHeaders();
    Iterator it = arrayHeaders.iterator();
    while (it.hasNext()) {
      String nameHeader = (String) it.next();
      arrayPane.addArrayColumn(nameHeader);
    }

    ContactFatherDetail contactFather = null;
    ContactDetail contact = null;
    UserFull userFull = null;
    String nodeName = null;
    String fatherId = null;
    String icon = null;
    String link = null;
    iterator = contacts.iterator();
    while (iterator.hasNext()) {
      contactFather = (ContactFatherDetail) iterator.next();
      contact = contactFather.getContactDetail();
      userFull = contact.getUserFull();
      nodeName = Encode.javaStringToHtmlString(contactFather.getNodeName());
      fatherId = contactFather.getNodeId();
      if (fatherId.equals("0")) {
        nodeName = Encode.javaStringToHtmlString(componentLabel);
      } else if (fatherId.equals("1")) {
        continue;
      }
      
      // remplissage des lignes
      ArrayLine ligne = arrayPane.addArrayLine();
      
      if ("fromGroup".equals(contact.getPK().getId())) {
        icon = resources.getIcon("yellowpages.user");
        link = "javaScript:goToUser('" + contact.getUserId() + "');";
      } else {
        icon = resources.getIcon("yellowpages.contact");
        link = "javascript:onClick=contactGoToUserInTopic('"
            + contact.getPK().getId() + "','" + fatherId + "')";
      }
      IconPane iconPane = gef.getIconPane();
      Icon carte = iconPane.addIcon();
      carte.setProperties(icon, "", link);
      
      String nameColumn = "";
      String value = "";
      List properties = yellowpagesScc.getProperties();
      Iterator itp = properties.iterator();
      while (itp.hasNext()) {
        nameColumn = (String) itp.next();
        if (nameColumn.startsWith("domain.")) {
          String property = nameColumn.substring(7);
          // rechercher la valeur dans UserFull
          if (userFull != null) {
            ligne.addArrayCellText(Encode.javaStringToHtmlString(userFull.getValue(property)));
          }
        } else {
          // recherche la valeur dans ContactDetail
          if (nameColumn.equals("icon")) {
            ligne.addArrayCellIconPane(iconPane);
          }
          else {
            if (nameColumn.equals("lastname")) {
              value = contact.getLastName();
            }
            if (nameColumn.equals("firstname")) {
              value = contact.getFirstName();
            }
            if (nameColumn.equals("email")) {
              value = contact.getEmail();
            }
            if (nameColumn.equals("phone")) {
              value = contact.getPhone();
            }
            if (nameColumn.equals("fax")) {
              value = contact.getFax();
            }
	    if (nameColumn.equals("topic")) {
              value = nodeName;
            }
            ligne.addArrayCellText(Encode.javaStringToHtmlString(value)); 
          }
        } 
      } 

      
    }

    out.println(arrayPane.print());
  }
  
%>