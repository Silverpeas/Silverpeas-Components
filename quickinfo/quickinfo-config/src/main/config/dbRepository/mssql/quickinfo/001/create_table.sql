CREATE TABLE sc_quickinfo_news (
  id             		VARCHAR(40) PRIMARY KEY,
  instanceId     		VARCHAR(30) NOT NULL,
  foreignId		 		VARCHAR(40) NOT NULL,
  important      		BOOLEAN	 	NOT NULL,
  broadcastTicker		BOOLEAN 	NOT NULL,
  broadcastMandatory	BOOLEAN		NOT NULL,
  createDate     		DATETIME   	NOT NULL,
  createdBy      		VARCHAR(40) NOT NULL,
  lastUpdateDate 		DATETIME   	NOT NULL,
  lastUpdatedBy  		VARCHAR(40) NOT NULL,
  publishDate 			DATETIME	NULL,
  publishedBy  			VARCHAR(40) NULL,
  version        		BIGINT      NOT NULL
);

INSERT INTO sc_quickinfo_news (id, instanceId, foreignId,
	important, broadcastTicker, broadcastMandatory,
	createDate, createdBy, lastUpdateDate, lastUpdatedBy,
	publishDate, publishedBy, version)
  SELECT
    cast(pubId as varchar),
    instanceId,
	cast(pubId as varchar),
    false,
    false,
    false,
    convert(datetime, pubCreationDate, 111),
	pubCreatorId,
	convert(datetime, pubUpdateDate, 111),
    pubUpdaterId,
    convert(datetime, pubCreationDate, 111),
	pubCreatorId,
    0
  FROM
    sb_publication_publi
  WHERE
	instanceId like 'quickinfo%';