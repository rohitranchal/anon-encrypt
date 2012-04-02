var db = require('../db');

/*
 * GET home page.
 */
exports.index = function(req, res){
	db.getAllEntries(result);
	function result(val) {
		
		//Replace content with base 64 decoded data
		for(var i = 0; i < val.length; i++) {
			console.log(val['Content']);
			var s = val['Content'];
			
			val['Content'] = new Buffer(s, 'base64').toString('ascii');
		}
		
		res.render('index', { title: 'Public Channel' , entries : val });
	}
};

/*
 * Handle incoming entry requests
 */
exports.add = function(req, res){
	db.addEntry(req.params.val);  
	res.send('success');
};