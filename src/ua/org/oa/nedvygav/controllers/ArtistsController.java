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
import ua.org.oa.nedvygav.models.Genre;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class ArtistsController implements Initializable {

    @FXML
    private ListView<Artist> artistsView;

    //Disbled buttons: (enable on action)
    @FXML
    private Button listAlbumsButton;

    @FXML
    private MenuItem updateMenuItem;

    @FXML
    private MenuItem deleteMenuItem;
    //End of disabled buttons: (enable on action)

    @FXML
    private TextField nameField;

    @FXML
    private TextField countryField;

    @FXML
    private ComboBox<Genre> genreDropBox;

    @FXML
    private Label invalidDataLabel;

    private boolean createMode = false;
    private boolean updateMode = false;

    private Artist selectedArtist;

    private static final long ALL_GENRES_ID = -1;

    private static long genreId=ALL_GENRES_ID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getAllArtists(genreId);
        genreId=ALL_GENRES_ID;
    }

    private void getAllArtists(long genreId) {
        handleButtonsActivity(true);
        Task<Artist[]> getArtists = new Task<Artist[]>() {
            @Override
            protected Artist[] call() throws Exception {
                return getArtists(genreId);
            }
        };

        getArtists.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Artist[] artists = getArtists.getValue();
                ObservableList<Artist> artistsObservableList = FXCollections.observableArrayList(artists);

                artistsView.setItems(artistsObservableList);
            }
        });
        new Thread(getArtists).start();
    }

    private Artist[] getArtists(long genreId) {
        try {
            URL url;
            if (genreId==ALL_GENRES_ID){
                url = new URL("http://localhost:8080/artists?method=get");
            } else {
                url = new URL("http://localhost:8080/artists?method=get&genre_id="+genreId);
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

            Artist[] artists = gson.fromJson(response.toString(), Artist[].class);
            System.out.println("response : " + response.toString());
            return artists;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean createUpdateArtist(String name, String country, long genreId) {
        try {
            URL url = new URL("http://localhost:8080/artists");
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
                stringBuilder.append("country").append("=").append(country).append("&");
                stringBuilder.append("genre_id").append("=").append(genreId);
            } else if (updateMode) {
                stringBuilder.append("method").append("=").append("update").append("&");
                stringBuilder.append("name").append("=").append(name).append("&");
                stringBuilder.append("country").append("=").append(country).append("&");
                stringBuilder.append("id").append("=").append(selectedArtist.getId()).append("&");
                stringBuilder.append("genre_id").append("=").append(genreId);
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
        openArtistEditWindow();
    }

    @FXML
    private void handleUpdateClick(ActionEvent actionEvent) {
        updateMode = true;
        openArtistEditWindow();
    }

    private void openArtistEditWindow(){
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/window_artist_edit.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Edit artist");
        stage.setScene(new Scene(root, 300, 275));
        if(updateMode){
            nameField.setText(selectedArtist.getName());
            countryField.setText(selectedArtist.getCoutry());
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
            final String country = countryField.getText();
            final long genreId = genreDropBox.getSelectionModel().getSelectedItem().getId();
            System.out.println(genreId);
            Task<Boolean> createArtist = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return createUpdateArtist(name, country , genreId);
                }
            };

            createArtist.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    nameField.getScene().getWindow().hide();
                    getAllArtists(ALL_GENRES_ID);
                    createMode = false;
                    updateMode = false;
                }
            });
            new Thread(createArtist).start();
        } catch (NullPointerException e){
            nameField.getScene().getWindow().hide();
            openArtistEditWindow();
            invalidDataLabel.setText("Invalid data");
        }

    }

    public void handleDeleteClick(ActionEvent actionEvent) {
        try {
            URL url = new URL("http://localhost:8080/artists?method=delete&id="+selectedArtist.getId());
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
        getAllArtists(ALL_GENRES_ID);
    }

    @FXML
    private void handleListClick(MouseEvent mouseEvent) {
        selectedArtist=artistsView.getSelectionModel().getSelectedItem();
        if (selectedArtist!=null){
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
        artistsView.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleMenuArtistsClick(ActionEvent actionEvent) {
        getAllArtists(ALL_GENRES_ID);
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
        artistsView.getScene().getWindow().hide();
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
        artistsView.getScene().getWindow().hide();
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
        artistsView.getScene().getWindow().hide();
        stage.show();
    }

    public static void setGenreId(long genreId) {
        ArtistsController.genreId = genreId;
    }

    public void handleListAlbumsClick(ActionEvent actionEvent) {
        AlbumsController.setArtistId(selectedArtist.getId());
        handleMenuAlbumsClick(actionEvent);
    }

    @FXML
    private void handleGenreDropBox(Event event) {
        genreDropBox.getItems().addAll(getGenres());
    }
    private Genre[] getGenres() {
        try {
            URL url=url = new URL("http://localhost:8080/genres?method=get");
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

            Genre[] genres = gson.fromJson(response.toString(), Genre[].class);
            System.out.println("response : " + response.toString());
            return genres;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handleButtonsActivity(boolean disable){
        if (disable){
            listAlbumsButton.setDisable(true);
            updateMenuItem.setDisable(true);
            deleteMenuItem.setDisable(true);
        } else {
            listAlbumsButton.setDisable(false);
            updateMenuItem.setDisable(false);
            deleteMenuItem.setDisable(false);
        }
    }
}