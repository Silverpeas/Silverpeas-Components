ALTER TABLE SC_Forums_Forum WITH NOCHECK ADD 
	 CONSTRAINT PK_Forums_Forum PRIMARY KEY  CLUSTERED 
	(
		forumId
	)   
;

ALTER TABLE SC_Forums_Message WITH NOCHECK ADD 
	 CONSTRAINT PK_Forums_Message PRIMARY KEY CLUSTERED 
	(
		messageId
	)   
;

ALTER TABLE SC_Forums_Rights WITH NOCHECK ADD 
	 CONSTRAINT PK_Forums_Rights PRIMARY KEY  CLUSTERED 
	(
		userId,
		forumId
	)   
;

ALTER TABLE SC_Forums_Subscription WITH NOCHECK ADD 
	 CONSTRAINT PK_Forums_Subscription PRIMARY KEY  CLUSTERED 
	(
		userId,
		messageId
	)   
;

ALTER TABLE SC_Forums_HistoryUser WITH NOCHECK ADD
	CONSTRAINT PK_Forums_HistoryUser PRIMARY KEY CLUSTERED
	(
		userId,
		messageId
	)
;  