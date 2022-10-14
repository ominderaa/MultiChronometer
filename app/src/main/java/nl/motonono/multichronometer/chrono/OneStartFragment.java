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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.transition.TransitionInflater;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import nl.motonono.multichronometer.ChronoManager;
import nl.motonono.multichronometer.R;
import nl.motonono.multichronometer.VibrationHelper;
import nl.motonono.multichronometer.databinding.FragmentOneStartBinding;
import nl.motonono.multichronometer.model.Chronometer;
import nl.motonono.multichronometer.utils.TimeFormatter;

public class OneStartFragment extends Fragment {
    FragmentOneStartBinding mBinding;
    private ChronoManager mViewModel;

    List<OneStartFragment.ViewHolder> mChronoViews = new ArrayList<>();
    Timer mUpdateTimer;

    public OneStartFragment() {
        // Required empty public constructor
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
        mBinding = FragmentOneStartBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initChronoView(mViewModel);

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

        mBinding.btnResuls.setOnClickListener(v -> NavHostFragment.findNavController(OneStartFragment.this)
                .navigate(R.id.action_Navigate_to_OverviewFragment));

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

            OneStartFragment.ViewHolder holder = new ViewHolder(listItem, mViewModel, chrono);
            mChronoViews.add(chronoNumber, holder);
            holder.mChronoName.setText(chrono.getName());

            holder.mStartbutton.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    VibrationHelper.getInstance().vibrateOnClick();
                    if (holder.mChronometer.getState() == CS_IDLE) {
                        holder.mChronometer.startAt(SystemClock.elapsedRealtime());
                    }
                }
            });

            holder.mLapbutton.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    VibrationHelper.getInstance().vibrateOnClick();
                    if (holder.mChronometer.getState() == CS_RUNNING) {
                        holder.mChronometer.lap();
                        holder.lap();
                    }
                }
            });
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
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mUpdateTimer != null)
            mUpdateTimer.cancel();
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    private void initChronoView(ChronoManager chronoManager) {
        Button btnStop = mBinding.getRoot().findViewById(R.id.btnStopChronos);
        Button btnStartChrono = mBinding.getRoot().findViewById(R.id.btnStartChronos);

        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.IDLE ) {
            btnStop.setVisibility(View.GONE);

            switch (chronoManager.getRunmode()) {
                case ALL_AT_ONCE:
                case INTERVAL:
                    btnStartChrono.setVisibility(View.VISIBLE);
                    break;
                case ONE_BY_ONE:
                    btnStartChrono.setVisibility(View.INVISIBLE);
                    break;
            }
        }
        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.RUNNING ) {
            btnStartChrono.setVisibility(View.GONE);
            btnStop.setVisibility(View.VISIBLE);
        }
        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.HALTED ) {
            btnStartChrono.setVisibility(View.GONE);
            btnStop.setVisibility(View.VISIBLE);
        }
    }

    private final Handler mUpdateHandler  = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            for(OneStartFragment.ViewHolder holder : mChronoViews) {
                holder.tick();
            }
        }
    };

    private static class ViewHolder {
        public ChronoManager chronoManager;
        public Chronometer mChronometer;
        public TextView mChronoName;
        public TextView mLapCount;
        public TextView mLastLaptime;
        public TextView mTotalTime;
        public TextView mCurrentTime;
        public Button mStartbutton;
        public Button mLapbutton;

        public ViewHolder(View itemView, ChronoManager chronoManager, Chronometer chronometer) {
            this.chronoManager = chronoManager;
            this.mChronometer = chronometer;
            this.mChronoName = itemView.findViewById(R.id.txChronoName);
            this.mLapCount =  itemView.findViewById(R.id.txLapcount);
            this.mLastLaptime = itemView.findViewById(R.id.txLastlap);
            this.mTotalTime = itemView.findViewById(R.id.txTotalTime);
            this.mCurrentTime = itemView.findViewById(R.id.txCurrentTime);
            this.mStartbutton = itemView.findViewById(R.id.btnStart);
            this.mLapbutton = itemView.findViewById(R.id.btnLap);
            updateHolderUI();
        }

        public void tick() {
            long currentTime = mChronometer.getCurrentTime();
            if(currentTime < 0) {
                mCurrentTime.setText(TimeFormatter.toTextSeconds(abs(currentTime)+1000));
            }
            else {
                mCurrentTime.setText(TimeFormatter.toTextShort(currentTime));
            }
            mLapCount.setText(String.format( Locale.getDefault(),"Laps %02d", mChronometer.getLapcount()));
            updateHolderUI();
        }

        public void lap() {
            mLapCount.setText(String.format( Locale.getDefault(),"Laps %02d", mChronometer.getLapcount()));
            mLastLaptime.setText(TimeFormatter.toTextShort(mChronometer.getLastLaptime()));
            mTotalTime.setText(TimeFormatter.toTextShort(mChronometer.getCurrentTime()));
        }

        private void updateHolderUI() {
            if(chronoManager.getRunmode() == ChronoManager.RunMode.ALL_AT_ONCE ) {
                if (mChronometer.getState() == Chronometer.ChronoState.CS_IDLE) {
                    mStartbutton.setVisibility(View.INVISIBLE);
                    mLapbutton.setVisibility(View.GONE);
                } else if (mChronometer.getState() == Chronometer.ChronoState.CS_RUNNING) {
                    mStartbutton.setVisibility(View.GONE);
                    mLapbutton.setVisibility(View.VISIBLE);
                } else if (mChronometer.getState() == Chronometer.ChronoState.CS_HALTED) {
                    mStartbutton.setVisibility(View.GONE);
                    mLapbutton.setVisibility(View.GONE);
                }
            }

            if(chronoManager.getRunmode() == ChronoManager.RunMode.ONE_BY_ONE ) {
                if (mChronometer.getState() == Chronometer.ChronoState.CS_IDLE) {
                    mStartbutton.setVisibility(View.VISIBLE);
                    mLapbutton.setVisibility(View.GONE);
                } else if (mChronometer.getState() == Chronometer.ChronoState.CS_RUNNING) {
                    mStartbutton.setVisibility(View.GONE);
                    mLapbutton.setVisibility(View.VISIBLE);
                } else if (mChronometer.getState() == Chronometer.ChronoState.CS_HALTED) {
                    mStartbutton.setVisibility(View.GONE);
                    mLapbutton.setVisibility(View.GONE);
                }
            }

            if(chronoManager.getRunmode() == ChronoManager.RunMode.INTERVAL ) {
                if (mChronometer.getState() == Chronometer.ChronoState.CS_IDLE) {
                    mStartbutton.setVisibility(View.GONE);
                    mLapbutton.setVisibility(View.GONE);
                } else if (mChronometer.getState() == Chronometer.ChronoState.CS_RUNNING) {
                    mStartbutton.setVisibility(View.GONE);
                    if(mChronometer.getCurrentTime() > 0)
                        mLapbutton.setVisibility(View.VISIBLE);
                    else
                        mLapbutton.setVisibility(View.INVISIBLE);
                } else if (mChronometer.getState() == Chronometer.ChronoState.CS_HALTED) {
                    mStartbutton.setVisibility(View.GONE);
                    mLapbutton.setVisibility(View.GONE);
                }
            }
        }
    }

}