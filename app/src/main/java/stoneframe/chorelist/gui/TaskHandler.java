package stoneframe.chorelist.gui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.joda.time.DateTime;

import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.ToDoList;

public class TaskHandler extends ArrayAdapter implements AdapterView.OnItemClickListener {

    private ToDoList todoList;

    public TaskHandler(Context context, int resource, ToDoList toDoList) {
        super(context, resource);

        this.todoList = toDoList;
    }

    public void update() {
        clear();
        for (Task task : todoList.getTasks(DateTime.now())) {
            add(task);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Task task = (Task) getItem(position);
        this.remove(task);
        todoList.complete(task);
    }
}
