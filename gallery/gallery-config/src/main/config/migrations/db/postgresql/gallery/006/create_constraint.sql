ALTER TABLE SC_Gallery_Media
ADD CONSTRAINT PK_SC_Gallery_Media PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Internal
ADD CONSTRAINT PK_SC_Gallery_Internal PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Photo
ADD CONSTRAINT PK_SC_Gallery_Photo PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Video
ADD CONSTRAINT PK_SC_Gallery_Video PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Sound
ADD CONSTRAINT PK_SC_Gallery_Sound PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Streaming
ADD CONSTRAINT PK_SC_Gallery_Streaming PRIMARY KEY (mediaId);

ALTER TABLE SC_Gallery_Path
ADD CONSTRAINT PK_SC_Gallery_Path PRIMARY KEY (nodeId, mediaId);

ALTER TABLE SC_Gallery_Order
ADD CONSTRAINT PK_SC_Gallery_Order PRIMARY KEY (orderId);

ALTER TABLE SC_Gallery_OrderDetail
ADD CONSTRAINT PK_SC_Gallery_OrderDetail PRIMARY KEY (orderId, mediaId);