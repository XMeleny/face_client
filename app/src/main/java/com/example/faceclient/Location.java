package com.example.faceclient;

import java.util.List;

public class Location {
    private int up;
    private int down;
    private int left;
    private int right;

    public Location(int[] points)
    {
        up=points[0];
        right=points[1];
        down=points[2];
        left=points[3];
    }

    public int getUp() {
        return up;
    }

    public int getDown() {
        return down;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }
}
