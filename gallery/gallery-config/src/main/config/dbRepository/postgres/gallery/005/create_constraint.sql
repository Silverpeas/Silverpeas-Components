ALTER TABLE SC_Gallery_Photo
ADD CONSTRAINT PK_SC_Gallery_Photo PRIMARY KEY
	(
		photoId
	)   
;

ALTER TABLE SC_Gallery_Path
ADD CONSTRAINT PK_SC_Gallery_Path PRIMARY KEY
	(
		photoId, nodeId
	)   
;

ALTER TABLE SC_Gallery_Order
ADD CONSTRAINT PK_SC_Gallery_Order PRIMARY KEY
	(
		orderId
	)   
;

ALTER TABLE SC_Gallery_OrderDetail
ADD CONSTRAINT PK_SC_Gallery_OrderDetail PRIMARY KEY
	(
		orderId, photoId
	)   
;