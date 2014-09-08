CREATE TABLE sc_quickinfo_news (
  id             		VARCHAR(40) PRIMARY KEY,
  instanceId     		VARCHAR(30) NOT NULL,
  foreignId		 		VARCHAR(40) NOT NULL,
  important      		NUMBER(1)	 	NOT NULL,
  broadcastTicker		NUMBER(1) 	NOT NULL,
  broadcastMandatory	NUMBER(1)		NOT NULL,
  createDate     		TIMESTAMP   NOT NULL,
  createdBy      		VARCHAR(40) NOT NULL,
  lastUpdateDate 		TIMESTAMP   NOT NULL,
  lastUpdatedBy  		VARCHAR(40) NOT NULL,
  publishDate 			TIMESTAMP   NULL,
  publishedBy  			VARCHAR(40) NULL,
  version        		NUMBER(19, 0) NOT NULL
);

INSERT INTO sc_quickinfo_news (id, instanceId, foreignId,
	important, broadcastTicker, broadcastMandatory,
	createDate, createdBy, lastUpdateDate, lastUpdatedBy,
	publishDate, publishedBy, version)
SELECT
	to_char(pubId),
	instanceId,
	to_char(pubId),
	0,
	0,
	0,
	to_date(pubCreationDate, 'YYYY/MM/DD'),
	pubCreatorId,
	to_date(pubUpdateDate, 'YYYY/MM/DD'),
	pubUpdaterId,
	to_date(pubCreationDate, 'YYYY/MM/DD'),
	pubCreatorId,
	0
FROM
	sb_publication_publi
WHERE
	instanceId like 'quickinfo%';