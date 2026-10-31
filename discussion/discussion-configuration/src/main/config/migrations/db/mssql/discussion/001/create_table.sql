-- Copyright (C) 2000 - 2026 Silverpeas
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as
-- published by the Free Software Foundation, either version 3 of the
-- License, or (at your option) any later version.
--
-- As a special exception to the terms and conditions of version 3.0 of
-- the GPL, you may redistribute this Program in connection with Free/Libre
-- Open Source Software ("FLOSS") applications as described in Silverpeas's
-- FLOSS exception. You should have received a copy of the text describing
-- the FLOSS exception, and it is also available here:
-- "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY, without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.

CREATE TABLE sc_discussion_conversation (
  id VARCHAR(40) PRIMARY KEY,
  instanceId VARCHAR(30) NOT NULL,
  scopeKey VARCHAR(40) NOT NULL,
  lastSequence BIGINT NOT NULL,
  createDate DATETIME2 NOT NULL,
  createdBy VARCHAR(40) NOT NULL,
  lastUpdateDate DATETIME2 NOT NULL,
  lastUpdatedBy VARCHAR(40) NOT NULL,
  version BIGINT NOT NULL,
  CONSTRAINT uq_discussion_scope UNIQUE (instanceId, scopeKey)
);
CREATE TABLE sc_discussion_message (
  id VARCHAR(40) PRIMARY KEY,
  conversationId VARCHAR(40) NOT NULL,
  sequenceNumber BIGINT NOT NULL,
  body NVARCHAR(2000) NOT NULL,
  filesAttached BIT DEFAULT 0 NOT NULL,
  pinned BIT DEFAULT 0 NOT NULL,
  deleted BIT NOT NULL,
  createDate DATETIME2 NOT NULL,
  createdBy VARCHAR(40) NOT NULL,
  lastUpdateDate DATETIME2 NOT NULL,
  lastUpdatedBy VARCHAR(40) NOT NULL,
  version BIGINT NOT NULL,
  CONSTRAINT fk_discussion_message FOREIGN KEY (conversationId) REFERENCES sc_discussion_conversation(id),
  CONSTRAINT uq_discussion_sequence UNIQUE (conversationId, sequenceNumber)
);
CREATE TABLE sc_discussion_read (
  id VARCHAR(40) PRIMARY KEY,
  conversationId VARCHAR(40) NOT NULL,
  userId VARCHAR(40) NOT NULL,
  lastReadSequence BIGINT NOT NULL,
  createDate DATETIME2 NOT NULL,
  createdBy VARCHAR(40) NOT NULL,
  lastUpdateDate DATETIME2 NOT NULL,
  lastUpdatedBy VARCHAR(40) NOT NULL,
  version BIGINT NOT NULL,
  CONSTRAINT fk_discussion_read FOREIGN KEY (conversationId) REFERENCES sc_discussion_conversation(id),
  CONSTRAINT uq_discussion_reader UNIQUE (conversationId, userId)
);
CREATE INDEX idx_discussion_unread ON sc_discussion_message (conversationId, deleted, sequenceNumber);

CREATE TABLE sc_discussion_like (
  id VARCHAR(40) PRIMARY KEY,
  conversationId VARCHAR(40) NOT NULL,
  messageId VARCHAR(40) NOT NULL,
  userId VARCHAR(40) NOT NULL,
  createDate DATETIME2 NOT NULL,
  createdBy VARCHAR(40) NOT NULL,
  lastUpdateDate DATETIME2 NOT NULL,
  lastUpdatedBy VARCHAR(40) NOT NULL,
  version BIGINT NOT NULL,
  CONSTRAINT fk_discussion_like_message FOREIGN KEY (messageId) REFERENCES sc_discussion_message(id),
  CONSTRAINT fk_discussion_like_conv FOREIGN KEY (conversationId) REFERENCES sc_discussion_conversation(id),
  CONSTRAINT uq_discussion_like UNIQUE (messageId, userId)
);

CREATE TABLE sc_discussion_seen (
  id VARCHAR(40) PRIMARY KEY,
  conversationId VARCHAR(40) NOT NULL,
  messageId VARCHAR(40) NOT NULL,
  userId VARCHAR(40) NOT NULL,
  createDate DATETIME2 NOT NULL,
  createdBy VARCHAR(40) NOT NULL,
  lastUpdateDate DATETIME2 NOT NULL,
  lastUpdatedBy VARCHAR(40) NOT NULL,
  version BIGINT NOT NULL,
  CONSTRAINT fk_discussion_seen_message FOREIGN KEY (messageId) REFERENCES sc_discussion_message(id),
  CONSTRAINT fk_discussion_seen_conv FOREIGN KEY (conversationId) REFERENCES sc_discussion_conversation(id),
  CONSTRAINT uq_discussion_seen UNIQUE (messageId, userId)
);
