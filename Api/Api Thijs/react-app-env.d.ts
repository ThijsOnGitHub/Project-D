// <reference types=react-scripts />
import { Keypoint } from  "@tensorflow-models/body-pix/dist/types";
import * as bodypix from '@tensorflow-models/body-pix'

export interface BodyParts{
nose: Keypoint 
leftEye: Keypoint 
rightEye: Keypoint 
leftEar: Keypoint 
rightEar: Keypoint 
leftShoulder: Keypoint 
rightShoulder: Keypoint 
leftElbow: Keypoint 
rightElbow: Keypoint 
leftWrist: Keypoint 
rightWrist: Keypoint 
leftHip: Keypoint 
rightHip: Keypoint 
leftKnee: Keypoint 
rightKnee: Keypoint 
leftAnkle: Keypoint 
rightAnkle: Keypoint
}

export type PartFunc= (segmentation:bodypix.SemanticPartSegmentation,canvas:HTMLCanvasElement)=>number