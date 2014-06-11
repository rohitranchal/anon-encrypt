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


var name = process.argv[3];
var lie = process.argv.length == 5;
var msg_count = 0;
var contacts = new Array();
var my_pub_keys = new Array();

console.log('[' + name + '] : LIE ' + lie);

var Peer = java.import('org.ruchith.ae.peer.Peer');
var peer = new Peer(name, lie);



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

exports.do_action = function(req, res) {
	var action = req.query.action;

	if(action == 'add_contact') {
			console.log('['  + name + '] Creating contact ' + req.query.name);
			peer.createContactStr(req.query.name, function(err, result) {
				var priv_data = JSON.parse(result);
				//Send this to the contact
				var post_data = {"from" : name, 
								"to" : req.query.name,
								"priv_data" : priv_data};

				contacts[contacts.length] = {"name" : req.query.name, "msg_index" : 0, "available" : 0};

				request.post('http://localhost:5000/direct_message', {form:{msg:post_data}});
			});
		} else if(action == 'direct_message') {
			console.log('['  + name + '] Send message to ' + req.query.to);
			var post_data = {"from" : name, 
							"to" : req.query.to,
							"msg" : req.query.message};

			request.post('http://localhost:5000/direct_message', {form:{msg:post_data}});

			msg_count++;

			request('http://localhost:5000/set_message_index_of_peer?user=' + name + '&ind=' + msg_count, function (error, response, body) {
				// console.log(body);
			});
		}
		res.send('OK');
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
			console.log('['  + name + '] msg index ' + msg_index);
			if(j_data.length > 0) {
				// console.log('['  + name + '] PUB CHANNEL ' + data);
				for(i in j_data) {
					//Ignore expired or closed messages
					if(j_data[i].status != 'expired' && j_data[i].status != 'closed') {

						var tmp_data = JSON.stringify(j_data[i].data);
						if(j_data[i].data.type == 'data_request') {

							//If this is not one of my requests
							if(my_pub_keys.indexOf(j_data[i].data.tmpPubKey) == -1) {
								peer.generateResponseStr(tmp_data, function(err, result) {
									if(!err) {
										console.log('['  + name + '] Sending ' + result);
										var resp = JSON.parse(result);
										request.post('http://localhost:5000/add_message', {form:{msg:resp}});
									} else {
										console.log(err);
									}
								});
							}
							
						} else if(j_data[i].data.type == 'data_response') {
							//console.log('RESPONSE:' + tmp_data);
							var tmpPubKey = j_data[i].data.tmpPubKey;
							var tmpUser = j_data[i].data.user;

							//If I asked for this
							if(my_pub_keys.indexOf(tmpPubKey) != -1) {
								peer.processResponseStr(tmp_data, function(err, result) {
									if(!err && result != null) {
										console.log(result);
										if(result.indexOf("lie:") == -1) {
											//This replaces signature verification
											console.log('['  + name + ']NOT A LIE');
											//Send confirmation
											var conf = {"type" : "data_request_confirmation", "tmpPubKey" : tmpPubKey}
											request.post('http://localhost:5000/add_message', {form:{msg:conf}});

											for(var x = 0; x < contacts.length; x++) {
												if(contacts[x].name == tmpUser) {
													contacts[x].msg_index = contacts[x].available;
												}
											}

										} else {
											console.log('['  + name + ']LIE RECEIVED!!!');
										}
									} else {
										console.log(err);
									}
								});
							}

						}
					} else {
						console.log('expired message ' + i);
					}
				}

			}
		}
	});
}, pubchannel_update_interval*3);


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

						for(var x = 0; x < contacts.length; x++) {
							if(contacts[x].name == msg.from) {
								console.log('['  + name + '] Seting ' + msg.from + '  data index');
								contacts[x].msg_index = 9999;
							}
						}

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

//Periodically check for updates of contacts 
//If there is a new message then ask for it
setInterval(function() {
	for(var i = 0; i< contacts.length; i++) {
		console.log('['  + name + '] checking status of ' + contacts[i].name);
		request('http://localhost:5000/get_message_index_of_peer?user=' + contacts[i].name, function (error, response, data) {
			if (!error && response.statusCode == 200) {
				data = JSON.parse(data);

				//Check for the index of the user
				for(var j = 0; j < contacts.length; j++) {

					if(contacts[j].name == data.user && contacts[j].msg_index < data.index) {
						contacts[j].available = data.index;
						console.log('['  + name + '] generating request');
						//Make request for data
						peer.generateRequestStr(data.user, function(err, result) {
							if(err) {
								console.log(err);	
							} else {
								var data_req = JSON.parse(result);

								//Store the pub key
								my_pub_keys[my_pub_keys.length] = data_req.tmpPubKey;

								request.post('http://localhost:5000/add_message', {form:{msg:data_req}});
							}
							
						});
					}
				}
			}
		});
	}

}, pubchannel_update_interval * 4);