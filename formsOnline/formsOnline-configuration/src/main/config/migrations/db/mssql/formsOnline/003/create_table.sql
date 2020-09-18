CREATE TABLE SC_FormsOnline_Forms
(
    id                          int          NOT NULL,
    xmlFormName                 varchar(80)  NOT NULL,
    name                        varchar(200) NOT NULL,
    description                 varchar(1000),
    creationDate                datetime     NOT NULL,
    state                       int          NOT NULL,
    instanceId                  varchar(80)  NOT NULL,
    alreadyUsed                 int          NOT NULL DEFAULT 0,
    creatorId                   varchar(20)  NOT NULL,
    title                       varchar(200) NOT NULL DEFAULT '',
    hierarchicalValidation      BIT          NOT NULL DEFAULT 0,
    formInstExchangeReceiver    VARCHAR(200)          DEFAULT NULL,
    deleteAfterFormInstExchange BIT          NOT NULL DEFAULT 0
);

CREATE TABLE SC_FormsOnline_FormInstances
(
    id           int         NOT NULL,
    formId       int         NOT NULL,
    state        int         NOT NULL,
    creatorId    varchar(20) NOT NULL,
    creationDate datetime    NOT NULL,
    instanceId   varchar(50) NOT NULL
);

CREATE TABLE SC_FormsOnline_FormInstVali
(
    id                INT                     NOT NULL,
    formInstId        INT                     NOT NULL,
    validationBy      VARCHAR(40)             NOT NULL,
    validationType    VARCHAR(20)             NOT NULL,
    status            VARCHAR(20)             NOT NULL,
    validationDate    DATETIME                NOT NULL,
    validationComment VARCHAR(4000) DEFAULT NULL,
    follower          BIT           DEFAULT 0 NOT NULL
);

CREATE TABLE SC_FormsOnline_GroupRights
(
    formId     int         NOT NULL,
    instanceId varchar(80) NOT NULL,
    groupId    varchar(20) NOT NULL,
    rightType  varchar(1)  NOT NULL
);

CREATE TABLE SC_FormsOnline_UserRights
(
    formId     int         NOT NULL,
    instanceId varchar(80) NOT NULL,
    userId     varchar(20) NOT NULL,
    rightType  varchar(1)  NOT NULL
);
