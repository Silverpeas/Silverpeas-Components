ALTER TABLE SC_Almanach_Event ADD 
	 CONSTRAINT PK_Almanach_Event PRIMARY KEY
	(
		eventId
	)   
;

ALTER TABLE SC_Almanach_Periodicity ADD 
	 CONSTRAINT PK_Almanach_Periodicity PRIMARY KEY
	(
		id
	)   
;

ALTER TABLE SC_Almanach_PeriodicityExcept ADD 
	 CONSTRAINT PK_Almanach_PeriodicityExcept PRIMARY KEY
	(
		id
	)   
;

