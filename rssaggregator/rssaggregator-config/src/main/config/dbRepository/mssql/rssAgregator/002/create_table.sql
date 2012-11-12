CREATE TABLE SC_Rss_Channels 
(
	id			int		NOT NULL,
	url			varchar (1000)	NOT NULL,
	refreshRate		int		NOT NULL,
	nbDisplayedItems	int		NOT NULL,
	displayImage		int		NOT NULL,
	creatorId		varchar (100)	NOT NULL,
	creationDate		char(10)	NOT NULL,
	instanceId		varchar (50)	NOT NULL
)
;
