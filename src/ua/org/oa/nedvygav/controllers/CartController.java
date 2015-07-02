package ua.org.oa.nedvygav.controllers;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import ua.org.oa.nedvygav.Main;
import ua.org.oa.nedvygav.models.Album;
import ua.org.oa.nedvygav.models.Artist;
import ua.org.oa.nedvygav.models.Audio;
import ua.org.oa.nedvygav.models.Cart;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class CartController implements Initializable {

    @FXML
    private ListView<Audio> cartView;

    //Disbled buttons: (enable on action)
    @FXML
    private Button checkoutButton;

    @FXML
    private MenuItem deleteMenuItem;

    @FXML
    private MenuItem clearCartMenuItem;
    //End of disabled buttons: (enable on action)

    @FXML
    private Label checkoutLabel;

    private Audio selectedAudio;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getAllItems();
    }

    private void getAllItems() {
        checkoutLabel.setText("");
        ObservableList<Audio> audiosObservableList = FXCollections.observableArrayList(Cart.getCart().getItems());
        cartView.setItems(audiosObservableList);
        if (cartView.getItems().size()>0){
            checkoutButton.setDisable(false);
            clearCartMenuItem.setDisable(false);
        } else {
            cartView.setPlaceholder(new Label("Cart is empty"));
        }
    }

    @FXML
    private void handleDeleteClick(ActionEvent event){
        Cart.getCart().removeItem(selectedAudio);
        getAllItems();
    }

    @FXML
    private void handleClearCartClick(ActionEvent event){
        Cart.getCart().clearCart();
        getAllItems();
    }

    @FXML
    private void handleCheckout(ActionEvent event){
        try {
            URL url = new URL("http://localhost:8080/cart");
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            Gson gson = new Gson();
            StringBuilder request = new StringBuilder();
            request.append("method=buy").append("&");
            request.append("buy_from_app=").append(gson.toJson(Cart.getCart().getItems()));
            System.out.println(request);
            writer.write(request.toString());
            writer.flush();
            writer.close();

            int responseCode = urlConnection.getResponseCode();
            System.out.println("response code : " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = null;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            System.out.println("response : " + response.toString());
            Cart.getCart().clearCart();
            getAllItems();
            checkoutLabel.setText(" "+response.toString());
            checkoutButton.setDisable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleListClick(MouseEvent mouseEvent) {
        Audio audio=cartView.getSelectionModel().getSelectedItem();
        if (audio!=null){
            selectedAudio=audio;
            deleteMenuItem.setDisable(false);
        }
    }

    @FXML
    private void handleMenuGenresClick(ActionEvent actionEvent) {
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
        cartView.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleMenuArtistsClick(ActionEvent actionEvent) {
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
        cartView.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleMenuAlbumsClick(ActionEvent actionEvent) {
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
        cartView.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleMenuAudiosClick(ActionEvent actionEvent) {
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
        cartView.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleMenuCartClick(ActionEvent actionEvent) {
        getAllItems();
    }
}