CREATE TABLE ST_Space (
  id                    int PRIMARY KEY NOT NULL,
  domainFatherId        int,
  name                  varchar(100)  NOT NULL,
  description           varchar(400),
  createdBy             int,
  firstPageType         int NOT NULL,
  firstPageExtraParam   varchar(400),
  orderNum              int DEFAULT (0) NOT NULL,
  createTime            varchar(20),
  updateTime            varchar(20),
  removeTime            varchar(20),
  spaceStatus           char(1),
  updatedBy             int,
  removedBy             int,
  lang                  char(2),
  isInheritanceBlocked  int default(0) NOT NULL,
  look                  varchar(50),
  displaySpaceFirst     smallint,
  isPersonal            smallint
);

CREATE TABLE ST_ComponentInstance (
  id            	int           NOT NULL,
  spaceId       	int           NOT NULL,
  name          	varchar(100)  NOT NULL,
  componentName 	varchar(100)  NOT NULL,
  description   	varchar(400),
  createdBy     	int,
  orderNum 		    int DEFAULT (0) NOT NULL,
  createTime 		varchar(20),
  updateTime 		varchar(20),
  removeTime 		varchar(20),
  componentStatus   char(1),
  updatedBy 		int,
  removedBy 		int,
  isPublic		    int	DEFAULT(0)	NOT NULL,
  isHidden		    int	DEFAULT(0)	NOT NULL,
  lang		        char(2),
  isInheritanceBlocked	int	default(0) NOT NULL
);

CREATE TABLE IF NOT EXISTS SB_ContentManager_Instance (
  instanceId    int NOT NULL ,
  componentId   varchar(100) NOT NULL ,
  containerType varchar(100) NOT NULL ,
  contentType   varchar(100) NOT NULL
);

CREATE TABLE ST_Token (
  id          int8 NOT NULL ,
  tokenType   varchar(50) NOT NULL ,
  resourceId  varchar(50) NOT NULL ,
  token       varchar(50) NOT NULL ,
  saveCount   int NOT NULL ,
  saveDate    timestamp NOT NULL,
  CONSTRAINT const_st_token_pk PRIMARY KEY (id)
);

-- User

