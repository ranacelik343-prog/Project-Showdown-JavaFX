
package com.showdown;

import javafx.application.Application;
import javafx.stage.Stage;

// Oyunumuzun çalışmaya başladığı ana sınıfımız. JavaFX kullandığımız için Application'dan miras aldık.
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Arayüz işlemlerimizi yönetmesi için UIManager nesnemizi oluşturuyoruz.
        UIManager uiManager = new UIManager(primaryStage);
        
        // Uygulama ilk açıldığında ekrana Ana Menü gelsin diye bu metodu çağırdık.
        uiManager.showMainMenu();
        
        // Pencerenin üstündeki yazıyı ayarlayıp ekranda gösteriyoruz.
        primaryStage.setTitle("Showdown: Görsel Eşleştirme Oyunu");
        primaryStage.show();
    }

    public static void main(String[] args) {
        // JavaFX'i başlatan standart komut.
        launch(args);
    }
}
