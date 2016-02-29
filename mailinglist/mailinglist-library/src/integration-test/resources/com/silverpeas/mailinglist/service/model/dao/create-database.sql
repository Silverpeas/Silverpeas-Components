CREATE TABLE sc_mailinglist_attachment (
  id             VARCHAR(255) NOT NULL,
  version        INT4         NOT NULL,
  attachmentSize INT8,
  attachmentPath VARCHAR(255),
  fileName       VARCHAR(255),
  contentType    VARCHAR(255),
  md5Signature   VARCHAR(255),
  messageId      VARCHAR(255)
);

CREATE TABLE sc_mailinglist_external_user (
  id          VARCHAR(255) NOT NULL,
  version     INT4         NOT NULL,
  componentId VARCHAR(255),
  email       VARCHAR(255) NOT NULL,
  listId      VARCHAR(255)
);

CREATE TABLE sc_mailinglist_list (
  id          VARCHAR(255) NOT NULL,
  version     INT4         NOT NULL,
  componentId VARCHAR(255)
);

CREATE TABLE sc_mailinglist_message (
  id              VARCHAR(255) NOT NULL,
  version         INT4         NOT NULL,
  mailId          VARCHAR(255) NOT NULL,
  componentId     VARCHAR(255) NOT NULL,
  title           VARCHAR(255),
  summary         VARCHAR(255),
  sender          VARCHAR(255),
  sentDate        TIMESTAMP,
  referenceId     VARCHAR(255),
  moderated       BOOL,
  contentType     VARCHAR(255),
  attachmentsSize INT8,
  messageYear     INT4,
  messageMonth    INT4,
  body            TEXT
);

CREATE TABLE sc_mailinglist_internal_sub (
  id              VARCHAR(255) NOT NULL,
  version         INT4         NOT NULL,
  subscriber_type VARCHAR(255) NOT NULL,
  externalid      VARCHAR(255) NOT NULL,
  mailinglistid   VARCHAR(255) NOT NULL
);

ALTER TABLE sc_mailinglist_message
ADD CONSTRAINT pk_mailinglist_message
PRIMARY KEY (id);
ALTER TABLE sc_mailinglist_list
ADD CONSTRAINT pk_mailinglist_list
PRIMARY KEY (id);
ALTER TABLE sc_mailinglist_external_user
ADD CONSTRAINT pk_mailinglist_external_user
PRIMARY KEY (id);
ALTER TABLE sc_mailinglist_attachment
ADD CONSTRAINT pk_mailinglist_attachment
PRIMARY KEY (id);
ALTER TABLE sc_mailinglist_internal_sub
ADD CONSTRAINT pk_mailinglist_internal_sub
PRIMARY KEY (id);

ALTER TABLE sc_mailinglist_external_user
ADD CONSTRAINT FK9290F7C94B1A1B47
FOREIGN KEY (listId)
REFERENCES sc_mailinglist_list (id);
ALTER TABLE sc_mailinglist_attachment
ADD CONSTRAINT FKCE814959DB1C14EE
FOREIGN KEY (messageId)
REFERENCES sc_mailinglist_message (id);
ALTER TABLE sc_mailinglist_internal_sub
ADD CONSTRAINT fk_subscriber_mailinglist_id
FOREIGN KEY (mailinglistid)
REFERENCES sc_mailinglist_list (id);
ALTER TABLE sc_mailinglist_message
ADD CONSTRAINT mailinglist_message_mailid_key
UNIQUE (mailId, componentId);

CREATE TABLE Personalization (
  id                  VARCHAR(100) NOT NULL,
  languages           VARCHAR(100) NULL,
  look                VARCHAR(50)  NULL,
  personalWSpace      VARCHAR(50)  NULL,
  thesaurusStatus     INT          NOT NULL,
  dragAndDropStatus   INT         DEFAULT 1,
  webdavEditingStatus INT         DEFAULT 0,
  menuDisplay         VARCHAR(50) DEFAULT 'DEFAULT'
);

