<%
	if (userRoleLevel.equals("publisher") || userRoleLevel.equals("admin"))
	{
		tabbedPane.addTab(resource.getString("TabConsultation"), MyDBConstants.ACTION_MAIN, (tabIndex == 1));
		tabbedPane.addTab(resource.getString("TabTable"), MyDBConstants.ACTION_TABLE_SELECTION, (tabIndex == 2));
		if (userRoleLevel.equals("admin"))
		{
			tabbedPane.addTab(
				resource.getString("TabJDBCSetting"), MyDBConstants.ACTION_CONNECTION_SETTING, (tabIndex == 3));
		}
		out.println(tabbedPane.print());
	}
%>