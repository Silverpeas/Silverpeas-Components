CREATE TABLE IF NOT EXISTS UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

CREATE TABLE SB_ContainerManager_Instance
(
	instanceId		int NOT NULL ,
	componentId		varchar(100) NOT NULL ,
	containerType varchar(100) NOT NULL ,
	contentType		varchar(100) NOT NULL
);

CREATE TABLE SB_ContentManager_Instance
(
	instanceId	int		NOT NULL ,
	componentId	varchar(100)	NOT NULL ,
	containerType	varchar(100)	NOT NULL ,
	contentType	varchar(100)	NOT NULL
);

CREATE TABLE SB_ContentManager_Content
(
	silverContentId			int		NOT NULL ,
	internalContentId		varchar(100)	NOT NULL ,
	contentInstanceId		int		NOT NULL,
	authorId			int		NOT NULL,
	creationDate			date		NOT NULL,
	beginDate			varchar(10)	NULL,
	endDate				varchar(10)	NULL,
	isVisible			int		NULL
);

CREATE TABLE SC_QuestionReply_Question
(
	id			int		NOT NULL,
	title			varchar (100)	NOT NULL,
	content			varchar (2000)	NULL,
	creatorId		varchar (50)	NOT NULL,
	creationDate		varchar (10)	NOT NULL,
	status 			int 		NOT NULL,
	publicReplyNumber 	int 		NOT NULL,
	privateReplyNumber 	int 		NOT NULL,
	replyNumber 		int 		NOT NULL,
	instanceId		varchar (50)	NOT NULL,
	categoryId		varchar (50)	NULL
);

CREATE TABLE SC_QuestionReply_Reply
(
	id			int		NOT NULL,
	questionId 		int 		NOT NULL,
	title			varchar (100)	NOT NULL,
	content			varchar (2000)	NULL,
	creatorId		varchar (50)	NOT NULL,
	creationDate		varchar (10)	NOT NULL,
	publicReply 		int 		NOT NULL,
	privateReply 		int 		NOT NULL
);

CREATE TABLE SC_QuestionReply_Recipient
(
	id			int		NOT NULL,
	questionId 		int		NOT NULL,
	userId			varchar (50)	NOT NULL
);

CREATE TABLE SB_Attachment_Attachment
(
	attachmentId		int		NOT NULL ,
	attachmentPhysicalName	varchar (500)	NOT NULL ,
	attachmentLogicalName	varchar (255)	NOT NULL ,
	attachmentDescription	varchar (500)	NULL ,
	attachmentType		varchar (100)	NULL ,
	attachmentSize		varchar (100)	NULL ,
	attachmentContext	varchar (500)	NULL ,
	attachmentForeignkey	varchar (100)	NOT NULL,
	instanceId		varchar (50)	NOT NULL,
	attachmentCreationDate	varchar (10)	NULL,
	attachmentAuthor 	varchar	(100) 	NULL,
	attachmentTitle		varchar (100)   NULL,
	attachmentInfo		varchar (1000)  NULL,
	attachmentOrderNum	int		NOT NULL DEFAULT (0),
	workerId		varchar (50)	NULL,
	cloneId 		varchar (50)	NULL,
	lang			char(2),
	reservationDate	varchar (10)    NULL,
	alertDate		varchar (10)	NULL,
	expiryDate		varchar (10)	NULL,
	xmlForm			varchar(50)		NULL
);