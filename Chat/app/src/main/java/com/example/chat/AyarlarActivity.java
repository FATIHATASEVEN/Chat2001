package com.example.chat;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AyarlarActivity extends AppCompatActivity {

    private Button  hesapAyarlariniGuncelleme;
    private EditText kullaniciAdi,kullaniciDurumu;
    private CircleImageView kullaniciProfilResmi;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference veriYoluuuuu;
    private StorageReference KullaniciProfilResimleriYolu;
    private StorageTask yuklemeGorevi;

    private String mevcutKullaniciId;

    //Resim Seçme
    private static final int GaleriSecme = 1;

    //Yukleniyor
    private ProgressDialog yukleniyorBar;

    //Uri
    Uri resimUri;
    String myUri = "";

    //Tollabar
    private Toolbar ayarlarToolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar);

        //Firebase
        mYetki=FirebaseAuth.getInstance();
        veriYoluuuuu= FirebaseDatabase.getInstance().getReference();
        KullaniciProfilResimleriYolu= FirebaseStorage.getInstance().getReference().child("Profil Resimleri");

        mevcutKullaniciId=mYetki.getCurrentUser().getUid();

        //Kontrol tanımlamaları
        hesapAyarlariniGuncelleme=findViewById(R.id.ayarları_guncelleme_butonu);
        kullaniciAdi=findViewById(R.id.kullanici_adi_ayarla);
        kullaniciDurumu=findViewById(R.id.profil_durumu_ayarla);
        kullaniciProfilResmi=findViewById(R.id.profil_resmi_ayarla);

        //Toolbar
        ayarlarToolbar=findViewById(R.id.ayarlar_toolbar);
        setSupportActionBar(ayarlarToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Hesap Ayarları");

        //Yukleniyo
        yukleniyorBar=new ProgressDialog(this);

        hesapAyarlariniGuncelleme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AyarlariGuncelle();
            }
        });

        kullaniciAdi.setVisibility(View.INVISIBLE);

        KullaniniBilgisiAl();

        //---------------
        kullaniciProfilResmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Crop(Kırpma aktivity) açma
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(AyarlarActivity.this);

            }
        });
    }

    private String dosyaUzantisiAl(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //Resim seçme kodu
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
        {
            //Resim seçiliyorsa
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            resimUri = result.getUri();
            kullaniciProfilResmi.setImageURI(resimUri);

        }
        else
        {
            Toast.makeText(this, "Resim Seçilemedi'", Toast.LENGTH_LONG).show();
        }

    }

    private void KullaniniBilgisiAl() {

        veriYoluuuuu.child("Kullanicilar").child(mevcutKullaniciId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("ad")&& (dataSnapshot.hasChild("resim"))))
                        {
                            String kullaniciAdiAl = dataSnapshot.child("ad").getValue().toString();
                            String kullaniciDurumuAl = dataSnapshot.child("durum").getValue().toString();
                            String kullaniciResmiAl = dataSnapshot.child("resim").getValue().toString();

                            kullaniciAdi.setText(kullaniciAdiAl);
                            kullaniciDurumu.setText(kullaniciDurumuAl);
                            Picasso.get().load(kullaniciResmiAl).into(kullaniciProfilResmi);

                        }

                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("ad")))
                        {
                            String kullaniciAdiAl = dataSnapshot.child("ad").getValue().toString();
                            String kullaniciDurumuAl = dataSnapshot.child("durum").getValue().toString();

                            kullaniciAdi.setText(kullaniciAdiAl);
                            kullaniciDurumu.setText(kullaniciDurumuAl);

                        }

                        else
                        {
                            kullaniciAdi.setVisibility(View.VISIBLE);
                            Toast.makeText(AyarlarActivity.this, "Lütfen Profil verilerinizi ayarlayın!", Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private void AyarlariGuncelle() {

        String kullaniciAdiniAl = kullaniciAdi.getText().toString();
        String kullaniciDurumunuAl = kullaniciDurumu.getText().toString();

        if (TextUtils.isEmpty(kullaniciAdiniAl))
        {
            Toast.makeText(this, "Ad boş olamaz!", Toast.LENGTH_LONG).show();
        }

        if (TextUtils.isEmpty(kullaniciDurumunuAl))
        {
            Toast.makeText(this, "Durum boş olamaz!", Toast.LENGTH_LONG).show();
        }

        else
        {
            resimYukle();
        }
    }

    private void resimYukle() {

        yukleniyorBar.setTitle("Bilgi Aktarma");
        yukleniyorBar.setMessage("Lütfen bekleyin");
        yukleniyorBar.setCanceledOnTouchOutside(false);
        yukleniyorBar.show();

        if (resimUri ==null)
        {
            DatabaseReference veriYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

            String gonderiId = veriYolu.push().getKey();

            String kullaniciAdiAl = kullaniciAdi.getText().toString();
            String kullaniciDurumuAl = kullaniciDurumu.getText().toString();

            HashMap<String,Object> profilHaritasi = new HashMap<>();
            profilHaritasi.put("uid",gonderiId);
            profilHaritasi.put("ad",kullaniciAdiAl);
            profilHaritasi.put("durum",kullaniciDurumuAl);

            veriYolu.child(mevcutKullaniciId).updateChildren(profilHaritasi);

            yukleniyorBar.dismiss();

        }

        else
        {
            final StorageReference resimYolu = KullaniciProfilResimleriYolu.child(mevcutKullaniciId+"."+dosyaUzantisiAl(resimUri));

            yuklemeGorevi = resimYolu.putFile(resimUri);

            yuklemeGorevi.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return resimYolu.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    //Görev tamamlandığında
                    if (task.isSuccessful())
                    {
                        //Başarılıysa
                        Uri indirmeUrisi=task.getResult();
                        myUri=indirmeUrisi.toString();

                        DatabaseReference veriYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

                        String gonderiId = veriYolu.push().getKey();

                        String kullaniciAdiAl = kullaniciAdi.getText().toString();
                        String kullaniciDurumuAl = kullaniciDurumu.getText().toString();

                        HashMap<String,Object> profilHaritasi = new HashMap<>();
                        profilHaritasi.put("uid",gonderiId);
                        profilHaritasi.put("ad",kullaniciAdiAl);
                        profilHaritasi.put("durum",kullaniciDurumuAl);
                        profilHaritasi.put("resim",myUri);

                        veriYolu.child(mevcutKullaniciId).updateChildren(profilHaritasi);


                        yukleniyorBar.dismiss();


                    }
                    else
                    {
                        //Başarısızsa
                        String hata = task.getException().toString();
                        Toast.makeText(AyarlarActivity.this, "Hata: "+hata, Toast.LENGTH_SHORT).show();
                        yukleniyorBar.dismiss();
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(AyarlarActivity.this, "Hata: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    yukleniyorBar.dismiss();

                }
            });


        }




    }
}


