var http = require('http');
var formidable = require('formidable');
var fs = require('fs');


// Imports the Google Cloud client library
const vision = require('@google-cloud/vision');

// Creates a client
const client = new vision.ImageAnnotatorClient();



http.createServer(function (req, res) {
  if (req.url == '/vision') { // 2nd page
    var form = new formidable.IncomingForm();
    form.parse(req, async function (err, fields, files) {
      // Detect Labels
      const [results] = await client.labelDetection(files.filetoupload.path)
      const labels = results.labelAnnotations;

      res.write('Labels: \r\n');
      for (var i in labels) {
        res.write(labels[i].description + ' = ' + labels[i].score + '\r\n');
      }

      // Detect Landmarks

      const [resultsLand] = await client.landmarkDetection(files.filetoupload.path);
      const landmarks = resultsLand.landmarkAnnotations;
      res.write('\r\nLandmarks: \r\n');
     // landmarks.forEach(landmark => console.log(landmark));

      isLandmark = false;
      for (var i in landmarks) {
        res.write(JSON.stringify(landmarks[i].description) + '\r\n')
        isLandmark = true;
      }

      if (isLandmark === false) {
        res.write('No landmarks detected.')
      }

      // Detect Face Emotions
      const [result] = await client.faceDetection(files.filetoupload.path);
      const faces = result.faceAnnotations;
      res.write(`\r\n \r\nFace Detection: \r\n`);
      res.write(`Found ${faces.length} face${faces.length === 1 ? '' : 's'}. \r\n`);

      faces.forEach((face, i) => {
        res.write(`Expression: \r\n`);
        res.write(`Joy: ${face.joyLikelihood} \r\n`);
        res.write(`Anger: ${face.angerLikelihood} \r\n`);
        res.write(`Sorrow: ${face.sorrowLikelihood} \r\n`);
        res.write(`Surprise: ${face.surpriseLikelihood} \r\n`);
      });

      // Attempt at reading wiki
      res.write('\r\nWikipedia Summary:\r\n');

      const wiki = require('wikijs').default;

      // If there is a landmark give wiki summary of it. If not give default description.
      searchText = 'Batman';
      if(isLandmark) {
        tempText = JSON.stringify(landmarks[i].description);
        tempText = tempText.replace('_', ' ').replace(/["']/g, "");
        searchText = tempText;
      }

      wikiPage = wiki().page(searchText);
      await wikiPage.then(page => page.summary()).then(function (result) {
        //  console.log(result)
        res.write(result)
      });

      // Testing geo stuff
      // Seems useful for geo-caching app - can show places of interest around a location that have articles in wiki.

 //     wiki().geoSearch(37.431313849999995, -122.16936535498309).then(titles => console.log(titles.length));
 //     wiki().geoSearch(37.431313849999995, -122.16936535498309).then(titles => console.log(titles[0]));
 //     wiki().geoSearch(37.431313849999995, -122.16936535498309).then(function (result) {
//        console.log(result.length)
//        for (var i in result){
//          console.log(result[i]);
//        }
//      });
     // wiki().page('Gun Barrel City, Texas').then(page => page.summary()).then(console.log);




      res.end();
    });
  } else { // The first page
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.write('<form action="vision" method="post" enctype="multipart/form-data">');
    res.write('<input type="file" name="filetoupload"> <br>');
    res.write('<input type="submit">');
    res.write('</form>');
    return res.end();
  }
}).listen(8080);