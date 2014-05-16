CREATE INDEX idx_suggestion_box ON sc_suggestion_box (instanceId, id);

CREATE INDEX idx_suggestion ON sc_suggestion (suggestionBoxId, id);