CREATE TABLE SC_CRM_Contacts
(
	id 		int 		NOT NULL ,
	crmId 		varchar (50)	NOT NULL ,
	instanceId 	varchar (50) 	NOT NULL ,
	name 		varchar (255)	NOT NULL ,
	functionContact 	varchar (255) 	NOT NULL ,
	tel 		varchar (50) 	NULL ,
	email 		varchar (50) 	NULL ,
	address 	varchar (255) 	NULL ,
	active 		varchar (1) 	NOT NULL 
)
;

CREATE TABLE SC_CRM_Delivery
(
	id 		int 		NOT NULL ,
	crmId 		varchar (50)	NOT NULL ,
	instanceId 	varchar (50) 	NOT NULL ,
	deliveryDate 	char (10) 	NOT NULL ,
	element 	varchar (255) 	NOT NULL ,
	version 	varchar (50) 	NOT NULL ,
	deliveryId 	varchar (50) 	NOT NULL ,
	deliveryName 	varchar (50) 	NOT NULL ,
	contactId 	int	NOT NULL ,
	contactName varchar (50) 	NOT NULL ,
	media 		varchar (255) 	NOT NULL 
) 
;

CREATE TABLE SC_CRM_Events
(
	id 		int		NOT NULL ,
	crmId 		varchar (50)	NOT NULL ,
	instanceId 	varchar (50) 	NOT NULL ,
	eventDate 	varchar (10) 	NOT NULL ,
	eventLib 	varchar (255) 	NOT NULL ,
	actionTodo 	varchar (255)	NOT NULL ,
	userId 		varchar (50) 	NOT NULL ,
	userName 	varchar (50)	NOT NULL ,
	actionDate 	varchar (10) 	NOT NULL ,
	state 		varchar (50) 	NOT NULL 
) 
;

CREATE TABLE SC_CRM_Infos
(
	id 		int 		NOT NULL ,
	clientName 	varchar (255)	NOT NULL ,
	projectCode 	varchar (255) 	NULL ,
	instanceId 	varchar (50) 	NOT NULL 
) 
;

CREATE TABLE SC_CRM_Participants 
(
	id 		int 		NOT NULL ,
	crmId 		varchar (50)	NOT NULL ,
	instanceId 	varchar (50) 	NOT NULL ,
	userName 	varchar (255) 	NOT NULL ,
	functionParticipant 	varchar (255) 	NOT NULL ,
	email 		varchar (255) 	NULL ,
	active 		varchar (1) 	NOT NULL ,
	userId 		varchar (50)	NOT NULL 
) 
;

