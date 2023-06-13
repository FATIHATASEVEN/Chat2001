package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;


import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private SekmeErisimAdapter mySekmeErisimAdapter;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference kullanicilarReference;

    private String aktifKullaniciId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar=findViewById(R.id.ana_sayfa_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WhatsApp");

        myViewPager=findViewById(R.id.ana_sekmeler_pager);
        mySekmeErisimAdapter=new SekmeErisimAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(mySekmeErisimAdapter);

        myTabLayout=findViewById(R.id.ana_sekmeler);
        myTabLayout.setupWithViewPager(myViewPager);

        //Firebase
        mYetki=FirebaseAuth.getInstance();
        kullanicilarReference= FirebaseDatabase.getInstance().getReference();


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser mevcutKullanici=mYetki.getCurrentUser();

        if (mevcutKullanici ==null)
        {
            KullaniciyiLoginActivityeGonder();
        }

        else
        {
            kullaniciDurumuGuncelle("çevrimiçi");
            KullanicininVarliginiDogrula();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser mevcutKullanici=mYetki.getCurrentUser();


        if (mevcutKullanici != null)
        {
            kullaniciDurumuGuncelle("çevrimdışı");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser mevcutKullanici=mYetki.getCurrentUser();


        if (mevcutKullanici != null)
        {
            kullaniciDurumuGuncelle("çevrimdışı");
        }
    }

    private void KullanicininVarliginiDogrula() {

        String mevcutKullaniciId = mYetki.getCurrentUser().getUid();

        kullanicilarReference.child("Kullanicilar").child(mevcutKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.child("ad").exists()))
                {
                    Toast.makeText(MainActivity.this, "Hoşgeldiniz..", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Intent ayarlar = new Intent(MainActivity.this,AyarlarActivity.class);
                    ayarlar.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(ayarlar);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void KullaniciyiLoginActivityeGonder()
    {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.secenekler_menu,menu);

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId()==R.id.ana_arkadas_bulma_secenegi)
        {
            Intent arkadasBul = new Intent(MainActivity.this,ArkadasBulActivity.class);
            startActivity(arkadasBul);

        }

        if (item.getItemId()==R.id.ana_ayarlar_secenegi)
        {
            Intent ayar=new Intent(MainActivity.this,AyarlarActivity.class);
            startActivity(ayar);

        }

        if (item.getItemId()==R.id.ana_cikis_secenegi)
        {
            kullaniciDurumuGuncelle("çevrimdışı");
            mYetki.signOut();
            Intent giris=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(giris);

        }

        if (item.getItemId()==R.id.ana_grup_olustur_secenegi)
        {
            yeniGrupTalebi();
        }

        return true;
    }

    private void yeniGrupTalebi() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Grup Adı Girin:");

        final EditText grupAdiAlani = new EditText(MainActivity.this);
        grupAdiAlani.setHint("Örnek: Sanal Akademi Dünyası");
        builder.setView(grupAdiAlani);

        builder.setPositiveButton("Oluştur", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String grupAdi = grupAdiAlani.getText().toString();

                if (TextUtils.isEmpty(grupAdi))
                {
                    Toast.makeText(MainActivity.this, "Grup adı boş bırakılamaz!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    YeniGrupOlustur(grupAdi);
                }

            }
        });

        builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        });



        builder.show();

    }

    private void YeniGrupOlustur(final String grupAdi) {

        kullanicilarReference.child("Gruplar").child(grupAdi).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, grupAdi+" adlı grup başarıyla oluşturuldu.", Toast.LENGTH_LONG).show();
                        }

                    }
                });


    }

    private void kullaniciDurumuGuncelle(String durum)
    {
        String kaydedilenAktifZaman, kaydedilenAktifTarih;

        Calendar calendar = Calendar.getInstance();

        //Tarih formatı
        SimpleDateFormat aktifTarih = new SimpleDateFormat("MMM dd, yyyy");
        kaydedilenAktifTarih=aktifTarih.format(calendar.getTime());

        //Saat formatı
        SimpleDateFormat aktifZaman = new SimpleDateFormat("hh:mm a");
        kaydedilenAktifZaman=aktifZaman.format(calendar.getTime());

        HashMap<String,Object> cevrimiciDurumuMap = new HashMap<>();
        cevrimiciDurumuMap.put("zaman",kaydedilenAktifZaman);
        cevrimiciDurumuMap.put("tarih",kaydedilenAktifTarih);
        cevrimiciDurumuMap.put("durum",durum);


        aktifKullaniciId=mYetki.getCurrentUser().getUid();
        kullanicilarReference.child("Kullanicilar").child(aktifKullaniciId).child("kullaniciDurumu").updateChildren(cevrimiciDurumuMap);

    }
}
