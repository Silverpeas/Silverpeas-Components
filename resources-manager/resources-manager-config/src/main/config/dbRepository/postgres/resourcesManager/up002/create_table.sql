CREATE TABLE SC_Resources_Managers
(
	resourceId 	INT	NOT NULL, 
	managerId	INT	NOT NULL
) 
;

ALTER TABLE SC_Resources_Reservation
ADD	status		varchar (50)	NULL
;

ALTER TABLE SC_Resources_ReservedResource
ADD	status		varchar (50)	NULL
;