
/**
 * Module dependencies.
 */

var express = require('express');
var routes = require('./routes');
var http = require('http');
var path = require('path');
var request = require('request');

var app = express();

// all environments
var port = process.argv[2];
app.set('port', process.env.PORT || port);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.json());
app.use(express.urlencoded());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));

// development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}

app.get('/', routes.index);
app.get('/contacts', routes.contacts);


app.get('/start', routes.start);

var msg_index = 0;
var pubchannel_update_interval = 5000;
//Thread to read the public channel
setInterval(function() {
	request('http://localhost:5000/get_all_messages_after?msg_id=' + msg_index, function (error, response, data) {
		if (!error && response.statusCode == 200) {
			//Convert to JSON
			j_data = JSON.parse(data);
			
			//Update message index
			msg_index += j_data.length;
			routes.process_pub_data(j_data);
		}
	});
}, pubchannel_update_interval);



http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});
