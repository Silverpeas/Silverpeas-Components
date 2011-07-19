	ALTER TABLE sc_scheduleevent_contributor
	ADD lastvalidation timestamp
	;
	
	UPDATE sc_scheduleevent_contributor
	SET lastvalidation = lastvisit
	;
	