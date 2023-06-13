package com.example.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class TelafonlaOturumActivity extends AppCompatActivity {

    private Button DogrulamaKoduGondermeButtonu,DogrulaButtonu;
    private EditText TelefonNumarasıGirdisi, DogrulamaKoduGirdisi;

    //Telefon doğrulama
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mDogrulamaId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    //Firebase
    FirebaseAuth mYetki;

    //Yükleniyor penceresi
    private ProgressDialog yuklemeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telafonla_oturum);

        //Tanımlamalar
        DogrulamaKoduGondermeButtonu=findViewById(R.id.dogrulama_kodu_gonder_buttonu);
        DogrulaButtonu=findViewById(R.id.dogrulama_buttonu);

        TelefonNumarasıGirdisi=findViewById(R.id.telefon_numarası_girdi);
        DogrulamaKoduGirdisi=findViewById(R.id.dogrulama_kodu_girdisi);

        //Progres dialog tanımlama
        yuklemeBar=new ProgressDialog(this);

        //Firebase tanımlama
        mYetki=FirebaseAuth.getInstance();

        DogrulamaKoduGondermeButtonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String telefonNumarası=TelefonNumarasıGirdisi.getText().toString();

                if (TextUtils.isEmpty(telefonNumarası))
                {
                    Toast.makeText(TelafonlaOturumActivity.this, "Telefon numarası boş olamaz!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    //Yükleniyor penceresi
                    yuklemeBar.setTitle("Telefonla Doğrulama");
                    yuklemeBar.setMessage("Lütfen bekleyin...");
                    yuklemeBar.setCanceledOnTouchOutside(false);
                    yuklemeBar.show();


                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            telefonNumarası,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            TelafonlaOturumActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks

                }
            }
        });

        DogrulaButtonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Görünürlük ayarlaması
                DogrulamaKoduGondermeButtonu.setVisibility(View.INVISIBLE);
                TelefonNumarasıGirdisi.setVisibility(View.INVISIBLE);

                String dogrulamaKodu = DogrulamaKoduGirdisi.getText().toString();

                if (TextUtils.isEmpty(dogrulamaKodu))
                {
                    Toast.makeText(TelafonlaOturumActivity.this, "Doğrulama kodu boş olamaz!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    //Yükleniyor penceresi
                    yuklemeBar.setTitle("Kodla Doğrulama");
                    yuklemeBar.setMessage("Lütfen bekleyin...");
                    yuklemeBar.setCanceledOnTouchOutside(false);
                    yuklemeBar.show();


                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mDogrulamaId, dogrulamaKodu);
                    telefonlaGirisYap(credential);

                }


            }
        });

        callbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)

            {

                telefonlaGirisYap(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e)

            {
                //Yukleme penceresi
                yuklemeBar.dismiss();


                Toast.makeText(TelafonlaOturumActivity.this, "Geçersizi telefon numarası!Lütfen ülke kodunuzla birlikte tekrar numaranızı girin..", Toast.LENGTH_LONG).show();

                //Görünürlük ayarlaması
                DogrulamaKoduGondermeButtonu.setVisibility(View.VISIBLE);
                DogrulaButtonu.setVisibility(View.INVISIBLE);

                TelefonNumarasıGirdisi.setVisibility(View.VISIBLE);
                DogrulamaKoduGirdisi.setVisibility(View.INVISIBLE);


            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {


                // Save verification ID and resending token so we can use them later
                mDogrulamaId = verificationId;
                mResendToken = token;

                //Yukleme penceresi
                yuklemeBar.dismiss();

                Toast.makeText(TelafonlaOturumActivity.this, "Kod gönderildi!", Toast.LENGTH_LONG).show();

                //Görünürlük ayarlaması
                DogrulamaKoduGondermeButtonu.setVisibility(View.INVISIBLE);
                DogrulaButtonu.setVisibility(View.VISIBLE);

                TelefonNumarasıGirdisi.setVisibility(View.INVISIBLE);
                DogrulamaKoduGirdisi.setVisibility(View.VISIBLE);



            }
        };

    }

    private void telefonlaGirisYap (PhoneAuthCredential credential) {
        mYetki.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            yuklemeBar.dismiss();
                            Toast.makeText(TelafonlaOturumActivity.this, "Tebrikler..Oturumunuz açıldı..", Toast.LENGTH_LONG).show();
                            KullaniciyiAnaSayfayaGonder();

                        }
                        else {

                            String hataMesaji=task.getException().toString();

                            Toast.makeText(TelafonlaOturumActivity.this, "Hata: "+hataMesaji, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void KullaniciyiAnaSayfayaGonder() {

        Intent anasayfa = new Intent(TelafonlaOturumActivity.this,MainActivity.class);
        anasayfa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(anasayfa);
        finish();
    }
}
