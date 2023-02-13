package nl.motonono.multichronometer.startup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import java.util.Locale;

import nl.motonono.multichronometer.ChronoManager;
import nl.motonono.multichronometer.R;
import nl.motonono.multichronometer.databinding.FragmentStartupBinding;
import nl.motonono.multichronometer.model.Chronometer;

public class StartupFragment extends Fragment {

    private FragmentStartupBinding mBinding;
    private StartupListAdapter mListAdapter;
    private ChronoManager mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(ChronoManager.class);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
        setExitTransition(inflater.inflateTransition(R.transition.slide_left));
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        mBinding = FragmentStartupBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChronoManager ChronoManager = ViewModelProviders.of(requireActivity()).get(ChronoManager.class);
        RecyclerView startupRecyclerView = view.findViewById(R.id.startuplistView);
        mListAdapter = new StartupListAdapter(ChronoManager);
        if(startupRecyclerView != null) {
            startupRecyclerView.setHasFixedSize(true);
            startupRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
            startupRecyclerView.setAdapter(mListAdapter);
        }

        mBinding.btnAdd.setOnClickListener(v -> {
            mViewModel.add(new Chronometer(mViewModel.getChronos().size()+1,
                    String.format(Locale.getDefault(), "Chronometer %d", mViewModel.getChronos().size()+1)));
            mListAdapter.notifyDataSetChanged();
            mListAdapter.notifyItemInserted(mViewModel.getChronos().size()-1);
            mBinding.startupViewTopLayout.requestLayout();
        });

        mBinding.btnReset.setOnClickListener(v -> {
            mViewModel.reset();
            mListAdapter.notifyDataSetChanged();
            mBinding.startupViewTopLayout.requestLayout();
        });

        mBinding.btnClear.setOnClickListener(v -> {
            mViewModel.clear();
            mListAdapter.notifyDataSetChanged();
            mBinding.startupViewTopLayout.requestLayout();
        });

        mBinding.btnStart.setOnClickListener(view1 -> {
            mViewModel.reset();

            switch(mViewModel.getRunmode()) {
                case ALL_AT_ONCE:
                    NavHostFragment.findNavController(StartupFragment.this)
                            .navigate(R.id.action_StartupFragment_to_oneStartFragment);
                    break;
                case INTERVAL:
                case TIMED_TRIAL:
                    NavHostFragment.findNavController(StartupFragment.this)
                            .navigate(R.id.action_StartupFragment_to_parametersFragment);
                    break;
                case ONE_BY_ONE:
                    NavHostFragment.findNavController(StartupFragment.this)
                            .navigate(R.id.action_StartupFragment_to_independentStartFragment);
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mListAdapter = null;
    }

}