const express = require("express");
const fileUpload = require("express-fileupload");
const { createCanvas } = require('canvas');
Canvas = require('canvas')

const tensorFlow = require("@tensorflow/tfjs-node");
const bodyPix = require("@tensorflow-models/body-pix");

// to run:
// 'node app.js' in terminal
// parameters:
// frontImage : file
// sideImage : file

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

    const frontImageSegmentation = await jsGetSegmentation(frontImage);
    const sideImageSegmentation = await jsGetSegmentation(sideImage);

    const imageInformation = {
        frontImage : frontImage,
        frontImageSegmentation : frontImageSegmentation,
        sideImage: sideImage,
        sideImageSegmentation : sideImageSegmentation
    };

    const chestSize = await jsMeasureChest(imageInformation);
    const hipSize = await jsMeasureHip(imageInformation);
    const waistSize = await jsMeasureWaist(imageInformation);

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

async function jsMeasureChest(imageInformation){
    //const chestYLine = jsGetChestYLine(imageInformation.frontImage);
    //const chestFrontSize = jsMeasureChestFront(chestYLine, imageInformation.frontImageSegmentation);
    //const chestSideSize = jsMeasureChestSide(chestYLine, imageInformation.sideImageSegmentation);

    //return jsCalculatePerimeter(chestFrontSize, chestSideSize);
    return 0;
}

async function jsMeasureChestFront(yLine, segmentation){

    var measureParts = []

    return await jsCalculateLineLength(yLine, segmentation, measureParts);
}

async function jsMeasureChestSide(yLine, segmentation){

    var measureParts = []

    return await jsCalculateLineLength(yLine, segmentation, measureParts);
}

async function jsGetChestYLine(image){

}

//#endregion

//#region Hip

async function jsMeasureHip(imageInformation){
    const hipYLine = await jsGetHipYLine(imageInformation.frontImage);
    const hipFrontSize = await jsMeasureHipFront(hipYLine, imageInformation.frontImageSegmentation);
    const hipSideSize = await jsMeasureHipSide(hipYLine, imageInformation.sideImageSegmentation);

    return jsCalculatePerimeter(hipFrontSize, hipSideSize);
}

async function jsMeasureHipFront(yLine, segmentation){
    //rightUpperLegFront, rightUpperLegBack, leftUpperLegFront, leftUpperLegBack, torsoFront, torsoBack
    var measureParts = [2,4,6,7,12,13]

    return await jsCalculateLineLength(yLine, segmentation, measureParts);
}

async function jsMeasureHipSide(yLine, segmentation){
    //rightUpperLegFront, rightUpperLegBack, leftUpperLegFront, leftUpperLegBack, torsoFront, torsoBack
    var measureParts = [2,4,6,7,12,13]

    return await jsCalculateLineLength(yLine, segmentation, measureParts);
}

async function jsGetHipYLine(image){
    var poseData = await jsConvertImageElementToPoseData(image);

    return Math.round((poseData.leftHip.position.y + poseData.rightHip.position.y)/2);
}

//#endregion

//#region Waist

async function jsMeasureWaist(imageInformation){
    //const waistYLine = jsGetWaistYLine(imageInformation.frontImage);
    //const waistFrontSize = jsMeasureWaistFront(waistYLine, imageInformation.frontImageSegmentation);
    //const waistSideSize = jsMeasureWaistSide(waistYLine, imageInformation.sideImageSegmentation);

    //return jsCalculatePerimeter(waistFrontSize, waistSideSize);
    return 0;
}

async function jsMeasureWaistFront(yLine, segmentation){

    var measureParts = []

    return await jsCalculateLineLength(yLine, segmentation, measureParts);
}

async function jsMeasureWaistSide(yLine, segmentation){

    var measureParts = []

    return await jsCalculateLineLength(yLine, segmentation, measureParts);
}

async function jsGetWaistYLine(image){

}

//#endregion

async function jsCalculateLineLength(yLine, segmentation, measureParts){
    var startPoint = segmentation.width * yLine;
    var endPoint = startPoint + segmentation.width;
    var outlinePixelNumber = [];

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

    return outlinePixelNumber[0] - outlinePixelNumber[outlinePixelNumber.length-1];
}

async function jsCalculatePerimeter(width, depth){
    var a = width / 2;
    var b = depth / 2;

    //p≈ π(3(a+b)- √((3a+b)(a+3b)))
    return Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (a + 3 * b)))
}

async function jsConvertImageElementToPoseData(image){
    var segmentation = await jsGetSegmentation(image);
    var poses = segmentation.allPoses;
    var pose = poses[0];

    var poseData = BodyParts
    pose.keypoints.forEach((keyPoint) => {
        poseData[keyPoint.part] = keyPoint;
    });

    return poseData;
}

var BodyParts=[
    "nose",
    "leftEye",
    "rightEye", 
    "leftEar",
    "rightEar" ,
    "leftShoulder", 
    "rightShoulder", 
    "leftElbow" ,
    "rightElbow" ,
    "leftWrist" ,
    "rightWrist" ,
    "leftHip" ,
    "rightHip" ,
    "leftKnee" ,
    "rightKnee" ,
    "leftAnkle" ,
    "rightAnkle"
]
