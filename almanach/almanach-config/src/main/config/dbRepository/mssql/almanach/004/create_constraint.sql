ALTER TABLE SC_Almanach_Event WITH NOCHECK ADD  
	 CONSTRAINT PK_Almanach_Event PRIMARY KEY CLUSTERED 
	(
		eventId
	)   
;

ALTER TABLE SC_Almanach_Periodicity WITH NOCHECK ADD 
	 CONSTRAINT PK_Almanach_Periodicity PRIMARY KEY CLUSTERED 
	(
		id
	)   
;

ALTER TABLE SC_Almanach_PeriodicityExcept WITH NOCHECK ADD 
	 CONSTRAINT PK_Almanach_PeriodicityExcept PRIMARY KEY CLUSTERED 
	(
		id
	)   
;