CREATE TABLE IF NOT EXISTS ST_AccessLevel (
  id   CHAR(1)      NOT NULL,
  name VARCHAR(100) NOT NULL,
  CONSTRAINT PK_AccessLevel PRIMARY KEY (id),
  CONSTRAINT UN_AccessLevel_1 UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS ST_User (
  id                            INT                  NOT NULL,
  domainId                      INT                  NOT NULL,
  specificId                    VARCHAR(500)         NOT NULL,
  firstName                     VARCHAR(100),
  lastName                      VARCHAR(100)         NOT NULL,
  email                         VARCHAR(100),
  login                         VARCHAR(50)          NOT NULL,
  loginMail                     VARCHAR(100),
  accessLevel                   CHAR(1) DEFAULT 'U'  NOT NULL,
  loginquestion                 VARCHAR(200),
  loginanswer                   VARCHAR(200),
  creationDate                  TIMESTAMP,
  saveDate                      TIMESTAMP,
  version                       INT DEFAULT 0        NOT NULL,
  tosAcceptanceDate             TIMESTAMP,
  lastLoginDate                 TIMESTAMP,
  nbSuccessfulLoginAttempts     INT DEFAULT 0        NOT NULL,
  lastLoginCredentialUpdateDate TIMESTAMP,
  expirationDate                TIMESTAMP,
  state                         VARCHAR(30)          NOT NULL,
  stateSaveDate                 TIMESTAMP            NOT NULL,
  notifManualReceiverLimit      INT,
  CONSTRAINT PK_User PRIMARY KEY (id),
  CONSTRAINT UN_User_1 UNIQUE (specificId, domainId),
  CONSTRAINT UN_User_2 UNIQUE (login, domainId),
  CONSTRAINT FK_User_1 FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel (id)
);

-- User Notificication API

CREATE TABLE ST_NotifChannel (
  id int NOT NULL ,
  name varchar (20) NOT NULL ,
  description varchar (200) NULL ,
  couldBeAdded char (1) NOT NULL DEFAULT ('Y') ,
  fromAvailable char (1) NOT NULL DEFAULT ('N') ,
  subjectAvailable char (1) NOT NULL DEFAULT ('N'),
  CONSTRAINT PK_NotifChannel PRIMARY KEY(id)
);

CREATE TABLE ST_NotifAddress (
  id int NOT NULL ,
  userId int NOT NULL ,
  notifName varchar (20) NOT NULL ,
  notifChannelId int NOT NULL ,
  address varchar (250) NOT NULL ,
  usage varchar (20) NULL ,
  priority int NOT NULL,
  CONSTRAINT PK_NotifAddress PRIMARY KEY(id),
  CONSTRAINT FK_NotifAddress_1 FOREIGN KEY(notifChannelId) REFERENCES ST_NotifChannel(id),
  CONSTRAINT FK_NotifAddress_2 FOREIGN KEY(userId) REFERENCES ST_User(id)
);

CREATE TABLE ST_NotifDefaultAddress (
  id int NOT NULL ,
  userId int NOT NULL ,
  notifAddressId int NOT NULL,
  CONSTRAINT PK_ST_NotifDefaultAddress PRIMARY KEY(id),
  CONSTRAINT FK_NotifDefaultAddress_1 FOREIGN KEY(userId) REFERENCES ST_User(id)
);

CREATE TABLE ST_NotifPreference (
  id int NOT NULL ,
  notifAddressId int NOT NULL ,
  componentInstanceId int NOT NULL ,
  userId int NOT NULL ,
  messageType int NOT NULL,
  CONSTRAINT PK_NotifAddr_Component PRIMARY KEY(id),
  CONSTRAINT FK_NotifPreference_2 FOREIGN KEY(userId) REFERENCES ST_User(id)
);

CREATE TABLE ST_NotifSended (
  notifId		int		NOT NULL,
  userId		int		NOT NULL,
  messageType	int		NULL,
  notifDate	    char (13)	NOT NULL,
  title		    varchar (255)	NULL,
  link		    varchar (255)	NULL,
  sessionId	    varchar (255)	NULL,
  componentId	varchar (255)	NULL,
  body		    int		NULL,
  CONSTRAINT PK_NotifSended PRIMARY KEY(notifId)
);

CREATE TABLE ST_NotifSendedReceiver (
  notifId		int		NOT NULL,
  userId		int		NOT NULL,
  CONSTRAINT PK_NotifSendedReceiver PRIMARY KEY(notifId, userId)
);

CREATE TABLE st_delayednotifusersetting (
  id 			int NOT NULL ,
  userId		int NOT NULL ,
  channel		int NOT NULL ,
  frequency	    varchar (4) NOT NULL,
  CONSTRAINT const_st_dnus_pk PRIMARY KEY (id),
  CONSTRAINT const_st_dnus_fk_userId FOREIGN KEY (userId) REFERENCES ST_User(id)
);

CREATE TABLE st_notificationresource (
  id 					int8 NOT NULL ,
  componentInstanceId	varchar(50) NOT NULL ,
  resourceId			varchar(50) NOT NULL ,
  resourceType			varchar(50) NOT NULL ,
  resourceName			varchar(500) NOT NULL ,
  resourceDescription	varchar(2000) NULL ,
  resourceLocation		varchar(500) NOT NULL ,
  resourceUrl			varchar(1000) NULL,
  CONSTRAINT const_st_nr_pk PRIMARY KEY (id)
);

CREATE TABLE st_delayednotification (
  id 						int8 NOT NULL ,
  userId					int NOT NULL ,
  fromUserId				int NOT NULL ,
  channel					int NOT NULL ,
  action					int NOT NULL ,
  notificationResourceId	int8 NOT NULL ,
  language				    varchar(2) NOT NULL ,
  creationDate			    timestamp NOT NULL ,
  message					varchar(2000) NULL,
  CONSTRAINT const_st_dn_pk PRIMARY KEY (id),
  CONSTRAINT const_st_dn_fk_nrId FOREIGN KEY (notificationResourceId) REFERENCES st_notificationresource(id),
  CONSTRAINT const_st_dn_fk_userId FOREIGN KEY (userId) REFERENCES ST_User(id)
);

CREATE TABLE IF NOT EXISTS SC_Community (
  id             VARCHAR(40) PRIMARY KEY,
  instanceId     VARCHAR(30) NOT NULL,
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(40) NOT NULL,
  lastUpdateDate TIMESTAMP   NOT NULL,
  lastUpdatedBy  VARCHAR(40) NOT NULL,
  version        INT8        NOT NULL
);