var data = new Array();
var direct_messages = new Array();
var msg_index = new Array();
var ttl = 5000;
var req_count = 0;
var resp_count = 0;
var conf_count = 0;
var closed_reqs = new Array();

exports.index = function(req, res) {
	res.render('index', {});
};


exports.msg_count = function(req, res) {
	res.send({'total_messages' : data.length, 'direct_messages' : direct_messages.length, 'req_count' : req_count, 'resp_count': resp_count, 'conf_count': conf_count});
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

	var now = new Date().getTime();
	var limit = now - ttl;
	for(var i = 0; i< tmp.length; i++) {
		if(tmp[i].timestamp < limit) {
			tmp[i].status = 'expired';
		}
		if(closed_reqs.indexOf(tmp[i].data.tmpPubKey) != -1) {
			tmp[i].status = 'closed';
		}
	}
	res.send(tmp);
};

exports.add_message = function(req, res) {
	var msg = req.query.msg;
	if(typeof msg == 'undefined') {
		msg = req.body.msg;
	}

	if(typeof msg != 'undefined') {

		//Handle confirmatiom messages
		if(msg.type == 'data_request_confirmation') {
			closed_reqs[closed_reqs.length] = msg.tmpPubKey;
			console.log('Closed : ' + msg.tmpPubKey);
			conf_count++;
		} else if (msg.type == 'data_request') {
			req_count++;
		} else if (msg.type == 'data_response') {
			resp_count++;
		}

		var ts = new Date().getTime();
		tmp_data = {"data" : msg, "timestamp" : ts, "status" : "live"};
		data[data.length] = tmp_data;
	}
	
	res.send('OK');
};

exports.get_message_index_of_peer = function(req, res) {

	var user = req.query.user;

	var user_obj = {};
	for(var i = 0; i < msg_index.length; i++) {
		if(msg_index[i].user == user) {
			user_obj = msg_index[i];
		}
	}

	res.send(user_obj);
}

exports.set_message_index_of_peer = function(req, res) {
	var user = req.query.user;
	var ind = req.query.ind;
	var user_obj_ind = -1;
	for(var i = 0; i < msg_index.length; i++) {
		console.log(msg_index[i].user);
		if(msg_index[i].user == user) {
			user_obj_ind = i;
		}
	}

	if(user_obj == null) {
		var user_obj = {"user" : user, "index" : ind};
		msg_index[msg_index.length] = user_obj;
	} else {
		msg_index[user_obj_ind].index = ind;
	}
	res.send('OK');
}

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