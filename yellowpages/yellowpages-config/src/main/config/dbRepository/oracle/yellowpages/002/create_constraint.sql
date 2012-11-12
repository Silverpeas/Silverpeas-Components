ALTER TABLE SC_Contact_GroupFather
ADD CONSTRAINT PK_SC_Contact_GroupFather PRIMARY KEY
	(
		groupId, fatherId, instanceId
	)   
;
