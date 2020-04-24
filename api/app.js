var express = require("express");
var fileUpload = require("express-fileupload");
var tensorFlow = require("@tensorflow/tfjs-node");
var bodyPix = require("@tensorflow-models/body-pix");

var app = express();
app.use(fileUpload());

app.get("/", (req, res, next) => {
    res.send("No action selected");
});

app.post("/measure", async(req, res, next) => {
    var image = req.files.image;
console.log(image);

    const net = await bodyPix.load({
        architecture: 'ResNet50',
        outputStride: 16
    });

    const segmentation = await net.segmentPersonParts(image);

console.log(segmentation);

    res.json(["test"]);
});

app.listen(3000, () => {
    console.log("Server running on port 3000, http://localhost:3000");
});

// const loadAndPredict = async()=> {
//     const net = await bodyPix.load({
//         architecture: 'ResNet50',
//         outputStride: 16
//     });

//     const segmentation = await net.segmentPersonParts(img);
// }