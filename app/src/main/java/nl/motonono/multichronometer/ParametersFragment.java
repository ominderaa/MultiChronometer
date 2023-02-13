package nl.motonono.multichronometer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import nl.motonono.multichronometer.databinding.FragmentParametersBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class ParametersFragment extends Fragment {
    FragmentParametersBinding mBinding;
    private ChronoManager mViewModel;
    private boolean mShowStartInterval;
    private boolean mShowDuration;
    private boolean mShowPredict;


    public ParametersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(ChronoManager.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(ChronoManager.class);
        mBinding = FragmentParametersBinding.inflate(inflater, container, false);

        switch ( mViewModel.getRunmode()) {
            case ALL_AT_ONCE:
            case ONE_BY_ONE:
                // should not happen
                mShowDuration = false;
                mShowStartInterval = false;
                mShowPredict = false;
                break;
            case INTERVAL:
                mShowDuration = false;
                mShowStartInterval = true;
                mShowPredict = false;
                break;
            case TIMED_TRIAL:
                mShowDuration = true;
                mShowStartInterval = true;
                mShowPredict = true;
                break;
        }
        return mBinding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.timedtrialStartintervalValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int interval = seekBar.getProgress() * 5;
                mBinding.timedtrialStartintervalText.setText(String.format("%d seconds", interval));
                mViewModel.setTimedStartInterval(interval);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                int interval = seekBar.getProgress() * 5;
                mBinding.timedtrialStartintervalText.setText(String.format("%d seconds", interval));
                mViewModel.setTimedStartInterval(interval);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int interval = seekBar.getProgress() * 5;
                mBinding.timedtrialStartintervalText.setText(String.format("%d seconds", interval));
                mViewModel.setTimedStartInterval(interval);
            }
        });

        mBinding.timedTrialDurationValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int interval = seekBar.getProgress() * 5;
                mBinding.timedTrialDurationText.setText(String.format("%d minutes", interval));
                mViewModel.setTimedTrialDuration(interval);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                int interval = seekBar.getProgress() * 5;
                mBinding.timedTrialDurationText.setText(String.format("%d minutes", interval));
                mViewModel.setTimedTrialDuration(interval);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int interval = seekBar.getProgress() * 5;
                mBinding.timedTrialDurationText.setText(String.format("%d minutes", interval));
                mViewModel.setTimedTrialDuration(interval);
            }
        });

        mBinding.swPredictAthlete.setOnCheckedChangeListener((compoundButton, b) -> {
            mViewModel.setPredictAthlete(b);
            mBinding.textView3.setText(b ? mBinding.swPredictAthlete.getTextOn() : mBinding.swPredictAthlete.getTextOff());
        });

        mBinding.groupDuration.setVisibility(mShowDuration ? View.VISIBLE : View.GONE);
        int duration = mViewModel.getTimedTrialDuration() / 5;
        mBinding.timedTrialDurationValue.setProgress(duration);

        mBinding.groupInterval.setVisibility(mShowStartInterval ? View.VISIBLE : View.GONE);
        int interval = mViewModel.getTimedStartInterval()/5;
        mBinding.timedtrialStartintervalValue.setProgress(interval);

        mBinding.groupPredictSwimmer.setVisibility(mShowPredict ? View.VISIBLE : View.GONE);

        mBinding.submitButton.setOnClickListener(view1 -> {
            switch(mViewModel.getRunmode()) {
                case ALL_AT_ONCE:
                case ONE_BY_ONE:
                    break; // should not happen
                case TIMED_TRIAL:
                    NavHostFragment.findNavController(ParametersFragment.this)
                            .navigate(R.id.action_parametersFragment_to_timeTrialFragment);
                    break;
                case INTERVAL:
                    NavHostFragment.findNavController(ParametersFragment.this)
                            .navigate(R.id.action_parametersFragment_to_timedStartFragment);
                    break;
            }
        });
    }

}