CREATE TABLE ST_AccessLevel
(
  id   CHAR(1)      NOT NULL,
  name VARCHAR(100) NOT NULL
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
  id           INT          NOT NULL,
  domainId     INT          NOT NULL,
  specificId   VARCHAR(500) NOT NULL,
  superGroupId INT,
  name         VARCHAR(100) NOT NULL,
  description  VARCHAR(400),
  synchroRule  VARCHAR(100)
);

CREATE TABLE ST_Group_User_Rel
(
  groupId INT NOT NULL,
  userId  INT NOT NULL
);

CREATE TABLE ST_Space
(
  id                   INT             NOT NULL,
  domainFatherId       INT,
  name                 VARCHAR(100)    NOT NULL,
  description          VARCHAR(400),
  createdBy            INT,
  firstPageType        INT             NOT NULL,
  firstPageExtraParam  VARCHAR(400),
  orderNum             INT DEFAULT (0) NOT NULL,
  createTime           VARCHAR(20),
  updateTime           VARCHAR(20),
  removeTime           VARCHAR(20),
  spaceStatus          CHAR(1),
  updatedBy            INT,
  removedBy            INT,
  lang                 CHAR(2),
  isInheritanceBlocked INT DEFAULT (0) NOT NULL,
  look                 VARCHAR(50),
  displaySpaceFirst    SMALLINT,
  isPersonal           SMALLINT
);

CREATE TABLE ST_SpaceI18N
(
  id          INT          NOT NULL,
  spaceId     INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(400)
);

CREATE TABLE ST_ComponentInstance
(
  id                   INT              NOT NULL,
  spaceId              INT              NOT NULL,
  name                 VARCHAR(100)     NOT NULL,
  componentName        VARCHAR(100)     NOT NULL,
  description          VARCHAR(400),
  createdBy            INT,
  orderNum             INT DEFAULT (0)  NOT NULL,
  createTime           VARCHAR(20),
  updateTime           VARCHAR(20),
  removeTime           VARCHAR(20),
  componentStatus      CHAR(1),
  updatedBy            INT,
  removedBy            INT,
  isPublic             INT DEFAULT (0)  NOT NULL,
  isHidden             INT DEFAULT (0)  NOT NULL,
  lang                 CHAR(2),
  isInheritanceBlocked INT DEFAULT (0)  NOT NULL
);

CREATE TABLE ST_ComponentInstanceI18N
(
  id          INT          NOT NULL,
  componentId INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(400)
);

CREATE TABLE ST_Instance_Data
(
  id          INT          NOT NULL,
  componentId INT          NOT NULL,
  name        VARCHAR(100) NOT NULL,
  label       VARCHAR(100) NOT NULL,
  value       VARCHAR(400)
);

CREATE TABLE ST_UserRole
(
  id          INT             NOT NULL,
  instanceId  INT             NOT NULL,
  name        VARCHAR(100)    NULL,
  roleName    VARCHAR(100)    NOT NULL,
  description VARCHAR(400),
  isInherited INT DEFAULT (0) NOT NULL,
  objectId    INT,
  objectType  VARCHAR(50)
);

CREATE TABLE ST_UserRole_User_Rel
(
  userRoleId INT NOT NULL,
  userId     INT NOT NULL
);

CREATE TABLE ST_UserRole_Group_Rel
(
  userRoleId INT NOT NULL,
  groupId    INT NOT NULL
);

CREATE TABLE ST_SpaceUserRole
(
  id          INT             NOT NULL,
  spaceId     INT             NOT NULL,
  name        VARCHAR(100)    NULL,
  roleName    VARCHAR(100)    NOT NULL,
  description VARCHAR(400),
  isInherited INT DEFAULT (0) NOT NULL
);

CREATE TABLE ST_SpaceUserRole_User_Rel
(
  spaceUserRoleId INT NOT NULL,
  userId          INT NOT NULL
);

