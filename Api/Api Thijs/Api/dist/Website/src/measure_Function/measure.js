"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
exports.__esModule = true;
var bodyPix = require("@tensorflow-models/body-pix");
function MeasureBodyParts(img, PartFuncs, canvas) {
    return __awaiter(this, void 0, void 0, function () {
        var loadAndPredict;
        var _this = this;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    loadAndPredict = function () { return __awaiter(_this, void 0, void 0, function () {
                        var net, segmentation_1, mask, object;
                        return __generator(this, function (_a) {
                            switch (_a.label) {
                                case 0: return [4 /*yield*/, bodyPix.load({
                                        architecture: 'ResNet50',
                                        outputStride: 16
                                    })];
                                case 1:
                                    net = _a.sent();
                                    if (!(img !== null)) return [3 /*break*/, 3];
                                    return [4 /*yield*/, net.segmentPersonParts(img)];
                                case 2:
                                    segmentation_1 = _a.sent();
                                    //if a canvas if defined it draws the mask on the canvas
                                    if (canvas != null) {
                                        mask = bodyPix.toColoredPartMask(segmentation_1);
                                        bodyPix.drawMask(canvas, img, mask);
                                        drawSkeleton(segmentation_1, canvas);
                                    }
                                    object = {};
                                    //All the functions
                                    Object.entries(PartFuncs).forEach(function (value) {
                                        object[value[0]] = value[1](segmentation_1, canvas == null ? undefined : canvas);
                                    });
                                    return [2 /*return*/, object];
                                case 3: throw new Error("Image is null");
                            }
                        });
                    }); };
                    return [4 /*yield*/, loadAndPredict()];
                case 1: return [2 /*return*/, _a.sent()];
            }
        });
    });
}
exports.MeasureBodyParts = MeasureBodyParts;
/**
 *
 * @param mask The mask of the segmentation to draw
 * @param canvas The canvas to draw the mask on
 *
 * This function draws the skeletion of the mask on the canvas
 */
function drawSkeleton(mask, canvas) {
    var poses = mask.allPoses.sort(function (a, b) { return b.score - a.score; });
    //console.log(poses)
    var pose = poses[0];
    pose.keypoints.forEach(function (value) {
        drawPoint(canvas, value.position.x, value.position.y, "red");
    });
}
/**
 * This function draws a y-line on the canvas
 * @param canvas The canvas to draw the line on
 * @param yLijn The Y-coördinate of the line
 */
function drawLine(canvas, yLijn) {
    var ctx = canvas.getContext("2d");
    if (ctx !== null) {
        ctx.fillStyle = "black";
    }
    ctx === null || ctx === void 0 ? void 0 : ctx.fillRect(0, yLijn - 0.5, canvas.width, 1);
}
/**
 *
 * @param canvas The canvas to draw the points on
 * @param x The X-coördinate of the point
 * @param y The Y-coördinate of the point
 * @param color The collor of the point
 */
function drawPoint(canvas, x, y, color) {
    var ctx = canvas.getContext("2d");
    if (ctx !== null) {
        ctx.fillStyle = color;
        ctx === null || ctx === void 0 ? void 0 : ctx.fillRect(x - 2.5, y - 2.5, 5, 5);
    }
    else {
        //console.log("canvas null")
    }
}
/**
 * This function calculate the length of the parts that needs to get mesured.
 * It draws a vertical line and marks the points where there is a transition between the mesureparts and the the other parts.
 *
 * @param mask The segmentation data to calculate on
 * @param yLijn The Y-coördinate of the line function
 * @param mesureParts The parts that count for the measure
 * @param canvas The canvas the function draws the outcome
 *
 * @returns The distance between the first and last point
 */
function clacLengte(mask, yLijn, mesureParts, canvas) {
    var beginPixel = mask.width * yLijn;
    var eindPixel = beginPixel + mask.width;
    var outlinePixelNumber = [];
    //console.log(beginPixel)
    // Here the code cycles througt the point and detects a transition between parts
    for (var i = beginPixel; i < eindPixel; i++) {
        var checkPixel = mask.data[i];
        var nextPixel = mask.data[i + 1];
        //console.log(i,checkPixel,nextPixel)
        //It checks if there is a transition between mesureParts a other parts
        if ((mesureParts.includes(checkPixel) && !mesureParts.includes(nextPixel)) || (mesureParts.includes(nextPixel) && !mesureParts.includes(checkPixel))) {
            //It adds the point to the collection of points
            outlinePixelNumber.push(i);
        }
    }
    outlinePixelNumber.sort(function (a, b) { return b - a; });
    console.log(outlinePixelNumber);
    console.log(canvas);
    //if a canvas is defined it draws the transition points
    if (canvas != undefined) {
        //console.log("color green!")
        outlinePixelNumber.forEach(function (value) {
            drawPoint(canvas, value % mask.width, yLijn, "darkblue");
        });
    }
    // it calculates the distance between the first and last transtion and returns it
    return outlinePixelNumber[0] - outlinePixelNumber[outlinePixelNumber.length - 1];
}
/**
 * This function is used to start a measure on a segmetation
 * It picks the most confident pose and collect the data into an object.
 * After this it calcutes the Y-coördinate for the line to mesure.
 * It calculates the
 * @param mask The segmentation data to calculate on
 * @param yLijnFunc This is the fucntion thats creates the y-line coördinate
 * @param mesurePart The parts that count for the measure
 * @param canvas The canvas the function draws the outcome
 *
 * @returns The function returns the width of the parts you want to measure
 */
function measurePart(mask, yLijnFunc, mesurePart, canvas) {
    //It selects the most confident pose
    var poses = mask.allPoses.sort(function (a, b) { return b.score - a.score; });
    //console.log(poses)
    var pose = poses[0];
    //it puts the data from the pose into as object
    //@ts-ignore
    var poseData = {};
    pose.keypoints.forEach(function (value) {
        //@ts-ignore
        poseData[value.part] = value;
    });
    //console.log(poseData)
    var yLijn = yLijnFunc(poseData);
    //if a canvas is defined the y line is drawn on the canvas
    if (canvas != undefined) {
        drawLine(canvas, yLijn);
    }
    //console.log(yLijn)
    //it calculates the width between the transitions and return the width
    var lengte = clacLengte(mask, yLijn, mesurePart, canvas);
    return lengte;
}
exports.measurePart = measurePart;
