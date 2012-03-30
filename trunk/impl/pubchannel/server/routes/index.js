var db = require('../db');

/*
 * GET home page.
 */
exports.index = function(req, res){
	db.getAllEntries(result);
	function result(val) {
		console.log(val);
		res.render('index', { title: 'Public Channel' , entries : val });
	}
};

/*
 * Handle incoming entry requests
 */
exports.add = function(req, res){
	db.addEntry(req.params.val);  
	res.send('test');
};