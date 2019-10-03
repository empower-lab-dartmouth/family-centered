var http = require('http');
var formidable = require('formidable');
var fs = require('fs');

// Imports the Google Cloud client library
const vision = require('@google-cloud/vision');
  
// Creates a client
const client = new vision.ImageAnnotatorClient();

http.createServer(function (req, res) {
  if (req.url == '/fileupload') {
    var form = new formidable.IncomingForm();
    form.parse(req, function (err, fields, files) {
      // var oldpath = files.filetoupload.path;
      // var newpath = '/Users/gabesaldivar/Desktop/vision_test/NodePics/' + files.filetoupload.name;
      // fs.rename(oldpath, newpath, function (err) {
      //   if (err) throw err;
      //   res.write('File uploaded and moved! \r\n');
        res.write('Google Vision Api Start \r\n');

        client
        // .labelDetection('/Users/gabesaldivar/Desktop/vision_test/NodePics/pill.jpg')
         .labelDetection(files.filetoupload.path)

        .then(results => {
            const labels = results[0].labelAnnotations;
            console.log('Labels: \r\n');
            labels.forEach(label => console.log(label.description));
        })
        .catch(err => {
            console.error('Error:', err);
        })


        res.write('Google Vision Api END');
        res.end();
      });
//  });
  } else {
    res.writeHead(200, {'Content-Type': 'text/html'});
    res.write('<form action="fileupload" method="post" enctype="multipart/form-data">');
    res.write('<input type="file" name="filetoupload"><br>');
    res.write('<input type="submit">');
    res.write('</form>');
    return res.end();
  }
}).listen(8080);








