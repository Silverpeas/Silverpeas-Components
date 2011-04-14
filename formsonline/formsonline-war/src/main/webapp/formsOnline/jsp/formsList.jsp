<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="check.jsp" %>

<%@page import="java.util.List"%>
<%@page import="com.silverpeas.formsonline.model.FormDetail"%>
<%@page import="com.silverpeas.util.StringUtil"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<%!
	//icones
	String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	String NotPublished=iconsPath+"/util/icons/lock.gif";
	String Published=iconsPath+"/util/icons/unlock.gif";
	String Create=iconsPath+"/util/icons/formManager_to_add.gif";
	String Suppr=iconsPath+"/util/icons/delete.gif";

	/**
	 * @param text The string to truncate if its size is greater than the maximum length given as
	 * 		  parameter.
	 * @param maxLength The maximum length required.
	 * @return The truncated string followed by '...' if needed. Returns the string itself if its
	 * 		   length is smaller than the required maximum length.
	 */
	String truncate(String text, int maxLength)
	{
		if (text == null || text.length() <= maxLength)
		{
			return text;
		}
		else if (maxLength <= 3)
		{
			return "...";
		}
		else
		{
			return text.substring(0, maxLength - 3) + "...";
		}
	}
	
%>

<%
	out.println(gef.getLookStyleSheet());
%>

	<script type="text/javascript">
	    function deleteForm(idModel) {    
	         if (window.confirm("<%=resource.getString("formsOnline.deleteFormConfirm")%>")) { 
	            document.deleteForm.formId.value = idModel;
	            document.deleteForm.submit();
	         }
	    }
	</script>

</head>
<body>

<form name="deleteForm" action="DeleteForm">
  <input type="hidden" name="formId"/>
</form>

<%
    List formsList = (List) request.getAttribute("formsList");
  
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
    
    TabbedPane tabbedPane = gef.getTabbedPane(1);
    tabbedPane.addTab(resource.getString("formsOnline.formsList"), "Main", true,1);  
    tabbedPane.addTab(resource.getString("formsOnline.outbox"), "OutBox", false,1);
    tabbedPane.addTab(resource.getString("formsOnline.inbox"), "InBox", false,1);
   
    ArrayPane arrayPane = gef.getArrayPane("Forms", "Main", request, session);
    arrayPane.setVisibleLineNumber(10);
    arrayPane.setTitle(resource.getString("formsOnline.formsList"));
    arrayPane.addArrayColumn(resource.getString("GML.name"));
    arrayPane.addArrayColumn(resource.getString("GML.description"));
    ArrayColumn arrayColumnState = arrayPane.addArrayColumn(resource.getString("GML.operation"));
    arrayColumnState.setSortable(false);

    FormDetail form;
    int i=0;
    while (i < formsList.size()) {
	    form = (FormDetail) formsList.get(i);           
	
	    /* ecriture des lignes du tableau */
	    String formId = String.valueOf(form.getId());//idForm externe (FormDesigner)
	    String nom = truncate( form.getName(), 40);
	    String description = truncate( form.getDescription(), 80 );
	    int state = form.getState();
	    boolean alreadyUsed = form.isAlreadyUsed();
	
	    ArrayLine arrayLine = arrayPane.addArrayLine();
		IconPane iconPane = gef.getIconPane();

		arrayLine.addArrayCellLink(nom, "EditForm?formId="+formId);
        arrayLine.addArrayCellText(description);       
        
        Icon deleteIcon = null;
        Icon publishIcon = null;
		
	    switch (state) {
	    	case FormDetail.STATE_NOT_YET_PUBLISHED :

		        publishIcon = iconPane.addIcon();
                publishIcon.setProperties(NotPublished, resource.getString("formsOnline.publishForm"), "PublishForm?formId="+formId);
	                
	            deleteIcon = iconPane.addIcon();
	            deleteIcon.setProperties(Suppr, resource.getString("formsOnline.deleteForm"), "javaScript:deleteForm('"+formId+"')");
		        break;
				
	    	case FormDetail.STATE_PUBLISHED :
			    Icon depublishIcon = iconPane.addIcon();
			    depublishIcon.setProperties(Published, resource.getString("formsOnline.unpublishForm"), "UnpublishForm?formId="+formId);

				if (!alreadyUsed) {//aucune fiche n'a �t� rempli, le mod�le n'a pas �t� utilis�   
				    deleteIcon = iconPane.addIcon();
				    deleteIcon.setProperties(Suppr, resource.getString("formsOnline.deleteForm"), "javaScript:deleteForm('"+formId+"')");
				}
				break;
				
	    	case FormDetail.STATE_UNPUBLISHED :
				publishIcon = iconPane.addIcon();
			    publishIcon.setProperties(NotPublished, resource.getString("formsOnline.republishForm"), "PublishForm?formId="+formId);

			    if (!alreadyUsed) {//aucune fiche n'a �t� rempli, le mod�le n'a pas �t� utilis�   
			        deleteIcon = iconPane.addIcon();
			        deleteIcon.setProperties(Suppr, resource.getString("formsOnline.deleteForm"), "javaScript:deleteForm('"+formId+"')");
			    }                               
				break;	    		
	    }		

        arrayLine.addArrayCellIconPane(iconPane);

	 	i++;
    }
      
    operationPane.addOperation(Create, resource.getString("formsOnline.createForm") , "CreateForm");
    
    frame.addTop(arrayPane.print());

    frame.addBottom("");

    window.addBody(tabbedPane.print()+frame.print());
    out.println( window.print());
	%>
</body>
</html>