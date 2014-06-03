/* Tables */
CREATE TABLE sc_quickinfo_news (
  id             		VARCHAR(40) NOT NULL,
  instanceId     		VARCHAR(30) NOT NULL,
  foreignId		 		VARCHAR(40) NOT NULL,
  important      		BOOLEAN	 	NOT NULL,
  broadcastTicker		BOOLEAN 	NOT NULL,
  broadcastMandatory	BOOLEAN		NOT NULL,
  createDate     		TIMESTAMP   NOT NULL,
  createdBy      		VARCHAR(40) NOT NULL,
  lastUpdateDate 		TIMESTAMP   NOT NULL,
  lastUpdatedBy  		VARCHAR(40) NOT NULL,
  version        		INT8        NOT NULL
);

/* Constraints */
ALTER TABLE sc_quickinfo_news ADD CONSTRAINT const_sc_quickinfo_news_pk PRIMARY KEY (id);

/* Tables */
CREATE TABLE IF NOT EXISTS uniqueId (
  maxId     INT          NOT NULL,
  tableName VARCHAR(100) NOT NULL
);