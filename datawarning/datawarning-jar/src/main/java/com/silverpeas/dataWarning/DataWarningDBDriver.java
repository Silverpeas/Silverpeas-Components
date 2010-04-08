package com.silverpeas.dataWarning;

public class DataWarningDBDriver extends Object
{
    public String DriverUniqueID = "";
    public String DriverName = "";
    public String ClassName = "";
    public String Description = "";
	public String JDBCUrl = null;

    public DataWarningDBDriver(String dui, String dn, String cn, String d, String du)
	{
        DriverUniqueID = dui;
        DriverName=dn;
        ClassName=cn;
        Description=d;
        JDBCUrl=du;
	}
}
