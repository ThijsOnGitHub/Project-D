package com.example.projectd;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.RequiresApi;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.HashMap;


//
enum MeasureResultsUnit {
    milimeter(1,"mm"),centimeter(0.1,"cm"),decimeter(0.01,"dm"),meter(0.001,"m");
    private final double factorFromMM;
    private final String abbreviation;
    MeasureResultsUnit(double i, String abbreviation) {
        this.factorFromMM=i;
        this.abbreviation = abbreviation;
    }
    public double getFactorFromMM(){
        return factorFromMM;
    }
    public String getAbbreviation(){ return abbreviation;}
}


public class MeasureResults  {

    public HashMap<String,Double> hashMap;

    public MeasureResults(HashMap<String, Double> hashMap) {
        this.hashMap = hashMap;
    }

    public Double getSize(MeasurePoints point, MeasureResultsUnit unit) {
        return hashMap.get(point+"Size")* unit.getFactorFromMM();
    }



    public String getResultString(Context context,MeasureResultsUnit unit,int amoutOfDecimals){
        String finalString ="";
        MeasurePoints[] points=MeasurePoints.values();
        for(MeasurePoints MeasurePoint:points){
            finalString=finalString+ context.getText(context.getResources().getIdentifier(String.valueOf(MeasurePoint),"string",context.getPackageName())) +": "+String.format("%."+amoutOfDecimals+"f",getSize(MeasurePoint,unit))+" "+unit.getAbbreviation()+"\n\n";
        }
        return finalString;
    }
}
