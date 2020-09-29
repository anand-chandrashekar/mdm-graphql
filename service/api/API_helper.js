const request = require('request')
const fetch = require("node-fetch");

module.exports = {
    /*
    ** This method returns a promise
    ** which gets resolved or rejected based
    ** on the result from the API
    */
    make_API_call : function(url){
        const query='query getCust{   getCustomer(id: "0x3d"){       name      } }';
        const queryBody = "{\"query\":\"query{   getCustomer(id: \"0x3d\"){       name      } }\" }";
        return new Promise((resolve, reject) => {
            console.log(url);
            console.log(query);
            fetch(url, {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
              },
              body: JSON.stringify({ query
              })
            })
              .then(r  => {
                           console.log(r.status , r.json())
                           r.json();
                               })
              .then(data => {
                console.log('data returned:', data);
                resolve(data);
              })


//            request.post(url, { body: queryString }, (err, res, body) => {
//              console.log("here " + res);
//              if (err) reject(err)
//              resolve(body)
//            });

        })

    }
}