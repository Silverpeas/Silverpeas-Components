	alter table sc_scheduleevent_contributor
	add lastvalidation timestamp
	;
	
	update sc_scheduleevent_contributor
	set lastvalidation = lastvisit
	;
	