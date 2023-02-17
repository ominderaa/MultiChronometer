package nl.motonono.multichronometer.chrono;

import static nl.motonono.multichronometer.model.Chronometer.ChronoState.CS_IDLE;
import static nl.motonono.multichronometer.model.Chronometer.ChronoState.CS_RUNNING;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.transition.TransitionInflater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import nl.motonono.multichronometer.ChronoManager;
import nl.motonono.multichronometer.R;
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
    Timer mPredictionTimer;

    public TimeTrialFragment() {
    }

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
        initChronoButtons(mViewModel);

        mBinding.btnStartChronos.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mViewModel.start();
                VibrationHelper.getInstance().vibrateOnClick();
                initChronoButtons(mViewModel);
            }
        });

        mBinding.btnStopChronos.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mViewModel.stop();
                VibrationHelper.getInstance().vibrateOnClick();
                initChronoButtons(mViewModel);
            }
        });

        mBinding.btnResuls.setOnClickListener(v -> NavHostFragment.findNavController(TimeTrialFragment.this)
                .navigate(R.id.action_Navigate_to_OverviewFragment));

        int chronoRow = 0;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireActivity());
        boolean lefthanded = sharedPreferences.getBoolean("lefthandedui", false);

        mBinding.chronoContainer.removeAllViews();
        mBinding.chronoContainer.setRowCount(mViewModel.getChronos().size());

        for (Chronometer chrono : mViewModel.getChronos()) {
            View chronoView;
            if (lefthanded) {
                chronoView = inflater.inflate(R.layout.chrono_item_left, mBinding.chronoContainer, false);
            } else {
                chronoView = inflater.inflate(R.layout.chrono_item_right, mBinding.chronoContainer, false);
            }

            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.rightMargin = 5;
            layoutParams.topMargin = 5;
            layoutParams.setMargins(5, 5, 5, 5);
            layoutParams.setGravity(ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setGravity(Gravity.FILL);
            layoutParams.columnSpec = GridLayout.spec(0, 1.0f);
            layoutParams.rowSpec = GridLayout.spec(chronoRow, 1.0f);
            chronoView.setLayoutParams(layoutParams);
            mBinding.chronoContainer.addView(chronoView);
            mChronoViews.add(new ViewHolder(this, chronoView, chrono));
            chronoRow++;
        }
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

        mUpdateTimer = new Timer();
        mUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mUpdateHandler.obtainMessage(1).sendToTarget();
            }
        }, 50, 100);
//        mPredictionTimer = new Timer();
//        mPredictionTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                mPredictionHandler.obtainMessage(1).sendToTarget();
//            }
//        }, 50, 1000);
        mBinding.chronoContainer.requestLayout();
    }

    private void reorderViews() {
        mChronoViews.sort(Comparator.comparing(ViewHolder::getOrder));
        int chronoRow = 0;
        for (ViewHolder vh : mChronoViews) {
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.rightMargin = 5;
            layoutParams.topMargin = 5;
            layoutParams.setMargins(5, 5, 5, 5);
            layoutParams.setGravity(ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setGravity(Gravity.FILL);
            layoutParams.columnSpec = GridLayout.spec(0, 1.0f);
            layoutParams.rowSpec = GridLayout.spec(chronoRow, 1.0f);
            vh.mItemView.setLayoutParams(layoutParams);
            chronoRow++;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUpdateTimer = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mUpdateTimer != null)
            mUpdateTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mUpdateTimer = new Timer();
        mUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mUpdateHandler.obtainMessage(1).sendToTarget();
            }
        }, 50, 100);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    private void initChronoButtons(ChronoManager chronoManager) {
        if (chronoManager.getManagerstate() == ChronoManager.ManagerState.IDLE) {
            mBinding.btnStartChronos.setVisibility(View.VISIBLE);
            mBinding.btnStopChronos.setVisibility(View.GONE);
            mBinding.btnResuls.setVisibility(View.GONE);
        }
        if (chronoManager.getManagerstate() == ChronoManager.ManagerState.RUNNING) {
            mBinding.btnStartChronos.setVisibility(View.GONE);
            mBinding.btnStopChronos.setVisibility(View.VISIBLE);
            mBinding.btnResuls.setVisibility(View.GONE);
        }
        if (chronoManager.getManagerstate() == ChronoManager.ManagerState.HALTED) {
            mBinding.btnStartChronos.setVisibility(View.GONE);
            mBinding.btnStopChronos.setVisibility(View.GONE);
            mBinding.btnResuls.setVisibility(View.VISIBLE);
        }
    }

    private final Handler mUpdateHandler = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            long currentTime = mViewModel.getCurrentTime();
            mBinding.txCurrentTime.setText(TimeFormatter.toTextLong(currentTime));
            for (TimeTrialFragment.ViewHolder holder : mChronoViews) {
                holder.tick();
            }
        }
    };

//    private final Handler mPredictionHandler = new Handler(Looper.myLooper()) {
//        public void handleMessage(Message msg) {
//            mViewModel.getChronos().forEach(m -> {
//                Log.i("LapPrediction", String.format("%s predicted to lap in %d msec", m.getName(), m.getNextlapPrediction()));
//            });
//        }
//    };

    private static class ViewHolder extends ChronoItemHolder {
        private final TimeTrialFragment mParentFragment;

        public ViewHolder(TimeTrialFragment parentFragment, View itemView, Chronometer chronometer) {
            super(itemView, chronometer);
            mParentFragment = parentFragment;

            mStartbutton.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    VibrationHelper.getInstance().vibrateOnClick();
                    if (mChronometer.getState() == CS_IDLE) {
                        mChronometer.startFuture(SystemClock.elapsedRealtime());
                    }
                }
            });

            mLapbutton.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    VibrationHelper.getInstance().vibrateOnClick();
                    if (mChronometer.getState() == CS_RUNNING) {
                        mChronometer.lap();
                        lap();
                    }
                }
            });
        }

        @Override
        void onLap() {
            mParentFragment.reorderViews();
        }

        void updateHolderUI() {
            if (mChronometer.getState() == Chronometer.ChronoState.CS_IDLE) {
                mStartbutton.setVisibility(View.INVISIBLE);
                mLapbutton.setVisibility(View.GONE);
                mCountdown.setVisibility(View.GONE);
            } else if (mChronometer.getState() == Chronometer.ChronoState.CS_RUNNING) {
                mStartbutton.setVisibility(View.GONE);
                mLapbutton.setVisibility(View.VISIBLE);
                mCountdown.setVisibility(View.GONE);
            } else if (mChronometer.getState() == Chronometer.ChronoState.CS_COUNTDOWN) {
                mStartbutton.setVisibility(View.GONE);
                mLapbutton.setVisibility(View.GONE);
                mCountdown.setVisibility(View.VISIBLE);
            } else if (mChronometer.getState() == Chronometer.ChronoState.CS_HALTED) {
                mStartbutton.setVisibility(View.GONE);
                mLapbutton.setVisibility(View.GONE);
                mCountdown.setVisibility(View.GONE);
            }
        }

        public long getOrder() {
            return mChronometer.getNextlapPrediction();
        }
    }
}