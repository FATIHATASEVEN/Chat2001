package com.example.chat;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat.MainActivity;
import com.example.chat.Model.Mesajlar;
import com.example.chat.R;
import com.example.chat.ResimGostermeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MesajAdaptor extends RecyclerView.Adapter<MesajAdaptor.MesajlarViewHolder>
{
    private List<Mesajlar> kullaniciMesajlariListesi;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference kullanicilarYolu;

    //Adaptör
    public MesajAdaptor (List<Mesajlar> kullaniciMesajlariListesi)
    {
        this.kullaniciMesajlariListesi=kullaniciMesajlariListesi;
    }

    //ViewHolder
    public class MesajlarViewHolder extends RecyclerView.ViewHolder
    {

        //Ozel mesajlar layouttaki kontroller
        public TextView gonderenMesajMetni,aliciMesajMetni;
        public CircleImageView aliciProfilResmi;
        public ImageView mesajGonderenResim, mesajAlanResim;

        public MesajlarViewHolder(@NonNull View itemView) {
            super(itemView);

            //Ozel mesajlar layouttaki kontrol tanımlamaları
            aliciMesajMetni=itemView.findViewById(R.id.alici_mesaj_metni);
            gonderenMesajMetni=itemView.findViewById(R.id.gonderen_mesaj_metni);
            aliciProfilResmi=itemView.findViewById(R.id.mesaj_profil_resmi);
            mesajGonderenResim=itemView.findViewById(R.id.mesaj_gonderen_image_view);
            mesajAlanResim=itemView.findViewById(R.id.mesaj_alan_image_view);
        }
    }

    @NonNull
    @Override
    public MesajlarViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ozel_mesajlar_layout,viewGroup,false);

        //Firebase tanımlama
        mYetki=FirebaseAuth.getInstance();

        return new MesajlarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MesajlarViewHolder mesajlarViewHolder, final int i)
    {
        String mesajGonderenId=mYetki.getCurrentUser().getUid();

        //Model tanımlama
        Mesajlar mesajlar = kullaniciMesajlariListesi.get(i);

        String kimdenKullaniciId = mesajlar.getKimden();
        String kimdenMesajTuru = mesajlar.getTur();

        //Veritabanı yolu
        kullanicilarYolu= FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(kimdenKullaniciId);

        //Firebaseden veri çekme
        kullanicilarYolu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("resim"))
                {
                    //Veritabanından resmi değişkene aktarma
                    String resimAlici=dataSnapshot.child("resim").getValue().toString();

                    //Kontrole resmi aktarma
                    Picasso.get().load(resimAlici).placeholder(R.drawable.profil_resmi).into(mesajlarViewHolder.aliciProfilResmi);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //Görünmez yapma
        mesajlarViewHolder.aliciMesajMetni.setVisibility(View.GONE);
        mesajlarViewHolder.aliciProfilResmi.setVisibility(View.GONE);
        mesajlarViewHolder.gonderenMesajMetni.setVisibility(View.GONE);
        mesajlarViewHolder.mesajGonderenResim.setVisibility(View.GONE);
        mesajlarViewHolder.mesajAlanResim.setVisibility(View.GONE);

        if (kimdenMesajTuru.equals("metin"))
        {
            if (kimdenKullaniciId.equals(mesajGonderenId))
            {
                mesajlarViewHolder.gonderenMesajMetni.setVisibility(View.VISIBLE);
                mesajlarViewHolder.gonderenMesajMetni.setBackgroundResource(R.drawable.gonderen_mesajlari_layout);
                mesajlarViewHolder.gonderenMesajMetni.setTextColor(Color.BLACK);
                mesajlarViewHolder.gonderenMesajMetni.setText(mesajlar.getMesaj()+"\n\n"+mesajlar.getZaman()+ "-"+mesajlar.getTarih());
            }

            else
            {


                //Görünür yapma
                mesajlarViewHolder.aliciProfilResmi.setVisibility(View.VISIBLE);
                mesajlarViewHolder.aliciMesajMetni.setVisibility(View.VISIBLE);

                mesajlarViewHolder.aliciMesajMetni.setBackgroundResource(R.drawable.alici_mesajlari_layout);
                mesajlarViewHolder.aliciMesajMetni.setTextColor(Color.BLACK);
                mesajlarViewHolder.aliciMesajMetni.setText(mesajlar.getMesaj()+"\n"+mesajlar.getZaman()+ "/"+mesajlar.getTarih());

            }
        }

        else if (kimdenMesajTuru.equals("resim"))
        {
            if (kimdenKullaniciId.equals(mesajGonderenId))
            {

                mesajlarViewHolder.mesajGonderenResim.setVisibility(View.VISIBLE);

                Picasso.get().load(mesajlar.getMesaj()).into(mesajlarViewHolder.mesajGonderenResim);
            }
            else
            {
                mesajlarViewHolder.aliciProfilResmi.setVisibility(View.VISIBLE);
                mesajlarViewHolder.mesajAlanResim.setVisibility(View.VISIBLE);

                Picasso.get().load(mesajlar.getMesaj()).into(mesajlarViewHolder.mesajAlanResim);

            }
        }
        else if(kimdenMesajTuru.equals("pdf")|| kimdenMesajTuru.equals("docx"))
        {
            if (kimdenKullaniciId.equals(mesajGonderenId))
            {
                mesajlarViewHolder.mesajGonderenResim.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-283c2.appspot.com/o/Resim%20Dosyalari%2Fdosya.png?alt=media&token=72fbd065-8c5e-4dc6-ad9e-f7f19dc58d77")
                        .into(mesajlarViewHolder.mesajGonderenResim);

            }
            else
            {
                mesajlarViewHolder.aliciProfilResmi.setVisibility(View.VISIBLE);
                mesajlarViewHolder.mesajAlanResim.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-283c2.appspot.com/o/Resim%20Dosyalari%2Fdosya.png?alt=media&token=72fbd065-8c5e-4dc6-ad9e-f7f19dc58d77")
                        .into(mesajlarViewHolder.mesajAlanResim);

            }
        }


        if (kimdenKullaniciId.equals(mesajGonderenId))
        {
            mesajlarViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (kullaniciMesajlariListesi.get(i).getTur().equals("pdf")||kullaniciMesajlariListesi.get(i).getTur().equals("docx"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden sil",
                                        "Bu belgeyi indir ve görüntüle",
                                        "İptal",
                                        "Herkesten sil"


                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(mesajlarViewHolder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin mi?");

                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0)
                                {
                                    //Benden sil
                                    gonderilenMesajiSil(i,mesajlarViewHolder);
                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), MainActivity.class);
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }
                                if (which == 1)
                                {
                                    //Belge indir görüntüle

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kullaniciMesajlariListesi.get(i).getMesaj()));
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }
                                if (which == 2)
                                {
                                    //İptal

                                }
                                if (which == 3)
                                {
                                    //Herkesten sil
                                    mesajiHerkestenSil(i,mesajlarViewHolder);
                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), MainActivity.class);
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }

                            }
                        });

                        builder.show();

                    }

                    else if (kullaniciMesajlariListesi.get(i).getTur().equals("metin"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden sil",
                                        "İptal",
                                        "Herkesten sil"


                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(mesajlarViewHolder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin mi?");

                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0)
                                {
                                    //Benden sil
                                    gonderilenMesajiSil(i,mesajlarViewHolder);
                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), MainActivity.class);
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }



                                else if (which == 2)
                                {
                                    //Herkesten sil
                                    mesajiHerkestenSil(i,mesajlarViewHolder);
                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), MainActivity.class);
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }

                            }
                        });

                        builder.show();

                    }

                    else if (kullaniciMesajlariListesi.get(i).getTur().equals("resim"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden sil",
                                        "Bu resmi görüntüle",
                                        "İptal",
                                        "Herkesten sil"


                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(mesajlarViewHolder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin mi?");

                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0)
                                {
                                    //Benden sil
                                    gonderilenMesajiSil(i,mesajlarViewHolder);
                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), MainActivity.class);
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }

                                else if (which == 1)
                                {
                                    //Bu resmi görüntüle
                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), ResimGostermeActivity.class);
                                    intent.putExtra("url",kullaniciMesajlariListesi.get(i).getMesaj());
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }



                                else if (which == 3)
                                {
                                    //Herkesten sil
                                    mesajiHerkestenSil(i,mesajlarViewHolder);
                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), MainActivity.class);
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }

                            }
                        });

                        builder.show();

                    }
                }
            });
        }
        else
        {
            //Alan kısmı
            mesajlarViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (kullaniciMesajlariListesi.get(i).getTur().equals("pdf")||kullaniciMesajlariListesi.get(i).getTur().equals("docx"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden sil",
                                        "Bu belgeyi indir ve görüntüle",
                                        "İptal"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(mesajlarViewHolder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin mi?");

                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0)
                                {
                                    //Benden sil
                                    alinanMesajiSil(i,mesajlarViewHolder);
                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), MainActivity.class);
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }
                                if (which == 1)
                                {
                                    //Belge indir görüntüle
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kullaniciMesajlariListesi.get(i).getMesaj()));
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }
                                if (which == 2)
                                {
                                    //İptal

                                }
                            }
                        });

                        builder.show();

                    }

                    else if (kullaniciMesajlariListesi.get(i).getTur().equals("metin"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden sil",
                                        "İptal"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(mesajlarViewHolder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin mi?");

                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0)
                                {
                                    //Benden sil
                                    alinanMesajiSil(i,mesajlarViewHolder);

                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), MainActivity.class);
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);


                                }
                            }
                        });

                        builder.show();

                    }

                    else if (kullaniciMesajlariListesi.get(i).getTur().equals("resim"))
                    {
                        CharSequence secenekler [] = new CharSequence[]
                                {
                                        "Benden sil",
                                        "Bu resmi görüntüle",
                                        "İptal"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(mesajlarViewHolder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin mi?");

                        builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0)
                                {
                                    //Benden sil
                                    alinanMesajiSil(i,mesajlarViewHolder);
                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), MainActivity.class);
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }

                                if (which == 1)
                                {
                                    //Bu resmi görüntüle
                                    Intent intent = new Intent(mesajlarViewHolder.itemView.getContext(), ResimGostermeActivity.class);
                                    intent.putExtra("url",kullaniciMesajlariListesi.get(i).getMesaj());
                                    mesajlarViewHolder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });

                        builder.show();

                    }
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return kullaniciMesajlariListesi.size();
    }


    private void gonderilenMesajiSil(final int pozisyon, final MesajlarViewHolder holder)
    {
        DatabaseReference mesajYolu = FirebaseDatabase.getInstance().getReference();
        mesajYolu.child("Mesajlar")
                .child(kullaniciMesajlariListesi.get(pozisyon).getKimden())
                .child(kullaniciMesajlariListesi.get(pozisyon).getKime())
                .child(kullaniciMesajlariListesi.get(pozisyon).getMesajID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            Toast.makeText(holder.itemView.getContext(), "Silme işlemi başarılı", Toast.LENGTH_LONG).show();

                        }
                        else
                        {
                            Toast.makeText(holder.itemView.getContext(), "Silme hatası!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }



    private void alinanMesajiSil(final int pozisyon, final MesajlarViewHolder holder)
    {
        DatabaseReference mesajYolu = FirebaseDatabase.getInstance().getReference();
        mesajYolu.child("Mesajlar")
                .child(kullaniciMesajlariListesi.get(pozisyon).getKime())
                .child(kullaniciMesajlariListesi.get(pozisyon).getKimden())
                .child(kullaniciMesajlariListesi.get(pozisyon).getMesajID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            Toast.makeText(holder.itemView.getContext(), "Silme işlemi başarılı", Toast.LENGTH_LONG).show();

                        }
                        else
                        {
                            Toast.makeText(holder.itemView.getContext(), "Silme hatası!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    private void mesajiHerkestenSil(final int pozisyon, final MesajlarViewHolder holder)
    {
        final DatabaseReference mesajYolu = FirebaseDatabase.getInstance().getReference();
        mesajYolu.child("Mesajlar")
                .child(kullaniciMesajlariListesi.get(pozisyon).getKime())
                .child(kullaniciMesajlariListesi.get(pozisyon).getKimden())
                .child(kullaniciMesajlariListesi.get(pozisyon).getMesajID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            mesajYolu.child("Mesajlar")
                                    .child(kullaniciMesajlariListesi.get(pozisyon).getKimden())
                                    .child(kullaniciMesajlariListesi.get(pozisyon).getKime())
                                    .child(kullaniciMesajlariListesi.get(pozisyon).getMesajID())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(holder.itemView.getContext(), "Silme işlemi başarılı", Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });



                        }
                        else
                        {
                            Toast.makeText(holder.itemView.getContext(), "Silme hatası!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


}




