package com.example.ejerciciof.app;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.example.ejerciciof.model.Persona;

import java.io.*;
import java.util.ArrayList;

/**
 * Controlador para la gestión de una tabla que contiene información de personas. Permite agregar, modificar, eliminar,
 * importar y exportar datos de personas en formato CSV.
 */
public class tablaController {

    @FXML
    private Button btAgregar;
    @FXML
    private Button btEliminar;
    @FXML
    private Button btModificar;
    @FXML
    private Button btExportar;
    @FXML
    private Button btImportar;
    @FXML
    private HBox contenedorBuscadorBotones;
    @FXML
    private Label lblBuscador;
    @FXML
    private TextField txtBuscar;
    @FXML
    private TableColumn<Persona, String> columnaApellidos;
    @FXML
    private TableColumn<Persona, Integer> columnaEdad;
    @FXML
    private TableColumn<Persona, String> columnaNombre;
    @FXML
    private VBox rootPane;
    @FXML
    private TableView<Persona> tablaVista;
    @FXML
    private HBox contenedorBotones;

    private ObservableList<Persona> personas = FXCollections.observableArrayList();
    private FilteredList<Persona> filtro;

    private TextField txtNombre;
    private TextField txtApellidos;
    private TextField txtEdad;
    private Button btnGuardar;
    private Button btnCancelar;
    private Stage modal;

