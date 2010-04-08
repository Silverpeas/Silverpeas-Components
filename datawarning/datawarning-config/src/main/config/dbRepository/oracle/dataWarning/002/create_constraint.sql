ALTER TABLE SC_DataWarning ADD 
	 CONSTRAINT PK_DataWarning PRIMARY KEY  
	(
		id
	)   
;

ALTER TABLE SC_DataWarning_Rel_Group ADD 
	 CONSTRAINT PK_DataWarning_Rel_Group PRIMARY KEY  
	(
		id
	)   
;

ALTER TABLE SC_DataWarning_Rel_User ADD 
	 CONSTRAINT PK_DataWarning_Rel_User PRIMARY KEY  
	(
		id
	)   
;

ALTER TABLE SC_DataWarning_Query ADD 
	 CONSTRAINT PK_DataWarning_Query PRIMARY KEY  
	(
		id
	)   
;

ALTER TABLE SC_DataWarning_Scheduler ADD 
	 CONSTRAINT PK_DataWarning_Scheduler PRIMARY KEY  
	(
		id
	)   
;
