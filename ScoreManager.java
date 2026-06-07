
package com.showdown;

import java.io.*;
import java.util.ArrayList;

// Oyun bitince skorların kaybolmaması için .txt belgesine yazdığımız sınıf (File I/O işlemleri).
public class ScoreManager {
    // Kayıtları tutacağımız metin belgesinin adı.
    private static final String DOSYA_ADI = "skorlar.txt";

    // Yeni skoru alıp metin belgesine yazar.
    public static void skorKaydet(String isim, int puan) {
        // FileWriter'daki "true" parametresi, eski skorları silmek yerine yeni skoru en alta ekler (append modu).
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOSYA_ADI, true))) {
            writer.write(isim + ":" + puan);
            writer.newLine(); // Bir sonraki skor için alt satıra geç.
        } catch (IOException e) {
            System.err.println("Skor kaydedilirken hata oluştu: " + e.getMessage());
        }
    }

    // Text belgesini okuyup skorları sıralı bir şekilde döndüren metod.
    public static ArrayList<String> enYuksekSkorlariGetir() {
        ArrayList<String> tumSkorlar = new ArrayList<>();
        File dosya = new File(DOSYA_ADI);
        
        if (!dosya.exists()) return tumSkorlar; // Dosya yoksa boş liste döndür.

        // Dosyayı satır satır okumak için BufferedReader kullanıyoruz.
        try (BufferedReader reader = new BufferedReader(new FileReader(dosya))) {
            String satir;
            while ((satir = reader.readLine()) != null) {
                if (!satir.trim().isEmpty()) {
                    tumSkorlar.add(satir);
                }
            }
        } catch (IOException e) {
            System.err.println("Skorlar okunurken hata oluştu: " + e.getMessage());
        }

        // İsim:Puan şeklinde tuttuğumuz veriyi ikiye bölüp, Puan kısmına göre büyükten küçüğe sıralıyoruz.
        tumSkorlar.sort((s1, s2) -> {
            int p1 = Integer.parseInt(s1.split(":")[1]);
            int p2 = Integer.parseInt(s2.split(":")[1]);
            return Integer.compare(p2, p1);
        });

        return tumSkorlar;
    }
}