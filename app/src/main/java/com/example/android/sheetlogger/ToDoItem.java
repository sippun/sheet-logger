package com.example.android.sheetlogger;

/**
 * Created by Joel on 3/29/2017.
 */

/**
 * Class to represent "to do" items
 */
public class ToDoItem extends Object {
    private String taskName;

    public ToDoItem(String name) {
        this.taskName = name;
    }

    public String getName() {
        return taskName;
    }
}