CREATE TABLE ST_SpaceUserRole_Group_Rel
(
  spaceUserRoleId INT NOT NULL,
  groupId         INT NOT NULL
);

CREATE TABLE DomainSP_Group (
  id           INT          NOT NULL,
  superGroupId INT          NULL,
  name         VARCHAR(100) NOT NULL,
  description  VARCHAR(400) NULL
);

CREATE TABLE DomainSP_User (
  id            INT                   NOT NULL,
  firstName     VARCHAR(100)          NULL,
  lastName      VARCHAR(100)          NOT NULL,
  phone         VARCHAR(20)           NULL,
  homePhone     VARCHAR(20)           NULL,
  cellPhone     VARCHAR(20)           NULL,
  fax           VARCHAR(20)           NULL,
  address       VARCHAR(500)          NULL,
  title         VARCHAR(100)          NULL,
  company       VARCHAR(100)          NULL,
  position      VARCHAR(100)          NULL,
  boss          VARCHAR(100)          NULL,
  login         VARCHAR(50)           NOT NULL,
  password      VARCHAR(123)          NULL,
  passwordValid CHAR(1) DEFAULT ('Y') NOT NULL,
  loginMail     VARCHAR(100)          NULL,
  email         VARCHAR(100)          NULL
);

CREATE TABLE DomainSP_Group_User_Rel (
  groupId INT NOT NULL,
  userId  INT NOT NULL
);

CREATE TABLE ST_Domain (
  id                   INT                        NOT NULL,
  name                 VARCHAR(100)               NOT NULL,
  description          VARCHAR(400)               NULL,
  propFileName         VARCHAR(100)               NOT NULL,
  className            VARCHAR(100)               NOT NULL,
  authenticationServer VARCHAR(100)               NOT NULL,
  theTimeStamp         VARCHAR(100) DEFAULT ('0') NOT NULL,
  silverpeasServerURL  VARCHAR(400)               NULL
);

CREATE TABLE ST_KeyStore (
  userKey  DECIMAL(18, 0) NOT NULL,
  login    VARCHAR(50)    NOT NULL,
  domainId INT            NOT NULL
);


CREATE TABLE ST_LongText (
  id          INT           NOT NULL,
  orderNum    INT           NOT NULL,
  bodyContent VARCHAR(2000) NOT NULL
);

CREATE TABLE ST_GroupUserRole (
  id       INT          NOT NULL,
  groupId  INT          NOT NULL,
  roleName VARCHAR(100) NOT NULL
);

CREATE TABLE ST_GroupUserRole_User_Rel (
  groupUserRoleId INT NOT NULL,
  userId          INT NOT NULL
);

CREATE TABLE ST_GroupUserRole_Group_Rel
(
  groupUserRoleId INT NOT NULL,
  groupId         INT NOT NULL
);

CREATE TABLE st_instance_modelused
(
  instanceId VARCHAR(50)               NOT NULL,
  modelId    VARCHAR(50)               NOT NULL,
  objectId   VARCHAR(50) DEFAULT ('0') NOT NULL
);

CREATE TABLE ST_UserFavoriteSpaces
(
  id      INT NOT NULL,
  userid  INT NOT NULL,
  spaceid INT NOT NULL
);

ALTER TABLE Personalization  ADD
CONSTRAINT PK_Personalization PRIMARY KEY
  (
    id
  );

ALTER TABLE ST_AccessLevel  ADD CONSTRAINT PK_AccessLevel PRIMARY KEY (id);
ALTER TABLE ST_AccessLevel ADD CONSTRAINT UN_AccessLevel_1 UNIQUE (name);

ALTER TABLE ST_User ADD CONSTRAINT PK_User PRIMARY KEY (id);
ALTER TABLE ST_User ADD CONSTRAINT UN_User_1 UNIQUE (specificId, domainId);
ALTER TABLE ST_User ADD CONSTRAINT UN_User_2 UNIQUE (login, domainId);
ALTER TABLE ST_User ADD CONSTRAINT FK_User_1 FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel (id);

