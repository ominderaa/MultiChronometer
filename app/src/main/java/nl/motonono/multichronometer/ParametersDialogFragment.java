package nl.motonono.multichronometer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import nl.motonono.multichronometer.databinding.FragmentParametersDialogBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ParametersDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParametersDialogFragment extends DialogFragment {
    private FragmentParametersDialogBinding mBinding;
    private ChronoManager mViewModel;
    private boolean mShowStartInterval;
    private boolean mShowDuration;

    public ParametersDialogFragment() {
        // Required empty public constructor
    }

    public ParametersDialogFragment(boolean showStartInterval, boolean showDuration) {
        this.mShowStartInterval = showStartInterval;
        this.mShowDuration = showDuration;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TimedTrialParametersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ParametersDialogFragment newInstance(boolean showStartInterval, boolean showDuration) {
        ParametersDialogFragment fragment = new ParametersDialogFragment(showStartInterval, showDuration);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(ChronoManager.class);
        mBinding = FragmentParametersDialogBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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

        mBinding.groupDuration.setVisibility(mShowDuration ? View.VISIBLE : View.GONE);
        int duration = mViewModel.getTimedTrialDuration() / 5;
        mBinding.timedTrialDurationValue.setProgress(duration);

        mBinding.groupInterval.setVisibility(mShowStartInterval ? View.VISIBLE : View.GONE);
        int interval = mViewModel.getTimedStartInterval()/5;
        mBinding.timedtrialStartintervalValue.setProgress(interval);

        mBinding.submitButton.setOnClickListener(view1 -> {
            dismiss();
        });
    }

}