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
	if ("yes".equalsIgnoreCase(resources.getSetting("showTelNumber")))
    {
		ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("GML.phoneNumber"));
		arrayColumn4.setSortable(false);
	}
    if ("yes".equalsIgnoreCase(resources.getSetting("showFaxNumber")))
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
		if ("Yes".equalsIgnoreCase(resources.getSetting("showTelNumber")))
		{
			ArrayCellText phoneCell = ligne1.addArrayCellText(Encode.javaStringToHtmlString(contact.getPhone()));
			phoneCell.setNoWrap(true);
		}
		if ("Yes".equalsIgnoreCase(resources.getSetting("showFaxNumber")))
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

void displayContactsUser(YellowpagesSessionController yellowpagesScc, Collection contacts, String id, String componentLabel, GraphicElementFactory gef, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException {

    Iterator iterator = contacts.iterator();
    int indexLastNameColumn = 1;

	ArrayPane arrayPane = null;
	if (id != null) {
			arrayPane = gef.getArrayPane("tableau1", "GoTo?Id="+id, request, session);
			arrayPane.setVisibleLineNumber(yellowpagesScc.getNbContactPerPage());
	}
    else
    {
    	arrayPane = gef.getArrayPane("tableau1", "PrintList", request, session);
    	arrayPane.setVisibleLineNumber(-1);
    }
    	
    if (!"No".equalsIgnoreCase(resources.getSetting("showContactIcon")))
    {
	    ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
		arrayColumn0.setSortable(false);
	    indexLastNameColumn = 2;
	}
    arrayPane.addArrayColumn(resources.getString("GML.name"));
    arrayPane.addArrayColumn(resources.getString("GML.surname"));
    arrayPane.addArrayColumn(resources.getString("GML.eMail"));
	if ("Yes".equalsIgnoreCase(resources.getSetting("showTelNumber")))
	{
		ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("GML.phoneNumber"));
		arrayColumn1.setSortable(false);
	}
	if ("Yes".equalsIgnoreCase(resources.getSetting("showFaxNumber")))
	{
	    ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("GML.faxNumber"));
	    arrayColumn2.setSortable(false);
	}
	if ("Yes".equalsIgnoreCase(resources.getSetting("showTopic","Yes")))
	{
    	arrayPane.addArrayColumn(resources.getString("TopicName"));
    }

	ContactFatherDetail contactFather = null;
	ContactDetail contact = null;
	String nodeName = null;
	String fatherId = null;
	String icon = null;
	String link = null;
	String firstName = null;
	String lastName = null;
	String email = null;
	String phone = null;
	String fax = null;
    while (iterator.hasNext()) 
    {
        contactFather 	= (ContactFatherDetail) iterator.next();
        contact 		= contactFather.getContactDetail();
        nodeName 		= Encode.javaStringToHtmlString(contactFather.getNodeName());
        fatherId 		= contactFather.getNodeId();
        if (fatherId.equals("0"))
        {
            nodeName = Encode.javaStringToHtmlString(componentLabel);
        } 
        else if (fatherId.equals("1"))
        {
        	continue;
        }
        
        firstName 	= contact.getFirstName();
		lastName 	= contact.getLastName();
		email 		= contact.getEmail();
		phone 		= contact.getPhone();
		fax 		= contact.getFax();
        
        if ("fromGroup".equals(contact.getPK().getId()))
        {
        	icon = resources.getIcon("yellowpages.user");
        	link = "javaScript:goToUser('"+contact.getUserId()+"');";
        }
        else
        {
        	icon = resources.getIcon("yellowpages.contact");
        	link = "javascript:onClick=contactGoToUserInTopic('"+contact.getPK().getId()+"','"+fatherId+"')";
        }

        ArrayLine ligne1 = arrayPane.addArrayLine();
	    if (!"No".equalsIgnoreCase(resources.getSetting("showContactIcon")))
	    {
			IconPane iconPane = gef.getIconPane();
			Icon carte = iconPane.addIcon();
			carte.setProperties(icon, "", link);
			ligne1.addArrayCellIconPane(iconPane);
		}
        ArrayCellText arrayCellText1 = ligne1.addArrayCellText("<A HREF=\""+link+"\">"+Encode.javaStringToHtmlString(lastName)+"</A>");
        ArrayCellText arrayCellText2 = ligne1.addArrayCellText(Encode.javaStringToHtmlString(firstName));
        ArrayCellText arrayCellText4 = null;
        if (email==null || "".equals(email))
        {
			arrayCellText4 = ligne1.addArrayCellText("");
		}
        else
        {
			arrayCellText4 = ligne1.addArrayCellText("<a href=mailto:"+email+">"+Encode.javaStringToHtmlString(email)+"</A>");
		}
		if ("Yes".equalsIgnoreCase(resources.getSetting("showTelNumber")))
		{
			ArrayCellText phoneCell = ligne1.addArrayCellText(Encode.javaStringToHtmlString(phone));
			phoneCell.setNoWrap(true);
		}
		if ("Yes".equalsIgnoreCase(resources.getSetting("showFaxNumber")))
		{
			ligne1.addArrayCellText(Encode.javaStringToHtmlString(fax));
		}
		if ("Yes".equalsIgnoreCase(resources.getSetting("showTopic","Yes")))
		{
        	ArrayCellText arrayCellText3 = ligne1.addArrayCellText(nodeName);
        	arrayCellText3.setCompareOn((String) (nodeName.toLowerCase()));
        }
        arrayCellText1.setCompareOn((String) ((lastName == null)?"":lastName.toLowerCase()));
        arrayCellText2.setCompareOn((String) ((firstName == null)?"":firstName.toLowerCase()));
        arrayCellText4.setCompareOn((String) ((email==null)?"":Encode.javaStringToHtmlString(email.toLowerCase())));
    }   
    if (arrayPane.getColumnToSort() == 0)
    {
		arrayPane.setColumnToSort(indexLastNameColumn);
	}

    out.println(arrayPane.print()); 
}

%>