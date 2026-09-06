package stoneframe.serena.gui.balancers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDateTime;

import java.util.Comparator;
import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.enable.ButtonEnabledLink;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.Serena;
import stoneframe.serena.balancers.Balancer;
import stoneframe.serena.balancers.BalancerManager;

public class AllBalancersFragment extends Fragment
{
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

            Button okButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            DialogUtils.addButtonStartSpacing(requireContext(), okButton);
            DialogUtils.alignButtonEndWithDialogContent(requireContext(), okButton);
            DialogUtils.styleAsPrimaryButton(requireContext(), okButton);

            new ButtonEnabledLink(
                okButton,
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

        int horizontalInset = getResources().getDimensionPixelSize(R.dimen.space_lg);
        FrameLayout inputContainer = new FrameLayout(requireContext());
        inputContainer.setPadding(horizontalInset, 0, horizontalInset, 0);
        inputContainer.addView(
            balancerNameText,
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        builder.setView(inputContainer);

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
        LocalDateTime when = balancer.getTimeToBoundary(now);

        if (when.isAfter(now))
        {
            return String.format("Enhancer - Depleted: %s", when.toString("yyyy-MM-dd HH:mm"));
        }

        return "Now";
    }

    private String getReplenishedText(Balancer balancer, LocalDateTime now)
    {
        LocalDateTime when = balancer.getTimeToBoundary(now);

        if (when.isAfter(now))
        {
            return String.format("Limiter - Replenished: %s", when.toString("yyyy-MM-dd HH:mm"));
        }

        return "Now";
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
        if (!balancer.isEnabled())
        {
            return colorPair(R.color.status_disabled_container, R.color.status_disabled);
        }

        switch (balancer.getType())
        {
            case Balancer.COUNTER:
                return colorPair(R.color.status_warning_container, R.color.status_warning);
            case Balancer.LIMITER:
            case Balancer.ENHANCER:
                return balancer.isAboveThreshold(LocalDateTime.now())
                    ? colorPair(
                        R.color.balancer_success_container,
                        R.color.balancer_success)
                    : colorPair(
                        R.color.balancer_error_container,
                        R.color.balancer_error);
            default:
                return colorPair(R.color.status_disabled_container, R.color.status_disabled);
        }
    }

    private Pair<Integer, Integer> colorPair(
        @ColorRes int backgroundColor,
        @ColorRes int borderColor)
    {
        return new Pair<>(
            ContextCompat.getColor(requireContext(), backgroundColor),
            ContextCompat.getColor(requireContext(), borderColor));
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
