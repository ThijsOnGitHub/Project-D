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
// scaleFront : number (cm)
// scaleSide : number (cm)
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
    const scaleFront = req.body.scaleFront;
    const scaleSide = req.body.scaleSide;
    const yLineChestFront = req.body.yLineChestFront;
    const yLineChestSide = req.body.yLineChestSide;
    const yLineHipFront = req.body.yLineHipFront;
    const yLineHipSide = req.body.yLineHipSide;
    const yLineWaistFront = req.body.yLineWaistFront;
    const yLineWaistSide = req.body.yLineWaistSide;

    const imageInformation = {
        frontImage : frontImage,
        frontImageSegmentation : await jsGetSegmentation(frontImage),
        sideImage: sideImage,
        sideImageSegmentation : await jsGetSegmentation(sideImage),
        scaleFront : scaleFront,
        scaleSide : scaleSide,
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
    const chestSizeFront = await jsCalculateLineLength(yLineChestFront, imageInformation.frontImageSegmentation, imageInformation.scaleFront);
    const chestSizeSide = await jsCalculateLineLength(yLineChestSide, imageInformation.sideImageSegmentation, imageInformation.scaleSide);

    return await jsCalculatePerimeter(chestSizeFront, chestSizeSide);
}

//#endregion

//#region Hip

async function jsMeasureHip(imageInformation, yLineHipFront, yLineHipSide){
    const hipSizeFront = await jsCalculateLineLength(yLineHipFront, imageInformation.frontImageSegmentation, imageInformation.scaleFront);
    const hipSizeSide = await jsCalculateLineLength(yLineHipSide, imageInformation.sideImageSegmentation, imageInformation.scaleSide);

    return await jsCalculatePerimeter(hipSizeFront, hipSizeSide);
}

//#endregion

//#region Waist

async function jsMeasureWaist(imageInformation, yLineWaistFront, yLineWaistSide){
    const waistSizeFront = await jsCalculateLineLength(yLineWaistFront, imageInformation.frontImageSegmentation, imageInformation.scaleFront);
    const waistSizeSide = await jsCalculateLineLength(yLineWaistSide, imageInformation.sideImageSegmentation, imageInformation.scaleSide);

    return await jsCalculatePerimeter(waistSizeFront, waistSizeSide);
}

//#endregion

async function jsCalculateLineLength(yLine, segmentation, scale){
    var startPoint = segmentation.width * yLine;
    var endPoint = startPoint + segmentation.width;
    var outlinePixelNumber = [];
    
    // All parts excluding the head, arms and hand
    var measureParts = [2,3,4,5,6,7,8,9,10,11,12,13];

    // Here the code cycles througt the point and detects a transition between parts
    for(var i = startPoint; i < endPoint; i++){
        var currentPixel = segmentation.data[i];
        var nextPixel = segmentation.data[i+1];

        //It checks if there is a transition between measureParts a other parts
        if((measureParts.includes(currentPixel) && !measureParts.includes(nextPixel)) || (measureParts.includes(nextPixel) && !measureParts.includes(currentPixel))){
            //It adds the point to the collection of points
            outlinePixelNumber.push(i);
        }
    }

    outlinePixelNumber.sort((a,b) => b - a);

    var lengthInPixels = outlinePixelNumber[0] - outlinePixelNumber[outlinePixelNumber.length-1];

    return lengthInPixels * scale;
}

async function jsCalculatePerimeter(width, depth){
    var a = width / 2;
    var b = depth / 2;

    //p≈ π(3(a+b)- √((3a+b)(a+3b)))
    return Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (a + 3 * b)));
}