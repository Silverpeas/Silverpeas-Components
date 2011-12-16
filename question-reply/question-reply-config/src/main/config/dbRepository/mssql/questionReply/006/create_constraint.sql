ALTER TABLE SC_QuestionReply_Question WITH NOCHECK ADD 
CONSTRAINT PK_QuestionReply_Question PRIMARY KEY  CLUSTERED 
(
	id
)   
;

ALTER TABLE SC_QuestionReply_Reply WITH NOCHECK ADD 
CONSTRAINT PK_QuestionReply_Reply PRIMARY KEY  CLUSTERED 
(
	id
)   
;

ALTER TABLE SC_QuestionReply_Recipient WITH NOCHECK ADD 
CONSTRAINT PK_QuestionReply_Recipient PRIMARY KEY  CLUSTERED 
(
	id
)   
;
