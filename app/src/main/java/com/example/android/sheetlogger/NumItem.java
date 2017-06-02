package com.example.android.sheetlogger;

/**
 * Created by Joel on 6/1/2017.
 */

public class NumItem extends ToDoItem{
    private int myNum;

    public NumItem(String name) {
        super(name);
        myNum = 0;
    }

    public NumItem(String name, int num) {
        super(name);
        myNum = num;
    }

    public int getNum() {
        return myNum;
    }

    public void setNum(int num) {
        myNum = num;
    }
}
