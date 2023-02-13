package nl.motonono.multichronometer;

import static nl.motonono.multichronometer.model.Chronometer.ChronoState.CS_IDLE;

import android.os.SystemClock;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import nl.motonono.multichronometer.chrono.IndependentStartFragment;
import nl.motonono.multichronometer.model.Chronometer;

public class ChronoManager extends ViewModel {

    public enum RunMode {
        ALL_AT_ONCE,
        ONE_BY_ONE,
        INTERVAL,
        TIMED_TRIAL
    }

    public enum ManagerState {
        IDLE,
        RUNNING,
        HALTED
    }

    private RunMode runmode = RunMode.ALL_AT_ONCE;
    private ManagerState managerstate = ManagerState.IDLE;
    private final List<Chronometer> chronometers = new ArrayList<>();
    private int timedStartInterval = 10;    // seconds
    private int timedTrialDuration = 20;    // minutes
    private boolean predictAthlete = false;

    private final Chronometer mChronometer = new Chronometer(0, "main");

    public ChronoManager() {
        add(new Chronometer(1, "Chronometer 1"));
    }

    public List<Chronometer> getChronos() { return chronometers; }
    public RunMode getRunmode() { return runmode; }
    public void setRunmode(RunMode runmode) {
        this.runmode = runmode;
    }
    public ManagerState getManagerstate() { return managerstate; }

    public int getTimedTrialDuration() {return timedTrialDuration;}
    public void setTimedTrialDuration(int timedTrialDuration) {
        this.timedTrialDuration = timedTrialDuration;
    }

    public int getTimedStartInterval() {return timedStartInterval;}
    public void setTimedStartInterval(int timedStartInterval) {
        this.timedStartInterval = timedStartInterval;
    }

    public boolean isPredictAthlete() { return predictAthlete; }
    public void setPredictAthlete(boolean predictAthlete) { this.predictAthlete = predictAthlete; }

    public void add(Chronometer c) {
        c.setStateChangeListener(which -> {
            if( which.getState() == Chronometer.ChronoState.CS_RUNNING) {
                onStart(which);
            }
            if( which.getState() == Chronometer.ChronoState.CS_HALTED) {
                onStop(which);
            }
        });
        chronometers.add(c);
    }

    public interface StateChangeListener {
        void onStateChange(ManagerState newState);
    }
    private final List<ChronoManager.StateChangeListener> mListeners = new ArrayList<>();
    public void setStateChangeListener(ChronoManager.StateChangeListener toAdd) { mListeners.add(toAdd);}
    public void removeStateChangeListener(ChronoManager.StateChangeListener toRemove) { mListeners.remove(toRemove);  }
    private void stateChanged() {
        for(ChronoManager.StateChangeListener l: mListeners) {
            l.onStateChange(this.managerstate);
        }
    }


    private void onStart(Chronometer which) {
        ManagerState nextState = ManagerState.RUNNING;
        if(nextState != managerstate) {
            managerstate = ManagerState.RUNNING;
            stateChanged();
        }
        managerstate = ManagerState.RUNNING;
    }
    private void onStop(Chronometer which) {
        ManagerState nextState = ManagerState.HALTED;
        for(Chronometer m : chronometers) {
            if (m.getState() == Chronometer.ChronoState.CS_RUNNING) {
                nextState = ManagerState.RUNNING;
            }
        }
        if(nextState != managerstate) {
            managerstate = nextState;
            stateChanged();
        }
        managerstate = nextState;
    }

    public void start() {
        long millis = SystemClock.elapsedRealtime();
        mChronometer.startFuture(millis);

        switch( runmode ) {
            case ALL_AT_ONCE:
                for(Chronometer chronometer : chronometers ) {
                    chronometer.startNow(millis);
                }
                break;
            case ONE_BY_ONE:
                for(Chronometer chronometer : chronometers ) {
                    if(chronometer.getState() == CS_IDLE) {
                        chronometer.startNow(millis);
                    }
                }
                break;
            case INTERVAL:
            case TIMED_TRIAL:
                for(Chronometer chronometer : chronometers ) {
                    if(chronometer.getState() == CS_IDLE) {
                        chronometer.startFuture(millis);
                        millis += timedStartInterval * 1000L;
                    }
                }
                break;
        }
        managerstate = ManagerState.RUNNING;
    }

    public void stop() {
        mChronometer.stop();
        for(Chronometer chronometer : chronometers ) {
            chronometer.stop();
        }
        managerstate = ManagerState.HALTED;
    }

    public void reset() {
        mChronometer.reset();
        for(Chronometer chronometer : chronometers ) {
            chronometer.reset();
        }
        managerstate = ManagerState.IDLE;
    }

    public void clear() {
        chronometers.clear();
        managerstate = ManagerState.IDLE;
    }

    public long getCurrentTime() {
        return mChronometer.getCurrentTime();
    }
}
