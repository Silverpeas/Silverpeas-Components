ALTER TABLE SC_Contact_GroupFather WITH NOCHECK ADD 
	 CONSTRAINT PK_SC_Contact_GroupFather PRIMARY KEY  CLUSTERED 
	(
		groupId, fatherId, instanceId
	)
;
