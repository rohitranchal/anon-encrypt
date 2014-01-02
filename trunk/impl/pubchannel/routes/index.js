var mysql      = require('mysql');
var connection = mysql.createConnection({
	host     : 'localhost',
	user     : 'root',
	password : '',
	database : 'pubchannel',
	multipleStatements: true
});
connection.connect();

exports.get_all_messages = function(req, res) {
	var sql = 'SELECT * FROM Message';
	connection.query(sql, function(err, rows, fields) {
		if (err) throw err;
		res.send(rows);
	});
}

exports.get_all_messages_after = function(req, res) {
	var msg_id = req.query.msg_id;
	var sql = 'SELECT * FROM Message WHERE id > ' + msg_id;
	connection.query(sql, function(err, rows, fields) {
		if (err) throw err;
		res.send(rows);
	});
}

exports.add_message = function(req, res) {
	var msg = req.query.msg;
	if(typeof msg == 'undefined') {
		msg = req.body.msg;
	}

	connection.query("INSERT INTO Message(data) VALUES ('" + msg + "')", function(err, rows, fields) {
		if (err) throw err;
		res.send('OK');
	});
}
