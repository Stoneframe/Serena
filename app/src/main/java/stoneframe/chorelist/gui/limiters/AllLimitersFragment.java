package stoneframe.chorelist.gui.limiters;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import org.joda.time.LocalDateTime;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.SimpleListAdapter;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.limiters.Limiter;

public class AllLimitersFragment extends Fragment
{
    private SimpleListAdapter<Limiter> limiterListAdapter;

    private GlobalState globalState;
    private ChoreList choreList;

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();
        choreList = globalState.getChoreList();

        View rootView = inflater.inflate(R.layout.fragment_all_limiters, container, false);

        limiterListAdapter = new SimpleListAdapter<>(
            requireContext(),
            choreList::getLimiters,
            l -> format("%s (%d)", l.getName(), l.getAvailable(LocalDateTime.now())));
        ListView limiterListView = rootView.findViewById(R.id.all_limiters);
        limiterListView.setAdapter(limiterListAdapter);
        limiterListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Limiter limiter = (Limiter)limiterListAdapter.getItem(position);

            openLimiterActivity(limiter);
        });

        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            final EditText limiterNameText = new EditText(getContext());

            limiterNameText.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Create limiter");
            builder.setView(limiterNameText);

            builder.setPositiveButton("OK", (dialog, which) ->
            {
                String limiterName = limiterNameText.getText().toString();

                Limiter limiter = choreList.createLimiter(limiterName);

                choreList.save();

                limiterListAdapter.notifyDataSetChanged();

                openLimiterActivity(limiter);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            new EditTextButtonEnabledLink(
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE),
                new EditTextCriteria(limiterNameText, EditTextCriteria.IS_NOT_EMPTY));
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        limiterListAdapter.notifyDataSetChanged();
    }

    private void openLimiterActivity(Limiter limiter)
    {
        globalState.setActiveLimiter(limiter);

        Intent intent = new Intent(requireContext(), LimiterActivity.class);
        startActivity(intent);
    }
}
