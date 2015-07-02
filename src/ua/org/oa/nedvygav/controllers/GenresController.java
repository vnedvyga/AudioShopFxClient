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
import ua.org.oa.nedvygav.models.Genre;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class GenresController implements Initializable {

    @FXML
    private ListView<Genre> genresView;

    //Disabled buttons: (enable on action)
    @FXML
    private Button listArtistsButton;

    @FXML
    private MenuItem updateMenuItem;

    @FXML
    private MenuItem deleteMenuItem;
    //End of disabled buttons: (enable on action)

    @FXML
    private TextField nameField;


    private boolean createMode = false;
    private boolean updateMode = false;

    private long genreId;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
            getAllGenres();
    }
    private void getAllGenres() {
        handleButtonsActivity(true);
        Task<Genre[]> getGenres = new Task<Genre[]>() {
            @Override
            protected Genre[] call() throws Exception {
                return getGenres();
            }
        };

        getGenres.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Genre[] genres = getGenres.getValue();
                ObservableList<Genre> genresObservableList = FXCollections.observableArrayList(genres);

                genresView.setItems(genresObservableList);
            }
        });
        new Thread(getGenres).start();
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

    private boolean createUpdateGenre(String name) {
        try {
            URL url = new URL("http://localhost:8080/genres");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            OutputStream outputStream = urlConnection.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            StringBuilder stringBuilder = new StringBuilder();
            if(createMode){
                stringBuilder.append("method").append("=").append("create").append("&");
                stringBuilder.append("name").append("=").append(name);
            } else if(updateMode){
                stringBuilder.append("method").append("=").append("update").append("&");
                stringBuilder.append("name").append("=").append(name).append("&");
                stringBuilder.append("id").append("=").append(genreId);
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
        openGenresEditWindow();
    }

    @FXML
    private void handleUpdateClick(ActionEvent actionEvent) {
        updateMode = true;
        openGenresEditWindow();
    }

    @FXML
    private void handleSaveClick(ActionEvent actionEvent) {
        final String name = nameField.getText();
        Task<Boolean> createGenre = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return createUpdateGenre(name);
            }
        };

        createGenre.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                nameField.getScene().getWindow().hide();
                getAllGenres();
                createMode = false;
                updateMode = false;
            }
        });
        new Thread(createGenre).start();
    }

    @FXML
    private void handleDeleteClick(ActionEvent actionEvent) {
        try {
            URL url = new URL("http://localhost:8080/genres?method=delete&id="+genreId);
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
        getAllGenres();
    }

    private void openGenresEditWindow(){
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/window_genre_edit.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Edit genre");
        stage.setScene(new Scene(root, 300, 275));
        if(updateMode){
            nameField.setText(genresView.getSelectionModel().getSelectedItem().getName());
        }
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                updateMode=false;
                createMode=false;
            }
        });
    }

    @FXML
    private void handleListClick(MouseEvent mouseEvent) {
        Genre genre = genresView.getSelectionModel().getSelectedItem();
        if (genre!=null){
            genreId=genre.getId();
            handleButtonsActivity(false);
        }
    }

    @FXML
    private void handleMenuGenresClick(ActionEvent actionEvent) {
        getAllGenres();
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
        genresView.getScene().getWindow().hide();
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
        genresView.getScene().getWindow().hide();
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
        stage.setTitle("Artists");
        stage.setScene(new Scene(root, 300, 275));
        genresView.getScene().getWindow().hide();
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
        genresView.getScene().getWindow().hide();
        stage.show();
    }

    @FXML
    private void handleListArtistsClick(ActionEvent actionEvent) {
        ArtistsController.setGenreId(genreId);
        handleMenuArtistsClick(actionEvent);
    }

    private void handleButtonsActivity(boolean disable){
        if (disable){
            listArtistsButton.setDisable(true);
            updateMenuItem.setDisable(true);
            deleteMenuItem.setDisable(true);
        } else {
            listArtistsButton.setDisable(false);
            updateMenuItem.setDisable(false);
            deleteMenuItem.setDisable(false);
        }
    }
}