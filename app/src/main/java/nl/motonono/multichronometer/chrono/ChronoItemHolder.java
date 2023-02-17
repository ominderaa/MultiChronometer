package nl.motonono.multichronometer.chrono;

import static java.lang.StrictMath.abs;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import nl.motonono.multichronometer.R;
import nl.motonono.multichronometer.model.Chronometer;
import nl.motonono.multichronometer.utils.TimeFormatter;

public abstract class ChronoItemHolder {
    public View mItemView;
    public Chronometer mChronometer;
    public TextView mChronoName;
    public TextView mLapCount;
    public TextView mLastLaptime;
    public TextView mCurrentTime;
    public TextView mTotalTime;
    public Button mStartbutton;
    public Button mLapbutton;
    public TextView mCountdown;

    private final Chronometer.ChronoState mLastState = Chronometer.ChronoState.CS_IDLE;

    protected ChronoItemHolder(View itemView,  Chronometer chronometer) {
        this.mItemView = itemView;
        this.mChronometer = chronometer;
        this.mChronoName = itemView.findViewById(R.id.txChronoName);
        this.mLapCount =  itemView.findViewById(R.id.txLapcount);
        this.mLastLaptime = itemView.findViewById(R.id.txLastlap);
        this.mCurrentTime = itemView.findViewById(R.id.txCurrentTime);
        this.mTotalTime = itemView.findViewById(R.id.txTotalTime);
        this.mStartbutton = itemView.findViewById(R.id.btnStart);
        this.mLapbutton = itemView.findViewById(R.id.btnLap);
        this.mCountdown = itemView.findViewById(R.id.txCountdown);

        String contentDesc = String.valueOf(mStartbutton.getContentDescription());
        mStartbutton.setContentDescription(String.format(contentDesc,chronometer.getName() ));
        contentDesc = String.valueOf(mLapbutton.getContentDescription());
        mLapbutton.setContentDescription(String.format(contentDesc,chronometer.getName() ));
        contentDesc = String.valueOf(mCountdown.getContentDescription());
        mCountdown.setContentDescription(String.format(contentDesc,chronometer.getName() ));
        mChronoName.setText(mChronometer.getName());
    }

    abstract void onLap();
    abstract void updateHolderUI();

    public void lap() {
        mLapCount.setText(String.format( Locale.getDefault(),"Laps %02d", mChronometer.getLapcount()));
        mLastLaptime.setText(TimeFormatter.toTextShort(mChronometer.getLastLaptime()));
        mTotalTime.setText(TimeFormatter.toTextShort(mChronometer.getCurrentTime()));
        Log.i("LapPrediction", String.format("%s lap at %d msec", mChronometer.getName(), mChronometer.getCurrentTime()));
        onLap();
    }

    public void tick() {
        long currentTime = mChronometer.getCurrentTime();
        mChronometer.tick();
        if(currentTime < 0) {
//            mCurrentTime.setText(TimeFormatter.toTextSeconds(0L));
            mCountdown.setText(TimeFormatter.toTextSeconds(currentTime));
        }
        else {
            mCurrentTime.setText(TimeFormatter.toTextShort(currentTime));
        }
        mLapCount.setText(String.format( Locale.getDefault(),"Laps %02d", mChronometer.getLapcount()));
        updateHolderUI();
    }
}
