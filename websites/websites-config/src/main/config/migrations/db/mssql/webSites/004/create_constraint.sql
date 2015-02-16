ALTER TABLE SC_WebSites_Icons WITH NOCHECK ADD 
	 CONSTRAINT PK_WebSites_Icons PRIMARY KEY  CLUSTERED 
	(
		iconsId
	)
	;   

ALTER TABLE SC_WebSites_Site WITH NOCHECK ADD 
	 CONSTRAINT PK_WebSites_Site PRIMARY KEY  CLUSTERED 
	(
		siteId
	)
	;   

ALTER TABLE SC_WebSites_siteIcons WITH NOCHECK ADD 
	 CONSTRAINT PK_WebSites_siteIcons PRIMARY KEY  CLUSTERED 
	(
		siteId,
		iconsId
	)
	;   