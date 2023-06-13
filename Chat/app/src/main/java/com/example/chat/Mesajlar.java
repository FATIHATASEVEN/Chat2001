package com.example.chat;

public class Mesajlar {
    private String kimden, mesaj, tur, kime, mesajID,zaman,tarih,ad;

    public Mesajlar() {
    }

    public Mesajlar(String kimden, String mesaj, String tur, String kime, String mesajID, String zaman, String tarih, String ad) {
        this.kimden = kimden;
        this.mesaj = mesaj;
        this.tur = tur;
        this.kime = kime;
        this.mesajID = mesajID;
        this.zaman = zaman;
        this.tarih = tarih;
        this.ad = ad;
    }

    public String getKimden() {
        return kimden;
    }

    public void setKimden(String kimden) {
        this.kimden = kimden;
    }

    public String getMesaj() {
        return mesaj;
    }

    public void setMesaj(String mesaj) {
        this.mesaj = mesaj;
    }

    public String getTur() {
        return tur;
    }

    public void setTur(String tur) {
        this.tur = tur;
    }

    public String getKime() {
        return kime;
    }

    public void setKime(String kime) {
        this.kime = kime;
    }

    public String getMesajID() {
        return mesajID;
    }

    public void setMesajID(String mesajID) {
        this.mesajID = mesajID;
    }

    public String getZaman() {
        return zaman;
    }

    public void setZaman(String zaman) {
        this.zaman = zaman;
    }

    public String getTarih() {
        return tarih;
    }

    public void setTarih(String tarih) {
        this.tarih = tarih;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }
}
