ALTER TABLE SC_ProjectManager_Tasks WITH NOCHECK ADD 
	 CONSTRAINT PK_ProjectManager_Tasks PRIMARY KEY  CLUSTERED 
	(
		id
	)
;
ALTER TABLE SC_ProjectManager_Calendar WITH NOCHECK ADD 
	 CONSTRAINT PK_ProjectManager_Calendar PRIMARY KEY  CLUSTERED 
	(
		holidayDate, fatherId, instanceId 
	)
;
ALTER TABLE SC_ProjectManager_Resources WITH NOCHECK ADD
	CONSTRAINT PK_ProjectManager_Resource PRIMARY KEY CLUSTERED
	(
		id
	)
;