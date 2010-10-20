CREATE TABLE sc_questionreply_question  (
	id			int		NOT NULL,
	title			varchar (100)	NOT NULL,
	content			varchar (2000)	NULL,
	creatorId		varchar (50)	NOT NULL,
	creationDate		varchar (10)	NOT NULL,
	status 			int 		NOT NULL,
	publicReplyNumber 	int 		NOT NULL, 
	privateReplyNumber 	int 		NOT NULL, 
	replyNumber 		int 		NOT NULL,
	instanceId		varchar (50)	NOT NULL,
	categoryId		varchar (50)	NULL
) 
;

CREATE TABLE SC_QuestionReply_Reply 
(
	id			int		NOT NULL,
	questionId 		int 		NOT NULL,
	title			varchar (100)	NOT NULL,
	content			varchar (2000)	NULL,
	creatorId		varchar (50)	NOT NULL,
	creationDate		varchar (10)	NOT NULL,
	publicReply 		int 		NOT NULL,
	privateReply 		int 		NOT NULL
) 
;

CREATE TABLE SC_QuestionReply_Recipient 
(
	id			int		NOT NULL,
	questionId 		int		NOT NULL,
	userId			varchar (50)	NOT NULL
) 
;

ALTER TABLE SC_QuestionReply_Question ADD 
CONSTRAINT PK_QuestionReply_Question PRIMARY KEY 
(
	id
)   
;

ALTER TABLE SC_QuestionReply_Reply ADD 
CONSTRAINT PK_QuestionReply_Reply PRIMARY KEY 
(
	id
)   
;

ALTER TABLE SC_QuestionReply_Recipient ADD 
CONSTRAINT PK_QuestionReply_Recipient PRIMARY KEY 
(
	id
)   
;
