CREATE TABLE IF NOT EXISTS UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

CREATE TABLE DomainSP_Group (
   id 		int NOT NULL,
   superGroupId int NULL ,
   name		varchar (100) NOT NULL ,
   description 	varchar (400) NULL
);

CREATE TABLE DomainSP_User (
	id		int NOT NULL,
	firstName	varchar (100) NULL ,
	lastName	varchar (100) NOT NULL ,
	phone		varchar (20) NULL ,
	homePhone	varchar (20) NULL ,
	cellPhone	varchar (20) NULL ,
	fax		varchar (20) NULL ,
	address		varchar (500) NULL ,
	title		varchar (100) NULL ,
	company		varchar (100) NULL ,
	position	varchar (100) NULL ,
	boss		varchar (100) NULL ,
	login		varchar (50) NOT NULL ,
	password	varchar (32) NULL ,
	passwordValid	char (1) DEFAULT ('Y') NOT NULL ,
	loginMail	varchar (100) NULL ,
	email		varchar (100) NULL 
);

CREATE TABLE ST_User
(
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
  stateSaveDate                 TIMESTAMP            NOT NULL
);

CREATE TABLE ST_Group
(
    id              int           NOT NULL,
    domainId        int           NOT NULL,
    specificId      varchar(500)  NOT NULL,
    superGroupId    int,
    name            varchar(100)  NOT NULL,
    description     varchar(400),
    synchroRule	    varchar(100)
);

CREATE TABLE ST_Group_User_Rel
(
    groupId int NOT NULL,
    userId  int NOT NULL
);

CREATE TABLE ST_Space
(
    id					int           NOT NULL,
    domainFatherId		int,
    name				varchar(100)  NOT NULL,
    description			varchar(400),
    createdBy			int,
    firstPageType		int           NOT NULL,
    firstPageExtraParam	varchar(400),
    orderNum 			int DEFAULT (0) NOT NULL,
    createTime 			varchar(20),
    updateTime 			varchar(20),
    removeTime 			varchar(20),
    spaceStatus 		char(1),
    updatedBy 			int,
    removedBy 			int,
    lang			char(2),
    isInheritanceBlocked	int	      default(0) NOT NULL,
    look			varchar(50),
    displaySpaceFirst		smallint,
    isPersonal			smallint
);

CREATE TABLE ST_SpaceI18N
(
    id			int		NOT NULL,
    spaceId		int		NOT NULL,
    lang		char(2)		NOT NULL,
    name		varchar(100)	NOT NULL,
    description		varchar(400)
);

CREATE TABLE ST_ComponentInstance
(
    id            	int           NOT NULL,
    spaceId       	int           NOT NULL,
    name          	varchar(100)  NOT NULL,
    componentName 	varchar(100)  NOT NULL,
    description   	varchar(400),
    createdBy     	int,
    orderNum 		int DEFAULT (0) NOT NULL,
    createTime 		varchar(20),
    updateTime 		varchar(20),
    removeTime 		varchar(20),
    componentStatus char(1),
    updatedBy 		int,
    removedBy 		int,
    isPublic		int	DEFAULT(0)	NOT NULL,
    isHidden		int	DEFAULT(0)	NOT NULL,
    lang		char(2),
    isInheritanceBlocked	int	default(0) NOT NULL
);

CREATE TABLE ST_ComponentInstanceI18N
(
    id			int		NOT NULL,
    componentId		int		NOT NULL,
    lang		char(2)		NOT NULL,
    name		varchar(100)	NOT NULL,
    description		varchar(400)
);

CREATE TABLE ST_Domain (
	id			int NOT NULL ,
	name			varchar (100) NOT NULL ,
	description		varchar (400) NULL ,
	propFileName		varchar (100) NOT NULL ,
	className		varchar (100) NOT NULL ,
	authenticationServer	varchar (100) NOT NULL ,
  theTimeStamp            varchar (100) DEFAULT('0') NOT NULL ,
  silverpeasServerURL     varchar (400) NULL 
);

CREATE TABLE DomainSP_Group_User_Rel (
   groupId 	int NOT NULL ,
   userId	int NOT NULL
);

