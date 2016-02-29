ALTER TABLE sc_mailinglist_attachment ALTER COLUMN attachmentSize bigint;
ALTER TABLE sc_mailinglist_message ALTER COLUMN attachmentsSize bigint;
ALTER TABLE sc_mailinglist_message ALTER COLUMN moderated bit;