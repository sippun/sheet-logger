package com.example.android.sheetlogger;

/**
 * Created by Joel on 3/29/2017.
 */

/**
 * Class to represent "to do" items
 */
public class ToDoItem extends Object {
    String taskName;
    Boolean done;

    public ToDoItem(String name) {
        this.taskName = name;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }
}
