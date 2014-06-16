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