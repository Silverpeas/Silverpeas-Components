CREATE TABLE SC_Resources_Category
(
	id 				BIGINT 		NOT NULL,
	instanceId 		VARCHAR(50) NOT NULL,
	name 			VARCHAR(50) NOT NULL,
	creationdate 	VARCHAR(20) NOT NULL,
	updatedate 		VARCHAR(20) NOT NULL,
	bookable 		INT,
	form 			VARCHAR(50),
	responsibleid 	INT,
	createrid 		VARCHAR(50),
	updaterid 		VARCHAR(50),
	description 	VARCHAR(2000)
) 
;

CREATE TABLE SC_Resources_Resource
(
	id 				BIGINT NOT NULL,
	instanceId 		VARCHAR(50) NOT NULL,
	categoryid  	BIGINT NOT NULL,
	name 			VARCHAR(128) NOT NULL,
	creationdate 	VARCHAR(20) NOT NULL,
	updatedate 		VARCHAR(20) NOT NULL,
	bookable 		INT,
	responsibleid 	INT,
	createrid 		VARCHAR(50),
	updaterid 		VARCHAR(50),
	description 	VARCHAR(2000)
) 
;

CREATE TABLE SC_Resources_Reservation
(
	id 				BIGINT NOT NULL,
	instanceId 		VARCHAR(50) NOT NULL,
	evenement 		VARCHAR(128) NOT NULL,
	userId 			INT NOT NULL,
	creationdate 	VARCHAR(20) NOT NULL,
	updatedate 		VARCHAR(20) NOT NULL,
	begindate 		VARCHAR(20) NOT NULL,
	enddate 		VARCHAR(20) NOT NULL,
	reason			VARCHAR(2000),
	place 			VARCHAR(128),
    status          VARCHAR(50) NULL
)
;

CREATE TABLE SC_Resources_ReservedResource
(
	reservationId 	BIGINT NOT NULL,
	resourceId 		BIGINT NOT NULL,
    status          VARCHAR(50) NULL
) 
;

CREATE TABLE SC_Resources_Managers
(
	resourceId 	BIGINT	NOT NULL, 
	managerId	BIGINT	NOT NULL
) 
;