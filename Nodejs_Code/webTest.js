var http = require('http');
var formidable = require('formidable');
var fs = require('fs');


// Imports the Google Cloud client library
const vision = require('@google-cloud/vision');

// Creates a client
const client = new vision.ImageAnnotatorClient();

/*
This function uses the vision api to find all the objects in the picture, which will be used to highlight each object later.
*/
async function detectObjects(inputFile) {
  const request = {
    image: {content: fs.readFileSync(inputFile)},
  };
  
  const [resultMulti] = await client.objectLocalization(request);
  const objects = resultMulti.localizedObjectAnnotations;
  objects.forEach(object => {
    console.log(`Name: ${object.name}`);
    console.log(`Confidence: ${object.score}`);
    const vertices = object.boundingPoly.normalizedVertices;
    vertices.forEach(v => console.log(`x: ${v.x}, y:${v.y}`));
    
  });
  return objects;
}

http.createServer(function (req, res) {
  if (req.url == '/vision') { // If on the second page.
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
      res.write(`\r\nFace Detection: \r\n`);
      res.write(`Found ${faces.length} face${faces.length === 1 ? '' : 's'}. \r\n`);

      faces.forEach((face, i) => {
        res.write(`Expression: \r\n`);
        res.write(`Joy: ${face.joyLikelihood} \r\n`);
        res.write(`Anger: ${face.angerLikelihood} \r\n`);
        res.write(`Sorrow: ${face.sorrowLikelihood} \r\n`);
        res.write(`Surprise: ${face.surpriseLikelihood} \r\n`);
      });

      // Detect Multiple Objects
      const request = {
        image: {content: fs.readFileSync(files.filetoupload.path)},
      };
      
      const [resultMulti] = await client.objectLocalization(request);
      const objects = resultMulti.localizedObjectAnnotations;
      objects.forEach(object => {
        console.log(`Name: ${object.name}`);
        console.log(`Confidence: ${object.score}`);
        const vertices = object.boundingPoly.normalizedVertices;
        vertices.forEach(v => 
          console.log(`x: ${v.x}, y:${v.y}`));
      });

      // HIGHLIGHT OBJECTS
      const Canvas = require('canvas');
      console.log('Highlighting...');

      const {promisify} = require('util');
      const readFile = promisify(fs.readFile);
      const image = await readFile(files.filetoupload.path);
      const Image = Canvas.Image;
      // Open the original image into a canvas
      const img = new Image();
      img.src = image;
      const canvas = new Canvas.Canvas(img.width, img.height);
      const context = canvas.getContext('2d');
      context.drawImage(img, 0, 0, img.width, img.height);
    
      // Now draw boxes around all the objects
      context.strokeStyle = 'rgba(0,255,0,0.8)';
      context.lineWidth = '5';
    
      objects.forEach(object => {
        context.beginPath();
        let origX = 0;
        let origY = 0;
        vertices = object.boundingPoly.normalizedVertices;
        var counter = 0;
        vertices.forEach(v => {
          if (counter === 0) {
            origX = v.x * img.width;
            origY = v.y * img.height;
            context.moveTo(origX, origY)
            counter+=1;
          } else {
            context.lineTo(v.x * img.width, v.y * img.height);
          }
        });
        context.lineTo(origX, origY);
        context.stroke();
      });
      // This next line needs to be changed so that it works for every user.
      var newPath = '/Users/gabesaldivar/Desktop/Geocache_code/storytime/Nodejs_Code/NodePics/' + files.filetoupload.name;
      fs.rename(files.filetoupload.path, newPath, function (err) {
        if (err) throw err;
      });
    
      // Write the result to a file
      console.log(`Writing to file ${newPath}`);
      const writeStream = fs.createWriteStream(newPath);
      const pngStream = canvas.pngStream();
    
      await new Promise((resolve, reject) => {
        pngStream
          .on('data', chunk => writeStream.write(chunk))
          .on('error', reject)
          .on('end', resolve);
      });

      console.log('Finished!');







/*
      // Wiki query
      const wiki = require('wikijs').default;

      // Default text
      searchText = 'Batman';
      // If there is a landmark give wiki summary of it. If not give default description.

      if (isLandmark) {
        tempText = JSON.stringify(landmarks[i].description);
        tempText = tempText.replace('_', ' ').replace(/["']/g, "");
        searchText = tempText;
      }

      res.write('\r\nWikipedia Summary for ' + searchText + ':\r\n');

      wikiPage = wiki().page(searchText);



      await wikiPage.then(page => page.summary()).then(function (result) {
        //  console.log(result)
        res.write(result)
      });

      // Testing geo stuff
      // Seems useful for geo-caching app - can show places of interest around a location that have articles in wiki.
      // Can enter in a km distance you want to search around, defaults to 1000 km.

      res.write('Geo Search:\r\n');
      var lat = 37.431313849999995;
      var long = -122.16936535498309;
      //var lat = 40.7128;
      //var long = -74.0060;
      res.write('Latitude: ' + lat + ' Longitude: ' + long + '\r\n\n');

      await wiki().geoSearch(lat, long).then(function (result) {
        res.write('Locations nearby with page titles: \r\n')
        for (var i in result) {
          res.write(result[i] + '\r\n')
          //   wiki().page(result[i]).then(page => page.summary()).then(console.log);
        }
      });
      // wiki().geoSearch(37.431313849999995, -122.16936535498309).then(titles => console.log(titles.length));
      // wiki().geoSearch(37.431313849999995, -122.16936535498309).then(titles => console.log(titles[0]));
      // wiki().geoSearch(37.431313849999995, -122.16936535498309).then(function (result) {
      //    console.log(result.length)
      //    for (var i in result){
      //      console.log(result[i]);
      //    }
      //  });

      // categories

      await wikiPage.then(page => page.categories()).then(function (result) {
        //  console.log(result)
        res.write('\r\nCategories: \r\n')
        categoriesArray = result;
        for (var i in result) {
          res.write(result[i] + '\r\n')
        }
      });


      // await wikiPage.then(page => page.backlinks()).then(function (result) {
      //   //  console.log(result)
      //   res.write('\r\nBacklinks: \r\n')
      //   for (var i in result) {
      //     res.write(result[i] + '\r\n')
      //   }
      // });


      // await wikiPage.then(page => page.links()).then(function (result) {
      //     console.log(result)
      //   res.write('\r\nLinks: \r\n')
      //   for (var i in result) {
      //     res.write(result[i] + '\r\n')
      //   }
      // });

    //   //gets all categories as a category
    //   await wiki().allCategories().then(function (result) {
    //     console.log(result)
    //   res.write('\r\n allCategories: \r\n')
    //   for (var i in result) {
    //     res.write(result[i] + '\r\n')
    //   }
    // });

    await wiki().pagesInCategory(categoriesArray[0]).then(function (result) {
      console.log(result)
    res.write('\r\n pagesInCategory: \r\n')
    //  for (var i in result) {
    //    res.write(result[i] + '\r\n')
    //  }
  });
*/

        res.end();
      });
    } else { // The first page, where the user can upload a picture to go through the vision api.
        console.log(`Server running at http://localhost:8080`);  
        res.writeHead(200, { 'Content-Type': 'text/html' });
        res.write('<form action="vision" method="post" enctype="multipart/form-data">');
        res.write('<input type="file" name="filetoupload"> <br>');
        res.write('<input type="submit">');
        res.write('</form>');
        return res.end();
      }
}).listen(8080);
