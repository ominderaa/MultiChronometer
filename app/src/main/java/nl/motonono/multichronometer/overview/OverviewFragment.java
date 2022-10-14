package nl.motonono.multichronometer.overview;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionInflater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

import nl.motonono.multichronometer.R;
import nl.motonono.multichronometer.ChronoManager;
import nl.motonono.multichronometer.model.Chronometer;
import nl.motonono.multichronometer.utils.TimeFormatter;

/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends Fragment {
    private ChronoManager viewModel;

    private final int[] graphcolors = {
            Color.rgb(255, 0, 0),
            Color.rgb(0, 255, 0),
            Color.rgb(0, 0, 255),
            Color.rgb(255, 255, 0),
            Color.rgb(255, 0, 255),
            Color.rgb(0, 255, 255),
            Color.rgb(0, 0, 0),
    };

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
        setExitTransition(inflater.inflateTransition(R.transition.slide_left));
        viewModel = new ViewModelProvider(requireActivity()).get(ChronoManager.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        LinearLayout layout = view.findViewById(R.id.overviewFragment);
        GraphView graph = view.findViewById(R.id.graphViewResults);

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX) {
                    return super.formatLabel(value, true);
                } else {
                    return TimeFormatter.toTextShort(Double.valueOf(value).longValue());
                }
            }
        });

        int chronoNumber = 0;
        for (Chronometer chrono: viewModel.getChronos() ) {
            int lapcounter = 1;
            long totaltime = 0L;
            View chronoView =  inflater.inflate(R.layout.overview_chrono_item, layout, false);
            layout.addView(chronoView);

            List<DataPoint> points = new ArrayList<>();
            TableLayout table = chronoView.findViewById(R.id.tableResults);

            for (Long laptime : chrono.getLaptimes()) {
                TableRow tableRow = (TableRow) inflater.inflate(R.layout.overview_item, table, false);
                if(totaltime == 0 ) {
                    ((TextView) tableRow.findViewById(R.id.txChronoName)).setText(chrono.getName());
                } else {
                    ((TextView) tableRow.findViewById(R.id.txChronoName)).setText("");
                }
                ((TextView) tableRow.findViewById(R.id.txLapnumber)).setText(String.valueOf(lapcounter));
                ((TextView) tableRow.findViewById(R.id.txLaptime)).setText(TimeFormatter.toTextShort(laptime));

                if(totaltime > 0 ) {
                    totaltime += laptime;
                    ((TextView) tableRow.findViewById(R.id.txTotaltime)).setText(TimeFormatter.toTextShort(totaltime));
                } else {
                    totaltime += laptime;
                    ((TextView) tableRow.findViewById(R.id.txTotaltime)).setText("");
                }

                table.addView(tableRow);
                points.add(new DataPoint(lapcounter, totaltime));
                lapcounter++;
            }

            int i = 0;
            DataPoint[] data = new DataPoint[points.size()+1];
            data[i++] = new DataPoint(0,0);
            for(DataPoint p : points) { data[i++] = p; }
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
            series.setDrawDataPoints(true);
            series.setColor( chronoNumber < graphcolors.length ? graphcolors[chronoNumber] : 0x000000ff );
            graph.addSeries(series);
            chronoNumber++;
        }
        layout.requestLayout();
        return view;
    }

}

