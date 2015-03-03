CREATE TABLE sc_scheduleevent_list (
  id           VARCHAR(255) NOT NULL,
  title        VARCHAR(255) NOT NULL,
  description  VARCHAR(4000),
  creationdate TIMESTAMP    NOT NULL,
  status       INT4         NOT NULL,
  creatorid    INTEGER      NOT NULL
);

CREATE TABLE sc_scheduleevent_options (
  id              VARCHAR(255) NOT NULL,
  scheduleeventid VARCHAR(255) NOT NULL,
  optionday       TIMESTAMP    NOT NULL,
  optionhour      INT4         NOT NULL
);

CREATE TABLE sc_scheduleevent_contributor (
  id              VARCHAR(255) NOT NULL,
  scheduleeventid VARCHAR(255) NOT NULL,
  userid          INTEGER      NOT NULL,
  lastvalidation  TIMESTAMP,
  lastvisit       TIMESTAMP
);

CREATE TABLE sc_scheduleevent_response (
  id              VARCHAR(255) NOT NULL,
  scheduleeventid VARCHAR(255) NOT NULL,
  userid          INTEGER      NOT NULL,
  optionid        VARCHAR(255) NOT NULL
);