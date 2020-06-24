const https = require('https');
const wiki = require('wikijs').default;
const fetch_sparql_endpoint = require('fetch-sparql-endpoint');

var express = require('express');
var app = express();
var qs = require('querystring');
var fs = require('fs');
var async = require('async');

const hostname = '127.0.0.1';
const port = 2000;

var url1 = 'https://en.wikipedia.org/w/api.php?action=query&prop=pageprops&ppprop=wikibase_item&format=json&redirects=1&titles=';

//next steps = 1) a vs an functionality 2) increase query types!

/*general structure
Query A --> converts a name into a wikidata qcode (item A)
Query B --> gets certain relationships/properties of item A
  (getting superclass = implemented, but this is easy to change)
Query C --> converts those properties (received in qcode form) to name and/or description
*/

//Currently, this is set-up to 1) get the superclasses of an object and 2) identify the type of object
  //increased functionality is super easy to add! Just command+F ***

//Also currently stores the results of Query A and Query B in files, which are later written over

var module_vars = {}
module_vars.num_descrips = 0;

//two flags, so when queries complete => flag changes => next event rigerred
module_vars.flag = {
  aInternal: false,
  aListener: function(val) {},
  set a(val) {
    this.aInternal = val;
    this.aListener(val);
  },
  get a() {
    return this.aInternal;
  },
  registerListener: function(listener) {
    this.aListener = listener;
  }
}

module_vars.flag2 = {
  aInternal: 0,
  aListener: function(val) {},
  set a(val) {
    this.aInternal = val;
    this.aListener(val);
  },
  get a() {
    return this.aInternal;
  },
  registerListener: function(listener) {
    this.aListener = listener;
  }
}

//Thanks @Gabe Saldivar

app.get('/', (req, res) => {
  //get was not necessary for the current interaction flow
});


app.listen(port, hostname, () => {
	console.log(`Server running at http://${hostname}:${port}/`);
});


