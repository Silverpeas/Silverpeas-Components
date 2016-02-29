CREATE TABLE UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

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
	imageName		varchar(255)	NULL,
	imageSize		int		NULL,
	imageMimeType		varchar(100)	NULL,
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

ALTER TABLE SC_Gallery_Photo
ADD CONSTRAINT PK_SC_Gallery_Photo PRIMARY KEY
	(
		photoId
	)
;

ALTER TABLE SC_Gallery_Path
ADD CONSTRAINT PK_SC_Gallery_Path PRIMARY KEY
	(
		photoId, nodeId
	)
;

ALTER TABLE SC_Gallery_Order
ADD CONSTRAINT PK_SC_Gallery_Order PRIMARY KEY
	(
		orderId
	)
;

ALTER TABLE SC_Gallery_OrderDetail
ADD CONSTRAINT PK_SC_Gallery_OrderDetail PRIMARY KEY
	(
		orderId, photoId
	)
;

CREATE INDEX IND_Photo ON SC_Gallery_Photo (photoId);

CREATE INDEX IND_Order ON SC_Gallery_Order (orderId);

CREATE TABLE sb_node_node
(
  nodeId      int   NOT NULL ,
  nodeName    varchar (1000)  NOT NULL ,
  nodeDescription   varchar (2000)  NULL,
  nodeCreationDate  varchar (10)  NOT NULL ,
  nodeCreatorId   varchar (100) NOT NULL ,
  nodePath    varchar (1000)  NOT NULL ,
  nodeLevelNumber   int   NOT NULL ,
  nodeFatherId    int   NOT NULL ,
  modelId     varchar (1000)  NULL ,
  nodeStatus    varchar (1000)  NULL ,
  instanceId    varchar (50)  NOT NULL,
  type      varchar (50)  NULL ,
  orderNumber   int   DEFAULT (0) NULL ,
  lang      char(2),
  rightsDependsOn   int   default(-1) NOT NULL
);

CREATE TABLE sb_node_nodei18N
(
  id      int   NOT NULL ,
  nodeId  int NOT NULL ,
  lang char (2)  NOT NULL ,
  nodeName  varchar (1000)  NOT NULL ,
  nodeDescription   varchar (2000)
);

ALTER TABLE SB_Node_Node ADD
   CONSTRAINT PK_Node_Node
   PRIMARY KEY
  (
    nodeId,
    instanceId
  )
;
ALTER TABLE SB_Node_NodeI18N ADD
   CONSTRAINT PK_Node_NodeI18N
   PRIMARY KEY
  (
    id
  )
;
