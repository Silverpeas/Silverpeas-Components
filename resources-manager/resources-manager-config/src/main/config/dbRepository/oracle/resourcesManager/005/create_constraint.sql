ALTER TABLE SC_Resources_Category 
ADD CONSTRAINT PK_Resources_Category PRIMARY KEY
	(
	id
	)   
;

ALTER TABLE SC_Resources_Resource
ADD CONSTRAINT PK_Resources_Resource PRIMARY KEY
	(
	id
	)   
;

ALTER TABLE SC_Resources_Reservation
ADD CONSTRAINT PK_Resources_Reservation PRIMARY KEY
	(
	id
	)   
;

ALTER TABLE SC_Resources_ReservedResource
ADD CONSTRAINT PK_Resources_ReservedResource PRIMARY KEY
	(
	reservationId,
	resourceId	
	)   
;

ALTER TABLE SC_Resources_Managers
ADD CONSTRAINT PK_Resources_Managers PRIMARY KEY 
	(
	resourceId,
	managerId
	)   
;
