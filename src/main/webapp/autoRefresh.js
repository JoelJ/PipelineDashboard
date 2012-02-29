Event.observe(window, 'load', function() {
	window.setInterval(updateView, 5000);
});

var updateView = (function() {
	var url = document.URL;
	new Ajax.Request(url, {
		method: 'get',
		onSuccess: function(transport) {
			var resultBody = transport.response;

			var myDiv = $(document.createElement("div"));
			myDiv.innerHTML = resultBody;

			var newPipelineDashboard = myDiv.getElementsBySelector('#PipelineDashboard');
			if(newPipelineDashboard.length > 0) {
				var oldPipelineDashboard = $('PipelineDashboard');
				oldPipelineDashboard.innerHTML = newPipelineDashboard[0].innerHTML;
			}
		},
		onError: function(transport) {
			console.log(transport);
		}
	});
});