var mysql = require('mysql');
var TEST_DATABASE = 'nodejs_mysql_test';
var TEST_TABLE = 'test';
var client = mysql.createClient({
  user: 'root',
  password: 'ooJ5woos',
});

client.query('USE pubchannel');

/**
 * Add an entry into the Message table.
 */
exports.addEntry = function(content) {
	console.log('adding entry ' + content);
	client.query(
			  'INSERT INTO Message '+
			  'SET Content = ?',
			  [content], 
			  	function(err, info) {
				  if(err) {
					  throw err;
				  }
				});
	
};

/**
 * Call the given callback function with the set of all entries.
 */
exports.getAllEntries = function(cb) {
	client.query('SELECT * FROM Message' , function selectCb(err, results, fields){
		if(err) {
			throw err;
		}
		
		cb(results);
	});
	
};