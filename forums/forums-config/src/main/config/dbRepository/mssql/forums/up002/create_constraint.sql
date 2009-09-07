ALTER TABLE SC_Forums_HistoryUser WITH NOCHECK ADD
	CONSTRAINT PK_Forums_HistoryUser PRIMARY KEY CLUSTERED
	(
		userId,
		messageId
	)
;