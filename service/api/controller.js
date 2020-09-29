'use strict';
const fetch = require("node-fetch");
// require API_helper.js
const api_helper = require('./API_helper');

var properties = require('../package.json')

var controllers = {
   about: function(req, res) {
       var aboutInfo = {
           name: properties.name,
           version: properties.version
       }
       res.json(aboutInfo);
   },
   aboutGraphql: function(req, res) {
          var aboutInfo = {
              name: "Graphql",
              version: "1.0.0"
          }
          res.json(aboutInfo);
      },
   standardizeTitle: function(req, res) {
            console.log(req.body);
//            const customerId=request.body.customerId;
            const transformedTitle=req.body.customerTitle.toUpperCase();
             var process = {
                 id: req.body.processId,
                 name: transformedTitle
             }


             function fetchGraphQL(operationsDoc, operationName, variables) {
               return fetch(
                 "http://localhost:8080/graphql",
                 {
                   method: "POST",
                   headers: {
                     "Content-Type": "application/json"
                   },
                   body: JSON.stringify({
                     query: operationsDoc,
                     variables: variables,
                     operationName: operationName
                   })
                 }
               ).then((result) => result.json());
             }

             const operationsDoc = `
               mutation updateTitle($customerId: ID!, $previousCustomerTitle: String, $newCustomerTitle: String, $comments: String, $byState: String) {
                 updateCustomer(input: {filter: {id: [$customerId]}, set: {title: $newCustomerTitle, history: [{title: $previousCustomerTitle, byState: {name: $byState}, comments: $comments}]}}) {
                   numUids
                 }
               }
             `;

             function executeUpdateTitle(customerId, previousCustomerTitle, newCustomerTitle, comments, byState) {
               return fetchGraphQL(
                 operationsDoc,
                 "updateTitle",
                 {"customerId": customerId, "previousCustomerTitle": previousCustomerTitle, "newCustomerTitle": newCustomerTitle, "comments": comments, "byState": byState}
               );
             }
            console.log(req.body.customerId, req.body.customerTitle, transformedTitle, "updated as a part of standardization process", "standardizeTitle");
             executeUpdateTitle(req.body.customerId, req.body.customerTitle, transformedTitle, "updated as a part of standardization process", "standardizeTitle")
               .then(({ data, errors }) => {
                 if (errors) {
                   // handle those errors like a pro
                   console.error(errors);
                 }
                 // do something great with this precious data
                 console.log(data);
               })
               .catch((error) => {
                 // handle errors from fetch itself
                 console.error(error);
               });


            //update process
            {
            function fetchGraphQL(operationsDoc, operationName, variables) {
              return fetch(
                "http://localhost:8080/graphql",
                {
                  method: "POST",
                  headers: {
                    "Content-Type": "application/json"
                  },
                  body: JSON.stringify({
                    query: operationsDoc,
                    variables: variables,
                    operationName: operationName
                  })
                }
              ).then((result) => result.json());
            }

            const operationsDoc = `
              mutation updateProcess($processId: [ID!], $eventName: String) {
                updateProcess(input: {filter: {id: $processId}, set: {lastEventName: $eventName}}) {
                  numUids
                }
              }
            `;

            function executeUpdateProcess(processId, eventName) {
              return fetchGraphQL(
                operationsDoc,
                "updateProcess",
                {"processId": processId, "eventName": eventName}
              );
            }

            executeUpdateProcess([req.body.processId], "CustomerTitleStandardized")
              .then(({ data, errors }) => {
                if (errors) {
                  // handle those errors like a pro
                  console.error(errors);
                }
                // do something great with this precious data
                console.log(data);
              })
              .catch((error) => {
                // handle errors from fetch itself
                console.error(error);
              });
            }

             res.json(process);
         },
   samplePost: function(req, res) {
            console.log(req.body);

            function fetchGraphQL(operationsDoc, operationName, variables) {
              return fetch(
                "http://localhost:8080/graphql",
                {
                  method: "POST",
                  headers: {
                    "Content-Type": "application/json"
                  },
                  body: JSON.stringify({
                    query: operationsDoc,
                    variables: variables,
                    operationName: operationName
                  })
                }
              ).then((result) => result.json());
            }

            const operationsDoc = `
              query getCust {
                getCustomer(id: "0x3d") {
                  name
                }
              }
            `;

            function fetchGetCust() {
              return fetchGraphQL(
                operationsDoc,
                "getCust",
                {}
              );
            }

            fetchGetCust()
              .then(({ data, errors }) => {
                if (errors) {
                  // handle those errors like a pro
                  console.error(errors);
                }
                // do something great with this precious data
                console.log(data);
                res.json(data);
              })
              .catch((error) => {
                // handle errors from fetch itself
                console.error(error);
              });
         }

};

module.exports = controllers;