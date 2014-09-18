CREATE TABLE UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

CREATE TABLE SC_Forums_Forum
(
	forumId			int		NOT NULL ,
	forumName		varchar (1000)	NOT NULL ,
	forumDescription	varchar (2000)	NULL ,
	forumCreationDate	varchar (50)	NOT NULL ,
	forumCloseDate		varchar (50)	NULL ,
	forumCreator		varchar (255)	NOT NULL ,
	forumActive		int		NOT NULL ,
	forumParent		int		DEFAULT 0 NOT NULL ,
	forumModes		varchar (50)	NULL ,
	forumLockLevel		int		NULL ,
	instanceId		varchar (50)	NOT NULL ,
	categoryId		varchar (50)	NULL
);

CREATE TABLE SC_Forums_Message
(
	messageId		int		NOT NULL ,
	messageTitle		varchar (1000)	NOT NULL ,
	messageAuthor		varchar (255)	NOT NULL ,
	forumId			int		NOT NULL ,
	messageParentId		int		NULL ,
	messageDate		timestamp	NULL ,
	status			varchar (50)	NULL
);

CREATE TABLE SC_Forums_Rights
(
	userId			varchar (255)	NOT NULL ,
	forumId			varchar (255)	NOT NULL
);

CREATE TABLE SC_Forums_Subscription
(
	userId			varchar (255)	NOT NULL ,
	messageId		varchar (255)	NOT NULL
);

CREATE TABLE SC_Forums_HistoryUser
(
	userId			varchar (255)	NOT NULL ,
	messageId 		int		NOT NULL ,
	lastAccess		varchar (50)	NOT NULL
);
ALTER TABLE SC_Forums_Forum ADD
	 CONSTRAINT PK_Forums_Forum PRIMARY KEY
	(
		forumId
	)
;

ALTER TABLE SC_Forums_Message ADD
	 CONSTRAINT PK_Forums_Message PRIMARY KEY
	(
		messageId
	)
;



ALTER TABLE SC_Forums_Rights ADD
	 CONSTRAINT PK_Forums_Rights PRIMARY KEY
	(
		userId,
		forumId
	)
;

ALTER TABLE SC_Forums_Subscription ADD
	 CONSTRAINT PK_Forums_Subscription PRIMARY KEY
	(
		userId,
		messageId
	)
;

ALTER TABLE SC_Forums_HistoryUser ADD
	 CONSTRAINT PK_Forums_HistoryUser PRIMARY KEY
	(
		userId,
		messageId
	)
;