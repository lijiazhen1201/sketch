package me.xiaopan.sketchsample.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import me.xiaopan.androidinjector.InjectContentView;
import me.xiaopan.androidinjector.InjectView;
import me.xiaopan.sketch.display.TransitionImageDisplayer;
import me.xiaopan.sketch.process.RotateImageProcessor;
import me.xiaopan.sketchsample.MyFragment;
import me.xiaopan.sketchsample.R;
import me.xiaopan.sketchsample.widget.MyImageView;

@InjectContentView(R.layout.fragment_rotate)
public class RotateTestFragment extends MyFragment {
    @InjectView(R.id.image_rotateFragment)
    MyImageView imageView;

    @InjectView(R.id.button_rotateFragment)
    Button rotateButton;

    private int degrees;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView.getOptions().setImageDisplayer(new TransitionImageDisplayer());

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                degrees += 45;
                imageView.getOptions().setImageProcessor(new RotateImageProcessor(degrees));
                imageView.displayAssetImage("bizhi1.jpg");
            }
        });

        degrees = 0;
        rotateButton.performClick();
    }
}
