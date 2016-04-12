CREATE TABLE sc_scheduleevent_list (
  id           VARCHAR(255) NOT NULL,
  title        VARCHAR(255) NOT NULL,
  description  VARCHAR(4000),
  creationdate DATETIME     NOT NULL,
  status       INT          NOT NULL,
  creatorid    INTEGER      NOT NULL
);

CREATE TABLE sc_scheduleevent_options (
  id              VARCHAR(255) NOT NULL,
  scheduleeventid VARCHAR(255) NOT NULL,
  optionday       DATETIME     NOT NULL,
  optionhour      INT          NOT NULL
);

CREATE TABLE sc_scheduleevent_contributor (
  id              VARCHAR(255) NOT NULL,
  scheduleeventid VARCHAR(255) NOT NULL,
  userid          INT          NOT NULL,
  lastvalidation  DATETIME,
  lastvisit       DATETIME
);

CREATE TABLE sc_scheduleevent_response (
  id              VARCHAR(255) NOT NULL,
  scheduleeventid VARCHAR(255) NOT NULL,
  userid          INT          NOT NULL,
  optionid        VARCHAR(255) NOT NULL
);