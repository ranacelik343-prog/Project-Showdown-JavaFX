
package com.showdown;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;

// Oyunun bütün görsel tasarımlarını (butonlar, menüler) yöneten arayüz sınıfımız.
public class UIManager {
    private Stage stage; // Penceremiz
    private OyunMotoru oyunMotoru;
    private ArrayList<Kart> secilenKartlar = new ArrayList<>();
    private String playerName;
    private boolean isProcessing = false; // Oyuncu hızlıca 3. karta basamasın diye koyduğumuz kilit.
    private Label infoLabel;

    // Oyunu tekrar başlatabilmek için seçilen zorluk bilgilerini burada tutuyoruz.
    private int currentRows, currentCols;
    private String currentZorluk;

    public UIManager(Stage stage) { this.stage = stage; }

    // Oyun ilk açıldığında ekrana gelen Ana Menü.
    public void showMainMenu() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2c3e50; -fx-padding: 40;");

        // Oyun isminin renkli yazımı
        HBox logoContainer = new HBox();
        logoContainer.setAlignment(Pos.CENTER);
        Text show = new Text("SHOW"); show.setFill(Color.LIGHTPINK);
        Text down = new Text("DOWN"); down.setFill(Color.BLACK);
        show.setStyle("-fx-font-size: 70px; -fx-font-weight: bold;");
        down.setStyle("-fx-font-size: 70px; -fx-font-weight: bold;");
        logoContainer.getChildren().addAll(show, down);

        // Kullanıcı adını aldığımız kutucuk.
        TextField nameField = new TextField();
        nameField.setPromptText("Enter Hero Name 👤");
        nameField.setMaxWidth(300);
        nameField.setStyle("-fx-background-radius: 20; -fx-padding: 10;");

        String btnStyle = "-fx-background-radius: 30; -fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-min-width: 250; -fx-padding: 10;";
        
        Button playBtn = new Button("🎮 START GAME");
        Button scoreBtn = new Button("🏆 HIGH SCORES");
        Button howToBtn = new Button("📖 HOW TO PLAY");
        Button exitBtn = new Button("❌ EXIT");

        playBtn.setStyle(btnStyle); 
        scoreBtn.setStyle(btnStyle); 
        howToBtn.setStyle(btnStyle); 
        exitBtn.setStyle(btnStyle);

        // Oyna butonuna basılınca isim boş değilse zorluk ekranına geç.
        playBtn.setOnAction(e -> {
            if(!nameField.getText().trim().isEmpty()) {
                this.playerName = nameField.getText();
                showDifficultySelection();
            }
        });
        
        scoreBtn.setOnAction(e -> showHighScores());
        howToBtn.setOnAction(e -> showHowToPlay());
        exitBtn.setOnAction(e -> stage.close());

