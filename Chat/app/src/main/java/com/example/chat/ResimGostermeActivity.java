package com.example.chat;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ResimGostermeActivity extends AppCompatActivity {

    private ImageView imageView;
    private String resimUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resim_gosterme);

        imageView=findViewById(R.id.resim_goruntuleyici);
        resimUrl=getIntent().getStringExtra("url");

        Picasso.get().load(resimUrl).into(imageView);
    }
}