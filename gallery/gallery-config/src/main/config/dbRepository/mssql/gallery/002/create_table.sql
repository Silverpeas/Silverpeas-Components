CREATE TABLE SC_Gallery_Photo 
(
	photoId			int		NOT NULL,
	title			varchar (255)	NOT NULL,
	description		varchar(255)	NULL,
	sizeH			int		NULL,
	sizeL			int		NULL,
	creationDate		varchar(10)	NOT NULL,
	updateDate		varchar(10)	NULL,
	vueDate			varchar(10)	NULL,
	author			varchar(50)	NULL,
	download		int		NULL,
	albumLabel		int		NULL,
	status			char(1)		NULL,
	albumId			varchar(50)	NOT NULL,
	creatorId		varchar(50)	NOT NULL,
	updateId		varchar(50)	NULL,
	instanceId		varchar(50)     NOT NULL,
	imageName		varchar(50)	NULL,
	imageSize		int		NULL,
	imageMimeType		varchar(20)	NULL,
	beginDate		varchar(10)	NULL,
	endDate			varchar(10)	NULL
) 
;
