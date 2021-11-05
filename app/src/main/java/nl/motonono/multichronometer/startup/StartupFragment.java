package nl.motonono.multichronometer.startup;

import static nl.motonono.multichronometer.model.ChronoManager.RunMode.ALL_AT_ONCE;
import static nl.motonono.multichronometer.model.ChronoManager.RunMode.ONE_BY_ONE;
import static nl.motonono.multichronometer.model.ChronoManager.RunMode.TIMED_INTERVAL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import nl.motonono.multichronometer.R;
import nl.motonono.multichronometer.databinding.FragmentStartupBinding;
import nl.motonono.multichronometer.model.ChronoManager;
import nl.motonono.multichronometer.model.Chronometer;

public class StartupFragment extends Fragment {

    private FragmentStartupBinding binding;
    private StartupListAdapter startupListAdapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentStartupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChronoManager chronoManager = ViewModelProviders.of(getActivity()).get(ChronoManager.class);
        RecyclerView startupRecyclerView = (RecyclerView) view.findViewById(R.id.startuplistView);
        startupListAdapter = new StartupListAdapter(chronoManager);
        if(startupRecyclerView != null) {
            startupRecyclerView.setHasFixedSize(true);

            LinearLayoutManager layout = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            startupRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            startupRecyclerView.setAdapter(startupListAdapter);
        }

        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChronoManager chronoManager = ViewModelProviders.of(getActivity()).get(ChronoManager.class);
                chronoManager.getChronos().add(new Chronometer(chronoManager.getChronos().size()+1,
                        String.format("Chronometer %d", chronoManager.getChronos().size()+1)));
                startupListAdapter.notifyDataSetChanged();
                binding.startupViewTopLayout.requestLayout();
            }
        });

        binding.btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChronoManager chronoManager = ViewModelProviders.of(getActivity()).get(ChronoManager.class);
                chronoManager.reset();
                startupListAdapter.notifyDataSetChanged();
                binding.startupViewTopLayout.requestLayout();
            }
        });

        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChronoManager chronoManager = ViewModelProviders.of(getActivity()).get(ChronoManager.class);
                chronoManager.clear();
                startupListAdapter.notifyDataSetChanged();
                binding.startupViewTopLayout.requestLayout();
            }
        });

        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChronoManager chronoManager = ViewModelProviders.of(getActivity()).get(ChronoManager.class);
                int selectedId=binding.rgStartType.getCheckedRadioButtonId();
                switch( selectedId ) {
                    case R.id.rbStartAll:   chronoManager.setRunmode(ALL_AT_ONCE); break;
                    case R.id.rbStartOne:   chronoManager.setRunmode(ONE_BY_ONE); break;
                    case R.id.rbStartTimed: chronoManager.setRunmode(TIMED_INTERVAL); break;
                }
                chronoManager.reset();
//                NavHostFragment.findNavController(StartupFragment.this)
//                        .navigate(R.id.action_StartupFragment_to_RunFragment);
                NavHostFragment.findNavController(StartupFragment.this)
                        .navigate(R.id.action_StartupFragment_to_ChronoFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        startupListAdapter = null;
    }

}