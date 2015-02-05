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
  publishDate 			TIMESTAMP   NULL,
  publishedBy  			VARCHAR(40) NULL,
  version        		INT8        NOT NULL
);

/* Constraints */
ALTER TABLE sc_quickinfo_news ADD CONSTRAINT const_sc_quickinfo_news_pk PRIMARY KEY (id);

/* Tables */
CREATE TABLE IF NOT EXISTS uniqueId (
  maxId     INT          NOT NULL,
  tableName VARCHAR(100) NOT NULL
);

CREATE TABLE ST_User
(
  id                            INT                  NOT NULL,
  domainId                      INT                  NOT NULL,
  specificId                    VARCHAR(500)         NOT NULL,
  firstName                     VARCHAR(100),
  lastName                      VARCHAR(100)         NOT NULL,
  email                         VARCHAR(100),
  login                         VARCHAR(50)          NOT NULL,
  loginMail                     VARCHAR(100),
  accessLevel                   CHAR(1) DEFAULT 'U'  NOT NULL,
  loginquestion                 VARCHAR(200),
  loginanswer                   VARCHAR(200),
  creationDate                  TIMESTAMP,
  saveDate                      TIMESTAMP,
  version                       INT DEFAULT 0        NOT NULL,
  tosAcceptanceDate             TIMESTAMP,
  lastLoginDate                 TIMESTAMP,
  nbSuccessfulLoginAttempts     INT DEFAULT 0        NOT NULL,
  lastLoginCredentialUpdateDate TIMESTAMP,
  expirationDate                TIMESTAMP,
  state                         VARCHAR(30)          NOT NULL,
  stateSaveDate                 TIMESTAMP            NOT NULL
);
