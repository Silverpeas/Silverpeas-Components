/*
Saving the old structure.
 */

SELECT *
INTO SC_Gallery_Photo_up005
FROM SC_Gallery_Photo;
SELECT *
INTO SC_Gallery_Path_up005
FROM SC_Gallery_Path;
SELECT *
INTO SC_Gallery_Order_up005
FROM SC_Gallery_Order;
SELECT *
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
    cast(photoId as varchar),
    'Photo',
    instanceId,
    title,
    description,
    author,
    keyword,
    case
        when beginDate = '0000/00/00' THEN -2208992400000
        else cast(DATEDIFF(s, '1970-01-01 00:00:00', convert(datetime, beginDate, 111)) as bigint) * 1000
    END,
    case
        when endDate = '9999/99/99' THEN 32503590000000
        else cast(DATEDIFF(s, '1970-01-01 00:00:00', convert(datetime, endDate, 111)) as bigint) * 1000
    END,
    convert(datetime, creationDate, 111),
    creatorId,
    CASE WHEN updateDate IS NOT NULL THEN convert(datetime, updateDate, 111)
    ELSE convert(datetime, creationDate, 111) END,
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
    cast(photoId as varchar),
    imageName,
    imageSize,
    imageMimeType,
    download,
    CASE WHEN beginDownloadDate IS NULL THEN NULL
    ELSE cast(DATEDIFF(s, '1970-01-01 00:00:00', convert(datetime, beginDownloadDate, 111)) as bigint) * 1000 END,
    CASE WHEN endDownloadDate IS NULL THEN NULL
    ELSE cast(DATEDIFF(s, '1970-01-01 00:00:00', convert(datetime, endDownloadDate, 111)) as bigint) * 1000 END
  FROM
    SC_Gallery_Photo_up005;

/*
Copying the photo data.
 */

INSERT INTO SC_Gallery_Photo (mediaId, resolutionH, resolutionW)
  SELECT
    cast(photoId as varchar),
    sizeH,
    sizeL
  FROM
    SC_Gallery_Photo_up005;

/*
Copying path data.
 */
INSERT INTO SC_Gallery_Path (mediaId, instanceId, nodeId)
  SELECT
    cast(photoId as varchar),
    instanceId,
    nodeId
  FROM
    SC_Gallery_Path_up005;

/*
Copying order data
 */

INSERT INTO SC_Gallery_Order (orderId, userId, instanceId, createDate, processDate, processUser)
  SELECT
    cast(orderId as varchar),
    cast(userId as varchar),
    instanceId,
    dateadd(S, cast(creationDate as bigint) / 1000, '1970-01-01'),
    dateadd(S, cast(processDate as bigint) / 1000, '1970-01-01'),
    processUser
  FROM
    SC_Gallery_Order_up005;

/*
Copying oder details data
 */

INSERT INTO SC_Gallery_OrderDetail (orderId, mediaId, instanceId, downloadDate, downloadDecision)
  SELECT
    cast(orderId as varchar),
    cast(photoId as varchar),
    instanceId,
    dateadd(S, cast(downloadDate as bigint) / 1000, '1970-01-01'),
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