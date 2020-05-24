package com.example.projectd;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;


public class ImageData implements Parcelable {

    private String name;
    private Uri image;
    private double ratio;
    private HashMap<MeasurePoints,Double> poses;


    public ImageData(String name, Uri image, double ratio){
        this.name=name;
        this.image=image;
        this.ratio=ratio;
        this.poses= new HashMap<MeasurePoints, Double>();
    }

    public void setMeasurePoints(MeasurePoints pose, Double poseY) {
        poses.put(pose,poseY);
    }

    public String getName() {
        return name;
    }

    public Uri getImage() {
        return image;
    }

    public double getRatio() {
        return ratio;
    }


    protected ImageData(Parcel in) {
        name = in.readString();
        image = in.readParcelable(Uri.class.getClassLoader());
        ratio = in.readDouble();
        poses = (HashMap<MeasurePoints, Double>) in.readBundle().getSerializable("map");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(image, flags);
        dest.writeDouble(ratio);
        Bundle bundle = new Bundle();
        bundle.putSerializable("map", poses);
        dest.writeBundle(bundle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageData> CREATOR = new Creator<ImageData>() {
        @Override
        public ImageData createFromParcel(Parcel in) {
            return new ImageData(in);
        }

        @Override
        public ImageData[] newArray(int size) {
            return new ImageData[size];
        }
    };
}
enum MeasurePoints {
    waist,chest
}
