package com.example.chat;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chat.Model.Kisiler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class KisilerFragment extends Fragment {

    private View KisilerView;

    private RecyclerView kisilerListem;

    //Firebase
    private DatabaseReference SohbetlerYolu,KullanıcılarYolu;
    private FirebaseAuth mYetki;


    private String aktifKullaniciId;


    public KisilerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        KisilerView= inflater.inflate(R.layout.fragment_kisiler, container, false);

        //Recycler
        kisilerListem= KisilerView.findViewById(R.id.kisiler_listesi);
        kisilerListem.setLayoutManager(new LinearLayoutManager(getContext()));

        //Firebase

        mYetki=FirebaseAuth.getInstance();

        aktifKullaniciId=mYetki.getCurrentUser().getUid();
        SohbetlerYolu= FirebaseDatabase.getInstance().getReference().child("Sohbetler").child(aktifKullaniciId);
        KullanıcılarYolu= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        return KisilerView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions secenekler = new FirebaseRecyclerOptions.Builder<Kisiler>()
                .setQuery(SohbetlerYolu,Kisiler.class)
                .build();

        //Adapter
        FirebaseRecyclerAdapter<Kisiler,KisilerViewHolder>adapter = new FirebaseRecyclerAdapter<Kisiler, KisilerViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull final KisilerViewHolder holder, int position, @NonNull Kisiler model) {

                String tıklananSatırKullaniciIdsi = getRef(position).getKey();

                KullanıcılarYolu.child(tıklananSatırKullaniciIdsi).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists())
                        {

                            //Veri tabanından kullanıcı durumuna yönelik verileri çekme
                            if(dataSnapshot.child("kullaniciDurumu").hasChild("durum"))
                            {
                                String durum = dataSnapshot.child("kullaniciDurumu").child("durum").getValue().toString();
                                String tarih = dataSnapshot.child("kullaniciDurumu").child("tarih").getValue().toString();
                                String zaman = dataSnapshot.child("kullaniciDurumu").child("zaman").getValue().toString();

                                if (durum.equals("çevrimiçi"))
                                {
                                    holder.cevrimIciIkonu.setVisibility(View.VISIBLE);
                                }

                                else if (durum.equals("çevrimdışı"))
                                {
                                    holder.cevrimIciIkonu.setVisibility(View.INVISIBLE);
                                }
                            }

                            else
                            {
                                holder.cevrimIciIkonu.setVisibility(View.INVISIBLE);
                            }



                            if (dataSnapshot.hasChild("resim"))
                            {
                                //Verileri firebaseden çekme
                                String profilResmi = dataSnapshot.child("resim").getValue().toString();
                                String kullaniciAdi = dataSnapshot.child("ad").getValue().toString();
                                String kullaniciDurumu = dataSnapshot.child("durum").getValue().toString();

                                //Kontrollere veri aktaarımı
                                holder.kullaniciAdi.setText(kullaniciAdi);
                                holder.kullaniciDurumu.setText(kullaniciDurumu);
                                Picasso.get().load(profilResmi).placeholder(R.drawable.profil_resmi).into(holder.profilResmi);
                            }

                            else
                            {
                                //Verileri firebaseden çekme
                                String kullaniciAdi = dataSnapshot.child("ad").getValue().toString();
                                String kullaniciDurumu = dataSnapshot.child("durum").getValue().toString();

                                //Kontrollere veri aktaarımı
                                holder.kullaniciAdi.setText(kullaniciAdi);
                                holder.kullaniciDurumu.setText(kullaniciDurumu);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public KisilerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.kullanici_gosterme_layout,viewGroup,false);

                KisilerViewHolder viewHolder = new KisilerViewHolder(view);

                return  viewHolder;

            }
        };

        kisilerListem.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();
    }

    public static class KisilerViewHolder extends RecyclerView.ViewHolder{

        //Kontroller
        TextView kullaniciAdi,kullaniciDurumu;
        CircleImageView profilResmi;
        ImageView cevrimIciIkonu;


        public KisilerViewHolder(@NonNull View itemView) {
            super(itemView);

            //Kontrol tanımlamaları
            kullaniciAdi=itemView.findViewById(R.id.kullanici_profil_adi);
            kullaniciDurumu=itemView.findViewById(R.id.kullanici_durumu);
            profilResmi=itemView.findViewById(R.id.kullanicilar_profil_resmi);
            cevrimIciIkonu=itemView.findViewById(R.id.kullanici_cevrimici_olma_durumu);
        }
    }
}
