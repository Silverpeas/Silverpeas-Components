CREATE INDEX idx_Community_Component ON SC_Community (id, instanceId);
CREATE INDEX idx_Community_Space ON SC_Community (id, spaceId);
CREATE INDEX idx_Community_Membership ON SC_Community_Membership (id, community);