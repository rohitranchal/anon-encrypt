var db = require('../db');

/**
 * GET home page.
 */
exports.index = function(req, res){
	db.getAllEntries(result);
	function result(val) {
		
		//Replace content with base 64 decoded data
		for(var i = 0; i < val.length; i++) {
			var s = val[i].Content;
			val[i].Content = new Buffer(s, 'base64').toString('ascii');
		}
		
		res.render('index', { title: 'Public Channel' , entries : val });
	}
};

/**
 * Handle incoming entry requests
 */
exports.add = function(req, res){
	db.addEntry(req.params.val);  
	res.send('success');
};

/**
 * Return all entries starting from the given index value.
 */
exports.pull = function(req, res) {
	
	db.getEntriesFromIndex(req.params.val, function(val) {
		res.send(val);//Output JSON
	});
	
};

exports.entry = function(req, res) {
	db.getEntry(req.params.id, function(val){
		res.send(val);
	});
};