package nl.motonono.multichronometer;

import static nl.motonono.multichronometer.model.Chronometer.ChronoState.CS_IDLE;

import android.os.SystemClock;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

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
    private Chronometer mChronometer = new Chronometer(0, "main");

    public ChronoManager() {
        chronometers.add(new Chronometer(1, "Chronometer 1"));
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


    public void start() {
        long millis = SystemClock.elapsedRealtime();
        mChronometer.startAt(millis);

        switch( runmode ) {
            case ALL_AT_ONCE:
                for(Chronometer chronometer : chronometers ) {
                    chronometer.startAt(millis);
                }
                break;
            case ONE_BY_ONE:
                for(Chronometer chronometer : chronometers ) {
                    if(chronometer.getState() == CS_IDLE) {
                        chronometer.startAt(millis);
                    }
                }
                break;
            case INTERVAL:
                for(Chronometer chronometer : chronometers ) {
                    if(chronometer.getState() == CS_IDLE) {
                        chronometer.startAt(millis);
                        millis += timedStartInterval * 1000;
                    }
                }
                break;
            case TIMED_TRIAL:
                for(Chronometer chronometer : chronometers ) {
                    if(chronometer.getState() == CS_IDLE) {
                        chronometer.startAt(millis);
                        millis += timedStartInterval * 1000;
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
