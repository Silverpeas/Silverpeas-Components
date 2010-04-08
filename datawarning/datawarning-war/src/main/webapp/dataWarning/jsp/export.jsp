<Script language="JavaScript">
	function Driver(nameDrv,descriptionDrv,jdbcUrls)
	{
		this.nameDrv=nameDrv;
		this.descriptionDrv = descriptionDrv;
		this.jdbcUrls = jdbcUrls;
	}
	<%!
		String toStringList(Collection col)
		{
			Iterator i = col.iterator();
			String list = "";
			while (i.hasNext())
			{
				list +="\""+(String)i.next()+"\"";
				if (i.hasNext())
					list+=",";
			}
			
			return list;
		}
	%>
	<%	
		Collection drivers = dataWarningSC.getAvailableDriversNames();
		Collection driversDescriptions = dataWarningSC.getDriversDescriptions(); 
		
	%>

	drivers = new Array(<%=drivers.size()%>);
	
	<%
		Iterator di = drivers.iterator();
		Iterator dd = driversDescriptions.iterator();
		int indice = 0;
		while (di.hasNext())
		{		
			String name = (String)di.next();
			String desc = (String)dd.next();		
			Collection driversUrls = dataWarningSC.getJDBCUrlsForDriver(name);
	%>
			jdbcUrls = new Array(<%=toStringList(driversUrls)%>);
			nameDrv = <%="\""+name+"\""%>;
			descriptionDrv = <%="\""+desc+"\""%>;
			drivers[<%=indice%>] = new Driver(nameDrv,descriptionDrv,jdbcUrls);
	<%
			indice ++;
		}
	%>

	function selectDriver()
	{	
		// efface le selecte des urls
		for(i=document.processForm.JDBCUrlsSelect.options.length-1;i>=0;i--)
		{
			document.processForm.JDBCUrlsSelect.options[i]=null;
		}
		// rempli le select des urls
		for(i=0;i<drivers[document.processForm.JDBCdriverNameSelect.selectedIndex].jdbcUrls.length;i++)
			document.processForm.JDBCUrlsSelect.options[i]=new Option( drivers[document.processForm.JDBCdriverNameSelect.selectedIndex].jdbcUrls[i]);
		
		// mise à jour de la description
		document.processForm.DescriptionDrv.value=drivers[document.processForm.JDBCdriverNameSelect.selectedIndex].descriptionDrv;
	}
	
	function processUpdate()
	{
		document.processForm.JDBCdriverName.value = document.processForm.JDBCdriverNameSelect.options[document.processForm.JDBCdriverNameSelect.selectedIndex].value;
		document.processForm.JDBCurl.value = document.processForm.JDBCUrlsSelect.options[document.processForm.JDBCUrlsSelect.selectedIndex].text;
		if (isValidTextField(document.processForm.Login)== false)
		{
			document.processForm.Login.focus();
			alert("<%=messages.getString("erreurChampsTropLong")%>");
		}
		else if (isValidTextField(document.processForm.Password)== false)
		{
			document.processForm.Password.focus();
			alert("<%=messages.getString("erreurChampsTropLong")%>");
		}
		else if(document.processForm.RowLimit.value == "")
		{
			document.processForm.RowLimit.focus();
			alert("<%=messages.getString("erreurChampsVide")%>");
		}
		else if (isFinite(document.processForm.RowLimit.value)== false)
		{
			document.processForm.RowLimit.focus();
			alert("<%=messages.getString("erreurChampsNonEntier")%>");
		}
		else
		{
			document.processForm.action = "updateConnection";
			document.processForm.submit();
		}
	}
</Script>