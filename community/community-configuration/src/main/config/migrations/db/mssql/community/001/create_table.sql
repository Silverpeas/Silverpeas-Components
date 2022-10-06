CREATE TABLE SC_Community
(
    id           VARCHAR(40) PRIMARY KEY,
    instanceId   VARCHAR(30)  NOT NULL,
    spaceId      VARCHAR(40)  NOT NULL,
    homePage     VARCHAR(400) NULL,
    homePageType INT          NULL,
    charterURL   VARCHAR(400) NULL,
    CONSTRAINT UN_COMMUNITY UNIQUE (instanceId, spaceId)
);

CREATE TABLE SC_Community_Membership
(
    id             VARCHAR(40) PRIMARY KEY,
    community      VARCHAR(40) NOT NULL,
    userId         INT         NOT NULL,
    status         VARCHAR(15) NOT NULL,
    joiningDate    DATETIME    NULL,
    createDate     DATETIME    NOT NULL,
    createdBy      VARCHAR(40) NOT NULL,
    lastUpdateDate DATETIME    NOT NULL,
    lastUpdatedBy  VARCHAR(40) NOT NULL,
    version        BIGINT      NOT NULL,
    CONSTRAINT FK_COMMUNITY FOREIGN KEY (community) REFERENCES SC_Community (id),
    CONSTRAINT FK_USER FOREIGN KEY (community) REFERENCES st_user (id)
);