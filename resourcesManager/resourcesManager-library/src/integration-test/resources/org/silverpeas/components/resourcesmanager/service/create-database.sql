CREATE TABLE ST_User
(
    id                            INT                   NOT NULL,
    domainId                      INT                   NOT NULL,
    specificId                    VARCHAR(500)          NOT NULL,
    firstName                     VARCHAR(100),
    lastName                      VARCHAR(100)          NOT NULL,
    email                         VARCHAR(100),
    login                         VARCHAR(100)          NOT NULL,
    loginMail                     VARCHAR(100),
    accessLevel                   CHAR(1) DEFAULT 'U'   NOT NULL,
    loginquestion                 VARCHAR(200),
    loginanswer                   VARCHAR(200),
    creationDate                  TIMESTAMP,
    saveDate                      TIMESTAMP,
    version                       INT     DEFAULT 0     NOT NULL,
    tosAcceptanceDate             TIMESTAMP,
    lastLoginDate                 TIMESTAMP,
    nbSuccessfulLoginAttempts     INT     DEFAULT 0     NOT NULL,
    lastLoginCredentialUpdateDate TIMESTAMP,
    expirationDate                TIMESTAMP,
    state                         VARCHAR(30)           NOT NULL,
    stateSaveDate                 TIMESTAMP             NOT NULL,
    notifManualReceiverLimit      INT,
    sensitiveData                 BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE ST_Group
(
    id            INT          NOT NULL,
    domainId      INT          NOT NULL,
    specificId    VARCHAR(500) NOT NULL,
    superGroupId  INT,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(400),
    synchroRule   VARCHAR(100),
    creationDate  timestamp,
    saveDate      timestamp,
    state         varchar(30)  NOT NULL,
    stateSaveDate timestamp    NOT NULL,
    CONSTRAINT PK_Group PRIMARY KEY (id),
    CONSTRAINT UN_Group_1 UNIQUE (specificId, domainId),
    CONSTRAINT UN_Group_2 UNIQUE (superGroupId, name, domainId),
    CONSTRAINT FK_Group_1 FOREIGN KEY (superGroupId) REFERENCES ST_Group (id)
);

CREATE TABLE ST_Group_User_Rel
(
    groupId int NOT NULL,
    userId  int NOT NULL
);

CREATE TABLE ST_UserRole
(
    id          int             NOT NULL,
    instanceId  int             NOT NULL,
    name        varchar(100)    NULL,
    roleName    varchar(100)    NOT NULL,
    description varchar(400),
    isInherited int default (0) NOT NULL,
    objectId    int,
    objectType  varchar(50)
);

CREATE TABLE ST_UserRole_User_Rel
(
    userRoleId int NOT NULL,
    userId     int NOT NULL
);

CREATE TABLE ST_UserRole_Group_Rel
(
    userRoleId int NOT NULL,
    groupId    int NOT NULL
);

CREATE TABLE ST_ComponentInstance
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
    isInheritanceBlocked int default (0) NOT NULL
);

CREATE TABLE ST_ComponentInstanceI18N
(
    id          int          NOT NULL,
    componentId int          NOT NULL,
    lang        char(2)      NOT NULL,
    name        varchar(100) NOT NULL,
    description varchar(400)
);

CREATE TABLE ST_Instance_Data
(
    id          int          NOT NULL,
    componentId int          NOT NULL,
    name        varchar(100) NOT NULL,
    label       varchar(100) NOT NULL,
    value       varchar(1000)
);

CREATE TABLE SC_Resources_Category
(
    id            BIGINT      NOT NULL,
    instanceId    VARCHAR(50) NOT NULL,
    name          VARCHAR(50) NOT NULL,
    creationdate  VARCHAR(20) NOT NULL,
    updatedate    VARCHAR(20) NOT NULL,
    bookable      INT,
    form          VARCHAR(50),
    responsibleid INT,
    createrid     VARCHAR(50),
    updaterid     VARCHAR(50),
    description   VARCHAR(2000)
)
;

CREATE TABLE SC_Resources_Resource
(
    id            BIGINT       NOT NULL,
    instanceId    VARCHAR(50)  NOT NULL,
    categoryid    BIGINT       NOT NULL,
    name          VARCHAR(128) NOT NULL,
    creationdate  VARCHAR(20)  NOT NULL,
    updatedate    VARCHAR(20)  NOT NULL,
    bookable      INT,
    responsibleid INT,
    createrid     VARCHAR(50),
    updaterid     VARCHAR(50),
    description   VARCHAR(2000)
)
;

CREATE TABLE SC_Resources_Reservation
(
    id           BIGINT       NOT NULL,
    instanceId   VARCHAR(50)  NOT NULL,
    evenement    VARCHAR(128) NOT NULL,
    userId       INT          NOT NULL,
    creationdate VARCHAR(20)  NOT NULL,
    updatedate   VARCHAR(20)  NOT NULL,
    begindate    VARCHAR(20)  NOT NULL,
    enddate      VARCHAR(20)  NOT NULL,
    reason       VARCHAR(2000),
    place        VARCHAR(128),
    status       VARCHAR(50)  NULL
)
;

CREATE TABLE SC_Resources_ReservedResource
(
    reservationId BIGINT      NOT NULL,
    resourceId    BIGINT      NOT NULL,
    status        VARCHAR(50) NULL
)
;

CREATE TABLE SC_Resources_Managers
(
    resourceId BIGINT NOT NULL,
    managerId  BIGINT NOT NULL
)
;

ALTER TABLE SC_Resources_Category
    ADD CONSTRAINT PK_Resources_Category PRIMARY KEY
        (
         id
            )
;

ALTER TABLE SC_Resources_Resource
    ADD CONSTRAINT PK_Resources_Resource PRIMARY KEY
        (
         id
            )
;

ALTER TABLE SC_Resources_Reservation
    ADD CONSTRAINT PK_Resources_Reservation PRIMARY KEY
        (
         id
            )
;

ALTER TABLE SC_Resources_ReservedResource
    ADD CONSTRAINT PK_Resources_ReservedResource PRIMARY KEY
        (
         reservationId,
         resourceId
            )
;

ALTER TABLE SC_Resources_Managers
    ADD CONSTRAINT PK_Resources_Managers PRIMARY KEY
        (
         resourceId,
         managerId
            )
;
