import * as bodyPix from '@tensorflow-models/body-pix';
import { measurePart } from "./measure"
import { BodyParts, PartFunc } from '../react-app-env';

//export type PartFunc= (segmentation:bodyPix.SemanticPartSegmentation,canvas:HTMLCanvasElement)=>number

export class bodyPartFuncs{
    static heup=createPartFunc(
        (parts)=>{
        var res=Math.round((parts.leftHip.position.y+parts.rightHip.position.y)/2)
        return res
    },[2,3,4,5,12,13])
    
    static borst=createPartFunc(
            (parts)=>{
            var res=Math.round((parts.leftShoulder.position.y+parts.rightShoulder.position.y)/2)
            return res
        },[2,3,4,5,12,13])
}



function createPartFunc(yLijnFunc:(parts:BodyParts)=>number,measureParts:number[]):PartFunc{
    return (segmentation:bodyPix.SemanticPartSegmentation,canvas?:HTMLCanvasElement)=>{return measurePart(segmentation,yLijnFunc,measureParts,canvas || undefined)}
}

