package com.example.android.sheetlogger;

/**
 * Created by Joel on 5/26/2017.
 */

public class BoolItem extends ToDoItem {
    private Boolean done;

    public BoolItem(String name) {
        super(name);
        done = false;
    }

    public BoolItem(ToDoItem item) {
        super(item.getName());
        done = false;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public void toggle() {
        done = !done;
    }
}