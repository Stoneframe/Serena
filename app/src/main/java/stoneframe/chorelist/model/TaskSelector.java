package stoneframe.chorelist.model;

import java.util.List;

public interface TaskSelector {

    List<Task> selectTasks(List<Task> tasks, int effort);

}
