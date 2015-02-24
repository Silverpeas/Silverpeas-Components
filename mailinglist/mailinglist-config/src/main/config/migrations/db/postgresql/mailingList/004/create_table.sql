CREATE TABLE sc_mailinglist_attachment (
  id             VARCHAR(255) NOT NULL,
  version        INT4         NOT NULL,
  attachmentSize INT8,
  attachmentPath VARCHAR(255),
  fileName       VARCHAR(255),
  contentType    VARCHAR(255),
  md5Signature   VARCHAR(255),
  messageId      VARCHAR(255)
);

CREATE TABLE sc_mailinglist_external_user (
  id          VARCHAR(255) NOT NULL,
  version     INT4         NOT NULL,
  componentId VARCHAR(255),
  email       VARCHAR(255) NOT NULL,
  listId      VARCHAR(255)
);

CREATE TABLE sc_mailinglist_list (
  id          VARCHAR(255) NOT NULL,
  version     INT4         NOT NULL,
  componentId VARCHAR(255)
);

CREATE TABLE sc_mailinglist_message (
  id              VARCHAR(255) NOT NULL,
  version         INT4         NOT NULL,
  mailId          VARCHAR(255) NOT NULL,
  componentId     VARCHAR(255) NOT NULL,
  title           VARCHAR(255),
  summary         VARCHAR(255),
  sender          VARCHAR(255),
  sentDate        TIMESTAMP,
  referenceId     VARCHAR(255),
  moderated       BOOL,
  contentType     VARCHAR(255),
  attachmentsSize INT8,
  messageYear     INT4,
  messageMonth    INT4,
  body            TEXT
);


CREATE TABLE sc_mailinglist_internal_sub (
  id              VARCHAR(255) NOT NULL,
  version         INT4         NOT NULL,
  subscriber_type VARCHAR(255) NOT NULL,
  externalid      VARCHAR(255) NOT NULL,
  mailinglistid   VARCHAR(255) NOT NULL
);

