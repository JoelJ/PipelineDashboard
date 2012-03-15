Event.observe(window, 'load', function() {
	window.setInterval(updateView, 10000);
});

var updateView = (function() {
	var url = document.URL;
	new Ajax.Request(url, {
		method: 'get',
		evalJS: 'false',
		onSuccess: function(transport) {
			var resultBody = transport.response;

			var myDiv = $(document.createElement("div"));
			myDiv.innerHTML = resultBody;

			var newPipelineDashboard = myDiv.getElementsBySelector('#PipelineDashboard');
			if(newPipelineDashboard.length > 0) {
				var oldPipelineDashboard = $('PipelineDashboard');
				oldPipelineDashboard.innerHTML = newPipelineDashboard[0].innerHTML;
			}

			if(window.initMagicPopup) {
				window.initMagicPopup();
			}
		},
		onError: function(transport) {
			console.log(transport);
		}
	});
});