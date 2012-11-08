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
	beginDate		varchar(10)	DEFAULT ('0000/00/00') NOT NULL,
	endDate			varchar(10)	DEFAULT ('9999/99/99') NOT NULL,
	keyWord			varchar(1000)	NULL,
	beginDownloadDate	varchar(10)	NULL,
	endDownloadDate		varchar(10)	NULL
); 

CREATE TABLE SC_Gallery_Path 
(
	photoId			int		NOT NULL,
	nodeId			int		NOT NULL,
	instanceId		varchar(50)     NOT NULL
);

CREATE TABLE SC_Gallery_Order
(
	orderId			int		NOT NULL,
	userId			int		NOT NULL,
	instanceId		varchar(50)	NOT NULL,
	creationDate		char(13) 	NOT NULL,
	processDate		char(13)	NULL,
	processUser		int		NULL
);

CREATE TABLE SC_Gallery_OrderDetail
(
	orderId			int		NOT NULL,
	photoId			int		NOT NULL,
	instanceId		varchar(50)	NOT NULL,
	downloadDate		char(13) 	NULL,
	downloadDecision	varchar(50)	NULL
);