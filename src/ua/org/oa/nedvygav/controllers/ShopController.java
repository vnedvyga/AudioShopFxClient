package ua.org.oa.nedvygav.controllers;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ua.org.oa.nedvygav.Main;
import ua.org.oa.nedvygav.models.Genre;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class ShopController{

    @FXML
    private VBox Shop;

    @FXML
    private void handleGenresButton(ActionEvent actionEvent) {
        Parent root =null;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/window_genres.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Genres");
        stage.setScene(new Scene(root, 300, 275));
        Shop.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleArtistsButton(ActionEvent actionEvent) {
        Parent root =null;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/window_artists.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Artists");
        stage.setScene(new Scene(root, 300, 275));
        Shop.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleAlbumsButton(ActionEvent actionEvent) {
        Parent root =null;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/window_albums.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Albums");
        stage.setScene(new Scene(root, 300, 275));
        Shop.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleAudiosButton(ActionEvent actionEvent) {
        Parent root =null;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/window_audios.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Audios");
        stage.setScene(new Scene(root, 300, 275));
        Shop.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleCartButton(ActionEvent actionEvent) {
        Parent root =null;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/window_cart.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Cart");
        stage.setScene(new Scene(root, 300, 275));
        Shop.getScene().getWindow().hide();
        stage.show();
    }
}