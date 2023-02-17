package nl.motonono.multichronometer;

import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import nl.motonono.multichronometer.databinding.FragmentWelcomeBinding;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class WelcomeFragment extends Fragment {

    private FragmentWelcomeBinding mBinding;
    private ChronoManager mViewModel;
    private final AlphaAnimation mButtonClickAnimation = new AlphaAnimation(1F, 0.8F);

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(ChronoManager.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentWelcomeBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.tileAllAtOnce.setOnClickListener(t -> {
            view.startAnimation(mButtonClickAnimation);
            mViewModel.setRunmode(ChronoManager.RunMode.ALL_AT_ONCE);
            mViewModel.reset();
            NavHostFragment.findNavController(WelcomeFragment.this)
                    .navigate(R.id.action_welcomeFragment_to_StartupFragment);
        });
        addButtonEffect(mBinding.tileAllAtOnce);
        mBinding.tileOneByOne.setOnClickListener(t -> {
            view.startAnimation(mButtonClickAnimation);
            mViewModel.setRunmode(ChronoManager.RunMode.ONE_BY_ONE);
            mViewModel.reset();
            NavHostFragment.findNavController(WelcomeFragment.this)
                    .navigate(R.id.action_welcomeFragment_to_StartupFragment);
        });
        addButtonEffect(mBinding.tileOneByOne);
        mBinding.tileAtInterval.setOnClickListener(t -> {
            view.startAnimation(mButtonClickAnimation);
            mViewModel.setRunmode(ChronoManager.RunMode.INTERVAL);
            mViewModel.reset();
            NavHostFragment.findNavController(WelcomeFragment.this)
                    .navigate(R.id.action_welcomeFragment_to_StartupFragment);
        });
        addButtonEffect(mBinding.tileAtInterval);
        mBinding.tileTimedTrial.setOnClickListener(t -> {
            view.startAnimation(mButtonClickAnimation);
            mViewModel.setRunmode(ChronoManager.RunMode.TIMED_TRIAL);
            mViewModel.reset();
            NavHostFragment.findNavController(WelcomeFragment.this)
                    .navigate(R.id.action_welcomeFragment_to_StartupFragment);
        });
        addButtonEffect(mBinding.tileTimedTrial);
    }

    public void addButtonEffect(View button){
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    v.getBackground().setColorFilter(0xe0ffffff, PorterDuff.Mode.SRC_ATOP);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    v.getBackground().clearColorFilter();
                    v.invalidate();
                    v.performClick();
                    break;
                }
            }
            return false;
        });
    }
}
