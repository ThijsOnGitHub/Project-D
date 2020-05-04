"use strict";
exports.__esModule = true;
var measure_1 = require("./measure");
//export type PartFunc= (segmentation:bodyPix.SemanticPartSegmentation,canvas:HTMLCanvasElement)=>number
var bodyPartFuncs = /** @class */ (function () {
    function bodyPartFuncs() {
    }
    bodyPartFuncs.heup = createPartFunc(function (parts) {
        var res = Math.round((parts.leftHip.position.y + parts.rightHip.position.y) / 2);
        return res;
    }, [2, 3, 4, 5, 12, 13]);
    bodyPartFuncs.borst = createPartFunc(function (parts) {
        var res = Math.round((parts.leftShoulder.position.y + parts.rightShoulder.position.y) / 2);
        return res;
    }, [2, 3, 4, 5, 12, 13]);
    return bodyPartFuncs;
}());
exports.bodyPartFuncs = bodyPartFuncs;
function createPartFunc(yLijnFunc, measureParts) {
    return function (segmentation, canvas) { return measure_1.measurePart(segmentation, yLijnFunc, measureParts, canvas || undefined); };
}
