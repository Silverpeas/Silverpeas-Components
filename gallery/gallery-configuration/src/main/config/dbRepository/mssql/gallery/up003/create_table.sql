CREATE TABLE SC_Gallery_Order
(
	orderId			int		NOT NULL,
	userId			int		NOT NULL,
	instanceId		varchar(50)	NOT NULL,
	creationDate		char(13) 	NOT NULL,
	processDate		char(13)	NULL,
	processUser		int		NULL
);

CREATE TABLE SC_Gallery_OrderDetail
(
	orderId			int		NOT NULL,
	photoId			int		NOT NULL,
	instanceId		varchar(50)	NOT NULL,
	downloadDate		char(13) 	NULL,
	downloadDecision	varchar(50)	NULL
);