package com.mona.cardetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity   implements CropIwaResultReceiver.Listener{

    private ImageView imgProfilPhoto;
    private CropIwaResultReceiver cropResultReceiver;
    Uri image_uri;
    private RequestQueue requestQueue;
    private RelativeLayout loadingPanel;

    private static final int RESULT_LOAD_IMAGE = 123;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static boolean isImageLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        LinearLayout topLayout = findViewById(R.id.topLayout);
        topLayout.setBackgroundColor(getColor(R.color.loginBackgroundColor));

        cropResultReceiver = new CropIwaResultReceiver();
        cropResultReceiver.setListener(this);
        cropResultReceiver.register(this);

        initsignup();
    }
    private void initsignup(){
        cropResultReceiver = new CropIwaResultReceiver();
        cropResultReceiver.setListener(this);
        cropResultReceiver.register(this);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        loadingPanel = findViewById(R.id.loadingPanel);

        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnSearch = findViewById(R.id.btnSearch);

        imgProfilPhoto = findViewById(R.id.imgPhoto);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectUserProfil(view);
                //onNewProfilImage();
            }
        });

        imgProfilPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edtProductId = findViewById(R.id.edtProductId);
                EditText edtProductName = findViewById(R.id.edtProductName);
                EditText edtProductLocation = findViewById(R.id.edtProductLocation);

                LinearLayout productDetailsLayout = findViewById(R.id.productDetailsLayout);

                edtProductId.setText("");
                edtProductName.setText("");
                edtProductLocation.setText("");

                productDetailsLayout.setVisibility(View.INVISIBLE);
                imgProfilPhoto.setImageDrawable(getDrawable(R.drawable.img_select_product));
                isImageLoaded = false;
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isImageLoaded ) {
                    loadingPanel.setVisibility(View.VISIBLE);
                    imgProfilPhoto.invalidate();
                    BitmapDrawable drawable = (BitmapDrawable) imgProfilPhoto.getDrawable();
                    Bitmap thumbnail = drawable.getBitmap();

                    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String encodedImage = Base64.encodeToString(byteArray,Base64.DEFAULT);

                    String[] ids = {"7245","11062","22368","11927","22008","26129","18599","21727","22771","10091","7014"};
                    int selectId = (int) ((Math.random() * (ids.length - 0)) + 0);
                    postImage2Service(ids[selectId],encodedImage);




                }

            }
        });

    }
    private void selectUserProfil(View view){
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_profilphotosource, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {


                final Handler handler = new Handler();
                switch (item.getItemId()) {
                    case R.id.btnCamera:

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                                    PackageManager.PERMISSION_DENIED ||
                                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                            PackageManager.PERMISSION_DENIED){
                                //permission not enabled, request it
                                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                //show popup to request permissions
                                requestPermissions(permission, PERMISSION_CODE);
                            }
                            else {
                                //permission already granted
                                openCamera();
                            }
                        }
                        else {
                            //system os < marshmallow
                            openCamera();
                        }

                        return true;
                    case R.id.btnGalery:
                        Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, RESULT_LOAD_IMAGE);
                        return true;
                    default:
                        return false;
                }

            }
        });

        popup.show();
    }
    @Override
    public void onCropSuccess(Uri croppedUri) {

        Bitmap thumbnail = null;
        imgProfilPhoto = findViewById(R.id.imgPhoto);

        try {
            thumbnail = App.getBitmapFormUri(MainActivity.this,croppedUri);
            imgProfilPhoto.setImageBitmap(thumbnail);
            isImageLoaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCropFailed(Throwable e) {

    }
    private void postImage2Service(String productId, String base64Image){
        StringRequest stringRequest = new StringRequest(Request.Method.POST,"https://car-detect-app.herokuapp.com/api/v1/predict",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("responseDATA :",response);

                        JSONObject jsonObject = null;

                        EditText edtProductId = findViewById(R.id.edtProductId);
                        EditText edtProductName = findViewById(R.id.edtProductName);
                        EditText edtProductLocation = findViewById(R.id.edtProductLocation);

                        LinearLayout productDetailsLayout = findViewById(R.id.productDetailsLayout);

                        try {
                            jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")){

                                edtProductId.setText("Ürün Bulundu");
                                edtProductName.setText(jsonObject.getString("predict"));
                                edtProductLocation.setText("Türkiye / Ankara");

                                productDetailsLayout.setVisibility(View.VISIBLE);
                            }
                            else{
                                edtProductId.setText("Ürün Bulunamadı");
                                edtProductName.setText("");
                                edtProductLocation.setText("");
                            }



                        } catch (JSONException e) {
                            edtProductId.setText("Ürün Bulunamadı");
                            edtProductName.setText("");
                            edtProductLocation.setText("");
                            Toast.makeText(MainActivity.this, "Bir hata oluştu. Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        loadingPanel.setVisibility(View.INVISIBLE);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR",error.getMessage());
                Toast.makeText(MainActivity.this, "İşleminiz yapılırken bir hata oluştu: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                loadingPanel.setVisibility(View.INVISIBLE);

            }

        }){
            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonObject = new JSONObject();

                try {

                    jsonObject.accumulate("content", base64Image);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("REQUEST111",jsonObject.toString());
                return jsonObject.toString().getBytes();
            }
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String >headers=new HashMap<String,String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }
    private void startCropActivity(Uri uri) {
        startActivity(CropActivity.callingIntent(MainActivity.this, uri));

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri picUri;

        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            //imgProfilPhoto.setImageURI(image_uri);
            startCropActivity(image_uri);
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
            image_uri = data.getData();
            startCropActivity(image_uri);

        }
    }
    @SuppressLint("MissingSuperCall")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        switch (requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    //permission from popup was granted
                    openCamera();
                }
                else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}