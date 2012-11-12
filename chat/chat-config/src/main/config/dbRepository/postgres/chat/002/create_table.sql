CREATE TABLE SC_chat_chatroom 
(
	ID			int		NOT NULL ,
	instanceID		varchar (50)	NOT NULL ,
	chatRoomID		int		NOT NULL
);

CREATE TABLE SC_chat_banned 
(
	ID			int		NOT NULL ,
	chatRoomID		int		NOT NULL ,
	userID			varchar (150)	NOT NULL
);