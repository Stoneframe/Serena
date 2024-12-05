package stoneframe.serena.gui.balancers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDateTime;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.EditTextButtonEnabledLink;
import stoneframe.serena.gui.util.EditTextCriteria;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.balancers.Balancer;
import stoneframe.serena.model.balancers.BalancerManager;

public class AllBalancersFragment extends Fragment
{
    private static final int LIGHT_GREEN = Color.parseColor("#c3fab6");
    private static final int LIGHT_GRAY = Color.parseColor("#e6e3e3");
    private static final int DARK_GREEN = Color.parseColor("#018a26");
    private static final int DARK_GRAY = Color.parseColor("#7e807e");

    private SimpleListAdapter<Balancer> balancerListAdapter;

    private GlobalState globalState;
    private Serena serena;
    private BalancerManager balancerManager;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();
        serena = globalState.getSerena();
        balancerManager = serena.getBalancerManager();

        View rootView = inflater.inflate(R.layout.fragment_all_balancers, container, false);

        balancerListAdapter = new SimpleListAdapterBuilder<>(
            requireContext(),
            balancerManager::getBalancers,
            Balancer::getName)
            .withSecondaryTextFunction(this::getAvailableText)
            .withBottomTextFunction(this::getReplenishedText)
            .withBackgroundColorFunction(this::getBackgroundColor)
            .withBorderColorFunction(this::getBorderColor)
            .create();

        ListView balancerListView = rootView.findViewById(R.id.all_balancers);
        balancerListView.setAdapter(balancerListAdapter);
        balancerListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Balancer balancer = (Balancer)balancerListAdapter.getItem(position);

            openBalancerActivity(balancer);
        });

        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            final EditText balancerNameText = new EditText(getContext());

            balancerNameText.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

            AlertDialog.Builder builder = getBuilder(balancerNameText);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            new EditTextButtonEnabledLink(
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE),
                new EditTextCriteria(balancerNameText, EditTextCriteria.IS_NOT_EMPTY));
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        balancerListAdapter.notifyDataSetChanged();
    }

    @NonNull
    private AlertDialog.Builder getBuilder(EditText balancerNameText)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create balancer");
        builder.setView(balancerNameText);

        builder.setPositiveButton("OK", (dialog, which) ->
        {
            String balancerName = balancerNameText.getText().toString();

            Balancer balancer = balancerManager.createBalancer(balancerName);

            serena.save();

            balancerListAdapter.notifyDataSetChanged();

            openBalancerActivity(balancer);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder;
    }

    private void openBalancerActivity(Balancer balancer)
    {
        globalState.setActiveBalancer(balancer);

        Intent intent = new Intent(requireContext(), BalanceActivity.class);
        startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    private @NonNull String getAvailableText(Balancer l)
    {
        return String.format("Remaining: %d", l.getAvailable(LocalDateTime.now()));
    }

    private @NonNull String getReplenishedText(Balancer l)
    {
        LocalDateTime now = LocalDateTime.now();

        String when = l.getAvailable(now) < 1
            ? l.getReplenishTime(now).toString("yyyy-MM-dd HH:mm")
            : "Now";

        return String.format("Replenished: %s", when);
    }

    private Integer getBackgroundColor(Balancer balancer)
    {
        return isBalancerReplenished(balancer) ? LIGHT_GREEN : LIGHT_GRAY;
    }

    private int getBorderColor(Balancer balancer)
    {
        return isBalancerReplenished(balancer) ? DARK_GREEN : DARK_GRAY;
    }

    private static boolean isBalancerReplenished(Balancer balancer)
    {
        return balancer.getAvailable(LocalDateTime.now()) > 0;
    }
}
