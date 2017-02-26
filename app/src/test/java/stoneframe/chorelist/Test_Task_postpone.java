package stoneframe.chorelist;

import org.junit.Test;

import stoneframe.chorelist.model.Task;

import static junit.framework.Assert.assertEquals;

public class Test_Task_postpone {

    @Test
    public void postpone_task_two_days() {
        Task task = new Task(TestUtils.createDateTime(2017, 2, 9), "", 1, 10);

        task.postpone(2);

        assertEquals(TestUtils.createDateTime(2017, 2, 11), task.getDate());
    }

}
