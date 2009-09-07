ALTER TABLE SC_Forums_Forum
ADD	categoryId	varchar (50) NULL
;

ALTER TABLE SC_Forums_Message
ADD	messageCreationTime	char(13) NOT NULL DEFAULT('0000000000000')
;

CREATE TABLE SC_Forums_HistoryUser
(
	userId			varchar (255)	NOT NULL ,
	messageId		int		NOT NULL , 
	lastAccess		varchar (50)	NOT NULL
);