package nl.motonono.multichronometer.model;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

public class Chronometer {
    private int mId;
    private String mName;
    private final List<Long> mLaptimepoints = new ArrayList<>();

    public enum ChronoState {
        CS_IDLE,
        CS_COUNTDOWN,
        CS_RUNNING,
        CS_HALTED
    }

    public enum ChronoMode {
        CM_COUNT_UP,
        CM_COUNT_DOWN
    }

    private ChronoState mChronoState = ChronoState.CS_IDLE;
    private final ChronoMode mChronoMode = ChronoMode.CM_COUNT_UP;

    private long mStartedAt;
    private long mStoppedAt;

    public interface StateChangeListener {
        void onStateChange(Chronometer which);
    }
    private final List<StateChangeListener> mListeners = new ArrayList<>();
    private void stateChanged() {
        for(StateChangeListener l: mListeners) {
            l.onStateChange(this);
        }
    }
    public void setStateChangeListener(StateChangeListener toAdd) {
        mListeners.add(toAdd);
    }
    public void removeStateChangeListener(StateChangeListener toRemove) { mListeners.remove(toRemove);  }

    public Chronometer(int id, String name) {
        this.mId = id;
        this.mName = name;
    }

    public int getId() {
        return mId;
    }
    public void setId(int id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        this.mName = name;
    }

    public int getLapcount() {
        return mLaptimepoints.size();
    }

    public long getCurrentTime() {
        switch(mChronoState) {
            case CS_COUNTDOWN:
                if(SystemClock.elapsedRealtime() - mStartedAt > 0 )
                    mChronoState = ChronoState.CS_RUNNING;
                return SystemClock.elapsedRealtime() - mStartedAt;
            case CS_RUNNING:
                return SystemClock.elapsedRealtime() - mStartedAt;
            case CS_HALTED:
                return mStoppedAt;
        }
        return 0L;
    }

    public long getTotalTime() {
        if(mChronoState == ChronoState.CS_RUNNING) {
            if (mLaptimepoints.size() == 0) {
                return 0;
            } else if (mLaptimepoints.size() == 1) {
                return mLaptimepoints.get(0);
            }
            int lastidx = mLaptimepoints.size() - 1;
            return mLaptimepoints.get(lastidx);
        } else if(mChronoState == ChronoState.CS_HALTED) {
            return mStoppedAt;
        }
        return 0L;
    }

    public long getLaptimepoint(int lapnr) {
        if( lapnr < mLaptimepoints.size() )
            return mLaptimepoints.get(lapnr);
        else
            return 0L;
    }

    public ChronoState getState() {
        if ( mChronoState == ChronoState.CS_COUNTDOWN ) {
            if(SystemClock.elapsedRealtime() > mStartedAt )
                mChronoState = ChronoState.CS_RUNNING;
        }
        return mChronoState;
    }

    public long getLastLaptime() {
        if(mLaptimepoints.size() == 0 ) {
            return getCurrentTime();
        }
        else if(mLaptimepoints.size() == 1 ) {
            return mLaptimepoints.get(0);
        }
        int lastidx = mLaptimepoints.size() - 1;
        return mLaptimepoints.get(lastidx) - mLaptimepoints.get(lastidx-1);
    }

    public long getCurrentLaptime() {
        return getCurrentTime() - getLastLaptime();
    }

    public List<Long> getLaptimes() {
        int i= 0;
        List<Long>  laptimes = new ArrayList<>();
        for(Long timepoint : mLaptimepoints) {
            if(i==0) {
                laptimes.add(timepoint);
            } else {
                laptimes.add(timepoint - getLaptimepoint(i-1));
            }
            i++;
        }
        return laptimes;
    }

    public long getNextlapPrediction() {
        if(mChronoState == ChronoState.CS_RUNNING && mLaptimepoints.size() > 0) {
            long lastLaptime = getLastLaptime();
            long totalTime = getTotalTime();
            long currentTime = getCurrentTime();
            return (totalTime + lastLaptime) - currentTime;
        }
        return 0;
    }

    public void startFuture(long millis) {
        mStartedAt = millis;
        mChronoState = ChronoState.CS_COUNTDOWN;
        stateChanged();
    }

    public void startNow(long millis) {
        mStartedAt = millis;
        mChronoState = ChronoState.CS_RUNNING;
        stateChanged();
    }

    public void stop() {
        mChronoState = ChronoState.CS_HALTED;
        if(mLaptimepoints.size() > 0 ) {
            int lastidx = mLaptimepoints.size() - 1;
            mStoppedAt = mLaptimepoints.get(lastidx);
        } else {
            mStoppedAt = SystemClock.elapsedRealtime() - mStartedAt;
        }
        stateChanged();
    }

    public void lap() {
        long lapTime = SystemClock.elapsedRealtime() - mStartedAt;
        mLaptimepoints.add(lapTime);
    }

    public void reset() {
        mChronoState = ChronoState.CS_IDLE;
        mStartedAt = SystemClock.elapsedRealtime();
        mLaptimepoints.clear();
        stateChanged();
    }

    public void tick() {
        if(mChronoState == ChronoState.CS_COUNTDOWN && mStartedAt <= SystemClock.elapsedRealtime()) {
            mChronoState = ChronoState.CS_RUNNING;
            stateChanged();
        }
    }
}
