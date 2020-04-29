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
function MeasureBodyParts(img, canvas) {
    return __awaiter(this, void 0, void 0, function () {
        var loadAndPredict;
        var _this = this;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    loadAndPredict = function () { return __awaiter(_this, void 0, void 0, function () {
                        var net, segmentation, mask, includeParts, heupMaat, borstMaat;
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
                                    segmentation = _a.sent();
                                    if (canvas != null) {
                                        mask = bodyPix.toColoredPartMask(segmentation);
                                        bodyPix.drawMask(canvas, img, mask);
                                    }
                                    includeParts = [2, 3, 4, 5, 12, 13];
                                    heupMaat = mesurePart(segmentation, function (parts) {
                                        var res = Math.round((parts.leftHip.position.y + parts.rightHip.position.y) / 2);
                                        return res;
                                    }, includeParts, canvas || undefined);
                                    borstMaat = mesurePart(segmentation, function (parts) {
                                        var res = Math.round((parts.leftShoulder.position.y + parts.rightShoulder.position.y) / 2);
                                        return res;
                                    }, includeParts, canvas || undefined);
                                    if (canvas != null) {
                                        drawSkeleton(segmentation, canvas);
                                    }
                                    //console.log(segmentation);
                                    return [2 /*return*/, { borstMaat: borstMaat, heupMaat: heupMaat }];
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
function drawSkeleton(mask, canvas) {
    var poses = mask.allPoses.sort(function (a, b) { return b.score - a.score; });
    //console.log(poses)
    var pose = poses[0];
    pose.keypoints.forEach(function (value) {
        drawPoint(canvas, value.position.x, value.position.y, "red");
    });
}
function drawLine(canvas, yLijn) {
    var ctx = canvas.getContext("2d");
    if (ctx !== null) {
        ctx.fillStyle = "black";
    }
    ctx === null || ctx === void 0 ? void 0 : ctx.fillRect(0, yLijn - 0.5, canvas.width, 1);
}
function drawPoint(canvas, x, y, kleur) {
    var ctx = canvas.getContext("2d");
    if (ctx !== null) {
        ctx.fillStyle = kleur;
        ctx === null || ctx === void 0 ? void 0 : ctx.fillRect(x - 2.5, y - 2.5, 5, 5);
    }
    else {
        //console.log("canvas null")
    }
}
function clacLengte(mask, yLijn, mesurePart, canvas) {
    var beginPixel = mask.width * yLijn;
    var eindPixel = beginPixel + mask.width;
    var outlinePixelNumber = [];
    //console.log(beginPixel)
    for (var i = beginPixel; i < eindPixel; i++) {
        var checkPixel = mask.data[i];
        var nextPixel = mask.data[i + 1];
        //console.log(i,checkPixel,nextPixel)
        if ((mesurePart.includes(checkPixel) && !mesurePart.includes(nextPixel)) || (mesurePart.includes(nextPixel) && !mesurePart.includes(checkPixel))) {
            outlinePixelNumber.push(i);
        }
    }
    outlinePixelNumber.sort(function (a, b) { return b - a; });
    //console.log(outlinePixelNumber)
    if (canvas != undefined) {
        //console.log("color green!")
        outlinePixelNumber.forEach(function (value) {
            drawPoint(canvas, value % mask.width, yLijn, "darkblue");
        });
    }
    return outlinePixelNumber[0] - outlinePixelNumber[outlinePixelNumber.length - 1];
}
function mesurePart(mask, yLijnFunc, dectNum, canvas) {
    var poses = mask.allPoses.sort(function (a, b) { return b.score - a.score; });
    //console.log(poses)
    var pose = poses[0];
    //@ts-ignore
    var poseData = {};
    pose.keypoints.forEach(function (value) {
        //@ts-ignore
        poseData[value.part] = value;
    });
    //console.log(poseData)
    var yLijn = yLijnFunc(poseData);
    if (canvas != undefined) {
        drawLine(canvas, yLijn);
    }
    //console.log(yLijn)
    var lengte = clacLengte(mask, yLijn, dectNum, canvas);
    return lengte;
}
