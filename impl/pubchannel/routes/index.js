var data = new Array();
var direct_messages = new Array();

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

exports.add_direct_message = function(req, res) {	
	var msg = req.body.msg;
	direct_messages[direct_messages.length] = msg;
	res.send('OK');
}


exports.get_all_direct_messages = function(req, res) {
	res.send(direct_messages);
}

exports.get_all_direct_messages_for = function(req, res) {
	var user = req.query.user;
	var results = new Array();

	//Select messages for user
	for( var i in direct_messages) {
		var msg = direct_messages[i];
		if(msg.to == user) { //Match user to the 'to' field
			results[results.length] = msg;
		}
	}

	res.send(results);
}