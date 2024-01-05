package stoneframe.chorelist;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import stoneframe.chorelist.model.Chore;

public class ChoreListTest
{
    @Test
    public void GetAllChores_NoChoresAdded_ReturnEmptyList()
    {
        ChoreList choreList = new ChoreList();

        List<Chore> allChores = choreList.getAllChores();

        assertEquals(Collections.emptyList(), allChores);
    }
}
