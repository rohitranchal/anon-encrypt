var data = new Array();
var direct_messages = new Array();

exports.index = function(req, res) {
	res.render('index', {});
};

exports.status = function(req, res) {

	var scn = new Array();	

	var peers = new Array();
	peers[peers.length] = {name: 'Alice', host: 'localhost:8001'};
	peers[peers.length] = {name: 'Bob', host: 'localhost:8002'};
	peers[peers.length] = {name: 'Charlie', host: 'localhost:8003'};

	scn[0] = {name: 'Basic Message Exchange', peers: peers};

	var peers = new Array();
	peers[peers.length] = {name: 'Alice', host: 'localhost:8001'};
	peers[peers.length] = {name: 'Bob', host: 'localhost:8002'};
	peers[peers.length] = {name: 'Charlie', host: 'localhost:8003'};

	scn[1] = {name: 'Message Exchange with Lying Peer', peers: peers};

	res.render('status', {scenarios: scn});
};

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
	var msg_id = req.query.msg_id;

	var results = new Array();

	//Select messages for user
	for( var i in direct_messages) {
		var msg = direct_messages[i];
		if(msg.to == user) { //Match user to the 'to' field
			results[results.length] = msg;
		}
	}

	results = results.slice(msg_id);
	res.send(results);
}