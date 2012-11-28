CREATE TABLE SC_Classifieds_Classifieds
(
	ClassifiedId	int		NOT NULL,
	instanceId	varchar(50)	NOT NULL,
	title		varchar(255)	NOT NULL,
	description	varchar(2000)	NOT NULL,
	price		int 			NULL,
	creatorId	varchar(50)	NOT NULL,
	creationDate	varchar(13)	NOT NULL,
	updateDate	varchar(13)	NULL,
	status		varchar(50)	NOT NULL,
	validatorId	varchar(50)	NULL,
	validateDate	varchar(13)	NULL
) 
;

CREATE TABLE SC_Classifieds_Images
(
	imageId		int		NOT NULL,
	classifiedId int	NOT NULL,
	imageName		varchar(255)	NOT NULL,
	mimeType 	 varchar(100)	NOT NULL
) 
;

CREATE TABLE SC_Classifieds_Subscribes
(
	SubscribeId	int		NOT NULL,
	userId		varchar(50)	NOT NULL,
	instanceId	varchar(50)	NOT NULL,
	field1		varchar(100)	NOT NULL,
	field2		varchar(100)	NOT NULL
);
