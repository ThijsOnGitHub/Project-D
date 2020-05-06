import * as measure from "../Website/src/measure_Function/measure"
import * as fs from 'fs';
import * as tfjs from '@tensorflow/tfjs-node'

import { Tensor3D } from "@tensorflow/tfjs-node";
import "../Website/src/measure_Function/partFuncties"
import { bodyPartFuncs } from "../Website/src/measure_Function/partFuncties";


async function loadImage(path:string){
  const file = await fs.promises.readFile(path);

  const image = await tfjs.node.decodeImage(file, 3);

  return image as Tensor3D;
}

async function start(){
  var image = await loadImage("./img.png")
  //@ts-ignore
  var resultaat = await measure.MeasureBodyParts(image,{"heupMaat":bodyPartFuncs.heup})
  console.log(resultaat)
}
start()