ALTER TABLE SC_Resources_Managers WITH NOCHECK ADD 
	 CONSTRAINT PK_Resources_Managers PRIMARY KEY CLUSTERED
	(
	resourceId,
	managerId
	)   
;
