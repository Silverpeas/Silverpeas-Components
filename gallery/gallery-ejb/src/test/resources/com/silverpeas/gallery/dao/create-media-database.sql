CREATE TABLE SC_Gallery_Media (
  mediaId             VARCHAR(40)                          NOT NULL,
  mediaType           VARCHAR(30)                          NOT NULL,
  instanceId          VARCHAR(50)                          NOT NULL,
  title               VARCHAR(255)                         NOT NULL,
  description         VARCHAR(255)                         NULL,
  author              VARCHAR(50)                          NULL,
  keyWord             VARCHAR(1000)                        NULL,
  beginVisibilityDate INT8 DEFAULT -2208992400000          NOT NULL,
  endVisibilityDate   INT8 DEFAULT 32503676399999          NOT NULL,
  createDate          TIMESTAMP                            NOT NULL,
  createdBy           VARCHAR(50)                          NOT NULL,
  lastUpdateDate      TIMESTAMP                            NOT NULL,
  lastUpdatedBy       VARCHAR(50)                          NULL
);

CREATE TABLE SC_Gallery_Internal (
  mediaId           VARCHAR(40)  NOT NULL,
  fileName          VARCHAR(255) NULL,
  fileSize          INT8         NULL,
  fileMimeType      VARCHAR(100) NULL,
  download          INT          NULL,
  beginDownloadDate INT8         NULL,
  endDownloadDate   INT8         NULL
);

CREATE TABLE SC_Gallery_Photo (
  mediaId     VARCHAR(40) NOT NULL,
  resolutionH INT         NULL,
  resolutionW INT         NULL
);

CREATE TABLE SC_Gallery_Video (
  mediaId     VARCHAR(40) NOT NULL,
  resolutionH INT         NULL,
  resolutionW INT         NULL,
  bitrate     INT8        NULL,
  duration    INT8        NULL
);

CREATE TABLE SC_Gallery_Sound (
  mediaId  VARCHAR(40) NOT NULL,
  bitrate  INT8        NULL,
  duration INT8        NULL
);

CREATE TABLE SC_Gallery_Streaming (
  mediaId     VARCHAR(40)   NOT NULL,
  homepageUrl VARCHAR(1000) NOT NULL,
  provider    VARCHAR(50)   NOT NULL
);

CREATE TABLE SC_Gallery_Path (
  mediaId    VARCHAR(40) NOT NULL,
  instanceId VARCHAR(50) NOT NULL,
  nodeId     INT         NOT NULL
);

CREATE TABLE SC_Gallery_Order (
  orderId     VARCHAR(40) NOT NULL,
  userId      VARCHAR(40) NOT NULL,
  instanceId  VARCHAR(50) NOT NULL,
  createDate  TIMESTAMP   NOT NULL,
  processDate TIMESTAMP   NULL,
  processUser VARCHAR(50) NULL
);

CREATE TABLE SC_Gallery_OrderDetail (
  orderId          VARCHAR(40) NOT NULL,
  mediaId          VARCHAR(40) NOT NULL,
  instanceId       VARCHAR(50) NOT NULL,
  downloadDate     TIMESTAMP   NULL,
  downloadDecision VARCHAR(50) NULL
);

CREATE TABLE UniqueId (
  maxId     INT          NOT NULL,
  tableName VARCHAR(100) NOT NULL
);

CREATE TABLE sb_node_node
(
  nodeId           INT              NOT NULL,
  nodeName         VARCHAR(1000)    NOT NULL,
  nodeDescription  VARCHAR(2000)    NULL,
  nodeCreationDate VARCHAR(10)      NOT NULL,
  nodeCreatorId    VARCHAR(100)     NOT NULL,
  nodePath         VARCHAR(1000)    NOT NULL,
  nodeLevelNumber  INT              NOT NULL,
  nodeFatherId     INT              NOT NULL,
  modelId          VARCHAR(1000)    NULL,
  nodeStatus       VARCHAR(1000)    NULL,
  instanceId       VARCHAR(50)      NOT NULL,
  type             VARCHAR(50)      NULL,
  orderNumber      INT DEFAULT (0)  NULL,
  lang             CHAR(2),
  rightsDependsOn  INT DEFAULT (-1) NOT NULL
);