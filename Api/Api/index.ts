import * as measure from "../Website/src/Api/measure"
import * as fs from 'fs';
import * as tfjs from '@tensorflow/tfjs-node'
import * as bodypix from '@tensorflow-models/body-pix'
import { Tensor3D } from "@tensorflow/tfjs-node";



async function loadImage(path:string){
  const file = await fs.promises.readFile(path);

  const image = await tfjs.node.decodeImage(file, 3);

  return image as Tensor3D;
}

async function start(){
  var image = await loadImage("./img.png")
  //@ts-ignore
  var resultaat = await measure.MeasureBodyParts(image)
  console.log(resultaat)
  }
start()