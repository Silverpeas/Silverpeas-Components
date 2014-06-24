/*
Saving the old structure.
 */

CREATE TABLE SC_Gallery_Photo_up005
AS SELECT
  *
FROM SC_Gallery_Photo;
CREATE TABLE SC_Gallery_Path_up005
AS SELECT
  *
FROM SC_Gallery_Path;
CREATE TABLE SC_Gallery_Order_up005
AS SELECT
  *
FROM SC_Gallery_Order;
CREATE TABLE SC_Gallery_OrderDetail_up005
AS SELECT
  *
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
  beginVisibilityDate number(19,0) DEFAULT -2208992400000          NOT NULL,
  endVisibilityDate   number(19,0) DEFAULT 32503676399999          NOT NULL,
  createDate          TIMESTAMP                            NOT NULL,
  createdBy           VARCHAR(50)                          NOT NULL,
  lastUpdateDate      TIMESTAMP                            NOT NULL,
  lastUpdatedBy       VARCHAR(50)                          NULL
);

CREATE TABLE SC_Gallery_Internal (
  mediaId           VARCHAR(40)  NOT NULL,
  fileName          VARCHAR(255) NULL,
  fileSize          number(19,0)         NULL,
  fileMimeType      VARCHAR(100) NULL,
  download          INT          NULL,
  beginDownloadDate number(19,0)         NULL,
  endDownloadDate   number(19,0)         NULL
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
  bitrate     number(19,0)        NULL,
  duration    number(19,0)        NULL
);

CREATE TABLE SC_Gallery_Sound (
  mediaId  VARCHAR(40) NOT NULL,
  bitrate  number(19,0)        NULL,
  duration number(19,0)        NULL
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
ADD CONSTRAINT PK_SC_Gallery_Media PRIMARY KEY
  (
    mediaId
  )
;

ALTER TABLE SC_Gallery_Internal
ADD CONSTRAINT PK_SC_Gallery_Internal PRIMARY KEY
  (
    mediaId
  )
;

ALTER TABLE SC_Gallery_Photo
ADD CONSTRAINT PK_SC_Gallery_Photo PRIMARY KEY
  (
    mediaId
  )
;

ALTER TABLE SC_Gallery_Video
ADD CONSTRAINT PK_SC_Gallery_Video PRIMARY KEY
  (
    mediaId
  )
;

ALTER TABLE SC_Gallery_Sound
ADD CONSTRAINT PK_SC_Gallery_Sound PRIMARY KEY
  (
    mediaId
  )
;

ALTER TABLE SC_Gallery_Streaming
ADD CONSTRAINT PK_SC_Gallery_Streaming PRIMARY KEY
  (
    mediaId
  )
;

ALTER TABLE SC_Gallery_Path
ADD CONSTRAINT PK_SC_Gallery_Path PRIMARY KEY
  (
    nodeId, mediaId
  )
;

ALTER TABLE SC_Gallery_Order
ADD CONSTRAINT PK_SC_Gallery_Order PRIMARY KEY
  (
    orderId
  )
;

ALTER TABLE SC_Gallery_OrderDetail
ADD CONSTRAINT PK_SC_Gallery_OrderDetail PRIMARY KEY
  (
    orderId, mediaId
  )
;

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
    to_char(photoId),
    'Photo',
    instanceId,
    title,
    description,
    author,
    keyword,
    case
    when beginDate = '0000/00/00' THEN -2208992400000
    else (to_date(beginDate, 'YYYY/MM/DD') - to_date('1-1-1970 00:00:00','MM-DD-YYYY HH24:Mi:SS'))*24*3600*1000 END,
    CASE
    WHEN endDate = '9999/99/99' THEN 32503590000000
    ELSE (to_date(endDate, 'YYYY/MM/DD') - to_date('1-1-1970 00:00:00','MM-DD-YYYY HH24:Mi:SS'))*24*3600*1000 END,
    to_timestamp(creationDate, 'YYYY/MM/DD'),
    creatorId,
    CASE WHEN updateDate IS NOT NULL THEN to_date(updateDate, 'YYYY/MM/DD')
    ELSE to_date(creationDate, 'YYYY/MM/DD') END,
    CASE WHEN updateDate IS NOT NULL THEN updateId
    ELSE creatorId END
FROM
    SC_Gallery_Photo_up005
;

/*
Copying the internal media data.
 */

INSERT INTO SC_Gallery_Internal (mediaId, fileName, fileSize, fileMimeType,
                                 download, beginDownloadDate, endDownloadDate)
  SELECT
    to_char(photoId),
    imageName,
    imageSize,
    imageMimeType,
    download,
    CASE WHEN beginDownloadDate IS NULL THEN NULL
    ELSE (to_date(beginDownloadDate, 'YYYY/MM/DD') - to_date('1-1-1970 00:00:00','MM-DD-YYYY HH24:Mi:SS'))*24*3600*1000 END,
    CASE WHEN endDownloadDate IS NULL THEN NULL
    ELSE (to_date(endDownloadDate, 'YYYY/MM/DD') - to_date('1-1-1970 00:00:00','MM-DD-YYYY HH24:Mi:SS'))*24*3600*1000 END
  FROM
    SC_Gallery_Photo_up005;

/*
Copying the photo data.
 */

INSERT INTO SC_Gallery_Photo (mediaId, resolutionH, resolutionW)
  SELECT
    to_char(photoId),
    sizeH,
    sizeL
  FROM
    SC_Gallery_Photo_up005;

/*
Copying path data.
 */
INSERT INTO SC_Gallery_Path (mediaId, instanceId, nodeId)
  SELECT
    to_char(photoId),
    instanceId,
    nodeId
  FROM
    SC_Gallery_Path_up005;

/*
Copying order data
 */

INSERT INTO SC_Gallery_Order (orderId, userId, instanceId, createDate, processDate, processUser)
  SELECT
    to_char(orderId),
    to_char(userId),
    instanceId,
    timestamp '1970-01-01 00:00:00' + numtodsinterval(creationDate / 1000,'second'),
    timestamp '1970-01-01 00:00:00' + numtodsinterval(processDate / 1000,'second'),
    processUser
  FROM
    SC_Gallery_Order_up005;

/*
Copying oder details data
 */

INSERT INTO SC_Gallery_OrderDetail (orderId, mediaId, instanceId, downloadDate, downloadDecision)
  SELECT
    to_char(orderId),
    to_char(photoId),
    instanceId,
    timestamp '1970-01-01 00:00:00' + numtodsinterval(downloadDate / 1000,'second'),
    downloadDecision
  FROM
    SC_Gallery_OrderDetail_up005;

/*
Cleaning temporary table
 */

DROP TABLE SC_Gallery_Photo_up005;
DROP TABLE SC_Gallery_Path_up005;
DROP TABLE SC_Gallery_Order_up005;
DROP TABLE SC_Gallery_OrderDetail_up005;