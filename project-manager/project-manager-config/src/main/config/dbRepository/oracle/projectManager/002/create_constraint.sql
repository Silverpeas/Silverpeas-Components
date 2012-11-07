ALTER TABLE SC_ProjectManager_Tasks ADD 
	CONSTRAINT PK_ProjectManager_Tasks PRIMARY KEY  
	(
		id
	)
;
ALTER TABLE SC_ProjectManager_Calendar ADD 
	CONSTRAINT PK_ProjectManager_Calendar PRIMARY KEY  
	(
		holidayDate, fatherId, instanceId
	)
;
   