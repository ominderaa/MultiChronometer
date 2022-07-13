package nl.motonono.multichronometer.model;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

public class Chronometer {
    private int     id;
    private String  name;
    private final List<Long> mLaptimepoints = new ArrayList<>();

    public enum ChronoState {
        CS_IDLE,
        CS_RUNNING,
        CS_HALTED
    }
    private ChronoState mChronoState = ChronoState.CS_IDLE;
    private long mStartedAt;
    private long mStoppedAt;

    public Chronometer(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getLapcount() {
        return mLaptimepoints.size();
    }

    public long getCurrentTime() {
        if(mChronoState == ChronoState.CS_RUNNING )
            return SystemClock.elapsedRealtime() - mStartedAt;
        else if(mChronoState == ChronoState.CS_HALTED)
            return mStoppedAt - mStartedAt;
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
            return mStoppedAt-mStartedAt;
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
        return mChronoState;
    }

    public long getLastLaptime() {
        if(mLaptimepoints.size() == 0 ) {
            return 0;
        }
        else if(mLaptimepoints.size() == 1 ) {
            return mLaptimepoints.get(0);
        }
        int lastidx = mLaptimepoints.size() - 1;
        return mLaptimepoints.get(lastidx) - mLaptimepoints.get(lastidx-1);
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


    public void startAt(long millis) {
        mStartedAt = millis;
        mChronoState = ChronoState.CS_RUNNING;
    }

    public void stop() {
        mChronoState = ChronoState.CS_HALTED;
        mStoppedAt = SystemClock.elapsedRealtime();
    }

    public void lap() {
        long lapTime = SystemClock.elapsedRealtime() - mStartedAt;
        mLaptimepoints.add(lapTime);
    }

    public void reset() {
        mChronoState = ChronoState.CS_IDLE;
        mStartedAt = SystemClock.elapsedRealtime();
        mLaptimepoints.clear();
    }
}
