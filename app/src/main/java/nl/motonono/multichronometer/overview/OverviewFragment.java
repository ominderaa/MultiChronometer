package nl.motonono.multichronometer.overview;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionInflater;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import nl.motonono.multichronometer.ChronoManager;
import nl.motonono.multichronometer.R;
import nl.motonono.multichronometer.databinding.FragmentOverviewBinding;
import nl.motonono.multichronometer.model.Chronometer;
import nl.motonono.multichronometer.utils.TimeFormatter;

/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends Fragment {
    FragmentOverviewBinding mBinding;
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

    private String buildText() {
        StringBuilder resultText = new StringBuilder("Resultaten\n");
        resultText.append(String.format("Datum:    \n"));

        for (Chronometer chrono : viewModel.getChronos()) {
            resultText.append(chrono.getName()).append("\n");
            int lapcounter = 1;
            long totaltime = 0L;

            for (Long laptime : chrono.getLaptimes()) {
                totaltime += laptime;
                resultText.append(String.format("%3d  %s %s\n", lapcounter++,
                        TimeFormatter.toTextShort(laptime),
                        TimeFormatter.toTextShort(totaltime)));
            }
            resultText.append("\n\n");
        }
        return resultText.toString();
    }

    private void shareAsEmail() {
        Context context = getActivity();
        String resultText = buildText();
        try {
            File csvAttachment = writeCsvFile(context);
            Uri contentUri = CsvFileProvider.getUriForFile(context, "nl.motonono.multichono.fileprovider", csvAttachment);
            Intent mailIntent = new Intent(Intent.ACTION_SEND);
            mailIntent.setType("message/rfc822");

            mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Multi-Chrono session results");
            mailIntent.putExtra(Intent.EXTRA_TEXT, resultText);
            mailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            startActivity(Intent.createChooser(mailIntent, "Send email.."));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "No_Email_Service", Toast.LENGTH_SHORT).show();
        }
    }


    private File writeCsvFile(Context ctx) throws IOException {
        final String header = "NAME;LAP;LAPTIME;TOTALTIME;LSAPTIME;STOTALTIME\n";
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        final String dateString = formatter.format(new Date());
        final String fileName = "Multichrono_" + dateString + ".csv";
        final File exportPath = new File(ctx.getFilesDir(), "export");
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr--r--");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
        Files.createDirectories(exportPath.toPath(), attr);
        File newFile = new File(exportPath, fileName);

        FileOutputStream fileout = new FileOutputStream(newFile);
        OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
        outputWriter.write(header);

        for (Chronometer chrono : viewModel.getChronos()) {
            int lapcounter = 1;
            long totaltime = 0L;

            for (Long laptime : chrono.getLaptimes()) {
                totaltime += laptime;
                String record = String.format("%s;%d;%d;%d;%s;%s\n",
                        chrono.getName(),
                        lapcounter++,
                        laptime,
                        totaltime,
                        TimeFormatter.toTextShort(laptime),
                        TimeFormatter.toTextShort(totaltime));
                outputWriter.write(record);
            }
        }
        outputWriter.flush();
        outputWriter.close();
        fileout.close();
        return newFile;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentOverviewBinding.inflate(inflater, container, false);
        mBinding.fabEmail.setOnClickListener(view -> {
            shareAsEmail();
        });

        mBinding.graphViewResults.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return super.formatLabel(value, true);
                } else {
                    return TimeFormatter.toTextShort(Double.valueOf(value).longValue());
                }
            }
        });

        int chronoNumber = 0;
        for (Chronometer chrono : viewModel.getChronos()) {
            int lapcounter = 1;
            long totaltime = 0L;
            View chronoView = inflater.inflate(R.layout.overview_chrono_item, container, false);
            mBinding.overviewFragment.addView(chronoView);

            List<DataPoint> points = new ArrayList<>();
            TableLayout table = chronoView.findViewById(R.id.tableResults);

            for (Long laptime : chrono.getLaptimes()) {
                TableRow tableRow = (TableRow) inflater.inflate(R.layout.overview_item, table, false);
                if (totaltime == 0) {
                    ((TextView) tableRow.findViewById(R.id.txChronoName)).setText(chrono.getName());
                } else {
                    ((TextView) tableRow.findViewById(R.id.txChronoName)).setText("");
                }
                ((TextView) tableRow.findViewById(R.id.txLapnumber)).setText(String.valueOf(lapcounter));
                ((TextView) tableRow.findViewById(R.id.txLaptime)).setText(TimeFormatter.toTextShort(laptime));

                if (totaltime > 0) {
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
            DataPoint[] data = new DataPoint[points.size() + 1];
            data[i++] = new DataPoint(0, 0);
            for (DataPoint p : points) {
                data[i++] = p;
            }
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
            series.setDrawDataPoints(true);
            series.setColor(chronoNumber < graphcolors.length ? graphcolors[chronoNumber] : 0x000000ff);
            mBinding.graphViewResults.addSeries(series);
            chronoNumber++;
        }
        mBinding.overviewFragment.requestLayout();
        return mBinding.getRoot();
    }

}

