package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GrupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton MesajGonderButtonu;
    private EditText kullaniciMesajiGirdisi;
    private ScrollView mScrollView;
    private TextView metinMesajlariniGoster;

    //Firabase
    private FirebaseAuth mYetki;
    private DatabaseReference kullaniciYolu, grupAdiYolu,grupMesajAnahtariYolu;

    //Intent Değişkeni
    private String mevcutGrupAdi,aktifKullaniciId,aktifKullaniciAdi,aktifTarih, aktifZaman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grup_chat);

        //Intenti al
        mevcutGrupAdi=getIntent().getExtras().get("grupAdı").toString();
        Toast.makeText(this, mevcutGrupAdi, Toast.LENGTH_LONG).show();

        //Firebase tanımlama
        mYetki=FirebaseAuth.getInstance();
        aktifKullaniciId=mYetki.getCurrentUser().getUid();
        kullaniciYolu= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        grupAdiYolu= FirebaseDatabase.getInstance().getReference().child("Gruplar").child(mevcutGrupAdi);



        //Tanımlamalar
        mToolbar = findViewById(R.id.grup_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(mevcutGrupAdi);

        MesajGonderButtonu=findViewById(R.id.mesaj_gonderme_buttonu);
        kullaniciMesajiGirdisi=findViewById(R.id.grup_mesaji_girdisi);
        metinMesajlariniGoster=findViewById(R.id.grup_chat_metni_gosterme);
        mScrollView=findViewById(R.id.my_scroll_view);

        //Kullanici bilgisi alma
        kullaniciBilgisiAl();

        //Meajı veri tabanına kayıt
        MesajGonderButtonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MesajiVeriTabaninaKaydet();

                kullaniciMesajiGirdisi.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        grupAdiYolu.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists())
                {
                    mesajlariGoster(dataSnapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists())
                {
                    mesajlariGoster(dataSnapshot);
                }


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void mesajlariGoster(DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
            String sohbetTarihi = (String)((DataSnapshot)iterator.next()).getValue();
            String sohbetMasaji = (String)((DataSnapshot)iterator.next()).getValue();
            String sohbetAdi = (String)((DataSnapshot)iterator.next()).getValue();
            String sohbetZamani = (String)((DataSnapshot)iterator.next()).getValue();

            metinMesajlariniGoster.append(sohbetAdi +" :\n"+ sohbetMasaji +"\n"+sohbetZamani+"    "+sohbetTarihi+"\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void MesajiVeriTabaninaKaydet() {

        String mesaj=kullaniciMesajiGirdisi.getText().toString();
        String mesajAnahtari=grupAdiYolu.push().getKey();

        if (TextUtils.isEmpty(mesaj))
        {
            Toast.makeText(this, "Mesaj alanı boş olamaz", Toast.LENGTH_LONG).show();
        }

        else{
            Calendar tarihİcinTak = Calendar.getInstance();
            SimpleDateFormat aktifTarihFormati = new SimpleDateFormat("MMM dd, yyyy");
            aktifTarih=aktifTarihFormati.format(tarihİcinTak.getTime());

            Calendar zamanİcinTak = Calendar.getInstance();
            SimpleDateFormat aktifZamanFormati = new SimpleDateFormat("hh:mm:ss a");
            aktifZaman = aktifZamanFormati.format(zamanİcinTak.getTime());

            HashMap<String,Object>grupMesajAnahtari = new HashMap<>();
            grupAdiYolu.updateChildren(grupMesajAnahtari);

            grupMesajAnahtariYolu = grupAdiYolu.child(mesajAnahtari);

            HashMap<String,Object> mesajBilgisiMap = new HashMap<>();

            mesajBilgisiMap.put("ad", aktifKullaniciAdi);
            mesajBilgisiMap.put("masaj", mesaj);
            mesajBilgisiMap.put("tarih", aktifTarih);
            mesajBilgisiMap.put("zaman", aktifZaman);

            grupMesajAnahtariYolu.updateChildren(mesajBilgisiMap);
        }
    }

    private void kullaniciBilgisiAl() {

        kullaniciYolu.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    aktifKullaniciAdi=dataSnapshot.child("ad").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
