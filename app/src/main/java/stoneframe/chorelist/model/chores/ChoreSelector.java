package stoneframe.chorelist.model.chores;

import java.util.List;

public interface ChoreSelector
{
    List<Chore> selectChores(List<Chore> chores, int effort);
}
