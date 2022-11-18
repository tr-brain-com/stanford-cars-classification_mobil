package com.mona.cardetection;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.mona.cardetection.config.CropViewConfigurator;
import com.steelkiwi.cropiwa.CropIwaView;
import com.yarolegovich.mp.MaterialPreferenceScreen;

import java.io.File;

public class CropActivity extends AppCompatActivity {

    private static final String EXTRA_URI = "https://pp.vk.me/c637119/v637119751/248d1/6dd4IPXWwzI.jpg";

    public static Intent callingIntent(Context context, Uri imageUri) {
        Intent intent = new Intent(context, com.mona.cardetection.CropActivity.class);
        intent.putExtra(EXTRA_URI, imageUri);
        return intent;
    }

    private CropIwaView cropView;
    private CropViewConfigurator configurator;

    public static Uri createNewEmptyFile() {

        return Uri.fromFile(new File(
                App.getInstance().getFilesDir(),
                System.currentTimeMillis() + ".png"));
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        setContentView(R.layout.activity_crop);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_crop);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Uri imageUri = getIntent().getParcelableExtra(EXTRA_URI);

        cropView = (CropIwaView) findViewById(R.id.crop_view);

        cropView.setImageUri(imageUri);

        MaterialPreferenceScreen cropPrefScreen = (MaterialPreferenceScreen) findViewById(R.id.crop_preference_screen);


        configurator = new CropViewConfigurator(cropView, cropPrefScreen);

        cropPrefScreen.setStorageModule(configurator);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_crop, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.done) {
            Toast.makeText(CropActivity.this, "Resim Yükleniyor...", Toast.LENGTH_SHORT).show();
            cropView.crop(configurator.getSelectedSaveConfig());
            finish();
        }
        if (item.getItemId() == android.R.id.home){
            onBackPressed();

        }
        return super.onOptionsItemSelected(item);
    }
}
