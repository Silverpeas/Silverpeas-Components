ALTER TABLE SC_WebSites_Icons ADD 
	 CONSTRAINT PK_WebSites_Icons PRIMARY KEY
	(
		iconsId
	)
	;   

ALTER TABLE SC_WebSites_Site ADD 
	 CONSTRAINT PK_WebSites_Site PRIMARY KEY
	(
		siteId
	)
	;   

ALTER TABLE SC_WebSites_siteIcons ADD 
	 CONSTRAINT PK_WebSites_siteIcons PRIMARY KEY
	(
		siteId,
		iconsId
	)
	;   
