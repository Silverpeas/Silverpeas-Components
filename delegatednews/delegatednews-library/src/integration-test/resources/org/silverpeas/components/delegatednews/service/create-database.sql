CREATE TABLE IF NOT EXISTS ST_AccessLevel
(
    id   CHAR(1)      NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT PK_AccessLevel PRIMARY KEY (id),
    CONSTRAINT UN_AccessLevel_1 UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS ST_User
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
    stateSaveDate                 TIMESTAMP            NOT NULL,
    notifManualReceiverLimit      INT,
    sensitiveData                 BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT PK_User PRIMARY KEY (id),
    CONSTRAINT UN_User_1 UNIQUE(specificId, domainId),
    CONSTRAINT UN_User_2 UNIQUE(login, domainId),
    CONSTRAINT FK_User_1 FOREIGN KEY(accessLevel) REFERENCES ST_AccessLevel(id)
);

CREATE TABLE ST_ComponentInstance
(
        id            	int           PRIMARY KEY,
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

CREATE TABLE sc_delegatednews_news (
        pubId		int		not null ,
        instanceId		varchar(50)     not null,
        status varchar(100) not null,
        contributorId varchar(50) not null,
        validatorId varchar(50) null,
        validationDate timestamp(0) null,
        beginDate timestamp(0) null,
        endDate timestamp(0) null,
        newsOrder int not null DEFAULT (0)
);

ALTER TABLE sc_delegatednews_news
        add constraint pk_delegatednews_news
        primary key (pubId);
