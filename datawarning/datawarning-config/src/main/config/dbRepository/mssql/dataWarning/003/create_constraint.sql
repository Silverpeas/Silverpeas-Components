ALTER TABLE SC_DataWarning WITH NOCHECK ADD 
	 CONSTRAINT PK_DataWarning PRIMARY KEY  CLUSTERED 
	(
		id
	)   
;

ALTER TABLE SC_DataWarning_Rel_Group WITH NOCHECK ADD 
	 CONSTRAINT PK_DataWarning_Rel_Group PRIMARY KEY  CLUSTERED 
	(
		id
	)   
;

ALTER TABLE SC_DataWarning_Rel_User WITH NOCHECK ADD 
	 CONSTRAINT PK_DataWarning_Rel_User PRIMARY KEY  CLUSTERED 
	(
		id
	)   
;

ALTER TABLE SC_DataWarning_Query WITH NOCHECK ADD 
	 CONSTRAINT PK_DataWarning_Query PRIMARY KEY  CLUSTERED 
	(
		id
	)   
;

ALTER TABLE SC_DataWarning_Scheduler WITH NOCHECK ADD 
	 CONSTRAINT PK_DataWarning_Scheduler PRIMARY KEY  CLUSTERED 
	(
		id
	)   
;