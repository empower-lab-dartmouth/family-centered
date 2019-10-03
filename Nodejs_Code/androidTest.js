const http = require('http');
const wiki = require('wikijs').default;

var express = require('express');
var app = express();
var qs = require('querystring');
var fs = require('fs');


const hostname = '127.0.0.1';
const port = 3000;
var wikiSearch = "Batman";

app.get('/', (req, res) => {
    res.statusCode = 200;
    res.setHeader('Content-Type', 'text/plain');
    // Default text
    searchText = wikiSearch;
    // If there is a landmark give wiki summary of it. If not give default description.
    res.write('\r\nWikipedia Summary for ' + searchText + ':\r\n');
    wikiPage = wiki().page(searchText);
    getWikiText(res);
});


/* When a post request is called from the android code, goes to this method to 
retrieve the wikipedia article, which is then sent back to the android code.
*/
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
            wikiPage = wiki().page(wikiSearch);
            getWikiText(res);
        });
    }
});
/*
Currently is not working, this should be called when a post request with the name
"label_picture" is called, which should then retrieve a picture taken from the android device
and then return a picture with all objects in the picture being highlighted with boxes around 
each object.
*/
app.post('/label_picture', function (req, res) {
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
            editedPost = (JSON.stringify(post)).replace(/["'{}:]/g, "");
           // picturePath = (JSON.stringify(post)).replace(/["'{}:]/g, "");
           // changedPicPath = "./.." + picturePath;
           // console.log("picPath: " + picturePath);
           // console.log("changedPicPath: " + changedPicPath)
            getLabeledPicture(res, editedPost);
        });
    }
});

app.listen(port, hostname, () => {
    console.log(`Server running at http://${hostname}:${port}/`);
});

async function getWikiText(res) {
    await wikiPage.then(page => page.summary()).then(function (result) {
        //   console.log(result)
        res.write(result)
    });
    res.end();
}

async function getLabeledPicture(res, picturePath) {
    const request = {
        image: {content: fs.readFileSync(picturePath)},
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
      const image = await readFile();
      const Image = Canvas.Image;
      // Open the original image into a canvas
      const img = new Image();
      img.src = image;
      const canvas = new Canvas.Canvas(img.width, img.height);
      const context = canvas.getContext('2d');
      context.drawImage(img, 0, 0, img.width, img.height);
    
      // Now draw boxes around all the faces
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
      var newPath = '/Users/gabesaldivar/Desktop/vision_test/NodePics/' + files.filetoupload.name;
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
      res.write(newPath);
      console.log('Finished!');

    res.end();

}
