package com.example.projectd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.IOException;
import java.util.ArrayList;

public class SetLines extends AppCompatActivity {

    //view variables
    ImageView imageView;
    TextView feedback;
    View line;
    ConstraintLayout dragView;
    Button next;

    //index to get track of progress
    int globalIndex;

    //List with de data of the image
    ArrayList<ImageData> takenImagesArray;

    //the index of the image that's being handeld
    int imageIndex;
    //array with points that needs to get handled
    MeasurePoints[] measurePoints;
    //index of point thats needs to get handled
    int measurePointIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_lines);

        //Connect variables with views
        imageView =findViewById(R.id.mIVTakenImagePreview);
        feedback = findViewById(R.id.mTVfeedback);
        line = findViewById(R.id.mVLine);
        dragView = findViewById(R.id.mCLdragView);
        next =findViewById(R.id.mBSetLine);

        //Get information from the intent
        Intent startIntent= getIntent();
        takenImagesArray = startIntent.getParcelableArrayListExtra("data");

        //Checks if there is data in the takenImagesArray
        if(takenImagesArray ==null || takenImagesArray.size()==0){
            throw new Error("No exta's detected");
        }

        //set global index
        globalIndex=0;

        //set the index for the takenImagesArray
        imageIndex = 0;

        //Create list with positions that need to be set
        measurePoints= MeasurePoints.values();
        measurePointIndex=0;

        //Set content of views
        Log.i("info","Starts Drawing");

        //Sets views in the correct value
        this.updateFeedbackText();
        this.updateImage();

        //Sets the line on a right position
        setLinePostion(1);


        //Code to move the line
        dragView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                GestureDetector gestureDetector= new GestureDetector(new GestureDetector.OnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent motionEvent) {
                        setLinePostion((int) motionEvent.getY());
                        return false;
                    }

                    @Override
                    public void onShowPress(MotionEvent motionEvent) {

                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent motionEvent) {
                        return false;
                    }

                    @Override
                    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                        setLinePostion((int) motionEvent1.getY());
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent motionEvent) {

                    }

                    @Override
                    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                        setLinePostion((int) motionEvent1.getY());
                        return false;
                    }
                });
                gestureDetector.onTouchEvent(motionEvent);

                return true;
            }
        });


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    increaseLineIndex(1,true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(globalIndex==0){
            super.onBackPressed();
        }else{
            try {
                increaseLineIndex(-1,false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public double getImageHeight(Uri image) throws IOException {
         Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),image);
         double height=bitmap.getHeight();
         Log.i("height",Double.toString(height));
         return height;
    }

    public void setLinePostion(int yPosition){
        Log.i("drag detected",Float.toString(yPosition));
        ViewGroup.MarginLayoutParams marginLayoutParams =(ViewGroup.MarginLayoutParams) line.getLayoutParams();
        int beamHeight=getBeamHeight();
        int imageViewHeight=imageView.getMeasuredHeight();
        int Yvalue=limit(yPosition,beamHeight,imageViewHeight-beamHeight);
        marginLayoutParams.setMargins(0, Yvalue,0,0);
        line.setLayoutParams(marginLayoutParams);
    }

    public double getLinePercentage(){
        ViewGroup.MarginLayoutParams marginLayoutParams =(ViewGroup.MarginLayoutParams) line.getLayoutParams();
        int topMargin=marginLayoutParams.topMargin;
        int beamHeight =getBeamHeight();
        int imageHeight= getRescaledImageHeigth(imageView).getHeight();
        double percentage=((double) topMargin-beamHeight)/imageHeight;
        Log.i("percentage",Double.toString(percentage));
        return percentage;
    }

    public int getYposition() throws IOException {
        return (int) Math.round(getImageHeight(getCurrentImage().getImage())*getLinePercentage());
}

    public int getBeamHeight(){
        Size size = getRescaledImageHeigth(imageView);
        int imageViewHeight=imageView.getMeasuredHeight();
        int imageHeight= size.getHeight();
        int beamHeight=(imageViewHeight-imageHeight)/2;
        return beamHeight;

    }

    public int limit(int value,int min,int max ){
        if (value<min){
            return min;
        }else if(value>max){
            return max;
        }
        return value;
    }

    public Size getRescaledImageHeigth(ImageView imageView){
        int ih=imageView.getMeasuredHeight();//height of imageView
        int iw=imageView.getMeasuredWidth();//width of imageView
        int iH=imageView.getDrawable().getIntrinsicHeight();//original height of underlying image
        int iW=imageView.getDrawable().getIntrinsicWidth();//original width of underlying image

        if (ih/iH<=iw/iW){
            iw=iW*ih/iH;  //rescaled width of image within ImageView
        } else {
            ih= iH*iw/iW;//rescaled height of image within ImageView
        }
        return new Size(iw,ih);
    }

    //Go to the next measure point that need to be set
    //If all te points are set it goes to the next activity
    private void increaseLineIndex(int amount,boolean setValue) throws IOException {
        if(setValue){
            getCurrentImage().setMeasurePoints(measurePoints[measurePointIndex], getYposition());
        }
        int takenImagesSize=takenImagesArray.size();
        //sets the indexes
        globalIndex += amount;
        measurePointIndex = globalIndex % measurePoints.length;
        imageIndex = (int)globalIndex/measurePoints.length;

        if(globalIndex>takenImagesSize*measurePoints.length-1) {
            //go to the next activaty
            Intent sendDataIntent = new Intent(getApplicationContext(), sendData.class);
            sendDataIntent.putParcelableArrayListExtra("data", takenImagesArray);
            this.startActivity(sendDataIntent);
        }else{
            //update the fields
            updateFeedbackText();
            updateImage();
        }
    }



    //Gets the image thats is currently used
    private ImageData getCurrentImage(){
        return takenImagesArray.get(imageIndex);
    };

    //Sets the right feedback text
    private void updateFeedbackText(){
        String value=String.valueOf(measurePoints[measurePointIndex]);
        feedback.setText("Zet de lijn op de "+ getText(getResources().getIdentifier(value,"string",getPackageName())));
    }

    //Sets the right image on the imageview
    //The image is scaled so that the image is not too big
    private void updateImage(){
        //Set image content
        Bitmap d = null;
        try {
            d = MediaStore.Images.Media.getBitmap(getContentResolver(), getCurrentImage().getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int nh = (int) ( d.getHeight() * (512.0 / d.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
        imageView.setImageBitmap(scaled);
    }

}

