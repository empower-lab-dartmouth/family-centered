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
      landmarks.forEach(landmark => console.log(landmark));

      areLandmarks = false;
      for (var i in landmarks) {
        res.write(JSON.stringify(landmarks[i].description) + '\r\n')
        areLandmarks = true;
      }

      if (areLandmarks === false) {
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
      res.write('\r\nWikipedia definition:');

      var wikipedia = require("node-wikipedia");
      wikipedia.page.data("Clifford_Brown", { content: true }, function (response) {
        console.log(response);
        console.log("response test...");
        //  console.log(response.description)
        //  console.log(response.content)
        //  console.log(response.data)
        //  console.log(response.parse)
        wikiText = response.text;
        //console.log(response.text);

        console.log('Printing out wikiText');
 
        for (var i in wikiText) {
          console.log(wikiText[i]);
        //  console.log(JSON.stringify(wikiText[i]));
        //  res.write(JSON.stringify(wikiText[i]));
        }

        });


      //   // Attempt at drawing
      //   console.log('START')

      //   const Canvas = require('canvas');

      //   const {promisify} = require('util');
      //   const readFile = promisify(fs.readFile);
      //   const image = await readFile(files.filetoupload.path);
      //   const Image = Canvas.Image;
      //   // Open the original image into a canvas
      //   console.log('END1')

      //   const img = new Image();
      //   img.src = image;
      //   const canvas = new Canvas.Canvas(img.width, img.height);
      //   const context = canvas.getContext('2d');
      //   context.drawImage(img, 0, 0, img.width, img.height);

      //   console.log('END2')

      //    // Now draw boxes around all the faces
      //   context.strokeStyle = 'rgba(0,255,0,0.8)';
      //   context.lineWidth = '5';

      //   // draws for faces
      //   // faces.forEach(face => {
      //   //     context.beginPath();
      //   //     let origX = 0;
      //   //     let origY = 0;
      //   //     face.boundingPoly.vertices.forEach((bounds, i) => {
      //   //        if (i === 0) {
      //   //             origX = bounds.x;
      //   //             origY = bounds.y;
      //   //        }
      //   //             context.lineTo(bounds.x, bounds.y);
      //   //      });
      //   //      context.lineTo(origX, origY);
      //   //      context.stroke();
      //   // });

      //   // draws for labels
      //   landmarks.forEach(landmark => {
      //     context.beginPath();
      //     let origX = 0;
      //     let origY = 0;
      //     landmark.boundingPoly.vertices.forEach((bounds, i) => {
      //        if (i === 0) {
      //             origX = bounds.x;
      //             origY = bounds.y;
      //        }
      //             context.lineTo(bounds.x, bounds.y);
      //      });
      //      context.lineTo(origX, origY);
      //      context.stroke();
      // });



      //   var newpath = '/Users/gabesaldivar/Desktop/vision_test/NodePics/' + files.filetoupload.name;
      //   fs.rename(files.filetoupload.path, newpath, function (err) {
      //     if (err) throw err;
      //   });

      //   console.log(`Writing to file ${newpath}`);
      //   const writeStream = fs.createWriteStream(newpath);
      //   const pngStream = canvas.pngStream();

      //   await new Promise((resolve, reject) => {
      //     pngStream
      //       .on('data', chunk => writeStream.write(chunk))
      //       .on('error', reject)
      //       .on('end', resolve);
      //   });

      //   console.log('END3')
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