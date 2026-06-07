
package com.showdown;

import java.util.ArrayList;
import java.util.Collections;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

// Oyunun kurallarını, puanı ve canı takip ettiğimiz beyni olan sınıf. Arayüzden tamamen bağımsız çalışır.
public class OyunMotoru {
    private ArrayList<Kart> kartListesi;
    private int puan;
    private int kalanSure;
    private int can; 
    private boolean oyunBittiMi; 
    private Timeline zamanlayici;
    private String zorlukSeviyesi;
    private int eslesenCiftSayisi;
    private int toplamCift;
    
    // Arayüzü tetiklemek için kullandığımız Runnable yapıları.
    private Runnable arayuzGuncelle;
    private Runnable oyunBittiIslemi;

    public OyunMotoru(String zorlukSeviyesi, Runnable arayuzGuncelle, Runnable oyunBittiIslemi) {
        this.zorlukSeviyesi = zorlukSeviyesi;
        this.kartListesi = new ArrayList<>();
        this.puan = 0;
        this.can = 3; // Başlangıçta 3 hata hakkı var.
        this.oyunBittiMi = false;
        this.eslesenCiftSayisi = 0;
        this.arayuzGuncelle = arayuzGuncelle;
        this.oyunBittiIslemi = oyunBittiIslemi;
        
        // Kullanıcının seçtiği zorluğa göre süre ve kart sayısını ayarlıyoruz.
        if (zorlukSeviyesi.equalsIgnoreCase("Kolay")) {
            this.kalanSure = -1; // -1 demek sınırsız süre demek.
            this.toplamCift = 6;
        } else if (zorlukSeviyesi.equalsIgnoreCase("Orta")) {
            this.kalanSure = 45;
            this.toplamCift = 10;
        } else {
            this.kalanSure = 30; // Zor modda süre daha az.
            this.toplamCift = 15;
        }
        
        kartlariHazirla();
    }

    // Seçilen moda göre resimlerin adlarını (örn: zor_1.jpg) bulup desteyi hazırlayan metod.
    private void kartlariHazirla() {
        String onEk = "";
        String uzanti = ""; 

        if (zorlukSeviyesi.equalsIgnoreCase("Kolay")) {
            onEk = "kolay_"; uzanti = ".jpg";
        } else if (zorlukSeviyesi.equalsIgnoreCase("Orta")) {
            onEk = "orta_"; uzanti = ".jpeg"; // Orta moddaki resimlerin uzantısı farklıydı.
        } else {
            onEk = "zor_"; uzanti = ".jpg";
        }

        // Resimlerin yolunu oluşturup 2'şer tane karta ekliyoruz (eşleştirmek için).
        for (int i = 1; i <= toplamCift; i++) {
            String kimlikVerisi = onEk + i; 
            String yol = "resimler/" + kimlikVerisi + uzanti; 
            kartListesi.add(new Kart(kimlikVerisi, yol));
            kartListesi.add(new Kart(kimlikVerisi, yol));
        }
        // Her oyunda kartların yerleri farklı olsun diye karıştırıyoruz.
        Collections.shuffle(kartListesi);
    }

    // Tıklanan iki kartın eşleşip eşleşmediğini kontrol eden mekanizmamız.
    public boolean eslesmeKontrolEt(Kart k1, Kart k2) {
        if (oyunBittiMi) return false;

        if (k1.getKimlik().equals(k2.getKimlik())) {
            // Doğru bildiyse: Puanı 10 artır ve kartları kilitle.
            k1.setEslestiMi(true);
            k2.setEslestiMi(true);
            puan += 10;
            eslesenCiftSayisi++;
            
            if (eslesenCiftSayisi == toplamCift) {
                oyunBitir(); // Tüm çiftler bulunduysa oyunu bitir.
            }
            return true;
        } else {
            // Yanlış bildiyse: Puanı 2 azalt ve 1 can düş.
            puan -= 2;
            can--; 
            if (can <= 0) {
                oyunBitir(); // Can 0 olduysa oyunu bitir.
            }
            return false;
        }
    }

    // Oyunu kapatıp süreyi durduran yardımcı metodumuz.
    private void oyunBitir() {
        if (oyunBittiMi) return; 
        oyunBittiMi = true;
        
        if (zamanlayici != null) zamanlayici.stop();
        
        oyunBittiIslemi.run(); 
    }

    // JavaFX Timeline ile saniyede bir çalışan arka plan sayacımız.
    public void zamanlayiciyiBaslat() {
        if (kalanSure == -1) return; // Süre sınırsızsa sayacı çalıştırmaya gerek yok.

        zamanlayici = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (oyunBittiMi) return;

            kalanSure--;
            arayuzGuncelle.run(); // Arayüze "süreyi güncelle" diyoruz.
            
            if (kalanSure <= 0) {
                oyunBitir(); // Süre sıfırlanınca oyunu bitiriyoruz.
            }
        }));
        zamanlayici.setCycleCount(Timeline.INDEFINITE); // Biz durdurana kadar sonsuza dek çalışır.
        zamanlayici.play();
    }
    
    public void zamanlayiciyiDurdur() {
        if (zamanlayici != null) zamanlayici.stop();
    }

    // Verilere güvenli şekilde ulaşmak için Getter metodlarımız.
    public ArrayList<Kart> getKartListesi() { return kartListesi; }
    public int getPuan() { return puan; }
    public int getKalanSure() { return kalanSure; }
    public int getCan() { return can; }
    public boolean isOyunBittiMi() { return oyunBittiMi; }
    public boolean isKazandiMi() { return eslesenCiftSayisi == toplamCift; }
}