CREATE TABLE sc_scheduleevent_list (
  id           VARCHAR2(255) NOT NULL,
  title        VARCHAR2(255) NOT NULL,
  description  VARCHAR2(4000),
  creationdate DATE          NOT NULL,
  status       NUMBER(10, 0) NOT NULL,
  creatorid    INT           NOT NULL
);

CREATE TABLE sc_scheduleevent_options (
  id              VARCHAR2(255) NOT NULL,
  scheduleeventid VARCHAR2(255) NOT NULL,
  optionday       DATE          NOT NULL,
  optionhour      NUMBER(10, 0) NOT NULL
);

CREATE TABLE sc_scheduleevent_contributor (
  id              VARCHAR2(255) NOT NULL,
  scheduleeventid VARCHAR2(255) NOT NULL,
  userid          INT           NOT NULL,
  lastvalidation  DATE,
  lastvisit       DATE
);

CREATE TABLE sc_scheduleevent_response (
  id              VARCHAR2(255) NOT NULL,
  scheduleeventid VARCHAR2(255) NOT NULL,
  userid          INT           NOT NULL,
  optionid        VARCHAR2(255) NOT NULL
);