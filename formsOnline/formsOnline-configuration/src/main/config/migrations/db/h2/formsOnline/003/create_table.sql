CREATE TABLE SC_FormsOnline_Forms
(
    id                          integer      NOT NULL,
    xmlFormName                 VARCHAR(80)  NOT NULL,
    name                        VARCHAR(200) NOT NULL,
    description                 VARCHAR(1000),
    creationDate                date         NOT NULL,
    state                       integer      NOT NULL,
    instanceId                  VARCHAR(80)  NOT NULL,
    alreadyUsed                 smallint     NOT NULL DEFAULT 0,
    creatorId                   VARCHAR(20)  NOT NULL,
    title                       VARCHAR(200) NOT NULL DEFAULT '',
    hierarchicalValidation      BOOLEAN      NOT NULL DEFAULT false,
    formInstExchangeReceiver    VARCHAR(200)          DEFAULT NULL,
    deleteAfterFormInstExchange BOOLEAN      NOT NULL DEFAULT false
);

CREATE TABLE SC_FormsOnline_FormInstances
(
    id           integer     NOT NULL,
    formId       integer     NOT NULL,
    state        integer     NOT NULL,
    creatorId    VARCHAR(20) NOT NULL,
    creationDate date        NOT NULL,
    instanceId   VARCHAR(50) NOT NULL
);

CREATE TABLE SC_FormsOnline_FormInstVali
(
    id                INT                         NOT NULL,
    formInstId        INT                         NOT NULL,
    validationBy      VARCHAR(40)                 NOT NULL,
    validationType    VARCHAR(20)                 NOT NULL,
    status            VARCHAR(20)                 NOT NULL,
    validationDate    TIMESTAMP                   NOT NULL,
    validationComment VARCHAR(4000) DEFAULT NULL,
    follower          BOOLEAN       DEFAULT FALSE NOT NULL
);

CREATE TABLE SC_FormsOnline_GroupRights
(
    formId     integer     NOT NULL,
    instanceId VARCHAR(80) NOT NULL,
    groupId    VARCHAR(20) NOT NULL,
    rightType  VARCHAR(1)  NOT NULL
);

CREATE TABLE SC_FormsOnline_UserRights
(
    formId     integer     NOT NULL,
    instanceId VARCHAR(80) NOT NULL,
    userId     VARCHAR(20) NOT NULL,
    rightType  VARCHAR(1)  NOT NULL
);
