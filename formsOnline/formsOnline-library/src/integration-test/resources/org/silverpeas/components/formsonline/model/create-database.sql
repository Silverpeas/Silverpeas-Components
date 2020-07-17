CREATE TABLE SC_FormsOnline_Forms
(
    id                          integer                NOT NULL,
    xmlFormName                 character varying(80)  NOT NULL,
    name                        character varying(200) NOT NULL,
    description                 character varying(1000),
    creationDate                timestamp              NOT NULL,
    state                       integer                NOT NULL,
    instanceId                  character varying(80)  NOT NULL,
    alreadyUsed                 smallint               NOT NULL DEFAULT 0,
    creatorId                   character varying(20)  NOT NULL,
    title                       character varying(200) NOT NULL DEFAULT ''::character varying,
    hierarchicalValidation      BOOLEAN                NOT NULL DEFAULT false,
    formInstExchangeReceiver    VARCHAR(200)                    DEFAULT NULL,
    deleteAfterFormInstExchange BOOLEAN                NOT NULL DEFAULT false
);

CREATE TABLE SC_FormsOnline_FormInstances
(
    id           integer               NOT NULL,
    formId       integer               NOT NULL,
    state        integer               NOT NULL,
    creatorId    character varying(20) NOT NULL,
    creationDate timestamp             NOT NULL,
    instanceId   character varying(50) NOT NULL
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
    formId     integer               NOT NULL,
    instanceId character varying(80) NOT NULL,
    groupId    character varying(20) NOT NULL,
    rightType  character varying(1)  NOT NULL
);

CREATE TABLE SC_FormsOnline_UserRights
(
    formId     integer               NOT NULL,
    instanceId character varying(80) NOT NULL,
    userId     character varying(20) NOT NULL,
    rightType  character varying(1)  NOT NULL
);

ALTER TABLE SC_FormsOnline_Forms
    ADD CONSTRAINT PK_SC_FormsOnline_Forms PRIMARY KEY (id);

ALTER TABLE SC_FormsOnline_FormInstances
    ADD CONSTRAINT PK_SC_FormsOnline_FormInstances PRIMARY KEY (id);

ALTER TABLE SC_FormsOnline_FormInstances
    ADD CONSTRAINT FK_FormInstance FOREIGN KEY (formId) REFERENCES SC_FormsOnline_Forms (id);

ALTER TABLE SC_FormsOnline_FormInstVali
    ADD CONSTRAINT PK_SC_FormsOnline_FormInstVali PRIMARY KEY (id);

ALTER TABLE SC_FormsOnline_FormInstVali
    ADD CONSTRAINT FK_SC_FormsOnline_FormInstances_id FOREIGN KEY (formInstId) REFERENCES sc_formsonline_forminstances (id);

ALTER TABLE SC_FormsOnline_UserRights
    ADD CONSTRAINT FK_UserRights FOREIGN KEY (formId) REFERENCES SC_FormsOnline_Forms (id);

ALTER TABLE SC_FormsOnline_GroupRights
    ADD CONSTRAINT FK_GroupRights FOREIGN KEY (formId) REFERENCES SC_FormsOnline_Forms (id);

CREATE INDEX IND_SC_FormsOnline_UserRights_1
    ON SC_FormsOnline_UserRights (formId, instanceId, rightType);

CREATE INDEX IND_SC_FormsOnline_UserRights_2
    ON SC_FormsOnline_UserRights (rightType, userId);

CREATE INDEX IND_SC_FormsOnline_GroupRights_1
    ON SC_FormsOnline_GroupRights (formId, instanceId, rightType);

CREATE INDEX IND_SC_FormsOnline_GroupRights_2
    ON SC_FormsOnline_GroupRights (rightType, groupId);

CREATE TABLE ST_User
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
    notifManualReceiverLimit      INT
);
