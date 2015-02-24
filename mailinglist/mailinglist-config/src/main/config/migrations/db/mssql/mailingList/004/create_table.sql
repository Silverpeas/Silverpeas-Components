CREATE TABLE sc_mailinglist_attachment (
  id             VARCHAR(255) NOT NULL,
  version        INT          NOT NULL,
  attachmentSize BIGINT       NULL,
  attachmentPath VARCHAR(255) NULL,
  fileName       VARCHAR(255) NULL,
  contentType    VARCHAR(255) NULL,
  md5Signature   VARCHAR(255) NULL,
  messageId      VARCHAR(255) NULL
);

CREATE TABLE sc_mailinglist_external_user (
  id          VARCHAR(255) NOT NULL,
  version     INT          NOT NULL,
  componentId VARCHAR(255) NULL,
  email       VARCHAR(255) NOT NULL,
  listId      VARCHAR(255) NULL
);

CREATE TABLE sc_mailinglist_list (
  id          VARCHAR(255) NOT NULL,
  version     INT          NOT NULL,
  componentId VARCHAR(255) NULL
);

CREATE TABLE sc_mailinglist_message (
  id              VARCHAR(255) NOT NULL,
  version         INT          NOT NULL,
  mailId          VARCHAR(255) NOT NULL,
  componentId     VARCHAR(255) NOT NULL,
  title           VARCHAR(255) NULL,
  summary         VARCHAR(255) NULL,
  sender          VARCHAR(255) NULL,
  sentDate        DATETIME     NULL,
  referenceId     VARCHAR(255) NULL,
  moderated       BIT          NULL,
  contentType     VARCHAR(255) NULL,
  attachmentsSize BIGINT       NULL,
  messageYear     INT          NULL,
  messageMonth    INT          NULL,
  body            TEXT         NULL
);

CREATE TABLE sc_mailinglist_internal_sub (
  id              VARCHAR(255) NOT NULL,
  version         INT          NOT NULL,
  subscriber_type VARCHAR(255) NOT NULL,
  externalid      VARCHAR(255) NOT NULL,
  mailinglistid   VARCHAR(255) NOT NULL
);

