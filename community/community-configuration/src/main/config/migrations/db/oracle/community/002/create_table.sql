CREATE TABLE SC_Community
(
    id           VARCHAR(40) PRIMARY KEY,
    instanceId   VARCHAR(30)  NOT NULL,
    spaceId      VARCHAR(40)  NOT NULL,
    groupId      INT          NULL,
    homePage     VARCHAR(400) NULL,
    homePageType INT          NULL,
    charterURL   VARCHAR(400) NULL,
    CONSTRAINT UN_COMMUNITY UNIQUE (instanceId, spaceId),
    CONSTRAINT FK_GROUP FOREIGN KEY (groupId) REFERENCES st_group (id)
);

CREATE TABLE SC_Community_Membership
(
    id             VARCHAR(40) PRIMARY KEY,
    community      VARCHAR(40)   NOT NULL,
    userId         INT           NOT NULL,
    status         VARCHAR(15)   NOT NULL,
    joiningDate    TIMESTAMP     NULL,
    createDate     TIMESTAMP     NOT NULL,
    createdBy      VARCHAR(40)   NOT NULL,
    lastUpdateDate TIMESTAMP     NOT NULL,
    lastUpdatedBy  VARCHAR(40)   NOT NULL,
    version        NUMBER(19, 0) NOT NULL,
    CONSTRAINT FK_COMMUNITY FOREIGN KEY (community) REFERENCES SC_Community (id),
    CONSTRAINT FK_USER FOREIGN KEY (userid) REFERENCES st_user (id)
);