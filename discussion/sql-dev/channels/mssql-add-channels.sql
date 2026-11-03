ALTER TABLE sc_discussion_conversation ADD channelName NVARCHAR(80);
ALTER TABLE sc_discussion_conversation ADD channelDescription NVARCHAR(255);
ALTER TABLE sc_discussion_conversation ADD archived BIT DEFAULT 0 NOT NULL;
ALTER TABLE sc_discussion_read ADD notificationMode NVARCHAR(10) DEFAULT 'ALL' NOT NULL;
