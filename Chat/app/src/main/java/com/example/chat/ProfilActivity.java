package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilActivity extends AppCompatActivity {

    private String alinanKullaniciId,aktifKullaniciId,Aktif_Durum;

    private CircleImageView kullaniciProfilresmi;
    private TextView kullaniciProfilAdi,kullaniciProfilDurumu;
    private Button MesajGondermeTalebibuttonu,MesajDegerlendirmeTalebiButtonu;

    //Firebase
    private DatabaseReference KullaniciYolu,SohbetTalebiYolu,SohbetlerYolu,BildirimYolu;
    private FirebaseAuth mYetki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        alinanKullaniciId=getIntent().getExtras().get("tıklanan_kullanici_Id_goster").toString();

        //Tanımlamalar
        kullaniciProfilresmi=findViewById(R.id.profil_resmi_ziyaret);
        kullaniciProfilAdi=findViewById(R.id.kullanici_adi_ziyaret);
        kullaniciProfilDurumu=findViewById(R.id.profil_durumu_ziyaret);
        MesajGondermeTalebibuttonu=findViewById(R.id.mesaj_gonderme_talebi_buttonu);
        MesajDegerlendirmeTalebiButtonu=findViewById(R.id.mesaj_degerlendirme_talebi_buttonu);

        Aktif_Durum="yeni";

        //Firebase
        KullaniciYolu= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        SohbetTalebiYolu= FirebaseDatabase.getInstance().getReference().child("Sohbet Talebi");
        SohbetlerYolu= FirebaseDatabase.getInstance().getReference().child("Sohbetler");
        BildirimYolu= FirebaseDatabase.getInstance().getReference().child("Bildirimler");
        mYetki=FirebaseAuth.getInstance();

        aktifKullaniciId=mYetki.getCurrentUser().getUid();

        kullaniciBilgisiAl();
    }

    private void kullaniciBilgisiAl() {

        KullaniciYolu.child(alinanKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("resim")))
                {
                    //Veri tabanından verileri çekip değişkenlere aktarma
                    String kullaniciResmi= dataSnapshot.child("resim").getValue().toString();
                    String kullaniciAdi= dataSnapshot.child("ad").getValue().toString();
                    String kullaniciDurumu= dataSnapshot.child("durum").getValue().toString();

                    //Verileri kontrollere aktarme
                    Picasso.get().load(kullaniciResmi).placeholder(R.drawable.profil_resmi).into(kullaniciProfilresmi);
                    kullaniciProfilAdi.setText(kullaniciAdi);
                    kullaniciProfilDurumu.setText(kullaniciDurumu);

                    //Chat talebi gönderme metodu
                    chatTalepleriniYonet();

                }
                else
                {
                    //Veri tabanından verileri çekip değişkenlere aktarma
                    String kullaniciAdi= dataSnapshot.child("ad").getValue().toString();
                    String kullaniciDurumu= dataSnapshot.child("durum").getValue().toString();

                    //Verileri kontrollere aktarme
                    kullaniciProfilAdi.setText(kullaniciAdi);
                    kullaniciProfilDurumu.setText(kullaniciDurumu);

                    //Chat talebi gönderme metodu
                    chatTalepleriniYonet();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void chatTalepleriniYonet() {

        //Talep varsa buton iptali göstersin
        SohbetTalebiYolu.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(alinanKullaniciId))
                {
                    String talep_turu=dataSnapshot.child(alinanKullaniciId).child("talep_turu").getValue().toString();

                    if (talep_turu.equals("gonderildi"))
                    {
                        Aktif_Durum = "talep_gönderildi";
                        MesajGondermeTalebibuttonu.setText("Mesaj Talebi İptal");
                    }
                    else
                    {
                        Aktif_Durum = "talep_alindi";
                        MesajGondermeTalebibuttonu.setText("Mesaj Talebi Kabul");
                        MesajDegerlendirmeTalebiButtonu.setVisibility(View.VISIBLE);
                        MesajDegerlendirmeTalebiButtonu.setEnabled(true);

                        MesajDegerlendirmeTalebiButtonu.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                MesajTalebiIptal();
                            }
                        });
                    }

                }

                else
                {
                    SohbetlerYolu.child(aktifKullaniciId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(alinanKullaniciId))
                                    {
                                        Aktif_Durum = "arkadaşlar";
                                        MesajGondermeTalebibuttonu.setText("Bu sohbeti sil..");
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (aktifKullaniciId.equals(alinanKullaniciId))
        {
            //Buttonu sakla
            MesajGondermeTalebibuttonu.setVisibility(View.INVISIBLE);

        }
        else
        {
            //Mesaj talebi gitsin
            MesajGondermeTalebibuttonu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MesajGondermeTalebibuttonu.setEnabled(false);

                    if (Aktif_Durum.equals("yeni"))

                    {
                        SohbetTalebiGonder();
                    }
                    if (Aktif_Durum.equals("talep_gönderildi"))
                    {
                        MesajTalebiIptal();
                    }
                    if (Aktif_Durum.equals("talep_alindi"))
                    {
                        MesajTalebiKabul();
                    }
                    if (Aktif_Durum.equals("arkadaşlar"))
                    {
                        OzelSohbetiSil();
                    }
                }
            });
        }
    }

    private void OzelSohbetiSil() {

        //Sohbeti Sil
        SohbetlerYolu.child(aktifKullaniciId).child(alinanKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful())
                {
                    //Talebi alandan sil
                    SohbetlerYolu.child(alinanKullaniciId).child(aktifKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                                MesajGondermeTalebibuttonu.setEnabled(true);
                                Aktif_Durum = "yeni";
                                MesajGondermeTalebibuttonu.setText("Mesaj Talebi Gönder");

                                MesajDegerlendirmeTalebiButtonu.setVisibility(View.INVISIBLE);
                                MesajDegerlendirmeTalebiButtonu.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });
    }

    private void MesajTalebiKabul() {

        SohbetlerYolu.child(aktifKullaniciId).child(alinanKullaniciId).child("Sohbetler").setValue("Kaydedildi")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            SohbetlerYolu.child(alinanKullaniciId).child(aktifKullaniciId).child("Sohbetler").setValue("Kaydedildi")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                SohbetTalebiYolu.child(aktifKullaniciId).child(alinanKullaniciId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful())
                                                                {
                                                                    SohbetTalebiYolu.child(alinanKullaniciId).child(aktifKullaniciId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    MesajGondermeTalebibuttonu.setEnabled(true);
                                                                                    Aktif_Durum = "arkadaşlar";
                                                                                    MesajGondermeTalebibuttonu.setText("Bu sohbeti sil");
                                                                                    MesajDegerlendirmeTalebiButtonu.setVisibility(View.INVISIBLE);
                                                                                    MesajDegerlendirmeTalebiButtonu.setEnabled(false);




                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void MesajTalebiIptal() {

        //Talebi gönderenden sil
        SohbetTalebiYolu.child(aktifKullaniciId).child(alinanKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful())
                {
                    //Talebi alandan sil
                    SohbetTalebiYolu.child(alinanKullaniciId).child(aktifKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                                MesajGondermeTalebibuttonu.setEnabled(true);
                                Aktif_Durum = "yeni";
                                MesajGondermeTalebibuttonu.setText("Mesaj Talebi Gönder");

                                MesajDegerlendirmeTalebiButtonu.setVisibility(View.INVISIBLE);
                                MesajDegerlendirmeTalebiButtonu.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });



    }

    private void SohbetTalebiGonder() {

        //Veritabanına veri gönderme
        SohbetTalebiYolu.child(aktifKullaniciId).child(alinanKullaniciId).child("talep_turu").setValue("gonderildi")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            //Veri tabanına veri gönderme
                            SohbetTalebiYolu.child(alinanKullaniciId).child(aktifKullaniciId).child("talep_turu")
                                    .setValue("alındı").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {

                                                //Bildirim
                                                HashMap<String,String> chatBildirimMap=new HashMap<>();
                                                chatBildirimMap.put("kimden",aktifKullaniciId);
                                                chatBildirimMap.put("tur","talep");

                                                //Bildirim Veritabanı yoluna veri gönderme
                                                BildirimYolu.child(alinanKullaniciId).push().setValue(chatBildirimMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful())
                                                        {

                                                            MesajGondermeTalebibuttonu.setEnabled(true);
                                                            Aktif_Durum="talep_gönderildi";
                                                            MesajGondermeTalebibuttonu.setText("Mesaj Talebi İptal");

                                                        }

                                                    }
                                                });



                                            }

                                        }
                                    });
                        }

                    }
                });
    }
}
