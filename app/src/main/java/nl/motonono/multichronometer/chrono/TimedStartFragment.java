package nl.motonono.multichronometer.chrono;

import static java.lang.StrictMath.abs;
import static nl.motonono.multichronometer.ChronoManager.RunMode.ONE_BY_ONE;
import static nl.motonono.multichronometer.model.Chronometer.ChronoState.CS_IDLE;
import static nl.motonono.multichronometer.model.Chronometer.ChronoState.CS_RUNNING;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.transition.TransitionInflater;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import nl.motonono.multichronometer.ChronoManager;
import nl.motonono.multichronometer.R;
import nl.motonono.multichronometer.VibrationHelper;
import nl.motonono.multichronometer.databinding.FragmentTimedStartBinding;
import nl.motonono.multichronometer.model.Chronometer;

public class TimedStartFragment extends Fragment implements ChronoManager.StateChangeListener {
    FragmentTimedStartBinding mBinding;
    private ChronoManager mViewModel;

    List<TimedStartFragment.ViewHolder> mChronoViews = new ArrayList<>();
    Timer mUpdateTimer;

    public TimedStartFragment() {}

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
        mBinding = FragmentTimedStartBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initChronoButtons(mViewModel);

        mBinding.btnStartChronos.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mViewModel.start();
                VibrationHelper.getInstance().vibrateOnClick();
                if(mViewModel.getRunmode() != ONE_BY_ONE) {
                    mBinding.btnStartChronos.setVisibility(View.GONE);
                    mBinding.btnStopChronos.setVisibility(View.VISIBLE);
                }
            }
        });
        mBinding.btnStopChronos.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mViewModel.stop();
                VibrationHelper.getInstance().vibrateOnClick();
            }
        });
        mBinding.btnResuls.setOnClickListener(v -> NavHostFragment.findNavController(TimedStartFragment.this)
                .navigate(R.id.action_Navigate_to_OverviewFragment));
        mViewModel.setStateChangeListener(this);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireActivity());
        boolean lefthanded = sharedPreferences.getBoolean("lefthandedui", false);
        LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());

        int chronoNumber = 0;
        for (Chronometer chrono: mViewModel.getChronos() ) {
            View listItem;
            if(lefthanded) {
                listItem = layoutInflater.inflate(R.layout.chrono_item_left, mBinding.chronoContainer, false);
            } else {
                listItem = layoutInflater.inflate(R.layout.chrono_item_right, mBinding.chronoContainer, false);
            }
            mBinding.chronoContainer.addView(listItem);

            TimedStartFragment.ViewHolder holder = new ViewHolder(listItem, chrono);
            mChronoViews.add(chronoNumber, holder);
        }
        mUpdateTimer = new Timer();
        mUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mUpdateHandler.obtainMessage(1).sendToTarget();
            }
        }, 50, 100);
        mBinding.chronoContainer.requestLayout();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mUpdateTimer = null;
        mViewModel.removeStateChangeListener(this);
        Log.i("onDestroyView", "View destroyed");
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mUpdateTimer != null)
            mUpdateTimer.cancel();
        mViewModel.removeStateChangeListener(this);
        Log.i("onPause", "View paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mUpdateTimer != null)
            mUpdateTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mUpdateHandler.obtainMessage(1).sendToTarget();
                }
            }, 50, 100);
        Log.i("onResume", "View resumed");
        mViewModel.setStateChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
        Log.i("onStop", "View stopped");
        mViewModel.removeStateChangeListener(this);
    }

    private void initChronoButtons(ChronoManager chronoManager) {
        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.IDLE ) {
            mBinding.btnStopChronos.setVisibility(View.GONE);
            mBinding.btnStartChronos.setVisibility(View.VISIBLE);
            mBinding.btnResuls.setVisibility(View.GONE);
        }
        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.RUNNING ) {
            mBinding.btnStartChronos.setVisibility(View.GONE);
            mBinding.btnStopChronos.setVisibility(View.VISIBLE);
            mBinding.btnResuls.setVisibility(View.GONE);
        }
        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.HALTED ) {
            mBinding.btnStartChronos.setVisibility(View.GONE);
            mBinding.btnStopChronos.setVisibility(View.GONE);
            mBinding.btnResuls.setVisibility(View.VISIBLE);
        }
    }

    private final Handler mUpdateHandler  = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            for(TimedStartFragment.ViewHolder holder : mChronoViews) {
                holder.tick();
            }
        }
    };

    @Override
    public void onStateChange(ChronoManager.ManagerState newState) {
        initChronoButtons(mViewModel);
    }

    private static class ViewHolder  extends ChronoItemHolder {

        public ViewHolder(View itemView, Chronometer chronometer) {
            super(itemView, chronometer);
            mChronoName.setText(chronometer.getName());

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

        void updateHolderUI() {
            if (mChronometer.getState() == Chronometer.ChronoState.CS_IDLE) {
                mStartbutton.setVisibility(View.INVISIBLE);
                mLapbutton.setVisibility(View.GONE);
            } else if (mChronometer.getState() == Chronometer.ChronoState.CS_RUNNING) {
                mStartbutton.setVisibility(View.GONE);
                mLapbutton.setVisibility(View.VISIBLE);
            } else if (mChronometer.getState() == Chronometer.ChronoState.CS_HALTED) {
                mStartbutton.setVisibility(View.GONE);
                mLapbutton.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        void onLap() { /* nothing to do */ }
    }

}