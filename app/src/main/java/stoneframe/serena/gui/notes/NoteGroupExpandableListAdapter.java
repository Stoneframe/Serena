package stoneframe.serena.gui.notes;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import stoneframe.serena.R;
import stoneframe.serena.notes.NoteGroupView;
import stoneframe.serena.notes.NoteManager;
import stoneframe.serena.notes.NoteView;

public class NoteGroupExpandableListAdapter extends BaseExpandableListAdapter
{
    private final Context context;
    private final NoteManager noteManager;

    public NoteGroupExpandableListAdapter(Context context, NoteManager noteManager)
    {
        this.context = context;
        this.noteManager = noteManager;
    }

    @Override
    public int getGroupCount()
    {
        return noteManager.getAllGroups().size();
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return noteManager.getAllGroups().get(groupPosition).getNotes().size();
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return noteManager.getAllGroups().get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return noteManager.getAllGroups().get(groupPosition).getNotes().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return getGroup(groupPosition).hashCode();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return getChild(groupPosition, childPosition).hashCode();
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

    @Override
    public View getGroupView(
        int groupPosition,
        boolean isExpanded,
        View view,
        ViewGroup parent)
    {
        NoteGroupView group = (NoteGroupView)getGroup(groupPosition);

        if (view == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.expandable_list_group, null);
        }

        TextView groupNameTextView = view.findViewById(R.id.listTitle);

        groupNameTextView.setTypeface(null, Typeface.BOLD);
        groupNameTextView.setText(group.getName());

        return view;
    }

    @Override
    public View getChildView(
        int groupPosition,
        int childPosition,
        boolean isLastChild,
        View view,
        ViewGroup parent)
    {
        NoteView note = (NoteView)getChild(groupPosition, childPosition);

        if (view == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.expandable_list_item, null);
        }

        TextView noteTitleTextView = view.findViewById(R.id.expandedListItem);

        noteTitleTextView.setText(note.getTitle());

        return view;
    }
}
