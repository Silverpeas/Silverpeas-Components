<%@ include file="checkKmelia.jsp" %>

<%!

String displayFilePath(String path, String startPath) {

   String path_to_parse = path.substring(startPath.length(), path.length());
   StringTokenizer st = new StringTokenizer(path_to_parse, File.separator);
   String part_of_path;
   String link = startPath;
   String linkedPathString = "";
   while (st.hasMoreTokens()) {
      part_of_path = st.nextToken();
      link += File.separator + part_of_path;
      linkedPathString += " > ";
      linkedPathString += "<a href=\"javascript:onClick=fileGoTo('"+Encode.javaStringToJsString(link)+"')\">"+Encode.javaStringToHtmlString(part_of_path)+"</a>";
   }
   return linkedPathString;
}

%>

<% 
FileFolder 	folder 				= (FileFolder) request.getAttribute("Directory");
Collection 	path 				= (Collection) request.getAttribute("Path");
String		linkedPathString	= (String) request.getAttribute("LinkedPathString");

//création du chemin :
String 		chemin 		= "";
if (path != null)
{
	String 		namePath	= "";
	boolean 	suivant 	= false;
	Iterator 	itPath 		= (Iterator) path.iterator();
	
	while (itPath.hasNext()) 
	{
		String directory = (String) itPath.next();
		if (directory != null)
		{
			if (suivant) 
			{
				chemin = chemin + " > ";
				namePath = " > " + namePath;
			}
			chemin = chemin + "<a href=\"GoToDirectory?Path="+ directory + "\">" + Encode.javaStringToHtmlString(directory)+"</a>";
				
			namePath = namePath + directory;
			suivant = itPath.hasNext();
		}
	}
}
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">

</script>

</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<%
Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
Frame frame = gef.getFrame();

browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setPath(linkedPathString);
//browseBar.setPath(chemin);
//browseBar.setPath("" + displayFilePath("", folder.getPath()));

out.println(window.printBefore());
out.println(frame.printBefore());

// remplissage de l'ArrayPane avec la liste des sous répertoires
// -------------------------------------------------------------
Collection files = folder.getFolders();
if (files != null && files.size() > 0)
{
    Iterator i = files.iterator();
    String   fileName = "";
    
    ArrayPane arrayPane = gef.getArrayPane("folderList", "GoToDirectory?Path="+folder.getPath(), request, session);

    ArrayColumn columnType = arrayPane.addArrayColumn("");
    columnType.setWidth("40px");
    ArrayColumn columnName = arrayPane.addArrayColumn(resources.getString("Theme"));
    columnName.setWidth("615px");
    
    while (i.hasNext())
    {
        FileDetail file = (FileDetail) i.next();
        ArrayLine  arrayLine = arrayPane.addArrayLine();

        // icone du dossier
        IconPane icon = gef.getIconPane();
		Icon folderIcon = icon.addIcon();
		folderIcon.setProperties(resources.getIcon("kmelia.folder"), "");
   		icon.setSpacing("30px");
   		arrayLine.addArrayCellIconPane(icon);
        
        fileName = file.getName();

        arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(fileName), "GoToDirectory?Path="+file.getPath());
    }
    out.println(arrayPane.print());
}

//affichage des fichiers
//----------------------
Collection fileList = folder.getFiles();
if (fileList != null && fileList.size() > 0)
{
    Iterator itFile = fileList.iterator();
		
    ArrayPane arrayPane = gef.getArrayPane("fileList", "GoToDirectory?Path="+folder.getPath(), request, session);

    ArrayColumn columnType = arrayPane.addArrayColumn(resources.getString("GML.type"));
    columnType.setWidth("40px");
    ArrayColumn columnName = arrayPane.addArrayColumn(resources.getString("GML.name"));
    columnName.setWidth("550px");
    ArrayColumn columnSize = arrayPane.addArrayColumn(resources.getString("GML.size"));
    columnSize.setWidth("60px");
    
	FileDetail fileDetail = null;
	
	while (itFile.hasNext())
	{
		fileDetail = (FileDetail) itFile.next();

		ArrayLine  arrayLine = arrayPane.addArrayLine();
	
	    // icone du type du fichier
	    /*IconPane icon = gef.getIconPane();
		Icon fileIcon = icon.addIcon();
		fileIcon.setProperties(fileDetail.getFileIcon(), "");
		icon.setSpacing("30px");*/
		ArrayCellText cell = arrayLine.addArrayCellText("<img src=\""+fileDetail.getFileIcon()+"\" width=\"20\" height=\"20\"/>");
		cell.setCompareOn(FileRepositoryManager.getFileExtension(fileDetail.getName()));
	
	    ArrayCellLink cellLink = arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(fileDetail.getName()), fileDetail.getFileURL());
	    cellLink.setTarget("_blank");
	    
	    ArrayCellText cellSize = arrayLine.addArrayCellText(fileDetail.getFileSize());
	    cellSize.setCompareOn(new Long(fileDetail.getSize()));
	 }
	out.println(arrayPane.print());
}

out.println(frame.printAfter());
out.println(window.printAfter());

%>
<FORM name="folderDetailForm" action="viewDirectory" method="post">
<input type="hidden" name="FolderName">
<input type="hidden" name="FileName">
</FORM>
</body>
</html>