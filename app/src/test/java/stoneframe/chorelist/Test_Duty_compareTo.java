package stoneframe.chorelist;

import org.junit.Test;

import stoneframe.chorelist.model.Duty;

import static org.junit.Assert.*;

public class Test_Duty_compareTo {

    @Test
    public void test_compare_date_1() {
        Duty d1 = new Duty("Duty1", 1, 10, TestUtils.MOCK_NOW.plusDays(1), Duty.DAILY, 5);
        Duty d2 = new Duty("Duty2", 1, 10, TestUtils.MOCK_NOW, Duty.DAILY, 2);

        int expected = 1;
        int actual = d1.compareTo(d2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_date_2() {
        Duty d1 = new Duty("Duty1", 1, 10, TestUtils.MOCK_NOW ,Duty.DAILY, 3);
        Duty d2 = new Duty("Duty2", 1, 10, TestUtils.MOCK_NOW.plusWeeks(2), Duty.DAILY, 6);

        int expected = -1;
        int actual = d1.compareTo(d2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_priority_1() {
        Duty d1 = new Duty("Duty1", 2, 10, TestUtils.MOCK_NOW, Duty.DAILY, 3);
        Duty d2 = new Duty("Duty2", 6, 10, TestUtils.MOCK_NOW, Duty.DAILY, 3);

        int expected = -1;
        int actual = d1.compareTo(d2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_priority_2() {
        Duty d1 = new Duty("Duty1", 8, 10, TestUtils.MOCK_NOW, Duty.DAILY, 3);
        Duty d2 = new Duty("Duty2", 1, 10, TestUtils.MOCK_NOW, Duty.DAILY, 3);

        int expected = 1;
        int actual = d1.compareTo(d2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_1() {
        Duty d1 = new Duty("Duty1", 2, 3, TestUtils.MOCK_NOW, Duty.DAILY, 3);
        Duty d2 = new Duty("Duty2", 2, 5, TestUtils.MOCK_NOW, Duty.DAILY, 3);

        int expected = -1;
        int actual = d1.compareTo(d2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_2() {
        Duty d1 = new Duty("Duty1", 2, 60, TestUtils.MOCK_NOW, Duty.DAILY, 3);
        Duty d2 = new Duty("Duty2", 2, 25, TestUtils.MOCK_NOW, Duty.DAILY, 3);

        int expected = 1;
        int actual = d1.compareTo(d2);

        assertEquals(expected, actual);
    }

}
