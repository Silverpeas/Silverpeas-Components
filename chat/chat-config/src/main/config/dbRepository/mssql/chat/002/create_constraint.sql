ALTER TABLE SC_chat_chatroom WITH NOCHECK ADD 
	 CONSTRAINT PK_chat_chatroom PRIMARY KEY  CLUSTERED 
	(
		ID
	)   
;

ALTER TABLE SC_chat_banned WITH NOCHECK ADD 
	 CONSTRAINT PK_chat_banned PRIMARY KEY  CLUSTERED 
	(
		ID
	)   
;