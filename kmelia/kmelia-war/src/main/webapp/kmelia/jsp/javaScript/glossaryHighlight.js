//manage the tooltip displaying in publication
//the displaying personalization must be done here
$(document).ready(function() {
	$('.highlight-silver').each(function(){
		   $(this).qtip({
			   content: { text: false // Use each elements title attribute
			   },				   
		       style: 'silverpeas',
		       position: {
		    	   adjust: { screen: true }
			   }
		   });
		});
});
