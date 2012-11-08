ALTER TABLE sc_wiki_page WITH NOCHECK ADD 
	CONSTRAINT PK_SC_Wiki_Page PRIMARY KEY CLUSTERED 
	(
		id,
		instanceId
	)   
;
