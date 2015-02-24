CREATE TABLE sc_mailinglist_attachment (
  id             VARCHAR2(255) NOT NULL,
  version        NUMBER(10, 0) NOT NULL,
  attachmentSize NUMBER(19, 0),
  attachmentPath VARCHAR2(255),
  fileName       VARCHAR2(255),
  contentType    VARCHAR2(255),
  md5Signature   VARCHAR2(255),
  messageId      VARCHAR2(255)
);


CREATE TABLE sc_mailinglist_external_user (
  id          VARCHAR2(255) NOT NULL,
  version     NUMBER(10, 0) NOT NULL,
  componentId VARCHAR2(255),
  email       VARCHAR2(255) NOT NULL,
  listId      VARCHAR2(255)
);

CREATE TABLE sc_mailinglist_list (
  id          VARCHAR2(255) NOT NULL,
  version     NUMBER(10, 0) NOT NULL,
  componentId VARCHAR2(255)
);

CREATE TABLE sc_mailinglist_message (
  id              VARCHAR2(255) NOT NULL,
  version         NUMBER(10, 0) NOT NULL,
  mailId          VARCHAR2(255) NOT NULL,
  componentId     VARCHAR2(255) NOT NULL,
  title           VARCHAR2(255),
  summary         VARCHAR2(255),
  sender          VARCHAR2(255),
  sentDate        DATE,
  referenceId     VARCHAR2(255),
  moderated       NUMBER(1),
  contentType     VARCHAR2(255),
  attachmentsSize NUMBER(19, 0),
  messageYear     NUMBER(10, 0),
  messageMonth    NUMBER(10, 0),
  body            CLOB
);

CREATE TABLE sc_mailinglist_internal_sub (
  id              VARCHAR2(255) NOT NULL,
  version         NUMBER(10, 0) NOT NULL,
  subscriber_type VARCHAR2(255) NOT NULL,
  externalid      VARCHAR2(255) NOT NULL,
  mailinglistid   VARCHAR2(255) NOT NULL
);
