ALTER TABLE SC_Forums_Forum ADD 
	 CONSTRAINT PK_Forums_Forum PRIMARY KEY 
	(
		forumId
	)   
;

ALTER TABLE SC_Forums_Message ADD 
	 CONSTRAINT PK_Forums_Message PRIMARY KEY
	(
		messageId
	)   
;



ALTER TABLE SC_Forums_Rights ADD 
	 CONSTRAINT PK_Forums_Rights PRIMARY KEY
	(
		userId,
		forumId
	)   
;

ALTER TABLE SC_Forums_Subscription ADD 
	 CONSTRAINT PK_Forums_Subscription PRIMARY KEY 
	(
		userId,
		messageId
	)   
;

ALTER TABLE SC_Forums_HistoryUser ADD
	CONSTRAINT PK_Forums_HistoryUser PRIMARY KEY
	(
		userId,
		messageId
	)
;  