CREATE TABLE sc_suggestion_box (
  id              VARCHAR(40) PRIMARY KEY,
  instanceId      VARCHAR(30) NOT NULL,
  createDate      DATETIME    NOT NULL,
  createdBy       VARCHAR(40) NOT NULL,
  lastUpdateDate  DATETIME,
  lastUpdatedBy   VARCHAR(40),
  version         BIGINT      NOT NULL
);

CREATE TABLE sc_suggestion (
  id              VARCHAR(40) PRIMARY KEY,
  suggestionBoxId VARCHAR(40) NOT NULL,
  title           VARCHAR(2000) NOT NULL,
  createDate      DATETIME    NOT NULL,
  createdBy       VARCHAR(40) NOT NULL,
  lastUpdateDate  DATETIME,
  lastUpdatedBy   VARCHAR(40),
  version         BIGINT      NOT NULL
);