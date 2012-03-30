var db = require('../db');

/*
 * GET home page.
 */
exports.index = function(req, res){
  res.render('index', { title: 'Express' });
};

/*
 * Handle incoming entry requests
 */
exports.add = function(req, res){
	db.addEntry('testing');  
	res.send('test');
};