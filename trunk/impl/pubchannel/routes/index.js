var data = new Array();

exports.get_all_messages = function(req, res) {
	res.send(data);
};

exports.get_all_messages_after = function(req, res) {
	var msg_id = req.query.msg_id;
	var tmp = data.slice(msg_id);
	res.send(tmp);
};

exports.add_message = function(req, res) {
	var msg = req.query.msg;
	if(typeof msg == 'undefined') {
		msg = req.body.msg;
	}

	if(typeof msg != 'undefined') {
		var ts = new Date().getTime();
		tmp_data = {"data" : msg, "timestamp" : ts};
		data[data.length] = tmp_data;
	}
	
	res.send('OK');
};