    /**
     * Inicializa la tabla, configurando los valores para las columnas y activando el filtrado de datos
     * a partir del campo de búsqueda.
     */
    public void initialize() {
        columnaNombre.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getNombre()));
        columnaApellidos.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getApellidos()));
        columnaEdad.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getEdad()));

        personas = FXCollections.observableArrayList();
        filtro = new FilteredList<>(personas);
        tablaVista.setItems(filtro);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrar(null);
        });
    }

    /**
     * Método que muestra una ventana emergente para agregar una nueva persona a la tabla.
     * @param event Evento de acción.
     */
    @FXML
    void agregarPersona(ActionEvent event) {
        mostrarVentanaDatos((Stage) btAgregar.getScene().getWindow(), false);
        btnGuardar.setOnAction(actionEvent -> {
            guardar(false);
            filtro.setPredicate(null);
            tablaVista.getSelectionModel().clearSelection();
        });
        btnCancelar.setOnAction(actionEvent -> cancelar());
    }

    /**
     * Muestra una ventana modal para agregar o modificar datos de una persona.
     * @param ventanaPrincipal La ventana principal desde donde se abre el modal.
     * @param esModif Booleano que indica si se trata de una modificación (true) o un nuevo registro (false).
     */
    public void mostrarVentanaDatos(Stage ventanaPrincipal, boolean esModif) {
        modal = new Stage();
        modal.setResizable(false);
        try {
            Image img = new Image(getClass().getResource("/com/example/ejerciciof/agenda.png").toString());
            modal.getIcons().add(img);
        } catch (Exception e) {
            System.out.println("Error al cargar la imagen: " + e.getMessage());
        }
        modal.initOwner(ventanaPrincipal);
        modal.initModality(Modality.WINDOW_MODAL);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label lblNombre = new Label("Nombre");
        txtNombre = new TextField(esModif ? tablaVista.getSelectionModel().getSelectedItem().getNombre() : "");
        gridPane.add(lblNombre, 0, 0);
        gridPane.add(txtNombre, 1, 0);

        Label lblApellidos = new Label("Apellidos");
        txtApellidos = new TextField(esModif ? tablaVista.getSelectionModel().getSelectedItem().getApellidos() : "");
        gridPane.add(lblApellidos, 0, 1);
        gridPane.add(txtApellidos, 1, 1);

        Label lblEdad = new Label("Edad");
        txtEdad = new TextField(esModif ? String.valueOf(tablaVista.getSelectionModel().getSelectedItem().getEdad()) : "");
        gridPane.add(lblEdad, 0, 2);
        gridPane.add(txtEdad, 1, 2);

        btnGuardar = new Button("Guardar");
        btnCancelar = new Button("Cancelar");
        FlowPane flowPane = new FlowPane(btnGuardar, btnCancelar);
        flowPane.setAlignment(Pos.CENTER);
        flowPane.setHgap(20);
        gridPane.add(flowPane, 0, 3, 2, 1);

        Scene scene = new Scene(gridPane, 300, 150);
        modal.setScene(scene);
        modal.setResizable(false);
        modal.setTitle(esModif ? "Editar Persona" : "Nueva Persona");
        modal.show();
    }

    /**
     * Guarda una nueva persona o modifica la existente según el modo de operación.
     * @param esModificar Indica si se está modificando una persona existente (true) o agregando una nueva (false).
     */
    public void guardar(boolean esModificar) {
        if (valido()) {
            String nombre = txtNombre.getText();
            String apellidos = txtApellidos.getText();
            int edad = Integer.parseInt(txtEdad.getText());

            Persona nuevaPersona = new Persona(nombre, apellidos, edad);

            boolean existe = false;
            for (Persona persona : personas) {
                if (persona.equals(nuevaPersona)) {
                    if (esModificar && persona.equals(tablaVista.getSelectionModel().getSelectedItem())) {
                        continue;
                    }
                    existe = true;
                    break;
                }
            }

            if (existe) {
                ArrayList<String> errores = new ArrayList<>();
                errores.add("La persona ya existe.");
                mostrarAlertError(errores);
                return;
            }

            if (esModificar) {
                Persona personaSeleccionada = tablaVista.getSelectionModel().getSelectedItem();
                int index = personas.indexOf(personaSeleccionada);
                if (index >= 0) {
                    personas.set(index, nuevaPersona);
                    mostrarVentanaModificado();
                }
            } else {
                personas.add(nuevaPersona);
                mostrarVentanaAgregado();
            }

            modal.close();
            txtBuscar.setText("");
            filtro.setPredicate(null);
        }
    }

    /**
     * Valida los campos del formulario de persona para verificar que los datos son correctos.
     * @return true si todos los datos son válidos; de lo contrario, false.
     */
    private boolean valido() {
        boolean error = false;
        ArrayList<String> errores = new ArrayList<>();
        if (txtNombre.getText().equals("")) {
            errores.add("El campo Nombre es obligatorio.");
            error = true;
        }
        if (txtApellidos.getText().equals("")) {
            errores.add("El campo Apellidos es obligatorio.");
            error = true;
        }
        try {
            Integer.parseInt(txtEdad.getText());
        } catch (NumberFormatException e) {
            errores.add("El campo Edad debe ser numérico.");
            error = true;
        }
        if (error) {
            mostrarAlertError(errores);
            return false;
        }
        return true;
    }

    /**
     * Elimina la persona seleccionada en la tabla.
     * @param event Evento de acción.
     */
    @FXML
    void eliminar(ActionEvent event) {
        Persona p = tablaVista.getSelectionModel().getSelectedItem();
        if (p == null) {
            ArrayList<String> lst = new ArrayList<>();
            lst.add("No has seleccionado ninguna persona.");
            mostrarAlertError(lst);
        } else {
            personas.remove(p);
            filtro.setPredicate(null);
            mostrarVentanaEliminado();
            tablaVista.getSelectionModel().clearSelection();
            txtBuscar.setText("");
        }
    }

    /**
     * Modifica los datos de la persona seleccionada en la tabla.
     * @param event Evento de acción.
     */
    @FXML
    void modificar(ActionEvent event) {
        Persona p = tablaVista.getSelectionModel().getSelectedItem();
        if (p == null) {
            ArrayList<String> lst = new ArrayList<>();
            lst.add("No has seleccionado ninguna persona.");
            mostrarAlertError(lst);
        } else {
            mostrarVentanaDatos((Stage) btModificar.getScene().getWindow(), true);
            btnGuardar.setOnAction(actionEvent -> {
                guardar(true);
                tablaVista.getSelectionModel().clearSelection();
            });
            btnCancelar.setOnAction(actionEvent -> cancelar());
        }
    }

    /**
     * Filtra las personas de la tabla basándose en el texto ingresado en el campo de búsqueda.
     * @param event Evento de acción.
     */
    @FXML
    void filtrar(ActionEvent event) {
        if (txtBuscar.getText().isEmpty()) {
            tablaVista.setItems(personas);
        } else {
            String textoBusqueda = txtBuscar.getText().toLowerCase();
            filtro.setPredicate(persona ->
                    persona.getNombre().toLowerCase().startsWith(textoBusqueda)
            );
            tablaVista.setItems(filtro);
        }
    }

    /**
     * Muestra una alerta con los mensajes de error especificados.
     * @param lst Lista de mensajes de error.
     */
    private void mostrarAlertError(ArrayList<String> lst) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(btAgregar.getScene().getWindow());
        alert.setHeaderText(null);
        alert.setTitle("Error");
        String error = String.join("\n", lst);
        alert.setContentText(error);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta indicando que una persona ha sido agregada correctamente.
     */
    private void mostrarVentanaAgregado() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(btAgregar.getScene().getWindow());
        alerta.setHeaderText(null);
        alerta.setTitle("Info");
        alerta.setContentText("Persona agregada correctamente.");
        alerta.showAndWait();
    }

    /**
     * Muestra una alerta indicando que una persona ha sido modificada correctamente.
     */
    private void mostrarVentanaModificado() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(btAgregar.getScene().getWindow());
        alerta.setHeaderText(null);
        alerta.setTitle("Info");
        alerta.setContentText("Persona modificada correctamente.");
        alerta.showAndWait();
    }

    /**
     * Muestra una alerta indicando que una persona ha sido eliminada correctamente.
     */
    private void mostrarVentanaEliminado() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(btAgregar.getScene().getWindow());
        alerta.setHeaderText(null);
        alerta.setTitle("Info");
        alerta.setContentText("Persona eliminada correctamente.");
        alerta.showAndWait();
    }

    /**
     * Cierra la ventana modal actual.
     */
    public void cancelar() {
        modal.close();
    }

    /**
     * Exporta los datos de las personas a un archivo CSV.
     * @param actionEvent Evento de acción.
     */
    public void exportar(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = fileChooser.showSaveDialog(btExportar.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("Nombre,Apellidos,Edad\n");
                for (Persona persona : personas) {
                    bw.write(String.format("%s,%s,%d\n", persona.getNombre(), persona.getApellidos(), persona.getEdad()));
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(null);
                alert.setTitle("Exportado correctamente");
                alert.setContentText("Datos exportados correctamente.");
                alert.showAndWait();
            } catch (IOException e) {
                ArrayList<String> errores = new ArrayList<>();
                errores.add("No se ha podido exportar.");
                mostrarAlertError(errores);
            }
        }
    }

    /**
     * Importa los datos de las personas desde un archivo CSV.
     * @param actionEvent Evento de acción.
     */
    public void importar(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = fileChooser.showOpenDialog(btImportar.getScene().getWindow());

        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                br.readLine();
                ArrayList<String> errores = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    String[] partes = line.split(",");
                    if (partes.length != 3) {
                        errores.add("Error en la línea: " + line + ". No tiene  3 campos.");
                        continue;
                    }

                    String nombre = partes[0].trim();
                    String apellidos = partes[1].trim();
                    int edad;

                    try {
                        edad = Integer.parseInt(partes[2].trim());
                    } catch (NumberFormatException e) {
                        errores.add("Error en la línea: " + line + ". La edad tiene que ser numérica.");
                        continue;
                    }

                    Persona nuevaPersona = new Persona(nombre, apellidos, edad);
                    boolean existe = personas.contains(nuevaPersona);

                    if (!existe) {
                        personas.add(nuevaPersona);
                    }
                }

                if (!errores.isEmpty()) {
                    mostrarAlertError(errores);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setTitle("Importado Correctamente");
                    alert.setContentText("Datos importados correctamente.");
                    alert.showAndWait();
                }

            } catch (IOException e) {
                ArrayList<String> errores = new ArrayList<>();
                errores.add("Error al importar.");
                mostrarAlertError(errores);
            }
        }
    }
}