        layout.getChildren().addAll(logoContainer, nameField, playBtn, scoreBtn, howToBtn, exitBtn);
        stage.setScene(new Scene(layout, 1000, 750));
    }

    // Kolay, Orta, Zor seçimlerini yaptığımız ekran.
    public void showDifficultySelection() {
        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #34495e; -fx-padding: 40;");

        String s = "-fx-background-radius: 25; -fx-min-width: 280; -fx-padding: 15; -fx-font-weight: bold;";
        Button easy = new Button("EASY (12 Cards)");
        Button medium = new Button("MEDIUM (20 Cards)");
        Button hard = new Button("HARD - FOCUS (30 Cards)");

        easy.setStyle(s); medium.setStyle(s); hard.setStyle(s);

        // Seçilen moda göre tablo boyutlarını (satır ve sütun) gönderiyoruz.
        easy.setOnAction(e -> startGame(3, 4, "Kolay"));
        medium.setOnAction(e -> startGame(4, 5, "Orta"));
        hard.setOnAction(e -> startGame(5, 6, "Zor"));

        Button backBtn = new Button("🏠 BACK");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 20;");
        backBtn.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(new Label("Select Level"), easy, medium, hard, backBtn);
        stage.setScene(new Scene(layout, 1000, 750));
    }

    // Oyun tahtasının çizildiği ve başladığı yer.
    public void startGame(int rows, int cols, String zorluk) {
        secilenKartlar.clear();
        isProcessing = false;
        this.currentRows = rows; this.currentCols = cols; this.currentZorluk = zorluk;

        infoLabel = new Label();
        infoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Beynimiz olan OyunMotoru'nu başlatıyoruz.
        oyunMotoru = new OyunMotoru(zorluk, 
            () -> updateInfoLabel(zorluk), 
            () -> finishGame(oyunMotoru.isKazandiMi())
        );
        updateInfoLabel(zorluk);

        // Kartların düzgün dizilmesi için GridPane (Izgara) kullanıyoruz.
        GridPane gameGrid = new GridPane();
        gameGrid.setAlignment(Pos.CENTER);
        gameGrid.setHgap(10); gameGrid.setVgap(10); // Kartların arası boşluk

        ArrayList<Kart> hazirKartlar = oyunMotoru.getKartListesi();
        int index = 0;
        
        // Satır ve sütun döngüleriyle kartları tek tek ekrana basıyoruz.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Kart kart = hazirKartlar.get(index++);
                gameGrid.add(kart, j, i);
                kart.setOnMouseClicked(e -> handleLogic(kart, zorluk)); // Tıklanma olayını atıyoruz
            }
        }

        // Başlangıçta ezberlesinler diye tüm kartları aç.
        hazirKartlar.forEach(k -> k.baslangicDurumunuAyarla(true)); 

        // 5 saniye bekle, sonra kapat ve süreyi başlat.
        PauseTransition pt = new PauseTransition(Duration.seconds(5));
        pt.setOnFinished(e -> {
            hazirKartlar.forEach(k -> k.baslangicDurumunuAyarla(false));
            oyunMotoru.zamanlayiciyiBaslat(); 
        });
        pt.play();

        VBox root = new VBox(15, infoLabel, gameGrid);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50;");
        stage.setScene(new Scene(root, 1000, 750));
    }

    // Ekranda tepede yazan Skor, Can ve Süre yazılarını yeniler.
    private void updateInfoLabel(String zorluk) {
        String sureMetni = (zorluk.equals("Kolay")) ? "Sınırsız" : oyunMotoru.getKalanSure() + "s";
        String canMetni = oyunMotoru.getCan() > 0 ? "❤️ x" + oyunMotoru.getCan() : "💀";
        infoLabel.setText("Agent: " + playerName + " | Score: " + oyunMotoru.getPuan() + " | Time: " + sureMetni + " | Lives: " + canMetni);
    }

    // Bir karta tıklandığında çalışan mantık. OyunMotoru ile haberleşir.
    private void handleLogic(Kart kart, String zorluk) {
        // Zaten açık bir karta basıldıysa işlem yapma.
        if (isProcessing || kart.isEslestiMi() || kart.isAcikMi() || secilenKartlar.contains(kart) || oyunMotoru.isOyunBittiMi()) return;

        kart.kartiDondur();
        secilenKartlar.add(kart);

        if (secilenKartlar.size() == 2) {
            isProcessing = true; // İki kart seçiliyken 3.'ye basmayı engelliyoruz.
            Kart k1 = secilenKartlar.get(0);
            Kart k2 = secilenKartlar.get(1);

            boolean eslesti = oyunMotoru.eslesmeKontrolEt(k1, k2);
            updateInfoLabel(zorluk);

            if (eslesti) {
                // Eşleştiyse kartları temizle devam et.
                secilenKartlar.clear();
                isProcessing = false;
            } else {
                // Yanlışsa kullanıcının görmesi için 1 saniye bekletip geri kapat.
                PauseTransition p = new PauseTransition(Duration.seconds(1));
                p.setOnFinished(e -> {
                    k1.kartiDondur(); 
                    k2.kartiDondur();
                    secilenKartlar.clear();
                    isProcessing = false;
                });
                p.play();
            }
        }
    }

    // Kuralları anlatan sayfayı gösterir. Kitap sayfası animasyonu ekledik.
    public void showHowToPlay() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2c3e50; -fx-padding: 40;");

        VBox page = new VBox(20); 
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 30; -fx-background-radius: 5 15 15 5; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 0 5;");
        page.setMaxWidth(550); 
        page.setMinHeight(400);

        Label header = new Label("📖 NASIL OYNANIR?");
        header.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label instructions = new Label(
            "1. Oyuna başlamadan önce 5 saniye tüm kartları göreceksin.\n\n" +
            "2. Amacın aynı görsele sahip kartları eşleştirmek.\n\n" +
            "3. Her doğru eşleşme +10 puan, her yanlış -2 puan kazandırır.\n\n" +
            "4. Toplam 3 canın var. 3 kere yanlış yaparsan elenirsin!\n\n" +
            "5. Süre bitmeden tüm kartları bulmaya çalış."
        );
        instructions.setWrapText(true); 
        instructions.setStyle("-fx-font-size: 16px; -fx-text-fill: #34495e; -fx-font-weight: normal;");

        Button backBtn = new Button("🏠 BACK TO MENU");
        backBtn.setOnAction(e -> showMainMenu());
        backBtn.setStyle("-fx-background-radius: 30; -fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-padding: 10 40; -fx-font-weight: bold;");

        page.getChildren().addAll(header, instructions);
        layout.getChildren().addAll(page, backBtn);

        // Sayfayı Y ekseninde 90 dereceden 0'a getirip kitap açılıyor efekti veriyoruz.
        page.setRotationAxis(javafx.scene.transform.Rotate.Y_AXIS);
        page.setRotate(90); 

        RotateTransition pageFlip = new RotateTransition(Duration.millis(800), page);
        pageFlip.setFromAngle(90);
        pageFlip.setToAngle(0);
        pageFlip.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        // Yeni pencere açmak yerine setRoot diyerek varolan pencereyi değiştiriyoruz (RAM tasarrufu)
        stage.getScene().setRoot(layout); 
        pageFlip.play(); 
    }

    // Skorlar menüsünü ekrana getirir.
    public void showHighScores() {
        ArrayList<String> tumSiraliSkorlar = ScoreManager.enYuksekSkorlariGetir();
        
        VBox scoreLayout = new VBox(20);
        scoreLayout.setAlignment(Pos.CENTER);
        scoreLayout.setStyle("-fx-background-color: #2c3e50; -fx-padding: 30;");

        Text title = new Text("🏆 TOP 5 AGENTS");
        title.setFill(Color.LIGHTPINK);
        title.setFont(Font.font("System", FontWeight.BOLD, 40));

        VBox listContainer = new VBox(15);
        listContainer.setAlignment(Pos.CENTER);
        
        if (tumSiraliSkorlar.isEmpty()) {
            Label noScore = new Label("Henüz kayıtlı skor yok!");
            noScore.setStyle("-fx-text-fill: white;");
            listContainer.getChildren().add(noScore);
        } else {
            // Sadece ilk 5 oyuncuyu bas (Dosyada sıralı geldikleri için direkt basıyoruz).
            int gosterilecekAdet = Math.min(5, tumSiraliSkorlar.size());
            for (int i = 0; i < gosterilecekAdet; i++) {
                String s = tumSiraliSkorlar.get(i);
                Label scoreLabel = new Label((i + 1) + ". " + s.replace(":", " -> "));
                scoreLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 20px; -fx-font-weight: bold;");
                listContainer.getChildren().add(scoreLabel);
            }
        }

        Button backBtn = new Button("🏠 BACK TO MENU");
        backBtn.setStyle("-fx-background-radius: 30; -fx-background-color: #ecf0f1; -fx-font-weight: bold; -fx-padding: 10 40;");
        backBtn.setOnAction(e -> showMainMenu());

        scoreLayout.getChildren().addAll(title, listContainer, backBtn);
        stage.getScene().setRoot(scoreLayout);
    }

    // Oyun bittiğinde ekrana gelen sonuç sayfası.
    private void finishGame(boolean win) {
    	// Kazanma/kaybetme durumuna göre ses çalıyoruz.
    	try {
    	    String sesDosyasi = win ? "/sesler/kazandin.mp3" : "/sesler/kaybettin.mp3";
    	    javafx.scene.media.AudioClip ses = new javafx.scene.media.AudioClip(getClass().getResource(sesDosyasi).toExternalForm());
    	    ses.play();
    	} catch (Exception e) {
    	    System.out.println("Ses dosyası bulunamadı, ama oyun devam ediyor.");
    	}
    	
        oyunMotoru.zamanlayiciyiDurdur(); // Süreyi tamamen durdurduk.
        ScoreManager.skorKaydet(playerName, oyunMotoru.getPuan()); // İsim ve puanı txt dosyasına yolladık.

        // MÜHENDİSLİK ÇÖZÜMÜ: Süre bitince JavaFX Thread'i çöküp donmasın diye 
        // arayüzü Platform.runLater ile güvenli şekilde güncelliyoruz.
        Platform.runLater(() -> {
            VBox endLayout = new VBox(30);
            endLayout.setAlignment(Pos.CENTER);
            endLayout.setStyle("-fx-background-color: #2c3e50; -fx-padding: 50;");

            Text resultTitle = new Text(win ? "🏆 MISSION COMPLETE" : "💀 SESSION FAILED");
            resultTitle.setFill(win ? Color.SPRINGGREEN : Color.TOMATO);
            resultTitle.setFont(Font.font("System", FontWeight.BOLD, 50));

            // Neden bittiğini açıklayan yazı.
            String mesaj = win ? "Tüm hedefler başarıyla eşleştirildi." : (oyunMotoru.getCan() <= 0 ? "Tüm canların tükendi!" : "Zamanın doldu!");
            Label reasonLabel = new Label(mesaj);
            reasonLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 22px;");

            Label scoreLabel = new Label("Agent: " + playerName + "\nFinal Score: " + oyunMotoru.getPuan());
            scoreLabel.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold; -fx-text-alignment: center;");
            scoreLabel.setPadding(new Insets(20));

            String btnStyle = "-fx-background-radius: 30; -fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-min-width: 200; -fx-padding: 12;";
            
            Button btnAgain = new Button("🔄 PLAY AGAIN");
            btnAgain.setStyle(btnStyle);
            btnAgain.setOnAction(e -> startGame(currentRows, currentCols, currentZorluk)); // Aynı zorlukla geri döner.

            Button btnMenu = new Button("🏠 MAIN MENU");
            btnMenu.setStyle(btnStyle);
            btnMenu.setOnAction(e -> showMainMenu());

            HBox buttonBox = new HBox(20, btnAgain, btnMenu);
            buttonBox.setAlignment(Pos.CENTER);

            Label footer = new Label("Showdown Project - Meryem, Rana, Muhammet Ali");
            footer.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
            footer.setPadding(new Insets(50, 0, 0, 0));

            endLayout.getChildren().addAll(resultTitle, reasonLabel, scoreLabel, buttonBox, footer);
            stage.getScene().setRoot(endLayout);
        });
    }
}