package com.example.projectd;

import com.google.gson.Gson;

public class MeasureResult {
    public Double chestSize;
    public Double hipSize;
    public Double waistSize;

    public String getString(){
        return new Gson().toJson(this);
    }
}
