CREATE TABLE sc_suggestion_box (
  id              VARCHAR(40) PRIMARY KEY,
  instanceId      VARCHAR(30) NOT NULL,
  createDate      TIMESTAMP   NOT NULL,
  createdBy       VARCHAR(40) NOT NULL,
  lastUpdateDate  TIMESTAMP,
  lastUpdatedBy   VARCHAR(40),
  version         int        NOT NULL
);

CREATE TABLE sc_suggestion (
  id              VARCHAR(40) PRIMARY KEY,
  suggestionBoxId VARCHAR(40) NOT NULL,
  createDate      TIMESTAMP   NOT NULL,
  createdBy       VARCHAR(40) NOT NULL,
  lastUpdateDate  TIMESTAMP,
  lastUpdatedBy   VARCHAR(40),
  version         int        NOT NULL
);