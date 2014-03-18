/* Table */
CREATE TABLE sc_suggestion_box (
  id              VARCHAR(40) NOT NULL,
  instanceId      VARCHAR(30) NOT NULL,
  createDate      TIMESTAMP   NOT NULL,
  createdBy       VARCHAR(40) NOT NULL,
  lastUpdateDate  TIMESTAMP,
  lastUpdatedBy   VARCHAR(40),
  version         INT8        NOT NULL
);

/* Table constraints */
ALTER TABLE sc_suggestion_box ADD CONSTRAINT const_sc_suggestion_box_pk PRIMARY KEY (id);

/* Table */
CREATE TABLE sc_suggestion (
  id              VARCHAR(40) NOT NULL,
  suggestionBoxId VARCHAR(40) NOT NULL,
  title           VARCHAR(2000) NOT NULL,
  createDate      TIMESTAMP   NOT NULL,
  createdBy       VARCHAR(40) NOT NULL,
  lastUpdateDate  TIMESTAMP,
  lastUpdatedBy   VARCHAR(40),
  version         INT8        NOT NULL
);

/* Table constraints */
ALTER TABLE sc_suggestion ADD CONSTRAINT const_sc_suggestion_pk PRIMARY KEY (id);