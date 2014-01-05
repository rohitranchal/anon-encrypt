var java = require("java");
var fs = require("fs");
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

var configData = fs.readFileSync('./config.json').toString();
var config = JSON.parse(configData);

var Peer = java.import('org.ruchith.ae.peer.Peer');
var peer = new Peer(config.name);

var process_action = function(val) {
	if(typeof val.parameters != 'undefined') {
		console.log(val);
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

exports.index = function(req, res){
  res.render('index', { title: 'Express' });
};
