ALTER TABLE SC_Kmelia_ModelUsed WITH NOCHECK ADD 
	 CONSTRAINT PK_SC_Kmelia_ModelUsed PRIMARY KEY  CLUSTERED 
	(
		instanceId,
		modelId
	)
;
