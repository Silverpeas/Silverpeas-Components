-- User and groups of users

CREATE TABLE IF NOT EXISTS ST_AccessLevel
(
    id   CHAR(1)      NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT PK_AccessLevel PRIMARY KEY (id),
    CONSTRAINT UN_AccessLevel_1 UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS ST_User
(
    id                            INT                 NOT NULL,
    domainId                      INT                 NOT NULL,
    specificId                    VARCHAR(500)        NOT NULL,
    firstName                     VARCHAR(100),
    lastName                      VARCHAR(100)        NOT NULL,
    email                         VARCHAR(100),
    login                         VARCHAR(50)         NOT NULL,
    loginMail                     VARCHAR(100),
    accessLevel                   CHAR(1) DEFAULT 'U' NOT NULL,
    loginquestion                 VARCHAR(200),
    loginanswer                   VARCHAR(200),
    creationDate                  TIMESTAMP,
    saveDate                      TIMESTAMP,
    version                       INT     DEFAULT 0   NOT NULL,
    tosAcceptanceDate             TIMESTAMP,
    lastLoginDate                 TIMESTAMP,
    nbSuccessfulLoginAttempts     INT     DEFAULT 0   NOT NULL,
    lastLoginCredentialUpdateDate TIMESTAMP,
    expirationDate                TIMESTAMP,
    state                         VARCHAR(30)         NOT NULL,
    stateSaveDate                 TIMESTAMP           NOT NULL,
    notifManualReceiverLimit      INT,
    CONSTRAINT PK_User PRIMARY KEY (id),
    CONSTRAINT UN_User_1 UNIQUE (specificId, domainId),
    CONSTRAINT UN_User_2 UNIQUE (login, domainId),
    CONSTRAINT FK_User_1 FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel (id)
);

CREATE TABLE IF NOT EXISTS ST_Group
(
    id            int          NOT NULL,
    domainId      int          NOT NULL,
    specificId    varchar(500) NOT NULL,
    superGroupId  int,
    name          varchar(100) NOT NULL,
    description   varchar(400),
    synchroRule   varchar(2000),
    creationDate  timestamp,
    saveDate      timestamp,
    state         varchar(30)  NOT NULL,
    stateSaveDate timestamp    NOT NULL,
    CONSTRAINT PK_Group PRIMARY KEY (id),
    CONSTRAINT UN_Group_1 UNIQUE (specificId, domainId),
    CONSTRAINT UN_Group_2 UNIQUE (superGroupId, name, domainId),
    CONSTRAINT FK_Group_1 FOREIGN KEY (superGroupId) REFERENCES ST_Group (id)
);

CREATE TABLE IF NOT EXISTS ST_Group_User_Rel
(
    groupId int NOT NULL,
    userId  int NOT NULL,
    CONSTRAINT PK_Group_User_Rel PRIMARY KEY (groupId, userId),
    CONSTRAINT FK_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES ST_Group (id),
    CONSTRAINT FK_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

-- Organization in spaces and components

CREATE TABLE IF NOT EXISTS ST_Space
(
    id                   int             NOT NULL,
    domainFatherId       int,
    name                 varchar(100)    NOT NULL,
    description          varchar(400),
    createdBy            int,
    firstPageType        int             NOT NULL,
    firstPageExtraParam  varchar(400),
    orderNum             int DEFAULT (0) NOT NULL,
    createTime           varchar(20),
    updateTime           varchar(20),
    removeTime           varchar(20),
    spaceStatus          char(1),
    updatedBy            int,
    removedBy            int,
    lang                 char(2),
    isInheritanceBlocked int default (0) NOT NULL,
    look                 varchar(50),
    displaySpaceFirst    smallint,
    isPersonal           smallint,
    CONSTRAINT PK_Space PRIMARY KEY (id),
    CONSTRAINT UN_Space_1 UNIQUE (domainFatherId, name),
    CONSTRAINT FK_Space_1 FOREIGN KEY (createdBy) REFERENCES ST_User (id),
    CONSTRAINT FK_Space_2 FOREIGN KEY (domainFatherId) REFERENCES ST_Space (id)
);

CREATE TABLE IF NOT EXISTS ST_SpaceI18N
(
    id          INT          NOT NULL,
    spaceId     INT          NOT NULL,
    lang        CHAR(2)      NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(400)
);

CREATE TABLE IF NOT EXISTS ST_ComponentInstance
(
    id                   int             NOT NULL,
    spaceId              int             NOT NULL,
    name                 varchar(100)    NOT NULL,
    componentName        varchar(100)    NOT NULL,
    description          varchar(400),
    createdBy            int,
    orderNum             int DEFAULT (0) NOT NULL,
    createTime           varchar(20),
    updateTime           varchar(20),
    removeTime           varchar(20),
    componentStatus      char(1),
    updatedBy            int,
    removedBy            int,
    isPublic             int DEFAULT (0) NOT NULL,
    isHidden             int DEFAULT (0) NOT NULL,
    lang                 char(2),
    isInheritanceBlocked int default (0) NOT NULL,
    CONSTRAINT PK_ComponentInstance PRIMARY KEY (id),
    CONSTRAINT UN_ComponentInstance_1 UNIQUE (spaceId, name),
    CONSTRAINT FK_ComponentInstance_1 FOREIGN KEY (spaceId) REFERENCES ST_Space (id),
    CONSTRAINT FK_ComponentInstance_2 FOREIGN KEY (createdBy) REFERENCES ST_User (id)
);

CREATE TABLE IF NOT EXISTS ST_Instance_Data
(
    id          INT          NOT NULL,
    componentId INT          NOT NULL,
    name        VARCHAR(100) NOT NULL,
    label       VARCHAR(100) NOT NULL,
    value       VARCHAR(400),
    CONSTRAINT PK_Instance_Data PRIMARY KEY (id),
    CONSTRAINT UN_Instance_Data_1 UNIQUE (componentId, name),
    CONSTRAINT FK_Instance_Data_1 FOREIGN KEY (componentId) REFERENCES ST_ComponentInstance (id)
);

CREATE TABLE IF NOT EXISTS ST_ComponentInstanceI18N
(
    id          INT          NOT NULL,
    componentId INT          NOT NULL,
    lang        CHAR(2)      NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(400)
);

CREATE TABLE IF NOT EXISTS st_quota
(
    id           INT8 PRIMARY KEY NOT NULL,
    quotaType    VARCHAR(50)      NOT NULL,
    resourceId   VARCHAR(50)      NOT NULL,
    minCount     INT8             NOT NULL,
    maxCount     INT8             NOT NULL,
    currentCount INT8             NOT NULL,
    saveDate     TIMESTAMP        NOT NULL
);

CREATE TABLE IF NOT EXISTS ST_Token
(
    id         int8        NOT NULL,
    tokenType  varchar(50) NOT NULL,
    resourceId varchar(50) NOT NULL,
    token      varchar(50) NOT NULL,
    saveCount  int         NOT NULL,
    saveDate   timestamp   NOT NULL,
    CONSTRAINT const_st_token_pk PRIMARY KEY (id)
);

-- Required by component instance deletion as the mechanism invokes transverse services to clean up
-- potential resources allocated by the component instance in deletion

CREATE TABLE IF NOT EXISTS sb_node_node
(
    nodeId           int              NOT NULL,
    nodeName         varchar(1000)    NOT NULL,
    nodeDescription  varchar(2000)    NULL,
    nodeCreationDate varchar(10)      NOT NULL,
    nodeCreatorId    varchar(100)     NOT NULL,
    nodePath         varchar(1000)    NOT NULL,
    nodeLevelNumber  int              NOT NULL,
    nodeFatherId     int              NOT NULL,
    modelId          varchar(1000)    NULL,
    nodeStatus       varchar(1000)    NULL,
    instanceId       varchar(50)      NOT NULL,
    type             varchar(50)      NULL,
    orderNumber      int DEFAULT (0)  NULL,
    lang             char(2),
    rightsDependsOn  int default (-1) NOT NULL,
    CONSTRAINT PK_Node_Node PRIMARY KEY (nodeId, instanceId)
);

CREATE TABLE IF NOT EXISTS sb_node_nodei18N
(
    id              int PRIMARY KEY NOT NULL,
    nodeId          int             NOT NULL,
    lang            char(2)         NOT NULL,
    nodeName        varchar(1000)   NOT NULL,
    nodeDescription varchar(2000)
);

CREATE TABLE IF NOT EXISTS sb_coordinates_coordinates
(
    coordinatesId           int         NOT NULL,
    nodeId                  int         NOT NULL,
    coordinatesLeaf         varchar(50) NOT NULL,
    coordinatesDisplayOrder int         NULL,
    instanceId              varchar(50) NOT NULL,
    CONSTRAINT PK_Coordinates_Coordinates PRIMARY KEY (coordinatesId, nodeId, instanceId)
);

CREATE TABLE IF NOT EXISTS SB_Publication_Publi
(
    pubId                int PRIMARY KEY NOT NULL,
    infoId               varchar(50)     NULL,
    pubName              varchar(400)    NOT NULL,
    pubDescription       varchar(2000)   NULL,
    pubCreationDate      varchar(10)     NOT NULL,
    pubBeginDate         varchar(10)     NOT NULL,
    pubEndDate           varchar(10)     NOT NULL,
    pubCreatorId         varchar(100)    NOT NULL,
    pubImportance        int             NULL,
    pubVersion           varchar(100)    NULL,
    pubKeywords          varchar(1000)   NULL,
    pubContent           varchar(2000)   NULL,
    pubStatus            varchar(100)    NULL,
    pubUpdateDate        varchar(10)     NULL,
    instanceId           varchar(50)     NOT NULL,
    pubUpdaterId         varchar(100)    NULL,
    pubValidateDate      varchar(10)     NULL,
    pubValidatorId       varchar(50)     NULL,
    pubBeginHour         varchar(5)      NULL,
    pubEndHour           varchar(5)      NULL,
    pubAuthor            varchar(50)     NULL,
    pubTargetValidatorId varchar(50)     NULL,
    pubCloneId           int DEFAULT (-1),
    pubCloneStatus       varchar(50)     NULL,
    lang                 char(2)         NULL,
    pubdraftoutdate      varchar(10)     NULL
);

CREATE TABLE IF NOT EXISTS SB_Publication_PubliI18N
(
    id          int PRIMARY KEY NOT NULL,
    pubId       int             NOT NULL,
    lang        char(2)         NOT NULL,
    name        varchar(400)    NOT NULL,
    description varchar(2000),
    keywords    varchar(1000)
);

CREATE TABLE IF NOT EXISTS SB_Publication_PubliFather
(
    pubId       INT             NOT NULL,
    nodeId      INT             NOT NULL,
    instanceId  VARCHAR(50)     NOT NULL,
    aliasUserId INT,
    aliasDate   VARCHAR(20),
    pubOrder    INT DEFAULT (0) NULL,
    CONSTRAINT PK_Publication_PubliFather PRIMARY KEY (pubId, nodeId, instanceId)
);

CREATE TABLE IF NOT EXISTS SB_SeeAlso_Link
(
    id               INT PRIMARY KEY NOT NULL,
    objectId         INT             NOT NULL,
    objectInstanceId VARCHAR(50)     NOT NULL,
    targetId         INT             NOT NULL,
    targetInstanceId VARCHAR(50)     NOT NULL
);

CREATE TABLE IF NOT EXISTS SB_Publication_Validation
(
    id           int PRIMARY KEY NOT NULL,
    pubId        int             NOT NULL,
    instanceId   varchar(50)     NOT NULL,
    userId       int             NOT NULL,
    decisionDate varchar(20)     NOT NULL,
    decision     varchar(50)     NOT NULL
);

CREATE TABLE SB_Notation_Notation
(
    id           INT         NOT NULL,
    instanceId   VARCHAR(50) NOT NULL,
    externalId   VARCHAR(50) NOT NULL,
    externalType VARCHAR(50) NOT NULL,
    author       VARCHAR(50) NOT NULL,
    note         INT         NOT NULL,
    CONSTRAINT PK_SB_Notation_Notation PRIMARY KEY (id),
    CONSTRAINT UN_SB_Notation_Notation UNIQUE (instanceId, externalId, externalType, author)
);

CREATE TABLE sb_comment_comment
(
    commentId               INT PRIMARY KEY NOT NULL,
    commentOwnerId          INT             NOT NULL,
    commentCreationDate     CHAR(10)        NOT NULL,
    commentModificationDate CHAR(10),
    commentComment          VARCHAR(2000)   NOT NULL,
    instanceId              VARCHAR(50)     NOT NULL,
    resourceType            VARCHAR(50)     NOT NULL,
    resourceId              VARCHAR(50)     NOT NULL
);

CREATE TABLE IF NOT EXISTS SB_Thumbnail_Thumbnail
(
    instanceId             VARCHAR(50)  NOT NULL,
    objectId               INT          NOT NULL,
    objectType             INT          NOT NULL,
    originalAttachmentName VARCHAR(250) NOT NULL,
    modifiedAttachmentName VARCHAR(250) NULL,
    mimeType               VARCHAR(250) NULL,
    xStart                 INT          NULL,
    yStart                 INT          NULL,
    xLength                INT          NULL,
    yLength                INT          NULL,
    CONSTRAINT PK_Thumbnail_Thumbnail PRIMARY KEY (objectId, objectType, instanceId)
);

CREATE TABLE subscribe
(
    subscriberId       VARCHAR(100) NOT NULL,
    subscriberType     VARCHAR(50)  NOT NULL,
    subscriptionMethod VARCHAR(50)  NOT NULL,
    resourceId         VARCHAR(100) NOT NULL,
    resourceType       VARCHAR(50)  NOT NULL,
    space              VARCHAR(50)  NOT NULL,
    instanceId         VARCHAR(50)  NOT NULL,
    creatorId          VARCHAR(100) NOT NULL,
    creationDate       TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS SB_Statistic_History
(
    dateStat    varchar(10)  NOT NULL,
    heureStat   varchar(10)  NOT NULL,
    userId      varchar(100) NOT NULL,
    objectId    int          NOT NULL,
    componentId varchar(50)  NOT NULL,
    actionType  int          NOT NULL,
    objectType  varchar(50)  NOT NULL
);

-- User roles

CREATE TABLE IF NOT EXISTS ST_UserRole
(
    id          INT             NOT NULL,
    instanceId  INT             NOT NULL,
    name        VARCHAR(100)    NULL,
    roleName    VARCHAR(100)    NOT NULL,
    description VARCHAR(400),
    isInherited INT DEFAULT (0) NOT NULL,
    objectId    INT,
    objectType  VARCHAR(50),
    CONSTRAINT PK_UserRole PRIMARY KEY (id),
    CONSTRAINT UN_UserRole_1 UNIQUE (instanceId, roleName, isInherited, objectId),
    CONSTRAINT FK_UserRole_1 FOREIGN KEY (instanceId) REFERENCES ST_ComponentInstance (id)
);

CREATE TABLE IF NOT EXISTS ST_UserRole_User_Rel
(
    userRoleId INT NOT NULL,
    userId     INT NOT NULL,
    CONSTRAINT PK_UserRole_User_Rel PRIMARY KEY (userRoleId, userId),
    CONSTRAINT FK_UserRole_User_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole (id),
    CONSTRAINT FK_UserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE IF NOT EXISTS ST_UserRole_Group_Rel
(
    userRoleId int NOT NULL,
    groupId    int NOT NULL,
    CONSTRAINT PK_UserRole_Group_Rel PRIMARY KEY (userRoleId, groupId),
    CONSTRAINT FK_UserRole_Group_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole (id),
    CONSTRAINT FK_UserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group (id)
);

CREATE TABLE IF NOT EXISTS ST_SpaceUserRole
(
    id          INT             NOT NULL,
    spaceId     INT             NOT NULL,
    name        VARCHAR(100)    NULL,
    roleName    VARCHAR(100)    NOT NULL,
    description VARCHAR(400),
    isInherited INT DEFAULT (0) NOT NULL,
    CONSTRAINT PK_SpaceUserRole PRIMARY KEY (id),
    CONSTRAINT UN_SpaceUserRole_1 UNIQUE (spaceId, roleName, isInherited),
    CONSTRAINT FK_SpaceUserRole_1 FOREIGN KEY (spaceId) REFERENCES ST_Space (id)
);

CREATE TABLE IF NOT EXISTS ST_SpaceUserRole_User_Rel
(
    spaceUserRoleId INT NOT NULL,
    userId          INT NOT NULL,
    CONSTRAINT PK_SpaceUserRole_User_Rel PRIMARY KEY (spaceUserRoleId, userId),
    CONSTRAINT FK_SpaceUserRole_User_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole (id),
    CONSTRAINT FK_SpaceUserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User (id)
);

CREATE TABLE IF NOT EXISTS ST_SpaceUserRole_Group_Rel
(
    spaceUserRoleId int NOT NULL,
    groupId         int NOT NULL,
    CONSTRAINT PK_SpaceUserRole_Group_Rel PRIMARY KEY (spaceUserRoleId, groupId),
    CONSTRAINT FK_SpaceUserRole_Group_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole (id),
    CONSTRAINT FK_SpaceUserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group (id)

);

-- Community app

CREATE TABLE IF NOT EXISTS SC_Community
(
    id           VARCHAR(40) PRIMARY KEY,
    instanceId   VARCHAR(30)  NOT NULL,
    spaceId      VARCHAR(30)  NOT NULL,
    homePage     VARCHAR(400) NULL,
    homePageType INT          NULL,
    charterURL   VARCHAR(400) NULL,
    CONSTRAINT UN_COMMUNITY UNIQUE (instanceId, spaceId)
);

CREATE TABLE IF NOT EXISTS SC_Community_Membership
(
    id             VARCHAR(40) PRIMARY KEY,
    community      VARCHAR(40) NOT NULL,
    userId         INT         NOT NULL,
    status         VARCHAR(15) NOT NULL,
    joiningDate    TIMESTAMP   NULL,
    createDate     TIMESTAMP   NOT NULL,
    createdBy      VARCHAR(40) NOT NULL,
    lastUpdateDate TIMESTAMP   NOT NULL,
    lastUpdatedBy  VARCHAR(40) NOT NULL,
    version        INT8        NOT NULL,
    CONSTRAINT FK_COMMUNITY FOREIGN KEY (community) REFERENCES SC_Community (id),
    CONSTRAINT FK_USER FOREIGN KEY (userId) REFERENCES st_user (id)
)