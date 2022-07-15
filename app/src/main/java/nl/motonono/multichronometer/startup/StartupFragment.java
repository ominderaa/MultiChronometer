package nl.motonono.multichronometer.startup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import java.util.Locale;

import nl.motonono.multichronometer.R;
import nl.motonono.multichronometer.databinding.FragmentStartupBinding;
import nl.motonono.multichronometer.model.ChronoManager;
import nl.motonono.multichronometer.model.Chronometer;

public class StartupFragment extends Fragment {

    private FragmentStartupBinding binding;
    private StartupListAdapter startupListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
        setExitTransition(inflater.inflateTransition(R.transition.slide_left));
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentStartupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChronoManager chronoManager = ViewModelProviders.of(requireActivity()).get(ChronoManager.class);
        RecyclerView startupRecyclerView = view.findViewById(R.id.startuplistView);
        startupListAdapter = new StartupListAdapter(chronoManager);
        if(startupRecyclerView != null) {
            startupRecyclerView.setHasFixedSize(true);
            startupRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
            startupRecyclerView.setAdapter(startupListAdapter);
        }

        binding.btnAdd.setOnClickListener(v -> {
            ChronoManager chronoManager1 = ViewModelProviders.of(requireActivity()).get(ChronoManager.class);
            chronoManager1.getChronos().add(new Chronometer(chronoManager1.getChronos().size()+1,
                    String.format(Locale.getDefault(), "Chronometer %d", chronoManager1.getChronos().size()+1)));
            startupListAdapter.notifyDataSetChanged();
            binding.startupViewTopLayout.requestLayout();
        });

        binding.btnReset.setOnClickListener(v -> {
            ChronoManager chronoManager12 = ViewModelProviders.of(requireActivity()).get(ChronoManager.class);
            chronoManager12.reset();
            startupListAdapter.notifyDataSetChanged();
            binding.startupViewTopLayout.requestLayout();
        });

        binding.btnClear.setOnClickListener(v -> {
            ChronoManager chronoManager13 = ViewModelProviders.of(requireActivity()).get(ChronoManager.class);
            chronoManager13.clear();
            startupListAdapter.notifyDataSetChanged();
            binding.startupViewTopLayout.requestLayout();
        });

        binding.btnStartAll.setOnClickListener(view1 -> {
            ChronoManager chronoManager14 = ViewModelProviders.of(requireActivity()).get(ChronoManager.class);
            chronoManager14.setRunmode(ChronoManager.RunMode.ALL_AT_ONCE);
            chronoManager14.reset();
            NavHostFragment.findNavController(StartupFragment.this)
                    .navigate(R.id.action_StartupFragment_to_ChronoFragment);
        });
        binding.btnStartOneByOne.setOnClickListener(view1 -> {
            ChronoManager chronoManager14 = ViewModelProviders.of(requireActivity()).get(ChronoManager.class);
            chronoManager14.setRunmode(ChronoManager.RunMode.ONE_BY_ONE);
            chronoManager14.reset();
            NavHostFragment.findNavController(StartupFragment.this)
                    .navigate(R.id.action_StartupFragment_to_ChronoFragment);
        });
        binding.btnStartInterval.setOnClickListener(view1 -> {
            ChronoManager chronoManager14 = ViewModelProviders.of(requireActivity()).get(ChronoManager.class);
            chronoManager14.setRunmode(ChronoManager.RunMode.INTERVAL);
            chronoManager14.reset();
            NavHostFragment.findNavController(StartupFragment.this)
                    .navigate(R.id.action_StartupFragment_to_ChronoFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        startupListAdapter = null;
    }

}