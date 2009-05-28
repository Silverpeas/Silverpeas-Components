<SCRIPT language="JavaScript">
var dayWin = window;

//Clipboard
function clipboardCopy() {
  parent.IdleFrame.location.href = '../..<%=quickinfo.getComponentUrl()%>copy.jsp?Id=<%=(String) request.getParameter("Id")%>';
}

//Clipboard
function ClipboardCopyAll() {
  document.quickInfoForm.action = "<%=m_context%><%=quickinfo.getComponentUrl()%>multicopy.jsp";
  document.quickInfoForm.target = "IdleFrame";
  document.quickInfoForm.submit();
}

//Clipboard
function ClipboardCopyOne() {
  document.quickInfoForm.action = "<%=m_context%><%=quickinfo.getComponentUrl()%>copy.jsp";
  document.quickInfoForm.target = "IdleFrame";
  document.quickInfoForm.submit();
}

function ClipboardPaste() {
  top.IdleFrame.document.location.replace ('<%=m_context%><%=URLManager.getURL(URLManager.CMP_CLIPBOARD)%>paste.jsp?compR=Rquickinfo&SpaceFrom=<%=quickinfo.getSpaceId()%>&ComponentFrom=<%=quickinfo.getComponentId()%>&JSPPage=quickInfoPublisher.jsp&TargetFrame=MyMain&message=REFRESH');
}

function editQuickInfo(id) {
  document.quickInfoEditForm.Id.value = id;
  document.quickInfoEditForm.Action.value = "Edit";
  document.quickInfoEditForm.submit();
}

function addQuickInfo() {
  document.quickInfoEditForm.Action.value = "Add";
  document.quickInfoEditForm.submit();
}

function quickInfoDeleteConfirm()
{
    if (window.confirm("Etes-vous sûr(e) de vouloir supprimer ces quickinfo ?")){
          document.quickInfoForm.Action.value = "ReallyRemove";
          document.quickInfoForm.submit();
    }
}


function isCorrectForm() {
     	var errorMsg = "";
     	var errorNb = 0;
     	var beginDate = document.quickInfoEditForm.BeginDate.value;
        var endDate = document.quickInfoEditForm.EndDate.value;
        var yearBegin = extractYear(beginDate, '<%=quickinfo.getLanguage()%>');
        var monthBegin = extractMonth(beginDate, '<%=quickinfo.getLanguage()%>');
	var dayBegin = extractDay(beginDate, '<%=quickinfo.getLanguage()%>');
	var yearEnd = extractYear(endDate, '<%=quickinfo.getLanguage()%>'); 
	var monthEnd = extractMonth(endDate, '<%=quickinfo.getLanguage()%>');
	var dayEnd = extractDay(endDate, '<%=quickinfo.getLanguage()%>'); 
	var beginDateOK = false;
	var endDateOK = false;

	
	if (isWhitespace(document.quickInfoEditForm.Name.value)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.title")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
        }
	
     	if (!isValidTextArea(document.quickInfoEditForm.Description)) {
     		errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.description")%>' <%=resources.getString("ContainsTooLargeText")+resources.getString("NbMaxTextArea")+resources.getString("Characters")%>\n";
           	errorNb++; 
	}  	  	
       
        if (! isWhitespace(beginDate)) {
        	if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
                	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("dateDebut")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                 	errorNb++;
        	}
        	else beginDateOK = true;
        }	
      
        if (! isWhitespace(endDate)) {
        	if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
                	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("dateFin")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                 	errorNb++;
        	}
        	else endDateOK = true;
        }
        
        if (beginDateOK && endDateOK) {
        		if (isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin)==false) {
        			errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("dateFin")%>' <%=resources.getString("MustContainsPostDateToBeginDate")%>\n";
                                errorNb++;	
        		}
        }       	       
	
     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;	
     
}

function reallyAddQuickInfo() {
	if (isCorrectForm()) {
		document.quickInfoEditForm.Action.value = "ReallyAdd";
		document.quickInfoEditForm.submit();
	}		
}

function updateQuickInfo() {
	if (isCorrectForm()) {
		document.quickInfoEditForm.Action.value = "ReallyUpdate";
		document.quickInfoEditForm.submit();
	}	
}

// PopUp center
function SP_openWindow(page,nom,largeur,hauteur,options) {
	var top=(screen.height-hauteur)/2;
	var left=(screen.width-largeur)/2;
	fenetre=window.open(page,nom,"top="+top+",left="+left+",width="+largeur+",height="+hauteur+","+options);
	fenetre.focus();
	return fenetre;
}

function selectBeginDay(beginDate) {
	chemin = "<%=m_context%><%=URLManager.getURL(URLManager.CMP_AGENDA)%>calendar.jsp?indiceForm=0&nameElem="+beginDate;
  	largeur = "180";
	hauteur = "250";
	if (!dayWin.closed && dayWin.name== "Calendrier_QI")
		dayWin.close();
  	dayWin = SP_openWindow(chemin,'Calendrier_QI',largeur,hauteur,"");
}

function selectEndDay(endDate) {
	chemin = "<%=m_context%><%=URLManager.getURL(URLManager.CMP_AGENDA)%>calendar.jsp?indiceForm=0&nameElem="+endDate;
  	largeur = "180";
	hauteur = "250";
	if (!dayWin.closed && dayWin.name== "Calendrier_QI")
		dayWin.close();
  	dayWin = SP_openWindow(chemin,'Calendrier_QI',largeur,hauteur,"");

}

function editEndDay()
{
	chemin = "<%=m_context%><%=URLManager.getURL(URLManager.CMP_AGENDA)%>calendar.jsp?indiceForm=0&indiceElem=4";
	largeur = "180";
	hauteur = "200";
	SP_openWindow(chemin,"Calendrier_Todo",largeur,hauteur,"");
}

function quickInfoDeleteConfirm()
{
    if (window.confirm("<%=resources.getString("supprimerQIConfirmation")%>")){
          document.quickInfoEditForm.Action.value = "ReallyRemove";
          document.quickInfoEditForm.submit();
    }
}

</script>