ALTER TABLE ST_Group  ADD CONSTRAINT PK_Group PRIMARY KEY (id);
ALTER TABLE ST_Group ADD CONSTRAINT UN_Group_1 UNIQUE (specificId, domainId);
ALTER TABLE ST_Group ADD CONSTRAINT UN_Group_2 UNIQUE (superGroupId, name, domainId);
ALTER TABLE ST_Group ADD CONSTRAINT FK_Group_1 FOREIGN KEY (superGroupId) REFERENCES ST_Group (id);

ALTER TABLE ST_Group_User_Rel  ADD CONSTRAINT PK_Group_User_Rel PRIMARY KEY (groupId, userId);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT FK_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES ST_Group (id);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT FK_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id);

ALTER TABLE ST_Space  ADD CONSTRAINT PK_Space PRIMARY KEY (id);
ALTER TABLE ST_Space ADD CONSTRAINT UN_Space_1 UNIQUE (domainFatherId, name);
ALTER TABLE ST_Space ADD CONSTRAINT FK_Space_1 FOREIGN KEY (createdBy) REFERENCES ST_User (id);
ALTER TABLE ST_Space ADD CONSTRAINT FK_Space_2 FOREIGN KEY (domainFatherId) REFERENCES ST_Space (id);

ALTER TABLE ST_ComponentInstance  ADD CONSTRAINT PK_ComponentInstance PRIMARY KEY (id);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT UN_ComponentInstance_1 UNIQUE (spaceId, name);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT FK_ComponentInstance_1 FOREIGN KEY (spaceId) REFERENCES ST_Space (id);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT FK_ComponentInstance_2 FOREIGN KEY (createdBy) REFERENCES ST_User (id);

ALTER TABLE ST_Instance_Data  ADD CONSTRAINT PK_Instance_Data PRIMARY KEY (id);
ALTER TABLE ST_Instance_Data ADD CONSTRAINT UN_Instance_Data_1 UNIQUE (componentId, name);
ALTER TABLE ST_Instance_Data ADD CONSTRAINT FK_Instance_Data_1 FOREIGN KEY (componentId) REFERENCES ST_ComponentInstance (id);

ALTER TABLE ST_UserRole  ADD CONSTRAINT PK_UserRole PRIMARY KEY (id);
ALTER TABLE ST_UserRole ADD CONSTRAINT UN_UserRole_1 UNIQUE (instanceId, roleName, isInherited, objectId);
ALTER TABLE ST_UserRole ADD CONSTRAINT FK_UserRole_1 FOREIGN KEY (instanceId) REFERENCES ST_ComponentInstance (id);

ALTER TABLE ST_UserRole_User_Rel  ADD CONSTRAINT PK_UserRole_User_Rel PRIMARY KEY (userRoleId, userId);
ALTER TABLE ST_UserRole_User_Rel ADD CONSTRAINT FK_UserRole_User_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole (id);
ALTER TABLE ST_UserRole_User_Rel ADD CONSTRAINT FK_UserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id);

ALTER TABLE ST_UserRole_Group_Rel  ADD CONSTRAINT PK_UserRole_Group_Rel PRIMARY KEY (userRoleId, groupId);
ALTER TABLE ST_UserRole_Group_Rel ADD CONSTRAINT FK_UserRole_Group_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole (id);
ALTER TABLE ST_UserRole_Group_Rel ADD CONSTRAINT FK_UserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group (id);

ALTER TABLE ST_SpaceUserRole  ADD CONSTRAINT PK_SpaceUserRole PRIMARY KEY (id);
ALTER TABLE ST_SpaceUserRole ADD CONSTRAINT UN_SpaceUserRole_1 UNIQUE (spaceId, roleName, isInherited);
ALTER TABLE ST_SpaceUserRole ADD CONSTRAINT FK_SpaceUserRole_1 FOREIGN KEY (spaceId) REFERENCES ST_Space (id);

