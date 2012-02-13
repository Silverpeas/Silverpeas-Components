ALTER TABLE SC_Resources_Category WITH NOCHECK ADD 
	 CONSTRAINT PK_Resources_Category PRIMARY KEY CLUSTERED
	(
	id
	)   
;

ALTER TABLE SC_Resources_Resource WITH NOCHECK ADD 
	 CONSTRAINT PK_Resources_Resource PRIMARY KEY CLUSTERED
	(
	id
	)   
;

ALTER TABLE SC_Resources_Reservation WITH NOCHECK ADD 
	 CONSTRAINT PK_Resources_Reservation PRIMARY KEY CLUSTERED
	(
	id
	)   
;

ALTER TABLE SC_Resources_ReservedResource WITH NOCHECK ADD 
	 CONSTRAINT PK_Resources_ReservedResource PRIMARY KEY CLUSTERED
	(
	reservationId,
	resourceId	
	)   
;

ALTER TABLE SC_Resources_Managers WITH NOCHECK ADD 
	 CONSTRAINT PK_Resources_Managers PRIMARY KEY CLUSTERED
	(
	resourceId,
	managerId
	)   
;
