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