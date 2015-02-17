CREATE TABLE SC_SilverCrawler_Statistic
(
  dateDownload CHAR(13)      NOT NULL,
  userId       VARCHAR(100)  NOT NULL,
  path         VARCHAR(1000) NOT NULL,
  componentId  VARCHAR(50)   NOT NULL,
  objectType   VARCHAR(50)   NOT NULL
);