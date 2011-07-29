	ALTER TABLE sc_scheduleevent_contributor
	ADD lastvalidation datetime
	;
	
	UPDATE sc_scheduleevent_contributor
	SET lastvalidation = lastvisit
	;
	