app.post('/search_wiki', function (req, res) { 
    if (req.method == 'POST') {
        var body = '';
        req.on('data', function (data) {
            body += data;
            // 1e6 === 1 * Math.pow(10, 6) === 1 * 1000000 ~~~ 1MB
            if (body.length > 1e6) { 
                // FLOOD ATTACK OR FAULTY CLIENT, NUKE REQUEST
                req.connection.destroy();
            }
        });

        req.on('end', function () {
            var post = qs.parse(body);
            console.log("Received variable : " + JSON.stringify(post));
            wikiSearch = (JSON.stringify(post)).replace(/["'{}:]/g, "");
            console.log("wikiSearch : " + wikiSearch);

            //listens for when "flag" is changed, 
            //indicates that all previous processes have completed
            module_vars.flag.registerListener(function(val) {
                console.log("Flag is on!!");
                console.log("The number of generated files is " + module_vars.num_descrips);

                let rawdata = fs.readFileSync('description.json');
                var wikiDes = JSON.parse(rawdata);
                var base = "superclass"; //information type. Switch out word "superclass" to...
              
                var i = module_vars.num_descrips;

                //constructs sentences from labels
                var superclasses = extractLabels(base, "itemLabel", i);
                var descriptions = extractLabels(base, "itemDescription", i);
                //*** you could switch out the phrases " is a " and " is described as " for something else
                var sentences1 = constructSentences(wikiSearch, " is a ", superclasses); 
                var sentences2 = constructSentences(wikiSearch, " is described as: ", descriptions);
              
                writeOutput(res, sentences1, sentences2);
                res.end(); //make sure this is after all res.write() statements
            });

            //gets the wikidata Qcode for the given searchtext (from the android app)
            //first function to be called
            getQCode(wikiSearch).then(response => {
              var q_id = extractQCode(response);
              getInfo(wikiSearch, q_id);
            }).catch(error => {
              //error handling
            });

        console.log("num_descrips is (this is the incorrect value) " + module_vars.num_descrips); //for testing purposes, feel free to delete
      
      });
    }
})


/*
QUERY A
*/

//Query A
//returns the query result wrapped within a promise
  //when res.on('end') resolves, the promise resolves, and the program flow continues
function getQCode(wikiSearch){
  //thanks stack and 31piy: https://stackoverflow.com/questions/47986527/use-result-of-https-get-request-node-js
  var url2 = url1+wikiSearch

  return new Promise((resolve, reject) => {
      https.get(url2, (res) => {
        var { statusCode } = res;
        var contentType = res.headers['content-type'];

        let error;

        if (statusCode !== 200) {
          error = new Error('Request Failed.\n' +
            `Status Code: ${statusCode}`);
        } else if (!/^application\/json/.test(contentType)) {
          error = new Error('Invalid content-type.\n' +
            `Expected application/json but received ${contentType}`);
        }

        if (error) {
          console.error(error.message);
          // consume response data to free up memory
          res.resume();
        }

        res.setEncoding('utf8');
        let rawData = '';

        res.on('data', (chunk) => {
          rawData += chunk; //read input result
        });

        res.on('end', () => {
          try {
            const parsedData = JSON.parse(rawData);
            resolve(parsedData);
          } catch (e) {
            reject(e.message);
          }
        });
      }).on('error', (e) => {
        reject(`Got error: ${e.message}`);
      });

    });
}

//given page data (the json output returned by getQCode), extract the q code from the other data
function extractQCode(data){
  var page_info_dict = data['query']['pages'];
  for (k in page_info_dict){
    //if multiple results, return 1st
    page_info = page_info_dict[k];
    break;
  }
  var q_id = page_info['pageprops']['wikibase_item']; //q_code id, from wikidata 
  return q_id;
}


/*
QUERY B
*/

//Query B
//makes a query, in this case to get the superclasses of an object
function getInfo(wikiSearch, q_id){
  var url3 = 'https://query.wikidata.org/sparql';
  //*** could a mode 2 to querybank, and add a different query to querybank
  superclasses_query = querybank(q_id, 1);

  console.log('url3: ' + url3);
  //console.log('query: ' + superclasses_query);

  makeQuery(url3, superclasses_query, "description.json");
}

//mode == 0 --> Query B
//mode == 1 --> Query C
async function makeQuery(url, query, fname, mode=0){ 

  const myFetcher = new fetch_sparql_endpoint.SparqlEndpointFetcher();
  var bindingsStream = await myFetcher.fetchBindings(url, query), wikiData = '{"items":[';
  bindingsStream.on('data',function(data){
    wikiData += JSON.stringify(data)+',\n';
  });

  var local_vars = {};
  local_vars.words = '';

  bindingsStream.on('end', function(){
    words = wikiData.substring(0,wikiData.length-2)+']}';
    fs.writeFileSync(fname, wikiData.substring(0,wikiData.length-2)+']}', function(err) {
      if(err) {
        console.log(err);
      }
      console.log("The file was saved!");
    });
    local_vars.words = fs.readFileSync(fname);

    if (mode == 0){
      getInfoInner();
    }

    if (mode == 1){
      module_vars.flag2.a += 1;
    } 
  });
}

//called when query B completes 
function getInfoInner(){
  console.log("This is what description.json is: " + fs.readFileSync('description.json'));
  let rawdata = fs.readFileSync('description.json');
  var wikiDes = JSON.parse(rawdata);

  //*** superclass1, superclass2, superclass3, etc are the files in which the results of Query B are stored
  //*** changing this file name has no impact on the function, but might make for easier record keeping
  var base = "superclass"; 

  convertFormat(wikiDes, base).then(response => {
    module_vars.num_descrips = response;
  }).catch(error => {
    //error handling
  });
}


/*
QUERY C
*/

//convert a list of qcodes into a list of names
async function convertFormat(wikiDes, base){
  var q_ids2 = extractQCode2(wikiDes);
  var url3 = 'https://query.wikidata.org/sparql';

  var i = 1;
  var queries = [] //tuple of query and file number
  q_ids2.forEach(function(value) {
    var conversion_query = querybank(value, 0);
    queries.push([conversion_query,i]);
    i += 1;
  })
  console.log("cap is equal to: " + i);

  var j = 0;
  //flag2 changes every time a query completes
  //every time flag2 changes, the next query is called
  module_vars.flag2.registerListener(function(val) { 
    j += 1;
    if (j >= i){
      onFulfilled("Fulfilled!");
      return;
    }
    console.log("Flag2 changed.")
    //Query C
    //makeQuery is recursively called, via this event listener
    makeQuery(url3, queries[j-1][0], base+queries[j-1][1]+".json", 1);
  });

  module_vars.flag2.a = 1;

  //async.parallel(queries.forEach(value => makeQuery(url3, value[0], base+value[1]+".json", 1),onFulfilled("PARALLELLLLLL!"));
  return i-1;
}

//given relationship data, extract q codes
function extractQCode2(wikiDes){
  var text = JSON.stringify(wikiDes);

  var re = /Q\d+/; //regular expression for qcodes
  var rslt = re.exec(text);
  var q_ids2 = new Set();

  while (rslt !== null){
    q_ids2.add(rslt[0].toString());
    text = text.substring(rslt.index+rslt[0].length);
    rslt = re.exec(text);
  }
  return q_ids2;
}

//called when convertFormat() completes
function onFulfilled(text, val){
  console.log(text);
  module_vars.flag.a = true;
}


/* 
QUERY DATA
*/

//mode == 0 --> get name of object (given obj_id)
//mode == 1 --> get superclasses of object (given obj_id)
//*** add different queries, for example time of invention/appearance, as needed! 
function querybank(obj_id, mode){
  conversion_query = `SELECT ?item ?itemLabel ?itemDescription
  WHERE {
  VALUES ?item {
          wd:` + obj_id + `
        }
  SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
  }`

  superclasses_query = `SELECT ?instanceOf ?subclassOf ?subjRole ?objRole ?itemDescription
  WHERE {
      VALUES ?item {
          wd:` + obj_id + `
        }
      OPTIONAL {wd:`+obj_id+ ` wdt:P31 ?instanceOf}
      OPTIONAL {wd:`+obj_id +` wdt:P279 ?subclassOf}
      OPTIONAL {wd:`+obj_id+ ` wdt:P2868 ?subjRole}
      OPTIONAL {wd:`+obj_id+` wdt:P3831 ?objRole}
    SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
  }`

  if (mode == 0){
    return conversion_query;
  }
  return superclasses_query;
}


/* 
CONSTRUCT OUTPUT
*/

//triggered by module_vars.flag changing
//given query result, extract item name
function extractLabels(base, labelname, i){
  var items = [];
  for (let j = 1; j <= i; j++){
    let rawdata = fs.readFileSync(base+j+'.json');
    var des = JSON.parse(rawdata);
    if (des["items"][0][labelname] == undefined){
      continue;
    }
    var label = des["items"][0][labelname]["value"];
    items.push(label);
  }
  //console.log('items: ' + items);
  return items;
}

//given stored syntactic relationships, construct sentences
//could be modified in the future, for example to add a vs an functionality!
function constructSentences(wikiSearch, phrase, items){
  var sentences = [];
  for (i in items){
    x = items[i];
    sentences.push(wikiSearch + phrase + x);
  }
  return sentences;
}


/*
WRITE OUTPUT
*/

function writeOutput(res, sentences1, sentences2){
  var sentences = sentences1.concat(sentences2)
  for (i in sentences){
    s = sentences[i];
    res.write(s+'\n');
    console.log(s);
  }
  //Side note: a potential additional check for "organism" = if the wikidata object has an encyclopaedia brittanica id 
  var machineWords = ["machine", "invention", "vehicle", "appliance", "tech"];
  var organismWords = ["animal", "organism", "marine", "domesticated", "creature", "pet", "plant", "tree", "flower", "fungus"];
  var placeWords = ["country", "city", "state", "territory", "province", "town", "location", "place", "region"];

  var dict = {'machine':machineWords, 'organism':organismWords, 'place':placeWords};

  for (var k in dict){
    if (isCategory(sentences1, dict[k], sentences2)){
      res.write("This is a " + k + "!");
      console.log("This is a " + k + "!");
  }}             
}

function isCategory(sentences1, keywords, sentences2){
  //if any sentences (descriptions) of an object contain >=1 keywords,
  //the object is of type ____
  let rawdata = fs.readFileSync('description.json');
  var des = JSON.parse(rawdata);
  des = JSON.stringify(des);
  var isType = false; 

  var toCheck = [des]; //strings of text to be checked for keywords
  toCheck = toCheck.concat(sentences1, sentences2);
  for (let i = 0; i < toCheck.length; i++){ //prepare sentences
    toCheck[i] = toCheck[i].toLowerCase();
  }
  //checks each sentence for each keyword
  //this loop should never be more than 10 by 10
  for (let i = 0; i < keywords.length; i++){
    for (let j = 0; j < toCheck.length; j++){
      if (toCheck[j].indexOf(keywords[i]) !== -1){
        isType = true;
        break;
    }}
  }
  return isType;
}
