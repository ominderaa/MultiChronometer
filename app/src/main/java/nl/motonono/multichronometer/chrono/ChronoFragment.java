package nl.motonono.multichronometer.chrono;

import static nl.motonono.multichronometer.model.ChronoManager.RunMode.ONE_BY_ONE;
import static nl.motonono.multichronometer.model.Chronometer.ChronoState.CS_IDLE;
import static nl.motonono.multichronometer.model.Chronometer.ChronoState.CS_RUNNING;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import nl.motonono.multichronometer.R;
import nl.motonono.multichronometer.databinding.FragmentChronoBinding;
import nl.motonono.multichronometer.model.ChronoManager;
import nl.motonono.multichronometer.model.Chronometer;
import nl.motonono.multichronometer.utils.TimeFormatter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChronoFragment extends Fragment {
    FragmentChronoBinding binding;
    List<ViewHolder> mChronoViews = new ArrayList<>();
    Timer mUpdateTimer;

    public ChronoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChronoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChronoManager chronoManager = ViewModelProviders.of(requireActivity()).get(ChronoManager.class);

        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.IDLE ) {
            binding.btnStop.setVisibility(View.GONE);

            switch (chronoManager.getRunmode()) {
                case ALL_AT_ONCE:
                    binding.btnStartChrono.setVisibility(View.VISIBLE);
                    break;
                case TIMED_INTERVAL:
                case ONE_BY_ONE:
                    binding.btnStartChrono.setVisibility(View.GONE);
                    break;
            }
        }
        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.RUNNING ) {
            binding.btnStop.setVisibility(View.VISIBLE);
            binding.btnStartChrono.setVisibility(View.GONE);
        }
        if(chronoManager.getManagerstate() == ChronoManager.ManagerState.HALTED ) {
            binding.btnStop.setVisibility(View.VISIBLE);
            binding.btnStartChrono.setVisibility(View.GONE);
        }

        binding.btnStartChrono.setOnClickListener(v -> {
            chronoManager.start();
            if(chronoManager.getRunmode() != ONE_BY_ONE) {
                binding.btnStartChrono.setVisibility(View.GONE);
                binding.btnStop.setVisibility(View.VISIBLE);
                for(ViewHolder holder : mChronoViews) {
                    initChronoView(chronoManager, holder);
                }
            }
        });

        binding.btnStop.setOnClickListener(v -> {
            chronoManager.stop();
            for(ViewHolder holder : mChronoViews) {
                initChronoView(chronoManager, holder);
            }
            NavHostFragment.findNavController(ChronoFragment.this)
                    .navigate(R.id.action_ChronoFragment_to_OverviewFragment);
        });

        binding.btnResuls.setOnClickListener(v -> NavHostFragment.findNavController(ChronoFragment.this)
                .navigate(R.id.action_ChronoFragment_to_OverviewFragment));

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireActivity());
        boolean lefthanded = sharedPreferences.getBoolean("lefthandedui", false);
        LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());

        int chronoNumber = 0;
        for (Chronometer chrono: chronoManager.getChronos() ) {
            View listItem;
            if(lefthanded) {
                listItem = layoutInflater.inflate(R.layout.chrono_item_left, binding.chronoContainer, false);
            } else {
                listItem = layoutInflater.inflate(R.layout.chrono_item_right, binding.chronoContainer, false);
            }
            binding.chronoContainer.addView(listItem);

            ViewHolder holder = new ViewHolder(listItem, chrono);
            mChronoViews.add(chronoNumber, holder);
            holder.mChronoName.setText(chrono.getName());
            initChronoView(chronoManager, holder);

            holder.mStartbutton.setOnClickListener(v -> {
                if (holder.mChronometer.getState() == CS_IDLE) {
                    holder.mChronometer.startAt(SystemClock.elapsedRealtime());
                }
            });

            holder.mLapbutton.setOnClickListener(v -> {
                if (holder.mChronometer.getState() == CS_RUNNING) {
                    holder.mChronometer.lap();
                    holder.lap();
                }
            });
        }
        binding.chronoContainer.requestLayout();
        mUpdateTimer = new Timer();
        mUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mUpdateHandler.obtainMessage(1).sendToTarget();
            }
        }, 50, 100);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mUpdateTimer = null;
    }

    private void initChronoView(ChronoManager chronoManager, ViewHolder holder) {
        if(chronoManager.getRunmode() == ChronoManager.RunMode.ALL_AT_ONCE ||
                chronoManager.getRunmode() == ChronoManager.RunMode.TIMED_INTERVAL ) {
            if (holder.mChronometer.getState() == Chronometer.ChronoState.CS_COUNTDOWN) {
                holder.mStartbutton.setVisibility(View.GONE);
                holder.mLapbutton.setVisibility(View.INVISIBLE);
            } else if (holder.mChronometer.getState() == Chronometer.ChronoState.CS_IDLE) {
                holder.mStartbutton.setVisibility(View.INVISIBLE);
                holder.mLapbutton.setVisibility(View.GONE);
            } else if (holder.mChronometer.getState() == Chronometer.ChronoState.CS_RUNNING) {
                holder.mStartbutton.setVisibility(View.GONE);
                holder.mLapbutton.setVisibility(View.VISIBLE);
            } else if (holder.mChronometer.getState() == Chronometer.ChronoState.CS_HALTED) {
                holder.mStartbutton.setVisibility(View.GONE);
                holder.mLapbutton.setVisibility(View.GONE);
            }
        }

        if(chronoManager.getRunmode() == ChronoManager.RunMode.ONE_BY_ONE ) {
            if (holder.mChronometer.getState() == Chronometer.ChronoState.CS_IDLE) {
                holder.mStartbutton.setVisibility(View.VISIBLE);
                holder.mLapbutton.setVisibility(View.GONE);
            } else if (holder.mChronometer.getState() == Chronometer.ChronoState.CS_RUNNING) {
                holder.mStartbutton.setVisibility(View.GONE);
                holder.mLapbutton.setVisibility(View.VISIBLE);
            } else if (holder.mChronometer.getState() == Chronometer.ChronoState.CS_HALTED) {
                holder.mStartbutton.setVisibility(View.GONE);
                holder.mLapbutton.setVisibility(View.GONE);
            }
        }
    }

    private final Handler mUpdateHandler  = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            for(ViewHolder holder : mChronoViews) {
                holder.tick();
            }
        }
    };

    public static class ViewHolder {
        public Chronometer mChronometer;
        public TextView mChronoName;
        public TextView mLapCount;
        public TextView mLastLaptime;
        public TextView mTotalTime;
        public TextView mCurrentTime;
        public Button mStartbutton;
        public Button mLapbutton;

        public ViewHolder(View itemView, Chronometer chronometer) {
            this.mChronometer = chronometer;
            this.mChronoName = itemView.findViewById(R.id.txChronoName);
            this.mLapCount =  itemView.findViewById(R.id.txLapcount);
            this.mLastLaptime = itemView.findViewById(R.id.txLastlap);
            this.mTotalTime = itemView.findViewById(R.id.txTotalTime);
            this.mCurrentTime = itemView.findViewById(R.id.txCurrentTime);
            this.mStartbutton = itemView.findViewById(R.id.btnStart);
            this.mLapbutton = itemView.findViewById(R.id.btnLap);
        }

        public void tick() {
            mCurrentTime.setText(TimeFormatter.toTextShort(mChronometer.getCurrentTime()));
        }

        public void lap() {
            mLapCount.setText(String.format( Locale.getDefault(),"Laps %02d", mChronometer.getLapcount()));
            mLastLaptime.setText(TimeFormatter.toTextShort(mChronometer.getLastLaptime()));
            mTotalTime.setText(TimeFormatter.toTextShort(mChronometer.getTotalTime()));
        }
    }

}