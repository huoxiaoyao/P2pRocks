package ch.ethz.inf.vs.a4.simplep2p;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ArrowActivity extends AppCompatActivity {

    ImageView image;
    float currentDegrees = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrow);

        image = (ImageView) findViewById(R.id.arrowView);
    }

    public void setArrowRotation(float degrees) {
        currentDegrees = degrees;
        image.setRotation(degrees);
    }

    public void rotateBy20() {
        setArrowRotation(currentDegrees + 20);
    }

    public void onClick(View view) {
        rotateBy20();
    }
}
