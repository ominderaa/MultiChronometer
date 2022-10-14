package nl.motonono.multichronometer.chrono;

import static nl.motonono.multichronometer.model.Chronometer.ChronoState.CS_RUNNING;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.TransitionInflater;
import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import nl.motonono.multichronometer.ChronoManager;
import nl.motonono.multichronometer.R;
import nl.motonono.multichronometer.ParametersDialogFragment;
import nl.motonono.multichronometer.VibrationHelper;
import nl.motonono.multichronometer.databinding.FragmentTimeTrialBinding;
import nl.motonono.multichronometer.model.Chronometer;
import nl.motonono.multichronometer.utils.TimeFormatter;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class TimeTrialFragment extends Fragment {
    FragmentTimeTrialBinding mBinding;

    private ChronoManager mViewModel;
    List<ViewHolder> mChronoViews = new ArrayList<>();
    Timer mUpdateTimer;

    public TimeTrialFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(ChronoManager.class);

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
        setExitTransition(inflater.inflateTransition(R.transition.slide_left));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTimeTrialBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initChronoButtons(mViewModel);

        mBinding.btnStartChronos.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mViewModel.start();
                VibrationHelper.getInstance().vibrateOnClick();
                mBinding.btnStartChronos.setVisibility(View.GONE);
                mBinding.btnStopChronos.setVisibility(View.VISIBLE);
            }
        });

        mBinding.btnStopChronos.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mViewModel.stop();
                VibrationHelper.getInstance().vibrateOnClick();
            }
        });

        mBinding.btnResuls.setOnClickListener(v -> NavHostFragment.findNavController(TimeTrialFragment.this)
                .navigate(R.id.action_Navigate_to_OverviewFragment));

        int chronoRow = 0;
        int chronoCol = 0;

        WindowMetrics metrics = WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(requireActivity());
        float widthDp = metrics.getBounds().width(); /* /
                getResources().getDisplayMetrics().density; */

        int rowcount = mViewModel.getChronos().size() % 2 == 0 ?
                mViewModel.getChronos().size() / 2 : mViewModel.getChronos().size() / 2 + 1;
        LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
        mBinding.chronoContainer.removeAllViews();
        mBinding.chronoContainer.setColumnCount(2);
        mBinding.chronoContainer.setRowCount(rowcount);

        for (Chronometer chrono : mViewModel.getChronos()) {
            View chronoView = layoutInflater.inflate(R.layout.timedtrial_item, mBinding.chronoContainer, false);
            GridLayout.LayoutParams layoutParams =new GridLayout.LayoutParams();
            layoutParams.rightMargin = 5;
            layoutParams.topMargin = 5;
            layoutParams.setMargins(5, 5, 5, 5);
            layoutParams.setGravity(ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setGravity(Gravity.TOP);
            layoutParams.columnSpec = GridLayout.spec(chronoCol);
            layoutParams.rowSpec = GridLayout.spec(chronoRow);
            chronoView.setMinimumWidth((int)((widthDp-10) / 2));
            chronoView.setMinimumHeight(32);
            chronoView.setLayoutParams (layoutParams);
            mBinding.chronoContainer.addView(chronoView);
            mChronoViews.add(new ViewHolder(chronoView, chrono));
            chronoCol++;
            if(chronoCol > 1) {
                chronoRow++;
                chronoCol = 0;
            }
        }

        mUpdateTimer = new Timer();
        mUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mUpdateHandler.obtainMessage(1).sendToTarget();
            }
        }, 50, 100);
        mBinding.chronoContainer.requestLayout();
        showEditDialog();
    }

    private void showEditDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ParametersDialogFragment setparametersDialogFragment = ParametersDialogFragment.newInstance(true, true);
        setparametersDialogFragment.show(fm, "fragment_timed_trial_parameters");
    }

    private void initChronoButtons(ChronoManager chronoManager) {
        if (chronoManager.getManagerstate() == ChronoManager.ManagerState.IDLE) {
            mBinding.btnStopChronos.setVisibility(View.GONE);
        }
        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.RUNNING ) {
            mBinding.btnStartChronos.setVisibility(View.GONE);
            mBinding.btnStopChronos.setVisibility(View.VISIBLE);
        }
        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.HALTED ) {
            mBinding.btnStartChronos.setVisibility(View.GONE);
            mBinding.btnStopChronos.setVisibility(View.VISIBLE);
        }
    }

    private final Handler mUpdateHandler  = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            long currentTime = mViewModel.getCurrentTime();
            mBinding.txCurrentTime.setText(TimeFormatter.toTextLong(currentTime));
            for(TimeTrialFragment.ViewHolder holder : mChronoViews) {
                holder.tick();
            }
        }
    };

    private static class ViewHolder {
        public Chronometer mChronometer;
        public View mView;
        public TextView mChronoName;
        public TextView mLapCount;
        public TextView mLastLaptime;
        private Chronometer.ChronoState mLastState = Chronometer.ChronoState.CS_IDLE;

        public ViewHolder(View itemView,  Chronometer chronometer ) {
            this.mView = itemView;
            this.mChronometer = chronometer;
            this.mChronoName = itemView.findViewById(R.id.txChronoName);
            this.mLapCount =  itemView.findViewById(R.id.txLapcount);
            this.mLastLaptime = itemView.findViewById(R.id.txLastlap);
            mChronoName.setText(mChronometer.getName());
            mLapCount.setVisibility(View.INVISIBLE);
            mLastLaptime.setVisibility(View.INVISIBLE);
            mView.setBackgroundColor(ContextCompat.getColor(mView.getContext(), R.color.secondaryColor));
            mLastLaptime.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.secondaryTextColor));

            itemView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                if(mChronometer.getCurrentTime() > 0 ) {
                    VibrationHelper.getInstance().vibrateOnClick();
                    if (mChronometer.getState() == CS_RUNNING) {
                        mChronometer.lap();
                        lap();
                    }
                }
                }
            });
        }

        public void lap() {
            long currentTime = mChronometer.getLastLaptime();
            mLastLaptime.setText(TimeFormatter.toTextShort(currentTime));
            mLapCount.setText(String.format( Locale.getDefault(),"%d", mChronometer.getLapcount()));
        }

        public void tick() {
            if(mChronometer.getState() != Chronometer.ChronoState.CS_IDLE) {
                long currentTime = mChronometer.getLastLaptime();
                if (mChronometer.getLapcount() > 0) {
                    mLapCount.setVisibility(View.VISIBLE);
                    mLastLaptime.setVisibility(View.VISIBLE);
                    mLapCount.setText(String.format(Locale.getDefault(), "%d", mChronometer.getLapcount()));
                }
                if (mChronometer.getState() == Chronometer.ChronoState.CS_COUNTDOWN) {
                    mLastLaptime.setText(TimeFormatter.toTextSeconds(currentTime-1000));
                    if (currentTime < -4000 && currentTime > -5000) {
                        mLastLaptime.setVisibility(View.VISIBLE);
                    }
                }
                if (mChronometer.getState() == Chronometer.ChronoState.CS_RUNNING) {
                    if (mLastState == Chronometer.ChronoState.CS_COUNTDOWN ||
                            mLastState == Chronometer.ChronoState.CS_IDLE) {
                        mLastLaptime.setVisibility(View.INVISIBLE);
                        mLastLaptime.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.primaryTextColor));
                        mView.setBackgroundColor(ContextCompat.getColor(mView.getContext(), R.color.primaryColor));
                        mLastLaptime.setText(TimeFormatter.toTextSeconds(currentTime));
                    }
                    mLastState = mChronometer.getState();
                }
            }
        }
    }
}