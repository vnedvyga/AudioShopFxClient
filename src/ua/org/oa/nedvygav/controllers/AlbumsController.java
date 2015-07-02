package ua.org.oa.nedvygav.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AlbumsController implements Initializable {

    @FXML
    private ListView<Album> albumsView;

    //Disbled buttons: (enable on action)
    @FXML
    private Button listAudiosButton;

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
    private TextField yearField;

    @FXML
    private ComboBox<Artist> artistDropBox;

    @FXML
    private Label invalidDataLabel;

    @FXML
    private Label buyResponseLabel;

    private boolean createMode = false;
    private boolean updateMode = false;

    private Album selectedAlbum;

    private static final long ALL_ARTISTS_ID = -1;

    private static long artistsId=ALL_ARTISTS_ID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getAllAlbums(artistsId);
        artistsId=ALL_ARTISTS_ID;

    }

    private void getAllAlbums(long artistsId) {
        handleButtonsActivity(true);
        Task<Album[]> getAlbums = new Task<Album[]>() {
            @Override
            protected Album[] call() throws Exception {
                return getAlbums(artistsId);
            }
        };

        getAlbums.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Album[] albums = getAlbums.getValue();
                ObservableList<Album> albumsObservableList = FXCollections.observableArrayList(albums);

                albumsView.setItems(albumsObservableList);
            }
        });
        new Thread(getAlbums).start();
    }

    private Album[] getAlbums(long artistsId) {
        try {
            URL url;
            if (artistsId==ALL_ARTISTS_ID){
                url = new URL("http://localhost:8080/albums?method=get");
            } else {
                url = new URL("http://localhost:8080/albums?method=get&artist_id="+artistsId);
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

            Album[] albums = gson.fromJson(response.toString(), Album[].class);
            System.out.println("response : " + response.toString());
            return albums;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean createUpdateArtist(String name, int year, long artistId) {
        try {
            URL url = new URL("http://localhost:8080/albums");
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
                stringBuilder.append("year").append("=").append(year).append("&");
                stringBuilder.append("artist_id").append("=").append(artistId);
            } else if (updateMode) {
                stringBuilder.append("method").append("=").append("update").append("&");
                stringBuilder.append("name").append("=").append(name).append("&");
                stringBuilder.append("year").append("=").append(year).append("&");
                stringBuilder.append("id").append("=").append(selectedAlbum.getId()).append("&");
                stringBuilder.append("artist_id").append("=").append(artistId);
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
        openAlbumEditWindow();
    }

    @FXML
    private void handleUpdateClick(ActionEvent actionEvent) {
        updateMode = true;
        openAlbumEditWindow();
    }

    private void openAlbumEditWindow(){
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/window_album_edit.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Edit album");
        stage.setScene(new Scene(root, 300, 275));
        if(updateMode){
            nameField.setText(selectedAlbum.getName());
            yearField.setText(String.valueOf(selectedAlbum.getYear()));
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
        try {
            final String name = nameField.getText();
            final String yearString = yearField.getText();
            final int year = Integer.parseInt(yearString);
            final long artistId = artistDropBox.getSelectionModel().getSelectedItem().getId();
            Task<Boolean> createArtist = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return createUpdateArtist(name, year , artistId);
                }
            };

            createArtist.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    nameField.getScene().getWindow().hide();
                    getAllAlbums(ALL_ARTISTS_ID);
                    createMode = false;
                    updateMode = false;
                }
            });
            new Thread(createArtist).start();
        } catch (NullPointerException  | NumberFormatException e){
            nameField.getScene().getWindow().hide();
            openAlbumEditWindow();
            invalidDataLabel.setText("Invalid data");
        }

    }

    public void handleDeleteClick(ActionEvent actionEvent) {
        try {
            URL url = new URL("http://localhost:8080/albums?method=delete&id="+selectedAlbum.getId());
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
        getAllAlbums(ALL_ARTISTS_ID);
    }

    @FXML
    private void handleListClick(MouseEvent mouseEvent) {
        buyResponseLabel.setText("");
        Album album=albumsView.getSelectionModel().getSelectedItem();
        selectedAlbum=album;
        if (album!=null){
            handleButtonsActivity(false);
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
        albumsView.getScene().getWindow().hide();
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
        albumsView.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleMenuAlbumsClick(ActionEvent actionEvent) {
        getAllAlbums(ALL_ARTISTS_ID);
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
        stage.setTitle("Artists");
        stage.setScene(new Scene(root, 300, 275));
        albumsView.getScene().getWindow().hide();
        stage.show();
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
        albumsView.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleListAudiosClick(ActionEvent actionEvent) {
        AudiosController.setAlbumId(selectedAlbum.getId());
        handleMenuAudiosClick(actionEvent);
    }

    @FXML
    private void handleBuyClick(ActionEvent actionEvent){
        if(Cart.getCart().addAllItems(getAudios(selectedAlbum.getId()))){
            buyResponseLabel.setText(" OK!");
        } else {
            buyResponseLabel.setText(" NO!");
        }
    }
    private List<Audio> getAudios(long albumId) {
        try {
            URL url = new URL("http://localhost:8080/audios?method=get&album_id="+albumId);
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
            Type collectionType = new TypeToken<List<Audio>>(){
            }.getType();
            List<Audio> audios = gson.fromJson(response.toString(), collectionType);
            System.out.println("response : " + response.toString());
            return audios;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @FXML
    private void handleArtistDropBox(Event event) {
        artistDropBox.getItems().addAll(getArtists());
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

    public static void setArtistId(long artistId) {
        AlbumsController.artistsId = artistId;
    }

    private void handleButtonsActivity(boolean disable){
        if (disable){
            listAudiosButton.setDisable(true);
            updateMenuItem.setDisable(true);
            deleteMenuItem.setDisable(true);
            buyButton.setDisable(true);
        } else {
            listAudiosButton.setDisable(false);
            updateMenuItem.setDisable(false);
            deleteMenuItem.setDisable(false);
            buyButton.setDisable(false);
        }
    }
}