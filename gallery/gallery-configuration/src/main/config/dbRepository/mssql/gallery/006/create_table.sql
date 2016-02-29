CREATE TABLE SC_Gallery_Media (
  mediaId             VARCHAR(40)                          NOT NULL,
  mediaType           VARCHAR(30)                          NOT NULL,
  instanceId          VARCHAR(50)                          NOT NULL,
  title               VARCHAR(255)                         NOT NULL,
  description         VARCHAR(255)                         NULL,
  author              VARCHAR(50)                          NULL,
  keyWord             VARCHAR(1000)                        NULL,
  beginVisibilityDate bigint DEFAULT -2208992400000          NOT NULL,
  endVisibilityDate   bigint DEFAULT 32503676399999          NOT NULL,
  createDate          datetime                            NOT NULL,
  createdBy           VARCHAR(50)                          NOT NULL,
  lastUpdateDate      datetime                            NOT NULL,
  lastUpdatedBy       VARCHAR(50)                          NULL
);

CREATE TABLE SC_Gallery_Internal (
  mediaId           VARCHAR(40)  NOT NULL,
  fileName          VARCHAR(255) NULL,
  fileSize          bigint         NULL,
  fileMimeType      VARCHAR(100) NULL,
  download          INT          NULL,
  beginDownloadDate bigint         NULL,
  endDownloadDate   bigint         NULL
);

CREATE TABLE SC_Gallery_Photo (
  mediaId     VARCHAR(40) NOT NULL,
  resolutionW INT         NULL,
  resolutionH INT         NULL
);

CREATE TABLE SC_Gallery_Video (
  mediaId     VARCHAR(40) NOT NULL,
  resolutionW INT         NULL,
  resolutionH INT         NULL,
  bitrate     bigint        NULL,
  duration    bigint        NULL
);

CREATE TABLE SC_Gallery_Sound (
  mediaId  VARCHAR(40) NOT NULL,
  bitrate  bigint        NULL,
  duration bigint        NULL
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
  createDate  datetime   NOT NULL,
  processDate datetime   NULL,
  processUser VARCHAR(50) NULL
);

CREATE TABLE SC_Gallery_OrderDetail (
  orderId          VARCHAR(40) NOT NULL,
  mediaId          VARCHAR(40) NOT NULL,
  instanceId       VARCHAR(50) NOT NULL,
  downloadDate     datetime   NULL,
  downloadDecision VARCHAR(50) NULL
);
