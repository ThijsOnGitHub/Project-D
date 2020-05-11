const express = require("express");
const fileUpload = require("express-fileupload");
const tensorFlow = require("@tensorflow/tfjs-node");
const bodyPix = require("@tensorflow-models/body-pix");
const { createCanvas } = require('canvas')
Canvas = require('canvas')

var app = express();
app.use(fileUpload());

app.get("/", (req, res, next) => {
    res.send("No action selected");
});

app.post("/measure", async(req, res, next) => {
    const image = req.files.image;

    const img = new Canvas.Image;
    img.src = image.data;

    const canvas = createCanvas(img.width, img.height);
    const context = canvas.getContext("2d");
    context.drawImage(img, 0, 0);
    const input = tensorFlow.browser.fromPixels(canvas);
    
    const net = await bodyPix.load({
        architecture: 'ResNet50',
        outputStride: 16
    });

    const segmentation = await net.segmentPersonParts(input);

    res.json(segmentation);
});

app.listen(3000, () => {
    console.log("Server running on port 3000, http://localhost:3000");
});