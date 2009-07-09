CREATE TABLE SC_SilverCrawler_Statistic
(
	dateDownload		char(13)	NOT NULL,
	userId			varchar (100)	NOT NULL,
	path			varchar(1000)	NOT NULL,
	componentId		varchar(50)	NOT NULL,
	objectType		varchar(50)	NOT NULL
) 
;