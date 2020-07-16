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
