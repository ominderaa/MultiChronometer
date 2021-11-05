package nl.motonono.multichronometer.model;

import static nl.motonono.multichronometer.model.Chronometer.ChronoState.CS_IDLE;

import android.os.SystemClock;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChronoManager extends ViewModel {
    public enum RunMode {
        ALL_AT_ONCE,
        ONE_BY_ONE,
        TIMED_INTERVAL
    }

    public enum ManagerState {
        IDLE,
        RUNNING,
        HALTED
    }

    private RunMode runmode = RunMode.ALL_AT_ONCE;
    private ManagerState managerstate = ManagerState.IDLE;
    private final List<Chronometer> chronometers = new ArrayList<>();

    public ChronoManager() {
        chronometers.add(new Chronometer(1, "Chronometer 1"));
    }

    public List<Chronometer> getChronos() { return chronometers; }
    public RunMode getRunmode() { return runmode; }
    public void setRunmode(RunMode runmode) {
        this.runmode = runmode;
    }
    public ManagerState getManagerstate() { return managerstate; }

    public void start() {
        long millis;
        switch( runmode ) {
            case ALL_AT_ONCE:
                millis = SystemClock.elapsedRealtime();
                for(Chronometer chronometer : chronometers ) {
                    chronometer.startAt(millis);
                }
                break;
            case TIMED_INTERVAL:
                int i = 0;
                millis = SystemClock.elapsedRealtime();
                for(Chronometer chronometer : chronometers ) {
                    chronometer.startIn(millis + (i++ * 15000L));
                }
                break;
            case ONE_BY_ONE:
                millis = SystemClock.elapsedRealtime();
                for(Chronometer chronometer : chronometers ) {
                    if(chronometer.getState() == CS_IDLE) {
                        chronometer.startAt(millis);
                        break;
                    }
                }
                break;
        }
        managerstate = ManagerState.RUNNING;
    }

    public void stop() {
        for(Chronometer chronometer : chronometers ) {
            chronometer.stop();
        }
        managerstate = ManagerState.HALTED;
    }

    public void reset() {
        for(Chronometer chronometer : chronometers ) {
            chronometer.reset();
        }
        managerstate = ManagerState.IDLE;
    }

    public void clear() {
        chronometers.clear();
        managerstate = ManagerState.IDLE;
    }

    public void tick() {
        for(Chronometer chronometer : chronometers ) {
            chronometer.tick();
        }
    }
}
