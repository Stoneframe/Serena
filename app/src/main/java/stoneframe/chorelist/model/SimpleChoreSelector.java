package stoneframe.chorelist.model;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleChoreSelector implements ChoreSelector
{
    @Override
    public List<Chore> selectChores(List<Chore> chores, int effort)
    {
        List<Chore> selectedChores = new LinkedList<>();

        if (effort <= 0)
        {
            return chores.stream().filter(t -> t.getEffort() == 0).collect(Collectors.toList());
        }

        int currEffort = 0;
        for (Chore chore : chores)
        {
            currEffort += chore.getEffort();
            selectedChores.add(chore);
            if (currEffort >= effort)
            {
                break;
            }
        }

        return selectedChores;
    }
}
