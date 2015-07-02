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
import javafx.stage.WindowEvent;
import ua.org.oa.nedvygav.Main;
import ua.org.oa.nedvygav.models.Album;
import ua.org.oa.nedvygav.models.Artist;
import ua.org.oa.nedvygav.models.Audio;
import ua.org.oa.nedvygav.models.Cart;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class AudiosController implements Initializable {

    @FXML
    private ListView<Audio> audiosView;

    //Disbled buttons: (enable on action)
    @FXML
    private Button buyButton;

    @FXML
    private MenuItem updateMenuItem;

    @FXML
    private MenuItem deleteMenuItem;
    //End of disabled buttons: (enable on action)

    @FXML
    private TextField nameField;

    @FXML
    private TextField durationField;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<Artist> artistDropBox;

    @FXML
    private ComboBox<Album> albumDropBox;

    @FXML
    private Label invalidDataLabel;

    @FXML
    private Label buyResponseLabel;

    private boolean createMode = false;
    private boolean updateMode = false;

    private Audio selectedAudio;

    private static final long ALL_AUDIOS_ID = -1;

    private static long albumId=ALL_AUDIOS_ID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getAllAudios(albumId);
        albumId=ALL_AUDIOS_ID;

    }

    private void getAllAudios(long albumId) {
        handleButtonsActivity(true);
        Task<Audio[]> getAudios = new Task<Audio[]>() {
            @Override
            protected Audio[] call() throws Exception {
                return getAudios(albumId);
            }
        };

        getAudios.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Audio[] audios = getAudios.getValue();
                ObservableList<Audio> audiosObservableList = FXCollections.observableArrayList(audios);

                audiosView.setItems(audiosObservableList);
            }
        });
        new Thread(getAudios).start();
    }

    private Audio[] getAudios(long albumId) {
        try {
            URL url;
            if (albumId==ALL_AUDIOS_ID){
                url = new URL("http://localhost:8080/audios?method=get");
            } else {
                url = new URL("http://localhost:8080/audios?method=get&album_id="+albumId);
            }

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            System.out.println("response code : " + responseCode);
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            Gson gson = new Gson();

            Audio[] audios = gson.fromJson(response.toString(), Audio[].class);
            System.out.println("response : " + response.toString());
            return audios;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean createUpdateAudio(String name, int duration, int price, long artistId,  long albumId) {
        try {
            URL url = new URL("http://localhost:8080/audios");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            StringBuilder stringBuilder = new StringBuilder();

            if (createMode){
                stringBuilder.append("method").append("=").append("create").append("&");
                stringBuilder.append("name").append("=").append(name).append("&");
                stringBuilder.append("duration").append("=").append(duration).append("&");
                stringBuilder.append("price").append("=").append(price).append("&");
                stringBuilder.append("album_id").append("=").append(albumId).append("&");
                stringBuilder.append("artist_id").append("=").append(artistId);
            } else if (updateMode) {
                stringBuilder.append("method").append("=").append("update").append("&");
                stringBuilder.append("name").append("=").append(name).append("&");
                stringBuilder.append("duration").append("=").append(duration).append("&");
                stringBuilder.append("price").append("=").append(price).append("&");
                stringBuilder.append("album_id").append("=").append(albumId).append("&");
                stringBuilder.append("artist_id").append("=").append(artistId).append("&");
                stringBuilder.append("id").append("=").append(selectedAudio.getId());
            }

            writer.write(stringBuilder.toString());
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
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    private void handleCreateClick(ActionEvent actionEvent) {
        createMode = true;
        openAudioEditWindow();
    }

    @FXML
    private void handleUpdateClick(ActionEvent actionEvent) {
        updateMode = true;
        openAudioEditWindow();
    }

    private void openAudioEditWindow(){
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/window_audio_edit.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Edit audio");
        stage.setScene(new Scene(root, 300, 275));
        if(updateMode){
            nameField.setText(selectedAudio.getName());
            durationField.setText(String.valueOf(selectedAudio.getDuration()));
            priceField.setText(String.valueOf(selectedAudio.getPrice()));
        }
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                updateMode = false;
                createMode = false;
            }
        });
    }

    @FXML
    private void handleSaveClick(ActionEvent actionEvent) {
        try{
            final String name = nameField.getText();
            final String durationString = durationField.getText();
            final int duration = Integer.parseInt(durationString);
            final String priceString = priceField.getText();
            final int price = Integer.parseInt(priceString);
            final long artistId = artistDropBox.getSelectionModel().getSelectedItem().getId();
            final long albumId = albumDropBox.getSelectionModel().getSelectedItem().getId();

            Task<Boolean> createArtist = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return createUpdateAudio(name, duration, price, artistId, albumId);
                }
            };

            createArtist.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    nameField.getScene().getWindow().hide();
                    getAllAudios(ALL_AUDIOS_ID);
                    createMode = false;
                    updateMode = false;
                }
            });
            new Thread(createArtist).start();
        } catch (NullPointerException | NumberFormatException e){
            nameField.getScene().getWindow().hide();
            openAudioEditWindow();
            invalidDataLabel.setText("Invalid data");
        }

    }

    public void handleDeleteClick(ActionEvent actionEvent) {
        try {
            URL url = new URL("http://localhost:8080/audios?method=delete&id="+selectedAudio.getId());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            System.out.println("response code : " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = null;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            System.out.println("response : " + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        getAllAudios(ALL_AUDIOS_ID);
    }

    @FXML
    private void handleListClick(MouseEvent mouseEvent) {
        Audio audio=audiosView.getSelectionModel().getSelectedItem();
        if (audio!=null){
            handleButtonsActivity(false);
            selectedAudio=audio;
            buyResponseLabel.setText("");
        }
    }

    @FXML
    private void handleBuyClick(ActionEvent actionEvent){
        if(Cart.getCart().addItem(selectedAudio)){
            buyResponseLabel.setText(" OK!");
        } else {
            buyResponseLabel.setText(" NO!");
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
        audiosView.getScene().getWindow().hide();
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
        audiosView.getScene().getWindow().hide();
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
        audiosView.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleMenuAudiosClick(ActionEvent actionEvent) {
        getAllAudios(ALL_AUDIOS_ID);
    }

    @FXML
    private void handleMenuCartClick(ActionEvent actionEvent) {
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
        audiosView.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleArtistDropBox(Event event) {
        artistDropBox.getItems().clear();
        artistDropBox.getItems().addAll(getArtists());
        albumDropBox.getItems().clear();
        albumDropBox.setDisable(true);
    }
    private Artist[] getArtists() {
        try {
            URL url = new URL("http://localhost:8080/artists?method=get");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            System.out.println("response code : " + responseCode);
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            Gson gson = new Gson();

            Artist[] artists = gson.fromJson(response.toString(), Artist[].class);
            System.out.println("response : " + response.toString());
            return artists;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    private void handleArtistDropBoxAction(ActionEvent actionEvent) {
        if(artistDropBox.getSelectionModel().getSelectedItem()!=null){
            albumDropBox.setDisable(false);
        }
    }

    @FXML
    private void handleAlbumDropBox(Event event) {
        albumDropBox.getItems().clear();
        albumDropBox.getItems().addAll(getAlbums(artistDropBox.getSelectionModel().getSelectedItem().getId()));
    }
    private Album[] getAlbums(long artistsId) {
        try {
            URL url = new URL("http://localhost:8080/albums?method=get&artist_id="+artistsId);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            System.out.println("response code : " + responseCode);
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            Gson gson = new Gson();

            Album[] albums = gson.fromJson(response.toString(), Album[].class);
            System.out.println("response : " + response.toString());
            return albums;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setAlbumId(long albumId) {
        AudiosController.albumId = albumId;
    }

    private void handleButtonsActivity(boolean disable){
        if (disable){
            updateMenuItem.setDisable(true);
            deleteMenuItem.setDisable(true);
            buyButton.setDisable(true);
        } else {
            updateMenuItem.setDisable(false);
            deleteMenuItem.setDisable(false);
            buyButton.setDisable(false);
        }
    }
}