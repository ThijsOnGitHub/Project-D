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
// yLineChestFront : number
// yLineChestSide : number
// yLineWaistFront : number
// yLineWaistSide : number
// yLineHipFront : number
// yLineHipSide : number

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
    const yLineChestFront = req.yLineChestFront;
    const yLineChestSide = req.yLineChestSide;
    const yLineHipFront = req.yLineHipFront;
    const yLineHipSide = req.yLineHipSide;
    const yLineWaistFront = req.yLineWaistFront;
    const yLineWaistSide = req.yLineWaistSide;

    const imageInformation = {
        frontImage : frontImage,
        frontImageSegmentation : await jsGetSegmentation(frontImage),
        sideImage: sideImage,
        sideImageSegmentation : await jsGetSegmentation(sideImage),
        scale : scale
    };

    const chestSize = await jsMeasureChest(imageInformation, yLineChestFront, yLineChestSide);
    const hipSize = await jsMeasureHip(imageInformation, yLineHipFront, yLineHipSide);
    const waistSize = await jsMeasureWaist(imageInformation, yLineWaistFront, yLineWaistSide);

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

async function jsMeasureChest(imageInformation, yLineChestFront, yLineChestSide){
    const chestSizeFront = await jsCalculateLineLength(yLineChestFront, imageInformation.frontImageSegmentation);
    const chestSizeSide = await jsCalculateLineLength(yLineChestSide, imageInformation.sideImageSegmentation);

    return await jsCalculatePerimeter(chestSizeFront, chestSizeSide, imageInformation.scale);
}

//#endregion

//#region Hip

async function jsMeasureHip(imageInformation, yLineHipFront, yLineHipSide){
    const hipSizeFront = await jsCalculateLineLength(yLineHipFront, imageInformation.frontImageSegmentation);
    const hipSizeSide = await jsCalculateLineLength(yLineHipSide, imageInformation.sideImageSegmentation);

    return await jsCalculatePerimeter(hipSizeFront, hipSizeSide, imageInformation.scale);
}

//#endregion

//#region Waist

async function jsMeasureWaist(imageInformation, yLineWaistFront, yLineWaistSide){
    const waistSizeFront = await jsCalculateLineLength(yLineWaistFront, imageInformation.frontImageSegmentation);
    const waistSizeSide = await jsCalculateLineLength(yLineWaistSide, imageInformation.sideImageSegmentation);

    return await jsCalculatePerimeter(waistSizeFront, waistSizeSide, imageInformation.scale);
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

async function jsCalculatePerimeter(width, depth, scale){
    var a = width / 2;
    var b = depth / 2;

    //p≈ π(3(a+b)- √((3a+b)(a+3b)))
    var perimeter = Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (a + 3 * b)));
    
    return  perimeter * scale;
}