CREATE TABLE ST_NotifDefaultAddress (
	id int NOT NULL ,
	userId int NOT NULL ,
	notifAddressId int NOT NULL 
);

CREATE TABLE ST_Instance_Data
(
    id            int           NOT NULL,
    componentId   int           NOT NULL,
    name          varchar(100)  NOT NULL,
    label	  varchar(100)  NOT NULL,
    value	  varchar(400)
);

CREATE TABLE ST_UserRole
(
    id            int           NOT NULL,
    instanceId    int           NOT NULL,
    name          varchar(100)  NULL,
    roleName      varchar(100)  NOT NULL,
    description   varchar(400),
    isInherited	  int	        default(0) NOT NULL,
    objectId	  int,
    objectType	  varchar(50)
);

CREATE TABLE ST_UserRole_User_Rel
(
    userRoleId   int NOT NULL,
    userId       int NOT NULL
);

CREATE TABLE ST_UserRole_Group_Rel
(
    userRoleId   int NOT NULL,
    groupId      int NOT NULL
);

CREATE TABLE ST_SpaceUserRole
(
    id            int           NOT NULL,
    spaceId	  int           NOT NULL,
    name          varchar(100)  NULL,
    roleName      varchar(100)  NOT NULL,
    description   varchar(400),
    isInherited	  int	        default(0) NOT NULL 
);

CREATE TABLE ST_SpaceUserRole_User_Rel
(
    spaceUserRoleId   int NOT NULL,
    userId            int NOT NULL
);

CREATE TABLE ST_SpaceUserRole_Group_Rel
(
    spaceUserRoleId   int NOT NULL,
    groupId           int NOT NULL
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

CREATE TABLE ST_UserFavoriteSpaces
(
  id          INT   NOT NULL,
  userid      INT   NOT NULL,
  spaceid     INT   NOT NULL
);


create table sc_mailinglist_attachment (
        id varchar(255) not null,
        version int4 not null,
        attachmentSize int8,
        attachmentPath varchar(255),
        fileName varchar(255),
        contentType varchar(255),
        md5Signature varchar(255),
        messageId varchar(255)
    );

    create table sc_mailinglist_external_user (
        id varchar(255) not null,
        version int4 not null,
        componentId varchar(255),
        email varchar(255) not null,
        listId varchar(255)
    );

    create table sc_mailinglist_list (
        id varchar(255) not null,
        version int4 not null,
        componentId varchar(255)
    );

    create table sc_mailinglist_message (
        id varchar(255) not null,
        version int4 not null,
        mailId varchar(255) not null,
        componentId varchar(255) not null,
        title varchar(255),
        summary varchar(255),
        sender varchar(255),
        sentDate timestamp,
        referenceId varchar(255),
        moderated bool,
        contentType varchar(255),
        attachmentsSize int8,
        messageYear int4,
        messageMonth int4,
        body text
    );



    create table sc_mailinglist_internal_sub (
        id varchar(255) not null,
        version int4 not null,
        subscriber_type varchar(255) not null,
        externalid varchar(255) not null,
        mailinglistid varchar(255) not null
    );

CREATE TABLE Personalization (
	id varchar(100) NOT NULL ,
	languages varchar(100) NULL,
	look varchar(50) NULL,
	personalWSpace varchar(50) NULL,
	thesaurusStatus int NOT NULL,
	dragAndDropStatus int DEFAULT 1,
  webdavEditingStatus int DEFAULT 0,
  menuDisplay varchar(50) DEFAULT 'DEFAULT'
);

CREATE TABLE ST_NotifPreference (
	id int NOT NULL ,
	notifAddressId int NOT NULL ,
	componentInstanceId int NOT NULL ,
	userId int NOT NULL ,
	messageType int NOT NULL 
);

CREATE TABLE ST_NotifChannel (
	id int NOT NULL ,
	name varchar (20) NOT NULL ,
	description varchar (200) NULL ,
	couldBeAdded char (1) NOT NULL DEFAULT ('Y') ,
	fromAvailable char (1) NOT NULL DEFAULT ('N') ,
	subjectAvailable char (1) NOT NULL DEFAULT ('N')
);
