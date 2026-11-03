ALTER TABLE sc_discussion_conversation ADD channelName VARCHAR(80);
ALTER TABLE sc_discussion_conversation ADD channelDescription VARCHAR(255);
ALTER TABLE sc_discussion_conversation ADD archived BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE sc_discussion_read ADD notificationMode VARCHAR(10) DEFAULT 'ALL' NOT NULL;
