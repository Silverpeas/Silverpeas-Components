CREATE TABLE SC_WebSites_Icons 
(
	iconsId			int		NOT NULL ,
	iconsName		varchar (1000)	NOT NULL ,
	iconsDescription	varchar (2000)	NOT NULL ,
	iconsAddress		varchar (1000)	NOT NULL 
) 
;

CREATE TABLE SC_WebSites_Site 
(
	siteId			int		NOT NULL ,
	siteName		varchar (1000)	NOT NULL ,
	siteDescription		varchar (2000)	NOT NULL ,
	sitePage		varchar (1000)	NOT NULL ,
	siteType		int		NOT NULL ,
	siteAuthor		varchar (1000)	NOT NULL ,
	siteDate		varchar (10)	NOT NULL ,
	siteState		int		NOT NULL ,
	instanceId		varchar (50)	NOT NULL
) 
;

CREATE TABLE SC_WebSites_SiteIcons 
(
	siteId		int		NOT NULL ,
	iconsId		int		NOT NULL 
) 
;