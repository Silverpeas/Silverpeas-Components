<%

detailLevel =0;
try		{archives = news.getArchiveList();}
catch	(NewsEditoException e) {
	throw new NewsEditoException("init_JSP", NewsEditoException.ERROR, "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE_LIST", e);
}   

if (news.getArchiveId()!=null)
{
	detailLevel ++;
	try   {archiveDetail = news.getArchiveContent();}
	catch	(NewsEditoException e) {
		throw new NewsEditoException("init_JSP", NewsEditoException.ERROR, "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE_CONTENT", e);
	}   

	if (news.getTitleId()!=null)
	{

		detailLevel ++;
		try {titleDetail = news.getTitleDetail();}
		catch	(NewsEditoException e) {
			throw new NewsEditoException("init_JSP", NewsEditoException.ERROR, "NewsEdito.EX_PROBLEM_TO_GET_TITLE_DETAIL", e);
		}   


		if (news.getPublicationId()!=null)
		{
			detailLevel ++;
		    try {pubDetail=news.getPublicationDetail(news.getPublicationId());}
      		catch	(NewsEditoException e) {
				throw new NewsEditoException("init_JSP", NewsEditoException.ERROR, "NewsEdito.EX_PROBLEM_TO_GET_PUBLI", e);
			}

		}
	}
}

	navigationString = "";
	if (detailLevel >=1)
	{	
		// archive
		if (detailLevel ==1)
			navigationString += Encode.javaStringToHtmlString(archiveDetail.getName());
		else
			navigationString += "<A class=\"componentNav\" href=\"javascript:onClick=selectArchive('"+archiveDetail.getNodePK().getId()+"')\">"+ Encode.javaStringToHtmlString(archiveDetail.getName()) + "</A>";		
		
		if (detailLevel >=2)
		{	
			//titre 
			if (detailLevel ==2)
				navigationString += " > "+ Encode.javaStringToHtmlString(titleDetail.getName());
			else
				navigationString += " > <A class=\"componentNav\" href=\"javascript:onClick=selectTitle('"+titleDetail.getNodePK().getId()+"')\"> "+ Encode.javaStringToHtmlString(titleDetail.getName()) + "</A>";
			
			
			if (detailLevel >=3)
			{	//publication
				if (detailLevel == 3)
					navigationString += " > " + Encode.javaStringToHtmlString(pubDetail.getName());
				
			}
		}
		
	}
%>