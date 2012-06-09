var intervalID;
Event.observe(window, 'load', function() {
	intervalID = window.setInterval(update, 10000);
});

function update() {
	var url = document.URL;
	new Ajax.Request(url, {
		method: 'get',
		evalJS: 'false',
		onSuccess: function(transport) {
			var resultBody = transport.responseText;

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
			if(window.initHide) {
				window.initHide();
			}
		},
		onError: function(transport) {
			console.log(transport);
		}
	});
}

Event.observe(window, 'focus', function() {
	window.clearInterval(intervalID);
	intervalID = window.setInterval(update, 10000);
	update();
});

Event.observe(window, 'blur', function() {
	window.clearInterval(intervalID);
	intervalID = window.setInterval(update, 20000);
});