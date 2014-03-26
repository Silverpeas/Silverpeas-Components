CREATE TABLE sc_suggestion_box (
  id             VARCHAR(40) PRIMARY KEY,
  instanceId     VARCHAR(30) NOT NULL,
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(40) NOT NULL,
  lastUpdateDate TIMESTAMP   NOT NULL,
  lastUpdatedBy  VARCHAR(40) NOT NULL,
  version        INT8        NOT NULL
);

CREATE TABLE sc_suggestion (
  id                VARCHAR(40) PRIMARY KEY,
  suggestionBoxId   VARCHAR(40)   NOT NULL,
  title             VARCHAR(2000) NOT NULL,
  state             VARCHAR(20)   NOT NULL,
  validationDate    TIMESTAMP,
  validationComment VARCHAR(2000),
  createDate        TIMESTAMP     NOT NULL,
  createdBy         VARCHAR(40)   NOT NULL,
  lastUpdateDate    TIMESTAMP     NOT NULL,
  lastUpdatedBy     VARCHAR(40)   NOT NULL,
  version           INT8          NOT NULL
);