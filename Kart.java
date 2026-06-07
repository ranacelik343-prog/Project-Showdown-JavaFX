
package com.showdown;

import javafx.animation.RotateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

// Oyundaki her bir kartı temsil eden sınıfımız. StackPane ile arka ve ön yüzü üst üste koyacağız.
public class Kart extends StackPane {
    
    // Kartın verilerini dışarıdan değiştirilmesin diye private (kapsülleme) yaptık.
    private String kimlik;      
    private String gorselYolu;  
    private boolean acikMi;     
    private boolean eslestiMi;  

    // Resmin kendisi ve kart kapalıyken görünen arka yüz.
    private ImageView onYuzGorseli;
    private Rectangle arkaYuzGorseli;

    public Kart(String kimlik, String gorselYolu) {
        this.kimlik = kimlik;
        this.gorselYolu = gorselYolu;
        this.acikMi = false;    // Başlangıçta hepsi kapalı.
        this.eslestiMi = false; 

        // Kartın arka yüzünün tasarımı. Ekrandan taşmasın diye boyutları 90x120 olarak sabitledim.
        arkaYuzGorseli = new Rectangle(90, 120); 
        arkaYuzGorseli.setFill(Color.web("#34495e"));
        arkaYuzGorseli.setArcWidth(10); arkaYuzGorseli.setArcHeight(10); // Köşeleri hafif yuvarlattık.
        arkaYuzGorseli.setStroke(Color.WHITE);

        //  kod başka bilgisayarda açıldığında resimler bulunamazsa oyun çökmesin diye try-catch kullandım.
        try {
            String dosyaAdi = gorselYolu.replace("resimler/", ""); 
            
            // Dosya yolları IDE'de ve normalde farklı çalışabiliyor, o yüzden ihtimalleri deniyoruz.
            java.io.File[] olasiYerler = {
                new java.io.File("src/com/showdown/resimler/" + dosyaAdi), 
                new java.io.File("src/resimler/" + dosyaAdi),              
                new java.io.File("resimler/" + dosyaAdi)                   
            };
            
            java.io.File dogruDosya = null;
            for (java.io.File dosya : olasiYerler) {
                if (dosya.exists()) {
                    dogruDosya = dosya;
                    break; // Dosyayı bulursak aramayı bırak.
                }
            }

            if (dogruDosya != null) {
                // Resmi bulduk, karta yerleştirip resim boyutlarını ayarlıyoruz.
                Image img = new Image(dogruDosya.toURI().toString());
                onYuzGorseli = new ImageView(img);
                onYuzGorseli.setFitWidth(75); 
                onYuzGorseli.setFitHeight(105);
                onYuzGorseli.setPreserveRatio(false); 
            } else {
                // Resmi bulamazsa boş obje ata ki program NullPointerException verip patlamasın.
                System.err.println("RESİM BULUNAMADI: " + dosyaAdi);
                onYuzGorseli = new ImageView();
            }
            
        } catch (Exception e) { 
            System.err.println("Sistem Hatası: " + e.getMessage());
            onYuzGorseli = new ImageView(); 
        }

        // Ön yüz (resim) ve arka yüzü üst üste ekliyoruz. Başta resim gizli.
        this.getChildren().addAll(arkaYuzGorseli, onYuzGorseli);
        onYuzGorseli.setVisible(false); 
        arkaYuzGorseli.setVisible(true);
    }

    // Oyun başında oyuncuya 5 saniye kartları gösterdiğimiz metod.
    public void baslangicDurumunuAyarla(boolean onYuzAcikMi) {
        this.acikMi = onYuzAcikMi;
        onYuzGorseli.setVisible(onYuzAcikMi);
        arkaYuzGorseli.setVisible(!onYuzAcikMi);
    }

    // Kapsülleme (Encapsulation) gereği değerleri almak için oluşturduğum Getter metodları.
    public String getKimlik() { return kimlik; }
    public String getGorselYolu() { return gorselYolu; }
    public boolean isAcikMi() { return acikMi; }
    public boolean isEslestiMi() { return eslestiMi; }
    public void setEslestiMi(boolean eslestiMi) { this.eslestiMi = eslestiMi; }

    // Kartın ekranda 3 boyutlu dönme efektini yapan animasyon metodu.
    public void kartiDondur() {
        if (eslestiMi) return; // Eşleşen kartlara tekrar tıklanamasın.

        RotateTransition r1 = new RotateTransition(Duration.millis(200), this);
        r1.setAxis(Rotate.Y_AXIS);
        r1.setFromAngle(acikMi ? 180 : 0);
        r1.setToAngle(90);
        r1.setOnFinished(e -> {
            // Dönüşün tam ortasında resmi gösteriyoruz/gizliyoruz.
            acikMi = !acikMi;
            onYuzGorseli.setVisible(acikMi);
            arkaYuzGorseli.setVisible(!acikMi);
            
            RotateTransition r2 = new RotateTransition(Duration.millis(200), this);
            r2.setAxis(Rotate.Y_AXIS); r2.setFromAngle(90);
            r2.setToAngle(acikMi ? 180 : 0);
            r2.play();
        });
        r1.play();
    }
}
