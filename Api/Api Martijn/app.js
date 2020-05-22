const express = require("express");
const fileUpload = require("express-fileupload");
const { createCanvas } = require('canvas');
Canvas = require('canvas');

const tensorFlow = require("@tensorflow/tfjs-node");
const bodyPix = require("@tensorflow-models/body-pix");

// to run:
// 'node app.js' in terminal
// parameters:
// frontImage : file
// sideImage : file
// scale : number
// yLineChest : number
// yLineWaist : number
// yLineHip : number

var app = express();
app.use(fileUpload());

app.listen(3000, () => {
    console.log("Server running on port 3000, http://localhost:3000");
});

app.get("/", (req, res, next) => {
    res.send("No action selected");
});

app.post("/measure", async(req, res, next) => {
    const frontImage = await jsConvertImageToImageElement(req.files.frontImage);
    const sideImage = await jsConvertImageToImageElement(req.files.sideImage);
    const scale = req.scale;
    const yLineChest = req.yLineChest;
    const yLineWaist = req.yLineWaist;
    const yLineHip = req.yLineHip;

    const imageInformation = {
        frontImage : frontImage,
        frontImageSegmentation : await jsGetSegmentation(frontImage),
        sideImage: sideImage,
        sideImageSegmentation : await jsGetSegmentation(sideImage),
        scale : scale
    };

    const chestSize = await jsMeasureChest(imageInformation, yLineChest);
    const waistSize = await jsMeasureWaist(imageInformation, yLineWaist);
    const hipSize = await jsMeasureHip(imageInformation, yLineHip);

    res.json({ 
        chestSize : chestSize,
        hipSize : hipSize,
        waistSize : waistSize
    });
});

async function jsGetSegmentation(image){
    var net = await bodyPix.load({
        architecture: 'ResNet50',
        outputStride: 16
    });

    return await net.segmentPersonParts(image);
}

async function jsConvertImageToImageElement(image){
    var img = new Canvas.Image;
    img.src = image.data;

    var canvas = createCanvas(img.width, img.height);
    var context = canvas.getContext("2d");
    context.drawImage(img, 0, 0);

    return tensorFlow.browser.fromPixels(canvas);
}

//#region Chest

async function jsMeasureChest(imageInformation, yLineChest){
    const chestFrontSize = await jsCalculateLineLength(yLineChest, imageInformation.frontImageSegmentation);
    const chestSideSize = await jsCalculateLineLength(yLineChest, imageInformation.sideImageSegmentation);

    return jsCalculatePerimeter(chestFrontSize, chestSideSize);
}

//#endregion

//#region Hip

async function jsMeasureHip(imageInformation, yLineHip){
    const hipFrontSize = await jsCalculateLineLength(yLineHip, imageInformation.frontImageSegmentation);
    const hipSideSize = await jsCalculateLineLength(yLineHip, imageInformation.sideImageSegmentation);

    return jsCalculatePerimeter(hipFrontSize, hipSideSize);
}

//#endregion

//#region Waist

async function jsMeasureWaist(imageInformation, yLineWaist){
    const waistFrontSize = await jsCalculateLineLength(yLineWaist, imageInformation.frontImageSegmentation);
    const waistSideSize = await jsCalculateLineLength(yLineWaist, imageInformation.sideImageSegmentation);

    return jsCalculatePerimeter(waistFrontSize, waistSideSize);
}

//#endregion

async function jsCalculateLineLength(yLine, segmentation){
    var startPoint = segmentation.width * yLine;
    var endPoint = startPoint + segmentation.width;
    var outlinePixelNumber = [];

    // Here the code cycles througt the point and detects a transition between parts
    for(var i = startPoint; i < endPoint; i++){
        var currentPixel = segmentation.data[i];
        var nextPixel = segmentation.data[i+1];

        // It checks if there is a transition between the body and the rest
        if((currentPixel != -1 && nextPixel == -1) || (nextPixel != -1 && currentPixel == -1)){
            // It adds the point to the collection of points
            outlinePixelNumber.push(i);
        }
    }

    outlinePixelNumber.sort((a,b) => b - a);

    return outlinePixelNumber[0] - outlinePixelNumber[outlinePixelNumber.length-1];
}

async function jsCalculatePerimeter(width, depth){
    var a = width / 2;
    var b = depth / 2;

    //p≈ π(3(a+b)- √((3a+b)(a+3b)))
    return Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (a + 3 * b)))
}