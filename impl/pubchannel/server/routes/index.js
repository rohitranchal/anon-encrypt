var db = require('../db');

/*
 * GET home page.
 */
exports.index = function(req, res){
	db.getAllEntries(result);
	function result(val) {
		res.render('index', { title: 'Public Channel' , entries : val });
		return;
	}
};

/*
 * Handle incoming entry requests
 */
exports.add = function(req, res){
	db.addEntry(req.params.val);  
	res.send('success');
};