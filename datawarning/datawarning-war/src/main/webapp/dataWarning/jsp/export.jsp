<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
		
		// mise � jour de la description
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