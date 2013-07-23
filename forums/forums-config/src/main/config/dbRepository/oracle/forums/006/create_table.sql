CREATE TABLE SC_Forums_Forum
(
  forumId           INT           NOT NULL,
  forumName         VARCHAR(1000) NOT NULL,
  forumDescription  VARCHAR(2000) NULL,
  forumCreationDate VARCHAR(50)   NOT NULL,
  forumCloseDate    VARCHAR(50)   NULL,
  forumCreator      VARCHAR(255)  NOT NULL,
  forumActive       INT           NOT NULL,
  forumParent       INT DEFAULT 0 NOT NULL,
  forumModes        VARCHAR(50)   NULL,
  forumLockLevel    INT           NULL,
  instanceId        VARCHAR(50)   NOT NULL,
  categoryId        VARCHAR(50)   NULL
);

CREATE TABLE SC_Forums_Message
(
  messageId       INT           NOT NULL,
  messageTitle    VARCHAR(1000) NOT NULL,
  messageAuthor   VARCHAR(255)  NOT NULL,
  forumId         INT           NOT NULL,
  messageParentId INT           NULL,
  messageDate     TIMESTAMP     NULL,
  status          VARCHAR(50)   NULL
);

CREATE TABLE SC_Forums_Rights
(
  userId  VARCHAR(255) NOT NULL,
  forumId VARCHAR(255) NOT NULL
);

CREATE TABLE SC_Forums_HistoryUser
(
  userId     VARCHAR(255) NOT NULL,
  messageId  INT          NOT NULL,
  lastAccess VARCHAR(50)  NOT NULL
);	  	