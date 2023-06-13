package com.example.chat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat.Adapter.MesajAdaptor;
import com.example.chat.Model.Mesajlar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String IdMesajiAlici, AdMesajiAlici,ResimMesajiAlici,IdMesajGonderen;

    private TextView kullaniciAdi,kullaniciSonGorulmsi;
    private CircleImageView kullaniciResmi;
    private ImageView sohbeteGondermeOku;

    private ImageButton mesajGondermeButtonu,dosyaGondermeButtonu;
    private EditText GirilenMesanMetni;

    //Toolbar
    private Toolbar SohbetToolbar;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference mesajYolu,kullaniciYolu;

    private final List<Mesajlar> mesajlarList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MesajAdaptor mesajAdaptor;
    private RecyclerView kullaniciMesajlariListesi;

    private String kaydedilenAktifZaman, kaydedilenAktifTarih;
    private String kontrolcu="", myUrl="";
    private StorageTask yuklemeGorevi;
    private Uri dosyaUri;

    //Progress
    private ProgressDialog yuklemeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Chat fragmentten gelen Intenti al
        IdMesajiAlici=getIntent().getExtras().get("kullanici_id_ziyaret").toString();
        AdMesajiAlici=getIntent().getExtras().get("kullanici_adi_ziyaret").toString();
        ResimMesajiAlici=getIntent().getExtras().get("resim_ziyaret").toString();


        //Tanımlamalar
        kullaniciAdi=findViewById(R.id.kullanici_adi_gosterme_chat_activity);
        kullaniciSonGorulmsi=findViewById(R.id.kullanici_durumu_gosterme_chat_activity);
        kullaniciResmi=findViewById(R.id.kullanicilar_profil_resmi_chat_activity);
        sohbeteGondermeOku=findViewById(R.id.sohbetsayfasina_gonderme_resmi);
        mesajGondermeButtonu=findViewById(R.id.mesaj_gonder_btn);
        dosyaGondermeButtonu=findViewById(R.id.dosya_gonderme_btn);
        GirilenMesanMetni=findViewById(R.id.girilen_mesaj);

        mesajAdaptor=new MesajAdaptor(mesajlarList);
        kullaniciMesajlariListesi=findViewById(R.id.kullanicilarin_ozel_mesajlarinin_listesi);
        linearLayoutManager=new LinearLayoutManager(this);
        kullaniciMesajlariListesi.setLayoutManager(linearLayoutManager);
        kullaniciMesajlariListesi.setAdapter(mesajAdaptor);

        yuklemeBar=new ProgressDialog(this);

        //TAKVİM
        Calendar calendar = Calendar.getInstance();
        //Tarih formatı
        SimpleDateFormat aktifTarih = new SimpleDateFormat("MMM dd, yyyy");
        kaydedilenAktifTarih=aktifTarih.format(calendar.getTime());
        //Saat formatı
        SimpleDateFormat aktifZaman = new SimpleDateFormat("hh:mm a");
        kaydedilenAktifZaman=aktifZaman.format(calendar.getTime());

        //Firebase
        mYetki=FirebaseAuth.getInstance();
        mesajYolu= FirebaseDatabase.getInstance().getReference();
        kullaniciYolu= FirebaseDatabase.getInstance().getReference();
        IdMesajGonderen=mYetki.getCurrentUser().getUid();


        sohbeteGondermeOku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sohbet = new Intent(ChatActivity.this,MainActivity.class);
                startActivity(sohbet);
            }
        });



        //Kontrollere Intentle gelenleri aktarma
        kullaniciAdi.setText(AdMesajiAlici);
        Picasso.get().load(ResimMesajiAlici).placeholder(R.drawable.profil_resmi).into(kullaniciResmi);

        //Mesaj gönderme butonuna tıklandığında
        mesajGondermeButtonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MesajGonder();

            }
        });

        //Son görülme metodunu çağırma
        SonGorulmeyiGoster();


        //dosya gönderme butonuna tıkladığımızda
        dosyaGondermeButtonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence secenekler [] = new CharSequence[]
                        {
                                "Resimler",
                                "PDF",
                                "WORD"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Dosya Seç");

                builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which==0)
                        {
                            kontrolcu = "resim";

                            //Galeriyi açma
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Resim Seçin"),438);
                        }
                        if(which==1)
                        {

                            kontrolcu ="pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Pdf Seçin"),438);
                        }
                        if(which==2)
                        {

                            kontrolcu = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Word Dosyası Seçin"),438);

                        }

                    }
                });

                builder.show();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            //Progress
            yuklemeBar.setTitle("Dosya gönderiliyor");
            yuklemeBar.setMessage("Lütfen bekleyin..");
            yuklemeBar.setCanceledOnTouchOutside(false);
            yuklemeBar.show();



            dosyaUri=data.getData();

            if (!kontrolcu.equals("resim"))
            {

                StorageReference depolamaYolu = FirebaseStorage.getInstance().getReference().child("Dokuman Dosyalari");

                final String mesajGonderenYolu="Mesajlar/"+IdMesajGonderen+"/"+IdMesajiAlici;
                final String mesajAlanYolu="Mesajlar/"+IdMesajiAlici+"/"+IdMesajGonderen;

                DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici).push();

                final String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();

                final StorageReference dosyaYolu=depolamaYolu.child(mesajEklemeId +"."+ kontrolcu);

                yuklemeGorevi=dosyaYolu.putFile(dosyaUri);

                yuklemeGorevi.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful())
                        {

                            throw  task.getException();

                        }



                        return dosyaYolu.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener <Uri>() {
                    @Override
                    public void onComplete(@NonNull Task <Uri> task) {

                        if (task.isSuccessful())
                        {
                            Uri indirmeUrl = task.getResult();
                            myUrl=indirmeUrl.toString();


                            Map mesajMetniGovdesi = new HashMap();
                            mesajMetniGovdesi.put("mesaj",myUrl);
                            mesajMetniGovdesi.put("ad",dosyaUri.getLastPathSegment());
                            mesajMetniGovdesi.put("tur",kontrolcu);
                            mesajMetniGovdesi.put("kimden",IdMesajGonderen);
                            mesajMetniGovdesi.put("kime",IdMesajiAlici);
                            mesajMetniGovdesi.put("mesajID",mesajEklemeId);
                            mesajMetniGovdesi.put("zaman",kaydedilenAktifZaman);
                            mesajMetniGovdesi.put("tarih",kaydedilenAktifTarih);

                            Map mesajGovdesiDetaylari = new HashMap();
                            mesajGovdesiDetaylari.put(mesajGonderenYolu+"/"+mesajEklemeId,mesajMetniGovdesi);
                            mesajGovdesiDetaylari.put(mesajAlanYolu+"/"+mesajEklemeId,mesajMetniGovdesi);

                            mesajYolu.updateChildren(mesajGovdesiDetaylari).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful())
                                    {
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj Gönderildi!", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj gönderme hatalı!!!", Toast.LENGTH_SHORT).show();
                                    }

                                    GirilenMesanMetni.setText("");

                                }
                            });
                        }
                    }
                });

            }
            else if(kontrolcu.equals("resim"))
            {
                StorageReference depolamaYolu = FirebaseStorage.getInstance().getReference().child("Resim Dosyalari");

                final String mesajGonderenYolu="Mesajlar/"+IdMesajGonderen+"/"+IdMesajiAlici;
                final String mesajAlanYolu="Mesajlar/"+IdMesajiAlici+"/"+IdMesajGonderen;

                DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici).push();

                final String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();

                final StorageReference dosyaYolu=depolamaYolu.child(mesajEklemeId +"."+ "jpg");

                yuklemeGorevi=dosyaYolu.putFile(dosyaUri);

                yuklemeGorevi.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful())
                        {

                            throw  task.getException();

                        }



                        return dosyaYolu.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener <Uri>() {
                    @Override
                    public void onComplete(@NonNull Task <Uri> task) {

                        if (task.isSuccessful())
                        {
                            Uri indirmeUrl = task.getResult();
                            myUrl=indirmeUrl.toString();


                            Map mesajMetniGovdesi = new HashMap();
                            mesajMetniGovdesi.put("mesaj",myUrl);
                            mesajMetniGovdesi.put("ad",dosyaUri.getLastPathSegment());
                            mesajMetniGovdesi.put("tur",kontrolcu);
                            mesajMetniGovdesi.put("kimden",IdMesajGonderen);
                            mesajMetniGovdesi.put("kime",IdMesajiAlici);
                            mesajMetniGovdesi.put("mesajID",mesajEklemeId);
                            mesajMetniGovdesi.put("zaman",kaydedilenAktifZaman);
                            mesajMetniGovdesi.put("tarih",kaydedilenAktifTarih);

                            Map mesajGovdesiDetaylari = new HashMap();
                            mesajGovdesiDetaylari.put(mesajGonderenYolu+"/"+mesajEklemeId,mesajMetniGovdesi);
                            mesajGovdesiDetaylari.put(mesajAlanYolu+"/"+mesajEklemeId,mesajMetniGovdesi);

                            mesajYolu.updateChildren(mesajGovdesiDetaylari).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful())
                                    {
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj Gönderildi!", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj gönderme hatalı!!!", Toast.LENGTH_SHORT).show();
                                    }

                                    GirilenMesanMetni.setText("");

                                }
                            });
                        }
                    }
                });

            }
            else
            {
                yuklemeBar.dismiss();
                Toast.makeText(this, "Hata: Öge seçilemedi!", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private  void SonGorulmeyiGoster()
    {
        kullaniciYolu.child("Kullanicilar").child(IdMesajiAlici).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Veri tabanından kullanıcı durumuna yönelik verileri çekme
                if(dataSnapshot.child("kullaniciDurumu").hasChild("durum"))
                {
                    String durum = dataSnapshot.child("kullaniciDurumu").child("durum").getValue().toString();
                    String tarih = dataSnapshot.child("kullaniciDurumu").child("tarih").getValue().toString();
                    String zaman = dataSnapshot.child("kullaniciDurumu").child("zaman").getValue().toString();

                    if (durum.equals("çevrimiçi"))
                    {
                        kullaniciSonGorulmsi.setText("çevrimiçi");
                    }

                    else if (durum.equals("çevrimdışı"))
                    {
                        kullaniciSonGorulmsi.setText("Son görülme: "+ tarih +" "+ zaman );
                    }
                }

                else
                {
                    kullaniciSonGorulmsi.setText("çevrimdışı");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {

        super.onStart();

        //Veri tabanından verileri çekme
        mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        //Veri tabanından veriyi alıp modele aktarma
                        Mesajlar mesajlar = dataSnapshot.getValue(Mesajlar.class);

                        //Modeli listeye ekleme
                        mesajlarList.add(mesajlar);

                        mesajAdaptor.notifyDataSetChanged();

                        //Scrollview ayarlama
                        kullaniciMesajlariListesi.smoothScrollToPosition(kullaniciMesajlariListesi.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    private void MesajGonder() {

        //Mesajı kontrolden alma
        String mesajMetni = GirilenMesanMetni.getText().toString();

        if (TextUtils.isEmpty(mesajMetni))
        {
            Toast.makeText(this, "Mesaj yazmanız gerekiyor!", Toast.LENGTH_SHORT).show();
        }

        else
        {
            String mesajGonderenYolu="Mesajlar/"+IdMesajGonderen+"/"+IdMesajiAlici;
            String mesajAlanYolu="Mesajlar/"+IdMesajiAlici+"/"+IdMesajGonderen;

            DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(IdMesajGonderen).child(IdMesajiAlici).push();

            String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();

            Map mesajMetniGovdesi = new HashMap();
            mesajMetniGovdesi.put("mesaj",mesajMetni);
            mesajMetniGovdesi.put("tur","metin");
            mesajMetniGovdesi.put("kimden",IdMesajGonderen);
            mesajMetniGovdesi.put("kime",IdMesajiAlici);
            mesajMetniGovdesi.put("mesajID",mesajEklemeId);
            mesajMetniGovdesi.put("zaman",kaydedilenAktifZaman);
            mesajMetniGovdesi.put("tarih",kaydedilenAktifTarih);

            Map mesajGovdesiDetaylari = new HashMap();
            mesajGovdesiDetaylari.put(mesajGonderenYolu+"/"+mesajEklemeId,mesajMetniGovdesi);
            mesajGovdesiDetaylari.put(mesajAlanYolu+"/"+mesajEklemeId,mesajMetniGovdesi);

            mesajYolu.updateChildren(mesajGovdesiDetaylari).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Mesaj Gönderildi!", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Mesaj gönderme hatalı!!!", Toast.LENGTH_SHORT).show();
                    }

                    GirilenMesanMetni.setText("");

                }
            });
        }
    }
}