ALTER TABLE ST_SpaceUserRole_User_Rel  ADD CONSTRAINT PK_SpaceUserRole_User_Rel PRIMARY KEY (spaceUserRoleId, userId);
ALTER TABLE ST_SpaceUserRole_User_Rel ADD CONSTRAINT FK_SpaceUserRole_User_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole (id);
ALTER TABLE ST_SpaceUserRole_User_Rel ADD CONSTRAINT FK_SpaceUserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id);

ALTER TABLE ST_SpaceUserRole_Group_Rel  ADD CONSTRAINT PK_SpaceUserRole_Group_Rel PRIMARY KEY (spaceUserRoleId, groupId);
ALTER TABLE ST_SpaceUserRole_Group_Rel ADD CONSTRAINT FK_SpaceUserRole_Group_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole (id);
ALTER TABLE ST_SpaceUserRole_Group_Rel ADD CONSTRAINT FK_SpaceUserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group (id);

ALTER TABLE DomainSP_Group  ADD CONSTRAINT PK_DomainSP_Group PRIMARY KEY (id);
ALTER TABLE DomainSP_Group ADD CONSTRAINT UN_DomainSP_Group_1 UNIQUE (superGroupId, name);
ALTER TABLE DomainSP_Group ADD CONSTRAINT FK_DomainSP_Group_1 FOREIGN KEY (superGroupId) REFERENCES DomainSP_Group (id);

ALTER TABLE DomainSP_User  ADD CONSTRAINT PK_DomainSP_User PRIMARY KEY (id);
ALTER TABLE DomainSP_User ADD CONSTRAINT UN_DomainSP_User_1 UNIQUE (login);

ALTER TABLE DomainSP_Group_User_Rel ADD CONSTRAINT FK_DomainSP_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES DomainSP_Group (id);
ALTER TABLE DomainSP_Group_User_Rel ADD CONSTRAINT FK_DomainSP_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES DomainSP_User (id);

ALTER TABLE ST_Domain  ADD CONSTRAINT PK_ST_Domain PRIMARY KEY (id);

ALTER TABLE DomainSP_Group_User_Rel  ADD CONSTRAINT PK_DomainSP_Group_User_Rel PRIMARY KEY (groupId, userId);

ALTER TABLE ST_LongText ADD CONSTRAINT PK_ST_LongText PRIMARY KEY (id, orderNum);

ALTER TABLE st_instance_modelused ADD
CONSTRAINT PK_st_instance_modelused PRIMARY KEY
  (
    instanceId,
    modelId,
    objectId
  );

ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT PK_UserFavoriteSpaces PRIMARY KEY (id);
ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT FK_UserFavoriteSpaces_1 FOREIGN KEY (userid) REFERENCES ST_User (id);
ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT FK_UserFavoriteSpaces_2 FOREIGN KEY (spaceid) REFERENCES ST_Space (id);

INSERT INTO ST_AccessLevel (id, name) VALUES ('U', 'User');
INSERT INTO ST_AccessLevel (id, name) VALUES ('A', 'Administrator');
INSERT INTO ST_AccessLevel (id, name) VALUES ('G', 'Guest');
INSERT INTO ST_AccessLevel (id, name) VALUES ('R', 'Removed');
INSERT INTO ST_AccessLevel (id, name) VALUES ('K', 'KMManager');
INSERT INTO ST_AccessLevel (id, name) VALUES ('D', 'DomainManager');

INSERT INTO ST_User (id, specificId, domainId, lastName, login, accessLevel, state, stateSaveDate)
VALUES (0, '0', 0, 'Administrateur', '${ADMINLOGIN}', 'A', 'VALID', CURRENT_TIMESTAMP);

INSERT INTO DomainSP_User (id, lastName, login, password)
VALUES (0, 'Administrateur', '${ADMINLOGIN}', '${ADMINPASSWD}');

