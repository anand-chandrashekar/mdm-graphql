const express = require('express')
//const bodyParser = require('body-parser');
const cors = require('cors');
const port = process.env.PORT || 3000;

const app = express();
const routes = require('./api/routes');
routes(app);
app.use(cors());


// Configuring body parser middleware
//app.use(bodyParser.urlencoded({ extended: false }));
//app.use(bodyParser.json());

app.listen(port, function() {
   console.log('Server started on port: ' + port);
});