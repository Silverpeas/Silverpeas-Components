CREATE TABLE SC_Rss_Channels
(
  id               INT           NOT NULL,
  url              VARCHAR(1000) NOT NULL,
  refreshRate      INT           NOT NULL,
  nbDisplayedItems INT           NOT NULL,
  displayImage     INT           NOT NULL,
  creatorId        VARCHAR(100)  NOT NULL,
  creationDate     CHAR(10)      NOT NULL,
  instanceId       VARCHAR(50)   NOT NULL
);
