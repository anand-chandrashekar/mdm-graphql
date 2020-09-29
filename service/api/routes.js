'use strict';
const express = require('express');
const cors = require('cors');
const controller = require('./controller');

module.exports = function(app) {
   app.use(express.json());
//   app.use(cors());

//   app.route('/about')
//       .get(controller.aboutGraphql);

   app.route('/samplePost')
       .post(controller.samplePost);

    app.route('/standardizeTitle')
           .post(controller.standardizeTitle);
//   app.post('/standardizeTitle', function(req, res) {
//       console.log('receiving data ...');
//       console.log(JSON.stringify(req.headers));
//       console.log('body is ',req.body);
//       res.send({"name":"a response value"});
//   });

};