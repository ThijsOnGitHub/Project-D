import { Keypoint, BodyPixInput, ImageType } from '@tensorflow-models/body-pix/dist/types';
import * as bodyPix from '@tensorflow-models/body-pix';
import { BodyParts } from '../react-app-env';



export async function MeasureBodyParts(img: BodyPixInput ,canvas?:HTMLCanvasElement|null):Promise<{borstMaat:number,heupMaat:number}>{
      console.log("start")
      console.log(img)
  
           const loadAndPredict = async()=> {

            const net = await bodyPix.load({
              architecture: 'ResNet50',
              outputStride: 16
            });
          
            /**
             * One of (see documentation below):
             *   - net.segmentPerson
             *   - net.segmentPersonParts
             *   - net.segmentMultiPerson
             *   - net.segmentMultiPersonParts
             * See documentation below for details on each method.
              */
             
            if(img!==null){
              const segmentation = await net.segmentPersonParts(img);
              if(canvas!=null){
                var mask=bodyPix.toColoredPartMask(segmentation)
            
                bodyPix.drawMask(canvas,img as ImageType,mask)
              }
              
              var includeParts=[2,3,4,5,12,13]
              var heupMaat=mesurePart(segmentation,(parts)=>{
                var res=Math.round((parts.leftHip.position.y+parts.rightHip.position.y)/2)
                return res
              },includeParts,canvas || undefined)

              var borstMaat=mesurePart(segmentation,(parts)=>{
                var res=Math.round((parts.leftShoulder.position.y+parts.rightShoulder.position.y)/2)
                return res
              },includeParts,canvas || undefined)

              if(canvas!=null){
                drawSkeleton(segmentation,canvas)
              }
              console.log(segmentation);
              return {borstMaat:borstMaat,heupMaat:heupMaat}
            }else{
              throw new Error("Image is null")
            }
          }
          return await loadAndPredict();
  }

  function drawSkeleton(mask:bodyPix.SemanticPartSegmentation,canvas:HTMLCanvasElement){
    var poses =mask.allPoses.sort((a , b) => b.score - a.score  )
    console.log(poses)
    var pose=poses[0]
      pose.keypoints.forEach(value=>{
        drawPoint(canvas,value.position.x,value.position.y,"red")
      })

  }

  function drawLine(canvas:HTMLCanvasElement,yLijn:number){
    var ctx=canvas.getContext("2d")
    if(ctx!==null){
      ctx.fillStyle="black"
    }
    ctx?.fillRect(0,yLijn-0.5,canvas.width,1)
  }

  function drawPoint(canvas:HTMLCanvasElement,x:number,y:number,kleur:string){
    var ctx=canvas.getContext("2d")
    if(ctx!==null){
      ctx.fillStyle = kleur
      ctx?.fillRect(x-2.5,y-2.5,5,5)
    }else{
      console.log("canvas null")
    }
  }

  function clacLengte(mask:bodyPix.SemanticPartSegmentation,yLijn:number,mesurePart:number[],canvas?:HTMLCanvasElement):number{
    var beginPixel=mask.width*yLijn
    var eindPixel=beginPixel+mask.width
    var outlinePixelNumber:number[]=[]

    console.log(beginPixel)

    for(var i=beginPixel;i<eindPixel;i++){
      var checkPixel=mask.data[i] 
      var nextPixel=mask.data[i+1]
      console.log(i,checkPixel,nextPixel)
      if((mesurePart.includes(checkPixel)&&!mesurePart.includes(nextPixel))||(mesurePart.includes(nextPixel)&&!mesurePart.includes(checkPixel))){
        outlinePixelNumber.push(i)
      }
    }
    outlinePixelNumber.sort((a,b)=>b-a)
    console.log(outlinePixelNumber)
    if(canvas!=undefined){
      console.log("color green!")
      outlinePixelNumber.forEach(value=>{
        drawPoint(canvas,value%mask.width,yLijn,"darkblue")
      })
    }
    return outlinePixelNumber[0]-outlinePixelNumber[outlinePixelNumber.length-1]
  }

  function mesurePart(mask:bodyPix.SemanticPartSegmentation,yLijnFunc:(parts:BodyParts)=>number,dectNum:number[],canvas?:HTMLCanvasElement){
    var poses =mask.allPoses.sort((a , b) => b.score - a.score  )
    console.log(poses)
    var pose=poses[0]
    //@ts-ignore
    var poseData:BodyParts={}
    
    pose.keypoints.forEach((value:Keypoint) =>{
      //@ts-ignore
        poseData[value.part]=value
    })

    console.log(poseData)
    var yLijn=yLijnFunc(poseData)
    if(canvas!=undefined){
      drawLine(canvas,yLijn)
    }
    
    console.log(yLijn)
    var lengte=clacLengte(mask,yLijn,dectNum,canvas)
    return lengte
  }
