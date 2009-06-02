<%@ include file="check.jsp" %>

<%
response.setContentType("text/html");

List tasks = (List) request.getAttribute("Tasks");
String separatorCSV = ",";

String statut		= resource.getString("projectManager.TacheStatut");
String numero 		= resource.getString("projectManager.TacheNumero");
String nom 			= resource.getString("projectManager.TacheNom");
String responsable 	= resource.getString("projectManager.TacheResponsable");
String debut 		= resource.getString("projectManager.TacheDebut");
String fin 			= resource.getString("projectManager.TacheFin");
String charge 		= resource.getString("projectManager.TacheCharge");
String conso 		= resource.getString("projectManager.TacheConso");
String reste 		= resource.getString("projectManager.TacheReste");

out.println(statut+separatorCSV+numero+separatorCSV+nom+separatorCSV+responsable+separatorCSV+debut+separatorCSV+fin+separatorCSV+charge+separatorCSV+conso+separatorCSV+reste);
out.println("<BR>");

	TaskDetail task = null;
	for (int a=1; a<tasks.size(); a++)
	{
		int indCol = 0; 
		
    	task = (TaskDetail) tasks.get(a);
    	
    	switch (task.getStatut())
		{
			case 0 : statut = resource.getString("projectManager.TacheStatutEnCours");
					 break;
			case 1 : statut = resource.getString("projectManager.TacheStatutGelee");
					 break;
			case 2 : statut = resource.getString("projectManager.TacheStatutAbandonnee");
					 break;
			case 3 : statut = resource.getString("projectManager.TacheStatutRealisee");
					 break;
			case 4 : statut = resource.getString("projectManager.TacheStatutEnAlerte");
					 break;    						     						     						 
		}
		
		numero 		= new Integer(task.getChrono()).toString();
		nom 		= task.getNom();
		responsable = task.getResponsableFullName();
		debut 		= task.getUiDateDebut();
		fin 		= task.getUiDateFin();
		charge 		= new Float(task.getCharge()).toString();
		conso 		= new Float(task.getConsomme()).toString();
		reste 		= new Float(task.getRaf()).toString();
    	
    	out.println(statut+separatorCSV+numero+separatorCSV+nom+separatorCSV+responsable+separatorCSV+debut+separatorCSV+fin+separatorCSV+charge+separatorCSV+conso+separatorCSV+reste);
		out.println("<BR>");
	}

out.println("<script language=\"Javascript\">alert(\'"+resource.getString("projectManager.CSV")+"\');</script>");
%>