INSERT INTO ST_Domain (id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
VALUES (-1, 'internal', 'Do not remove - Used by Silverpeas engine', '-', '-', '-', '0', '');

INSERT INTO ST_Domain (id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
VALUES (0, 'domainSilverpeas', 'default domain for Silverpeas',
        'com.stratelia.silverpeas.domains.domainSP',
        'com.silverpeas.domains.silverpeasdriver.SilverpeasDomainDriver', 'autDomainSP', '0',
        '${URLSERVER}');

CREATE TABLE ST_NotifChannel (
  id               INT          NOT NULL,
  name             VARCHAR(20)  NOT NULL,
  description      VARCHAR(200) NULL,
  couldBeAdded     CHAR(1)      NOT NULL DEFAULT ('Y'),
  fromAvailable    CHAR(1)      NOT NULL DEFAULT ('N'),
  subjectAvailable CHAR(1)      NOT NULL DEFAULT ('N')
);

CREATE TABLE ST_NotifAddress (
  id             INT          NOT NULL,
  userId         INT          NOT NULL,
  notifName      VARCHAR(20)  NOT NULL,
  notifChannelId INT          NOT NULL,
  address        VARCHAR(250) NOT NULL,
  usage          VARCHAR(20)  NULL,
  priority       INT          NOT NULL
);

CREATE TABLE ST_NotifDefaultAddress (
  id             INT NOT NULL,
  userId         INT NOT NULL,
  notifAddressId INT NOT NULL
);

CREATE TABLE ST_NotifPreference (
  id                  INT NOT NULL,
  notifAddressId      INT NOT NULL,
  componentInstanceId INT NOT NULL,
  userId              INT NOT NULL,
  messageType         INT NOT NULL
);

CREATE TABLE ST_NotifSended (
  notifId     INT          NOT NULL,
  userId      INT          NOT NULL,
  messageType INT          NULL,
  notifDate   CHAR(13)     NOT NULL,
  title       VARCHAR(255) NULL,
  link        VARCHAR(255) NULL,
  sessionId   VARCHAR(255) NULL,
  componentId VARCHAR(255) NULL,
  body        INT          NULL
);

CREATE TABLE ST_NotifSendedReceiver (
  notifId INT NOT NULL,
  userId  INT NOT NULL
);

ALTER TABLE ST_NotifChannel ADD CONSTRAINT PK_NotifChannel PRIMARY KEY (id);

ALTER TABLE ST_NotifDefaultAddress ADD CONSTRAINT PK_ST_NotifDefaultAddress PRIMARY KEY (id);
ALTER TABLE ST_NotifDefaultAddress ADD CONSTRAINT FK_NotifDefaultAddress_1 FOREIGN KEY (userId) REFERENCES ST_User (id);

ALTER TABLE ST_NotifPreference ADD CONSTRAINT PK_NotifAddr_Component PRIMARY KEY (id);
ALTER TABLE ST_NotifPreference ADD CONSTRAINT FK_NotifPreference_1 FOREIGN KEY (componentInstanceId) REFERENCES ST_ComponentInstance (id);
ALTER TABLE ST_NotifPreference ADD CONSTRAINT FK_NotifPreference_2 FOREIGN KEY (userId) REFERENCES ST_User (id);

ALTER TABLE ST_NotifAddress ADD CONSTRAINT PK_NotifAddress PRIMARY KEY (id);
ALTER TABLE ST_NotifAddress ADD CONSTRAINT FK_NotifAddress_1 FOREIGN KEY (notifChannelId) REFERENCES ST_NotifChannel (id);
ALTER TABLE ST_NotifAddress ADD CONSTRAINT FK_NotifAddress_2 FOREIGN KEY (userId) REFERENCES ST_User (id);

ALTER TABLE ST_NotifSended ADD CONSTRAINT PK_NotifSended PRIMARY KEY (notifId);

ALTER TABLE ST_NotifSendedReceiver ADD CONSTRAINT PK_NotifSendedReceiver PRIMARY KEY (notifId, userId);
