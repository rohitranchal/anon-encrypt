var java = require("java");
var fs = require("fs");
var request = require("request");
var Q = require("q");

var data_recived = 0;
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
		} else if(action == 'remove_contact') {
			console.log('['  + name + '] Remove contact ' + req.query.contact);
			
			var contact_ind = contacts.indexOf(req.query.contact);
			contacts.splice(contact_ind, 1);

			peer.removeContact(req.query.contact, function(err, result) {
				//Remove and send re-key information to pub channel
				var post_data = {"from" : name,
								"type" : "re-key",
								"msg" : result};

				request.post('http://localhost:5000/add_message', {form:{msg:post_data}});
			});
		}
		res.send('OK');
};

var pubchannel_update_interval = 1000;

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
								console.log('['  + name + '] Added priv data from ' + result);
							} else {
								console.log('['  + name + '] ERROR 3 ' + err);
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

						data_recived = 1;
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


var msg_index = 0;

// console.log('['  + name + '] SKIP ' + request_skip);
var confirmed_keys = new Array();


var read_pub_channel_loop = function() {
	// var t = pubchannel_update_interval*3;
	var t = Math.random() * 10000;

	//console.log('['  + name + '] DELAY ' + t);
	setTimeout(function() {
		read_msg_index();
		read_pub_channel();
		read_pub_channel_loop(); //Start again after t
	}, t);
};

read_pub_channel_loop();

var read_pub_channel = function() {
	request('http://localhost:5000/get_all_messages_after?msg_id=' + msg_index, function (error, response, data) {
		if (!error && response.statusCode == 200) {
			//Convert to JSON
			j_data = JSON.parse(data);
			
			//Update message index
			msg_index += j_data.length;
			// console.log('['  + name + '] msg index ' + msg_index);
			if(j_data.length > 0) {
				// console.log('['  + name + '] PUB CHANNEL ' + data);

				var incoming_request_count = 0;
				for(i in j_data) {
					//Ignore expired or closed messages
					if(j_data[i].status != 'expired' && j_data[i].status != 'closed') {

						var tmp_data = JSON.stringify(j_data[i].data);
						if(j_data[i].data.type == 'data_request') {

							//If this is not one of my requests
							if(my_pub_keys.indexOf(j_data[i].data.tmpPubKey) == -1) {

								if(Math.random() > 0.5) { // Answer 50% of the time
									peer.generateResponseStr(tmp_data, function(err, result) {
										if(!err) {
											if(result != null) {
												// console.log('['  + name + '] Sending response');
												var resp = JSON.parse(result);
												request.post('http://localhost:5000/add_message', {form:{msg:resp}});
											}
										} else {
											console.log('['  + name + '] ERROR 1 ' + err);
										}
									});

								}


								incoming_request_count++;

							}
							
						} else if(j_data[i].data.type == 'data_response') {
							//console.log('RESPONSE:' + tmp_data);
							var tmpPubKey = j_data[i].data.tmpPubKey;
							var tmpUser = j_data[i].data.user;


							//If I asked for this
							// console.log('['  + name + '] ' + confirmed_keys);

							if(my_pub_keys.indexOf(tmpPubKey) != -1 && confirmed_keys.indexOf(tmpPubKey) == -1) {
								if(data_recived == 0) {

									confirmed_keys[confirmed_keys.length] = tmpPubKey;
									console.log('['  + name + '] processing : ' + tmpPubKey + ' DATA : ' + data_recived);
									peer.processResponseStr(tmp_data, function(err, result) {
										if(!err && result != null) {
											var r = JSON.parse(result);
											result = r.plainText;
											// console.log('['  + name + ']' + result);
											if(data_recived == 0) {//If another iteraction of the loop processed a previous response
												if(result.indexOf("A") == 0) {
													data_recived = 1;
													//This replaces signature verification
													console.log('['  + name + '] NOT A LIE : ' + result);
													//Send confirmation
													console.log('['  + name + '] confirm : ' + r.tmpPubKey);
													var conf = {"type" : "data_request_confirmation", "tmpPubKey" : r.tmpPubKey}
													request.post('http://localhost:5000/add_message', {form:{msg:conf}});

													for(var x = 0; x < contacts.length; x++) {
														if(contacts[x].name == r.user) {
															contacts[x].msg_index = contacts[x].available;
														}
													}

												} else {
													console.log('['  + name + ']LIE RECEIVED!!!');
												}
											}
										} else {
											console.log('['  + name + '] ERROR 2 ' + err);
										}
									});
								} else {
									//console.log('['  + name + '] Skipped : ' + tmpPubKey + ' DATA : ' + data_recived);
								}
							}
						} else if(j_data[i].data.type == 're-key') {
							var tmp_rk_user = j_data[i].data.from;
							var tmp_msg = j_data[i].data.msg;
							if(name != tmp_rk_user) {
								peer.processReKeyInformation(tmp_rk_user, tmp_msg, function(err, result) {
									console.log('['  + name + '] Processed re-key from ' + tmp_rk_user + ' : ' + result)
								});
							}
							
						} else {
							//console.log('['  + name + '] UNKOWN MESSAGE' + data);
						}
					} else {
						//console.log('expired message ' + i);
					}
				}

			}
		}
	});
};



//Periodically check for updates of contacts 
//If there is a new message then ask for it
var read_msg_index = function() {
	for(var i = 0; i< contacts.length; i++) {
		// console.log('['  + name + '] checking status of ' + contacts[i].name);
		request('http://localhost:5000/get_message_index_of_peer?user=' + contacts[i].name, function (error, response, data) {
			if (!error && response.statusCode == 200) {
				data = JSON.parse(data);

				//Check for the index of the user
				for(var j = 0; j < contacts.length; j++) {

					if(contacts[j].name == data.user && contacts[j].msg_index < data.index) {
						contacts[j].available = data.index;
						// console.log('['  + name + '] generating request for ' + data.user + " : " + data.index + " : " + contacts[j].msg_index);
						//Make request for data
						peer.generateRequestStr(data.user, function(err, result) {
							if(!err) {
								var data_req = JSON.parse(result);
								console.log('['  + name + '] generating request for ' + data.user + " : "  + result);
								//Store the pub key
								my_pub_keys[my_pub_keys.length] = data_req.tmpPubKey;

								request.post('http://localhost:5000/add_message', {form:{msg:data_req}});
							} else {
								console.log('['  + name + '] ERROR 4 ' + err);	
							}
							
						});
					}
				}
			}
		});
	}

};