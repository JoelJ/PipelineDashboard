var intervalID;
Event.observe(window, 'load', function() {
	intervalID = window.setInterval(AutoRefresh.update, 10000);
});

var AutoRefresh = {
	onUpdateSuccess: function onUpdateSuccess(transport) {
		var resultBody = transport.responseText;

		var newDiv = $(document.createElement("div"));
		newDiv.innerHTML = resultBody;

		var newPipelineDashboard = newDiv.getElementsBySelector('#projectstatus');
		if(newPipelineDashboard.length > 0) {
			var oldPipelineDashboard = $('projectstatus');
			oldPipelineDashboard.innerHTML = newPipelineDashboard[0].innerHTML;
		}

		if(window.initMagicPopup) {
			window.initMagicPopup();
		}
	},

	onUpdateError: function onUpdateError(transport) {
		console.log(transport);
	},

	update: function update() {
		var url = document.URL;
		new Ajax.Request(url, {
			method: 'get',
			evalJS: 'false',
			onSuccess: AutoRefresh.onUpdateSuccess,
			onError: AutoRefresh.onUpdateError
		});
	}
};
