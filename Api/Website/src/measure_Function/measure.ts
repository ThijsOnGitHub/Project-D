import { Keypoint, BodyPixInput, ImageType } from '@tensorflow-models/body-pix/dist/types';
import * as bodyPix from '@tensorflow-models/body-pix';
import { BodyParts, PartFunc } from '../react-app-env';
import { bodyPartFuncs } from './partFuncties';



export async function MeasureBodyParts<T extends {[partName:string]:PartFunc}>(img: BodyPixInput ,PartFuncs:T,canvas?:HTMLCanvasElement|null):Promise<{[name:string]:number}>{
      //console.log("start")
      //console.log(img)

          
           const loadAndPredict = async()=> {
            //it loads the model
            const net = await bodyPix.load({
              architecture: 'ResNet50',
              outputStride: 16
            });
          
            //if no image is defined the functions doesn't do anything
            if(img!==null){
              //The segmentation is calculated on the image
              const segmentation = await net.segmentPersonParts(img);

              //if a canvas if defined it draws the mask on the canvas
              if(canvas!=null){
                var mask=bodyPix.toColoredPartMask(segmentation)
            
                bodyPix.drawMask(canvas,img as ImageType,mask)
                drawSkeleton(segmentation,canvas)
              }
              
              //This section measures the body parts with the custom functions and returns an object of it

              var object:Partial<{[name:string]:number}>= {};
              
              //All the functions
              Object.entries(PartFuncs).forEach((value)=>{
                object[value[0]]=value[1](segmentation,canvas == null ? undefined : canvas)
              })
              
              return object as {[name:string]:number}



            }else{
              throw new Error("Image is null")
            }
          }
        
          return await loadAndPredict();
  }
  /**
   * 
   * @param mask The mask of the segmentation to draw
   * @param canvas The canvas to draw the mask on
   * 
   * This function draws the skeletion of the mask on the canvas
   */
  function drawSkeleton(mask:bodyPix.SemanticPartSegmentation,canvas:HTMLCanvasElement){
    var poses =mask.allPoses.sort((a , b) => b.score - a.score  )
    //console.log(poses)
    var pose=poses[0]
      pose.keypoints.forEach(value=>{
        drawPoint(canvas,value.position.x,value.position.y,"red")
      })

  }


  /**
   * This function draws a y-line on the canvas
   * @param canvas The canvas to draw the line on
   * @param yLijn The Y-coördinate of the line
   */
  function drawLine(canvas:HTMLCanvasElement,yLijn:number){
    var ctx=canvas.getContext("2d")
    if(ctx!==null){
      ctx.fillStyle="black"
    }
    ctx?.fillRect(0,yLijn-0.5,canvas.width,1)
  }

  /**
   * 
   * @param canvas The canvas to draw the points on
   * @param x The X-coördinate of the point
   * @param y The Y-coördinate of the point
   * @param color The collor of the point
   */
  function drawPoint(canvas:HTMLCanvasElement,x:number,y:number,color:string){
    var ctx=canvas.getContext("2d")
    if(ctx!==null){
      ctx.fillStyle = color
      ctx?.fillRect(x-2.5,y-2.5,5,5)
    }else{
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
  function clacLengte(mask:bodyPix.SemanticPartSegmentation,yLijn:number,mesureParts:number[],canvas?:HTMLCanvasElement):number{
    var beginPixel=mask.width*yLijn
    var eindPixel=beginPixel+mask.width
    var outlinePixelNumber:number[]=[]

    //console.log(beginPixel)

    // Here the code cycles througt the point and detects a transition between parts
    for(var i=beginPixel;i<eindPixel;i++){
      var checkPixel=mask.data[i] 
      var nextPixel=mask.data[i+1]
      //console.log(i,checkPixel,nextPixel)

      //It checks if there is a transition between mesureParts a other parts
      if((mesureParts.includes(checkPixel)&&!mesureParts.includes(nextPixel))||(mesureParts.includes(nextPixel)&&!mesureParts.includes(checkPixel))){
        //It adds the point to the collection of points
        outlinePixelNumber.push(i)
      }
    }

    outlinePixelNumber.sort((a,b)=>b-a)
    console.log(outlinePixelNumber)

    console.log(canvas)
    //if a canvas is defined it draws the transition points
    if(canvas!=undefined){
      //console.log("color green!")
      outlinePixelNumber.forEach(value=>{
        drawPoint(canvas,value%mask.width,yLijn,"darkblue")
      })
    }

    // it calculates the distance between the first and last transtion and returns it
    return outlinePixelNumber[0]-outlinePixelNumber[outlinePixelNumber.length-1]
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
export function measurePart(mask:bodyPix.SemanticPartSegmentation,yLijnFunc:(parts:BodyParts)=>number,mesurePart:number[],canvas?:HTMLCanvasElement):number{
    
    //It selects the most confident pose
    var poses =mask.allPoses.sort((a , b) => b.score - a.score  )
    //console.log(poses)
    var pose=poses[0]

    //it puts the data from the pose into as object
    //@ts-ignore
    var poseData:BodyParts={}
    pose.keypoints.forEach((value:Keypoint) =>{
      //@ts-ignore
        poseData[value.part]=value
    })
    //console.log(poseData)


    var yLijn=yLijnFunc(poseData)

    //if a canvas is defined the y line is drawn on the canvas
    if(canvas!=undefined){
      drawLine(canvas,yLijn)
    }
    
    //console.log(yLijn)
    //it calculates the width between the transitions and return the width
    var lengte=clacLengte(mask,yLijn,mesurePart,canvas)
    return lengte
  }
