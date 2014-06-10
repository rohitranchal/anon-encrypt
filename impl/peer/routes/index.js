var java = require("java");
var fs = require("fs");
var request = require("request");
var Q = require("q");

//Java dependancies
var jars_dir = "/Users/ruchith/Documents/research/anon-encrypt/trunk/impl/base/target/lib/";
java.classpath.push(jars_dir + "bcprov-jdk16-1.46.jar");
java.classpath.push(jars_dir + "jackson-jaxrs-1.9.4.jar");
java.classpath.push(jars_dir + "jackson-mrbean-1.9.4.jar");
java.classpath.push(jars_dir + "jpbc-api-1.1.0.jar");
java.classpath.push(jars_dir + "jpbc-pbc-1.1.0.jar");
java.classpath.push(jars_dir + "junit-3.8.1.jar");
java.classpath.push(jars_dir + "jackson-core-asl-1.9.4.jar");
java.classpath.push(jars_dir + "jackson-mapper-asl-1.9.4.jar");
java.classpath.push(jars_dir + "jackson-xc-1.9.4.jar");
java.classpath.push(jars_dir + "jpbc-crypto-1.1.0.jar");
java.classpath.push(jars_dir + "jpbc-plaf-1.1.0.jar");
java.classpath.push(jars_dir + "../base-1.0-SNAPSHOT.jar");

var config_file = './config.json';

config_file = process.argv[3];


var config_data = fs.readFileSync(config_file).toString();
var config = JSON.parse(config_data);
var name = config.name;
var lie = (typeof config.lie != 'undefined' && config.lie);

console.log('[' + name + '] : LIE ' + lie);

var Peer = java.import('org.ruchith.ae.peer.Peer');
var peer = new Peer(name, lie);

var process_action = function(val) {
	if(typeof val.parameters != 'undefined') {
		if(val.action == 'add_contact') {
			console.log('['  + name + '] Creating contact ' + val.parameters.name);
			peer.createContactStr(val.parameters.name, function(err, result) {
				var priv_data = JSON.parse(result);
				//Send this to the contact
				var post_data = {"from" : name, 
								"to" : val.parameters.name,
								"priv_data" : priv_data};

				request.post('http://localhost:5000/direct_message', {form:{msg:post_data}});
			});
		} else if(val.action == 'direct_message') {
			console.log('['  + name + '] Send message to ' + val.parameters.to);
			var post_data = {"from" : name, 
							"to" : val.parameters.to,
							"msg" : val.parameters.message};

			request.post('http://localhost:5000/direct_message', {form:{msg:post_data}});
		} else if(val.action == 'request_update') {
			console.log('['  + name + '] Requesting latest message of ' + val.parameters.name);
			peer.generateRequestStr(val.parameters.name, function(err, result) {
				if(err) {
					console.log(err);	
				} else {
					var data_req = JSON.parse(result);
					request.post('http://localhost:5000/add_message', {form:{msg:data_req}});
				}
				
			});
		}
		return 1;
	}
	return 0;
};

var actions = config.actions;

exports.start = function(req, res) {
	actions.reduce(function (previous, item) {
		return Q(process_action(previous))
			.then(process_action(item));
	});
	res.send('OK');
};

exports.stop_peer = function(req, res) {
	console.log('['  + name + '] STOP');
	res.send('OK');
	process.exit();
}

exports.index = function(req, res) {
  res.render('index', { title: 'Express' });
};

exports.contacts = function(req, res) {
	peer.getContacts(function(err, results) {
		// console.log('['  + name + '] ERR' + err);
		// console.log(results);
		res.send(JSON.parse(results));
	});
};



var msg_index = 0;
var pubchannel_update_interval = 3000;

//Thread to read the public channel general messages
setInterval(function() {
	request('http://localhost:5000/get_all_messages_after?msg_id=' + msg_index, function (error, response, data) {
		if (!error && response.statusCode == 200) {
			//Convert to JSON
			j_data = JSON.parse(data);
			
			//Update message index
			msg_index += j_data.length;
			if(j_data.length > 0) {
				// console.log('['  + name + '] PUB CHANNEL ' + data);
				for(i in j_data) {
					//Ignore expired or closed messages
					if(j_data[i].status != 'expired' && j_data[i].status != 'closed') {
						var tmp_data = JSON.stringify(j_data[i].data);
						if(j_data[i].data.type == 'data_request') {
							peer.generateResponseStr(tmp_data, function(err, result) {
								if(!err) {
									console.log(result);
									var resp = JSON.parse(result);
									request.post('http://localhost:5000/add_message', {form:{msg:resp}});
								} else {
									console.log(err);
								}
							});
						} else if(j_data[i].data.type == 'data_response') {
							console.log('RESPONSE:' + tmp_data);
							var tmpPubKey = j_data[i].data.tmpPubKey;
							peer.processResponseStr(tmp_data, function(err, result) {
								if(!err && result != null) {
									console.log(result);
									if(result.indexOf("lie:") == -1) {
										//This replaces signature verification
										console.log('NOT A LIE');
										//Send confirmation
										var conf = {"type" : "data_request_confirmation", "tmpPubKey" : tmpPubKey}
										request.post('http://localhost:5000/add_message', {form:{msg:conf}});
									} else {
										console.log('LIE RECEIVED!!!');
									}
								} else {
									console.log(err);
								}
							});
						}
					} else {
						console.log('expired message ' + i);
					}
				}

			}
		}
	});
}, pubchannel_update_interval);


//Thread to read the public channel personal messages
var priv_msg_index = 0;
setInterval(function() {
	request('http://localhost:5000/get_direct_messages?user=' + name + '&msg_id=' + priv_msg_index, function (error, response, data) {
		if (!error && response.statusCode == 200) {
			//Convert to JSON
			j_data = JSON.parse(data);
			//console.log('['  + name + '] DATA ' + data);
			//Update message index
			priv_msg_index += j_data.length;
			if(j_data.length > 0) {
				for(var i in j_data) {
					var msg = j_data[i];
					if(typeof msg.priv_data != 'undefined') {
						//Private data from a peer
						peer.registerContactStr(msg.from, JSON.stringify(msg.priv_data), function(err, result) {
							if(!err) {
								console.log('['  + name + '] Added priv data from ' + msg.from);
							} else {
								console.log(err);
							}
						});
					} else if(typeof msg.msg != 'undefined') {
						// console.log(msg);
						//Direct message from a peer
						peer.addDirectMessage(msg.from, msg.msg, function(err, result) {
							if(!err) {
								console.log('['  + name + '] Message from ' + msg.from);
							} else {
								// console.log(err);
							}
						});
					}
				}
			}
		}
	});
}, pubchannel_update_interval);

