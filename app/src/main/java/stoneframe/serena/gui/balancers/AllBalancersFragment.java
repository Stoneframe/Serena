package stoneframe.serena.gui.balancers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
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

import java.util.Comparator;
import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.ButtonEnabledLink;
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
    private static final int LIGHT_RED = Color.parseColor("#ffc4c4");
    private static final int LIGHT_YELLOW = Color.parseColor("#fff08c");
    private static final int DARK_GREEN = Color.parseColor("#018a26");
    private static final int DARK_GRAY = Color.parseColor("#7e807e");
    private static final int DARK_RED = Color.parseColor("#ff0505");
    private static final int DARK_YELLOW = Color.parseColor("#ff8c00");

    private static final Pair<Integer, Integer> POSITIVE = new Pair<>(LIGHT_GREEN, DARK_GREEN);
    private static final Pair<Integer, Integer> NEGATIVE = new Pair<>(LIGHT_RED, DARK_RED);
    private static final Pair<Integer, Integer> COUNTER = new Pair<>(LIGHT_YELLOW, DARK_YELLOW);
    private static final Pair<Integer, Integer> DISABLED = new Pair<>(LIGHT_GRAY, DARK_GRAY);

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
            () -> balancerManager.getBalancers()
                .stream()
                .sorted(new BalancerComparator())
                .collect(Collectors.toList()),
            Balancer::getName)
            .withSecondaryTextFunction(this::getAvailableText)
            .withBottomTextFunction(this::getTimeToZeroText)
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

            new ButtonEnabledLink(
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
            String balancerName = balancerNameText.getText().toString().trim();

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
    private @NonNull String getAvailableText(Balancer balancer)
    {
        return balancer.isEnabled()
            ? String.format("Remaining: %d", balancer.getAvailable(LocalDateTime.now()))
            : "";
    }

    private @NonNull String getTimeToZeroText(Balancer balancer)
    {
        if (!balancer.isEnabled()) return "Disabled";

        LocalDateTime now = LocalDateTime.now();

        switch (balancer.getType())
        {
            case Balancer.ENHANCER:
                return getDepletedText(balancer, now);
            case Balancer.LIMITER:
                return getReplenishedText(balancer, now);
            case Balancer.COUNTER:
                return getCounterText();
            default:
                throw new IllegalStateException("Unknown balancer type: " + balancer.getType());
        }
    }

    private String getDepletedText(Balancer balancer, LocalDateTime now)
    {
        String when = balancer.getAvailable(now) > -1
            ? balancer.getTimeToZero(now).toString("yyyy-MM-dd HH:mm")
            : "Now";

        return String.format("Enhancer - Depleted: %s", when);
    }

    private String getReplenishedText(Balancer balancer, LocalDateTime now)
    {
        String when = balancer.getAvailable(now) < 1
            ? balancer.getTimeToZero(now).toString("yyyy-MM-dd HH:mm")
            : "Now";

        return String.format("Limiter - Replenished: %s", when);
    }

    private String getCounterText()
    {
        return "Counter";
    }

    private Integer getBackgroundColor(Balancer balancer)
    {
        return getColor(balancer).first;
    }

    private int getBorderColor(Balancer balancer)
    {
        return getColor(balancer).second;
    }

    private Pair<Integer, Integer> getColor(Balancer balancer)
    {
        if (!balancer.isEnabled()) return DISABLED;

        switch (balancer.getType())
        {
            case Balancer.COUNTER:
                return COUNTER;
            case Balancer.LIMITER:
            case Balancer.ENHANCER:
                return isBalancerGreaterThanZero(balancer) ? POSITIVE : NEGATIVE;
            default:
                return DISABLED;
        }
    }

    private static boolean isBalancerGreaterThanZero(Balancer balancer)
    {
        return balancer.getAvailable(LocalDateTime.now()) >= 0;
    }

    private static class BalancerComparator implements Comparator<Balancer>
    {
        @Override
        public int compare(Balancer balancer1, Balancer balancer2)
        {
            int compare;

            if ((compare = -Boolean.compare(balancer1.isEnabled(), balancer2.isEnabled())) != 0)
            {
                return compare;
            }

            if ((compare = Integer.compare(
                getOrder(balancer1.getType()),
                getOrder(balancer2.getType()))) != 0)
            {
                return compare;
            }

            return balancer1.getName().compareTo(balancer2.getName());
        }

        private static int getOrder(int balancerType)
        {
            switch (balancerType)
            {
                case Balancer.LIMITER:
                    return 0;
                case Balancer.ENHANCER:
                    return 1;
                case Balancer.COUNTER:
                    return 2;
                default:
                    return 3;
            }
        }
    }
}
