import * as bodyPix from '@tensorflow-models/body-pix';
import React from 'react';
import './App.css';
import { Keypoint, Pose, BodyPixInput, ImageType } from '@tensorflow-models/body-pix/dist/types';
import {  BodyParts } from './react-app-env';
import { MeasureBodyParts } from './measure_Function/measure';
import { bodyPartFuncs } from './measure_Function/partFuncties';

interface Istate{
  loading:boolean
  imageString:string
  file:File|null
  heupMaat:number
  borstMaat:number
}

interface Iprops{

}

class App extends React.Component<Iprops,Istate>{
  private image:React.RefObject<HTMLImageElement>
  private canvas:React.RefObject<HTMLCanvasElement>
  constructor(props:Iprops){
    super(props)
    this.image =React.createRef()
    this.canvas=React.createRef()
    this.state={
      loading:false,
      imageString: "",
      file:null,
      heupMaat:-1,
      borstMaat:-1
    }
  
  }


  handleInputChange=async(event:React.ChangeEvent<HTMLInputElement>)=> {
    const target = event.target;
    const value = target.name === 'isGoing' ? target.checked : target.value;
    const name = target.name;

    if(name=="image"&&target.files!==null){
      var file=target.files[0]
      var fileString=URL.createObjectURL(file)

      this.setState({imageString:fileString})
      
    }

    this.setState<never>({
      [name]: value
    });
  }

  updatePicture=async(target:EventTarget &HTMLInputElement)=>{
    if(target.files!==null){
      var file=target.files[0]
      var fileString=URL.createObjectURL(file)
      
      await this.setState({imageString:fileString})
      console.log("start!")
    }
  } 

  printData=async()=>{
    this.setState({loading:true})
    if(this.image.current!= null){
      var result=await MeasureBodyParts(this.image.current,{heupMaat:bodyPartFuncs.heup,borstMaat:bodyPartFuncs.borst},this.canvas.current)
      this.setState({heupMaat:result.heupMaat,borstMaat:result.borstMaat})
    }
    this.setState({loading:false})
  }
  

  render(){
    return (
      <div className="App">
        {
          this.state.heupMaat===-1 ||
          <p>Heupmaat: {this.state.heupMaat} pixels</p>
        }
          {
          this.state.borstMaat===-1 ||
          <p>borst: {this.state.borstMaat} pixels</p>
        }
        <input type="file" onChange={this.handleInputChange} name="image" />
        <img  ref={this.image} src={this.state.imageString}/>
        <canvas ref={this.canvas}></canvas>
        {
          this.state.loading?
          <div></div>:
          <button onClick={this.printData}>Start analyse!</button>
        }

        

      </div>
    )
  }
}

export default App;
