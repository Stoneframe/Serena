package stoneframe.chorelist.model;

import java.util.List;

public interface ChoreSelector
{
    List<Chore> selectChores(List<Chore> chores, int effort);
}
