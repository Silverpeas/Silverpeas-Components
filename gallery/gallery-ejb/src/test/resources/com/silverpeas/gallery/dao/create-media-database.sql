CREATE TABLE SC_Gallery_Media (
  mediaId            VARCHAR(40)                                        NOT NULL,
  mediaType          VARCHAR(30)                                        NOT NULL,
  instanceId         VARCHAR(50)                                        NOT NULL,
  title              VARCHAR(255)                                       NOT NULL,
  description        VARCHAR(255)                                       NULL,
  author             VARCHAR(50)                                        NULL,
  keyWord            VARCHAR(1000)                                      NULL,
  beginVisibiliyDate TIMESTAMP DEFAULT '1900-01-01 00:00:00.0'          NOT NULL,
  endVisibiliyDate   TIMESTAMP DEFAULT '2999-12-31 23:59:59.999'        NOT NULL,
  createDate         TIMESTAMP                                          NOT NULL,
  createdBy          VARCHAR(50)                                        NOT NULL,
  lastUpdateDate     TIMESTAMP                                          NOT NULL,
  lastUpdatedBy      VARCHAR(50)                                        NULL
);

CREATE TABLE SC_Gallery_Internal (
  mediaId           VARCHAR(40)  NOT NULL,
  fileName          VARCHAR(255) NULL,
  fileSize          INT8         NULL,
  fileMimeType      VARCHAR(100) NULL,
  download          INT          NULL,
  beginDownloadDate TIMESTAMP    NULL,
  endDownloadDate   TIMESTAMP    NULL
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
  nodeId     INT         NOT NULL,
  instanceId VARCHAR(50) NOT NULL
);

CREATE TABLE SC_Gallery_Order (
  orderId     INT         NOT NULL,
  userId      INT         NOT NULL,
  instanceId  VARCHAR(50) NOT NULL,
  createDate  TIMESTAMP   NOT NULL,
  processDate TIMESTAMP   NULL,
  processUser VARCHAR(50) NULL
);

CREATE TABLE SC_Gallery_OrderDetail (
  orderId          INT         NOT NULL,
  mediaId          VARCHAR(40) NOT NULL,
  instanceId       VARCHAR(50) NOT NULL,
  downloadDate     TIMESTAMP   NULL,
  downloadDecision VARCHAR(50) NULL
);
