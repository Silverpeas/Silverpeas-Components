ALTER TABLE SC_Forums_HistoryUser ADD
	CONSTRAINT PK_Forums_HistoryUser PRIMARY KEY
	(
		userId,
		messageId
	)
;  