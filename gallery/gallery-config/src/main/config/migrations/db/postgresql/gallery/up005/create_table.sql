/*
Saving the old structure.
 */

SELECT
  *
INTO SC_Gallery_Photo_up005
FROM SC_Gallery_Photo;
SELECT
  *
INTO SC_Gallery_Path_up005
FROM SC_Gallery_Path;
SELECT
  *
INTO SC_Gallery_Order_up005
FROM SC_Gallery_Order;
SELECT
  *
INTO SC_Gallery_OrderDetail_up005
FROM SC_Gallery_OrderDetail;

/*
Removing the old structure.
 */

DROP TABLE SC_Gallery_Photo;
DROP TABLE SC_Gallery_Path;
DROP TABLE SC_Gallery_Order;
DROP TABLE SC_Gallery_OrderDetail;

/*
The new structure.
 */
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
  resolutionW INT         NULL,
  resolutionH INT         NULL
);

CREATE TABLE SC_Gallery_Video (
  mediaId     VARCHAR(40) NOT NULL,
  resolutionW INT         NULL,
  resolutionH INT         NULL,
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

/*
Constraints.
 */

ALTER TABLE SC_Gallery_Media
ADD CONSTRAINT PK_SC_Gallery_Media PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Internal
ADD CONSTRAINT PK_SC_Gallery_Internal PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Photo
ADD CONSTRAINT PK_SC_Gallery_Photo PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Video
ADD CONSTRAINT PK_SC_Gallery_Video PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Sound
ADD CONSTRAINT PK_SC_Gallery_Sound PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Streaming
ADD CONSTRAINT PK_SC_Gallery_Streaming PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Path
ADD CONSTRAINT PK_SC_Gallery_Path PRIMARY KEY (nodeId, mediaId);

ALTER TABLE SC_Gallery_Order
ADD CONSTRAINT PK_SC_Gallery_Order PRIMARY KEY (orderId);

ALTER TABLE SC_Gallery_OrderDetail
ADD CONSTRAINT PK_SC_Gallery_OrderDetail PRIMARY KEY (orderId, mediaId);

/*
Indexes.
 */

CREATE INDEX IND_Path ON SC_Gallery_Path (nodeId);
CREATE INDEX IND_Order ON SC_Gallery_OrderDetail (orderId);

/*
Copying the media data.
 */

INSERT INTO SC_Gallery_Media (mediaId, mediaType, instanceId,
                              title, description, author, keyWord,
                              beginVisibilityDate, endVisibilityDate,
                              createDate, createdBy, lastUpdateDate, lastUpdatedBy)
  SELECT
    cast(photoId AS TEXT),
    'Photo',
    instanceId,
    title,
    description,
    author,
    keyword,
    CASE WHEN beginDate = '0000/00/00' THEN -2208992400000
    ELSE (extract(EPOCH FROM date_trunc('milliseconds', to_timestamp(beginDate, 'YYYY/MM/DD'))) *
          1000) END,
    CASE WHEN endDate = '9999/99/99' THEN 32503590000000
    ELSE (extract(EPOCH FROM date_trunc('milliseconds', to_timestamp(endDate, 'YYYY/MM/DD'))) *
          1000) END,
    to_timestamp(creationDate, 'YYYY/MM/DD'),
    creatorId,
    CASE WHEN updateDate IS NOT NULL THEN to_timestamp(updateDate, 'YYYY/MM/DD')
    ELSE to_timestamp(creationDate, 'YYYY/MM/DD') END,
    CASE WHEN updateDate IS NOT NULL THEN updateId
    ELSE creatorId END
  FROM
    SC_Gallery_Photo_up005;

/*
Copying the internal media data.
 */

INSERT INTO SC_Gallery_Internal (mediaId, fileName, fileSize, fileMimeType,
                                 download, beginDownloadDate, endDownloadDate)
  SELECT
    cast(photoId AS TEXT),
    imageName,
    imageSize,
    imageMimeType,
    download,
    CASE WHEN beginDownloadDate IS NULL THEN NULL
    ELSE (
      extract(EPOCH FROM date_trunc('milliseconds', to_timestamp(beginDownloadDate, 'YYYY/MM/DD')))
      * 1000) END,
    CASE WHEN endDownloadDate IS NULL THEN NULL
    ELSE (
      extract(EPOCH FROM date_trunc('milliseconds', to_timestamp(endDownloadDate, 'YYYY/MM/DD'))) *
      1000) END
  FROM
    SC_Gallery_Photo_up005;

/*
Copying the photo data.
 */

INSERT INTO SC_Gallery_Photo (mediaId, resolutionH, resolutionW)
  SELECT
    cast(photoId AS TEXT),
    sizeH,
    sizeL
  FROM
    SC_Gallery_Photo_up005;

/*
Copying path data.
 */
INSERT INTO SC_Gallery_Path (mediaId, instanceId, nodeId)
  SELECT
    cast(photoId AS TEXT),
    instanceId,
    nodeId
  FROM
    SC_Gallery_Path_up005;

/*
Copying order data
 */

INSERT INTO SC_Gallery_Order (orderId, userId, instanceId, createDate, processDate, processUser)
  SELECT
    cast(orderId AS TEXT),
    cast(userId AS TEXT),
    instanceId,
    to_timestamp(cast(creationDate AS INT8) / 1000),
    to_timestamp(cast(processDate AS INT8) / 1000),
    processUser
  FROM
    SC_Gallery_Order_up005;

/*
Copying oder details data
 */

INSERT INTO SC_Gallery_OrderDetail (orderId, mediaId, instanceId, downloadDate, downloadDecision)
  SELECT
    cast(orderId AS TEXT),
    cast(photoId AS TEXT),
    instanceId,
    to_timestamp(cast(downloadDate AS INT8) / 1000),
    downloadDecision
  FROM
    SC_Gallery_OrderDetail_up005;

/*
Cleaning
 */

DROP TABLE SC_Gallery_Photo_up005;
DROP TABLE SC_Gallery_Path_up005;
DROP TABLE SC_Gallery_Order_up005;
DROP TABLE SC_Gallery_OrderDetail_up005;