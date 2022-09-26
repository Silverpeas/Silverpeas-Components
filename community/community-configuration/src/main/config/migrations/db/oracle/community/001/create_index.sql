CREATE INDEX idx_Community ON SC_Community_Component (id, instanceId);
CREATE INDEX idx_Community ON SC_Community_Space (id, spaceId);
CREATE INDEX idx_Community_Member ON SC_Community_Member (id, community);