CREATE TABLE SC_ConnecteurJDBC_ConnectInfo 
(
	id			int		NOT NULL,
	JDBCdriverName		varchar (250)	NULL ,
	JDBCurl			varchar (250)	NULL ,
	login			varchar (250)	NULL ,
	password		varchar (250)	NULL ,
	SQLreq			varchar (4000)	NULL ,
	rowlimit		int		NULL ,
	instanceId		varchar (50)	NOT NULL
) 
;