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

alter table sc_mailinglist_message
        add constraint pk_mailinglist_message
        primary key (id);
alter table sc_mailinglist_list
        add constraint pk_mailinglist_list
        primary key (id);
alter table sc_mailinglist_external_user
        add constraint pk_mailinglist_external_user
        primary key (id);
alter table sc_mailinglist_attachment
        add constraint pk_mailinglist_attachment
        primary key (id);
alter table sc_mailinglist_internal_sub
        add constraint pk_mailinglist_internal_sub
        primary key (id);

alter table sc_mailinglist_external_user
        add constraint FK9290F7C94B1A1B47
        foreign key (listId)
        references sc_mailinglist_list (id);
alter table sc_mailinglist_attachment
        add constraint FKCE814959DB1C14EE
        foreign key (messageId)
        references sc_mailinglist_message (id);
alter table sc_mailinglist_internal_sub
        add constraint fk_subscriber_mailinglist_id
         foreign key (mailinglistid)
        references sc_mailinglist_list (id);
alter table sc_mailinglist_message
        add constraint mailinglist_message_mailid_key
        unique (mailId, componentId);

CREATE TABLE UniqueId (
	maxId BIGINT NOT NULL,
	tableName varchar(100) NOT NULL
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

CREATE TABLE ST_AccessLevel
(
    id   char(1)       NOT NULL,
    name varchar(100)  NOT NULL
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
	password	varchar (123) NULL ,
	passwordValid	char (1) DEFAULT ('Y') NOT NULL ,
	loginMail	varchar (100) NULL ,
	email		varchar (100) NULL
);

CREATE TABLE DomainSP_Group_User_Rel (
   groupId 	int NOT NULL ,
   userId	int NOT NULL
);

CREATE TABLE ST_Domain (
	id int NOT NULL ,
	name varchar (100) NOT NULL ,
	description varchar (400) NULL ,
	propFileName varchar (100) NOT NULL ,
	className		varchar (100) NOT NULL ,
	authenticationServer	varchar (100) NOT NULL ,
  theTimeStamp varchar (100) DEFAULT('0') NOT NULL ,
  silverpeasServerURL varchar (400) NULL
);

CREATE TABLE ST_KeyStore (
	userKey		decimal(18, 0)	NOT NULL ,
	login		varchar(50)	NOT NULL ,
	domainId	int		NOT NULL
);


CREATE TABLE ST_LongText (
	id int NOT NULL ,
	orderNum int NOT NULL ,
	bodyContent varchar(2000) NOT NULL
);

CREATE TABLE ST_GroupUserRole (
  id int NOT NULL,
  groupId int NOT NULL,
  roleName varchar(100) NOT NULL
);

CREATE TABLE ST_GroupUserRole_User_Rel (
  groupUserRoleId int NOT NULL,
  userId int NOT NULL
);

CREATE TABLE ST_GroupUserRole_Group_Rel
(
    groupUserRoleId   int NOT NULL,
    groupId           int NOT NULL
);

CREATE TABLE st_instance_modelused
(
	instanceId		varchar(50)     NOT NULL,
	modelId			varchar(50)     NOT NULL,
	objectId		varchar(50)	DEFAULT('0') NOT NULL
)
;

CREATE TABLE ST_UserFavoriteSpaces
(
  id          INT   NOT NULL,
  userid      INT   NOT NULL,
  spaceid     INT   NOT NULL
);

ALTER TABLE UniqueId  ADD
	CONSTRAINT PK_UniqueId PRIMARY KEY
	(
		tableName
	);

ALTER TABLE Personalization  ADD
	CONSTRAINT PK_Personalization PRIMARY KEY
	(
		id
	);

ALTER TABLE ST_AccessLevel  ADD CONSTRAINT PK_AccessLevel PRIMARY KEY (id);
ALTER TABLE ST_AccessLevel ADD CONSTRAINT UN_AccessLevel_1 UNIQUE (name);

ALTER TABLE ST_User ADD CONSTRAINT PK_User PRIMARY KEY (id);
ALTER TABLE ST_User ADD CONSTRAINT UN_User_1 UNIQUE(specificId, domainId);
ALTER TABLE ST_User ADD CONSTRAINT UN_User_2 UNIQUE(login, domainId);
ALTER TABLE ST_User ADD CONSTRAINT FK_User_1 FOREIGN KEY(accessLevel) REFERENCES ST_AccessLevel(id);

ALTER TABLE ST_Group  ADD CONSTRAINT PK_Group PRIMARY KEY (id);
ALTER TABLE ST_Group ADD CONSTRAINT UN_Group_1 UNIQUE(specificId, domainId);
ALTER TABLE ST_Group ADD CONSTRAINT UN_Group_2 UNIQUE(superGroupId, name, domainId);
ALTER TABLE ST_Group ADD CONSTRAINT FK_Group_1 FOREIGN KEY (superGroupId) REFERENCES ST_Group(id);

ALTER TABLE ST_Group_User_Rel  ADD CONSTRAINT PK_Group_User_Rel PRIMARY KEY (groupId, userId);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT FK_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES ST_Group(id);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT FK_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE ST_Space  ADD CONSTRAINT PK_Space PRIMARY KEY (id);
ALTER TABLE ST_Space ADD CONSTRAINT UN_Space_1 UNIQUE(domainFatherId, name);
ALTER TABLE ST_Space ADD CONSTRAINT FK_Space_1 FOREIGN KEY (createdBy) REFERENCES ST_User(id);
ALTER TABLE ST_Space ADD CONSTRAINT FK_Space_2 FOREIGN KEY (domainFatherId) REFERENCES ST_Space(id);

ALTER TABLE ST_ComponentInstance  ADD CONSTRAINT PK_ComponentInstance PRIMARY KEY (id);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT UN_ComponentInstance_1 UNIQUE(spaceId, name);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT FK_ComponentInstance_1 FOREIGN KEY (spaceId) REFERENCES ST_Space(id);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT FK_ComponentInstance_2 FOREIGN KEY (createdBy) REFERENCES ST_User(id);

ALTER TABLE ST_Instance_Data  ADD CONSTRAINT PK_Instance_Data PRIMARY KEY (id);
ALTER TABLE ST_Instance_Data ADD CONSTRAINT UN_Instance_Data_1 UNIQUE(componentId, name);
ALTER TABLE ST_Instance_Data ADD CONSTRAINT FK_Instance_Data_1 FOREIGN KEY (componentId) REFERENCES ST_ComponentInstance(id);

ALTER TABLE ST_UserRole  ADD CONSTRAINT PK_UserRole PRIMARY KEY (id);
ALTER TABLE ST_UserRole ADD CONSTRAINT UN_UserRole_1 UNIQUE(instanceId, roleName, isInherited, objectId);
ALTER TABLE ST_UserRole ADD CONSTRAINT FK_UserRole_1 FOREIGN KEY (instanceId) REFERENCES ST_ComponentInstance(id);

ALTER TABLE ST_UserRole_User_Rel  ADD CONSTRAINT PK_UserRole_User_Rel PRIMARY KEY (userRoleId, userId);
ALTER TABLE ST_UserRole_User_Rel ADD CONSTRAINT FK_UserRole_User_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole(id);
ALTER TABLE ST_UserRole_User_Rel ADD CONSTRAINT FK_UserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE ST_UserRole_Group_Rel  ADD CONSTRAINT PK_UserRole_Group_Rel PRIMARY KEY (userRoleId, groupId);
ALTER TABLE ST_UserRole_Group_Rel ADD CONSTRAINT FK_UserRole_Group_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole(id);
ALTER TABLE ST_UserRole_Group_Rel ADD CONSTRAINT FK_UserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group(id);

ALTER TABLE ST_SpaceUserRole  ADD CONSTRAINT PK_SpaceUserRole PRIMARY KEY (id);
ALTER TABLE ST_SpaceUserRole ADD CONSTRAINT UN_SpaceUserRole_1 UNIQUE(spaceId, roleName, isInherited);
ALTER TABLE ST_SpaceUserRole ADD CONSTRAINT FK_SpaceUserRole_1 FOREIGN KEY (spaceId) REFERENCES ST_Space(id);

ALTER TABLE ST_SpaceUserRole_User_Rel  ADD CONSTRAINT PK_SpaceUserRole_User_Rel PRIMARY KEY (spaceUserRoleId, userId);
ALTER TABLE ST_SpaceUserRole_User_Rel ADD CONSTRAINT FK_SpaceUserRole_User_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole(id);
ALTER TABLE ST_SpaceUserRole_User_Rel ADD CONSTRAINT FK_SpaceUserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE ST_SpaceUserRole_Group_Rel  ADD CONSTRAINT PK_SpaceUserRole_Group_Rel PRIMARY KEY (spaceUserRoleId, groupId);
ALTER TABLE ST_SpaceUserRole_Group_Rel ADD CONSTRAINT FK_SpaceUserRole_Group_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole(id);
ALTER TABLE ST_SpaceUserRole_Group_Rel ADD CONSTRAINT FK_SpaceUserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group(id);

ALTER TABLE DomainSP_Group  ADD CONSTRAINT PK_DomainSP_Group PRIMARY KEY (id);
ALTER TABLE DomainSP_Group ADD CONSTRAINT UN_DomainSP_Group_1 UNIQUE(superGroupId, name);
ALTER TABLE DomainSP_Group ADD CONSTRAINT FK_DomainSP_Group_1 FOREIGN KEY (superGroupId) REFERENCES DomainSP_Group(id);

ALTER TABLE DomainSP_User  ADD CONSTRAINT PK_DomainSP_User PRIMARY KEY (id);
ALTER TABLE DomainSP_User ADD CONSTRAINT UN_DomainSP_User_1 UNIQUE(login);

ALTER TABLE DomainSP_Group_User_Rel ADD CONSTRAINT FK_DomainSP_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES DomainSP_Group(id);
ALTER TABLE DomainSP_Group_User_Rel ADD CONSTRAINT FK_DomainSP_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES DomainSP_User(id);

ALTER TABLE ST_Domain  ADD CONSTRAINT PK_ST_Domain PRIMARY KEY (id);

ALTER TABLE DomainSP_Group_User_Rel  ADD CONSTRAINT PK_DomainSP_Group_User_Rel PRIMARY KEY (groupId,userId);

ALTER TABLE ST_LongText ADD CONSTRAINT PK_ST_LongText PRIMARY KEY (id,orderNum);

ALTER TABLE st_instance_modelused ADD
	 CONSTRAINT PK_st_instance_modelused PRIMARY KEY
	(
		instanceId,
		modelId,
		objectId
	)
;

ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT PK_UserFavoriteSpaces PRIMARY KEY (id);
ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT FK_UserFavoriteSpaces_1 FOREIGN KEY (userid) REFERENCES ST_User(id);
ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT FK_UserFavoriteSpaces_2 FOREIGN KEY (spaceid) REFERENCES ST_Space(id);

insert into ST_AccessLevel(id, name) values ('U', 'User');
insert into ST_AccessLevel(id, name) values ('A', 'Administrator');
insert into ST_AccessLevel(id, name) values ('G', 'Guest');
insert into ST_AccessLevel(id, name) values ('R', 'Removed');
insert into ST_AccessLevel(id, name) values ('K', 'KMManager');
insert into ST_AccessLevel(id, name) values ('D', 'DomainManager');

INSERT INTO ST_User (id, specificId, domainId, lastName, login, accessLevel, state, stateSaveDate)
  VALUES (0, '0', 0, 'Administrateur', '${ADMINLOGIN}', 'A', 'VALID', CURRENT_TIMESTAMP);

insert into DomainSP_User(id, lastName, login, password)
values             (0, 'Administrateur', '${ADMINLOGIN}', '${ADMINPASSWD}');

insert into ST_Domain(id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
values             (-1, 'internal', 'Do not remove - Used by Silverpeas engine', '-', '-', '-', '0', '');

insert into ST_Domain(id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
values             (0, 'domainSilverpeas', 'default domain for Silverpeas', 'com.stratelia.silverpeas.domains.domainSP', 'com.silverpeas.domains.silverpeasdriver.SilverpeasDomainDriver', 'autDomainSP', '0', '${URLSERVER}');

CREATE TABLE ST_NotifChannel (
	id int NOT NULL ,
	name varchar (20) NOT NULL ,
	description varchar (200) NULL ,
	couldBeAdded char (1) NOT NULL DEFAULT ('Y') ,
	fromAvailable char (1) NOT NULL DEFAULT ('N') ,
	subjectAvailable char (1) NOT NULL DEFAULT ('N')
)
;

CREATE TABLE ST_NotifAddress (
	id int NOT NULL ,
	userId int NOT NULL ,
	notifName varchar (20) NOT NULL ,
	notifChannelId int NOT NULL ,
	address varchar (250) NOT NULL ,
	usage varchar (20) NULL ,
	priority int NOT NULL
)
;

CREATE TABLE ST_NotifDefaultAddress (
	id int NOT NULL ,
	userId int NOT NULL ,
	notifAddressId int NOT NULL
)
;

CREATE TABLE ST_NotifPreference (
	id int NOT NULL ,
	notifAddressId int NOT NULL ,
	componentInstanceId int NOT NULL ,
	userId int NOT NULL ,
	messageType int NOT NULL
)
;

CREATE TABLE ST_NotifSended (
	notifId		int		NOT NULL,
	userId		int		NOT NULL,
	messageType	int		NULL,
	notifDate	char (13)	NOT NULL,
	title		varchar (255)	NULL,
	link		varchar (255)	NULL,
	sessionId	varchar (255)	NULL,
	componentId	varchar (255)	NULL,
	body		int		NULL
);

CREATE TABLE ST_NotifSendedReceiver (
	notifId		int		NOT NULL,
	userId		int		NOT NULL
);

ALTER TABLE ST_NotifChannel ADD	CONSTRAINT PK_NotifChannel PRIMARY KEY(id) ;

ALTER TABLE ST_NotifDefaultAddress ADD CONSTRAINT PK_ST_NotifDefaultAddress PRIMARY KEY(id);
ALTER TABLE ST_NotifDefaultAddress ADD CONSTRAINT FK_NotifDefaultAddress_1 FOREIGN KEY(userId) REFERENCES ST_User(id);

ALTER TABLE ST_NotifPreference ADD CONSTRAINT PK_NotifAddr_Component PRIMARY KEY(id);
ALTER TABLE ST_NotifPreference ADD CONSTRAINT FK_NotifPreference_1 FOREIGN KEY(componentInstanceId) REFERENCES ST_ComponentInstance (id);
ALTER TABLE ST_NotifPreference ADD CONSTRAINT FK_NotifPreference_2 FOREIGN KEY(userId) REFERENCES ST_User(id);

ALTER TABLE ST_NotifAddress ADD CONSTRAINT PK_NotifAddress PRIMARY KEY(id);
ALTER TABLE ST_NotifAddress ADD CONSTRAINT FK_NotifAddress_1 FOREIGN KEY(notifChannelId) REFERENCES ST_NotifChannel(id);
ALTER TABLE ST_NotifAddress ADD	CONSTRAINT FK_NotifAddress_2 FOREIGN KEY(userId) REFERENCES ST_User(id);

ALTER TABLE ST_NotifSended ADD CONSTRAINT PK_NotifSended PRIMARY KEY(notifId);

ALTER TABLE ST_NotifSendedReceiver ADD CONSTRAINT PK_NotifSendedReceiver PRIMARY KEY(notifId, userId);
