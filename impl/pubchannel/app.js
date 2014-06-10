
/**
 * Module dependencies.
 */

var express = require('express');
var routes = require('./routes');
var http = require('http');
var path = require('path');

var app = express();

// all environments
app.set('port', process.env.PORT || 5000);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.json());
app.use(express.urlencoded());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json());
app.use(express.urlencoded());

// development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}

app.get('/', routes.index);
app.get('/status', routes.status);
app.get('/all', routes.get_all_messages);
app.get('/add_message', routes.add_message);
app.post('/add_message', routes.add_message);
app.get('/get_all_messages_after', routes.get_all_messages_after);
app.get('/get_message_index_of_peer', routes.get_message_index_of_peer);
app.get('/set_message_index_of_peer', routes.set_message_index_of_peer);

app.post('/direct_message', routes.add_direct_message);
app.get('/get_direct_messages', routes.get_all_direct_messages_for);
app.get('/get_all_direct_messages', routes.get_all_direct_messages);


http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});
