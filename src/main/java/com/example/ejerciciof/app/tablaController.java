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
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.example.ejerciciof.model.Persona;

import java.util.ArrayList;

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
    private TableColumn<Persona,String> columnaApellidos;
    @FXML
    private TableColumn<Persona,Integer> columnaEdad;
    @FXML
    private TableColumn<Persona,String> columnaNombre;
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

    public void initialize() {
        columnaNombre.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getNombre()));
        columnaApellidos.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getApellidos()));
        columnaEdad.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getEdad()));

        // Llenar la tabla con la lista de personas (no filtrada)
        personas = FXCollections.observableArrayList();
        filtro = new FilteredList<>(personas);
        tablaVista.setItems(filtro); // Setea la tabla con la lista filtrada

        // Listener para búsqueda
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrar(null); // Llama a filtrar cuando el texto cambia
        });
    }

    @FXML
    void agregarPersona(ActionEvent event) {
        mostrarVentanaDatos((Stage) btAgregar.getScene().getWindow(), false);
        btnGuardar.setOnAction(actionEvent -> {
            guardar(false); // Guardar la nueva persona
            filtro.setPredicate(null); // Limpiar el filtro después de agregar
            tablaVista.getSelectionModel().clearSelection(); // Limpiar selección
        });
        btnCancelar.setOnAction(actionEvent -> cancelar()); // Cancelar operación
    }

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

        // Campos para nombre, apellidos y edad
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

    public void guardar(boolean esModificar) {
        if (valido()) {
            String nombre = txtNombre.getText();
            String apellidos = txtApellidos.getText();
            int edad = Integer.parseInt(txtEdad.getText());

            // Crear una nueva persona con los datos ingresados
            Persona nuevaPersona = new Persona(nombre, apellidos, edad);

            // Validar si la persona ya existe
            boolean existe = false;
            for (Persona persona : personas) {
                if (persona.equals(nuevaPersona)) {
                    // Si estamos modificando, ignoramos la persona actualmente seleccionada
                    if (esModificar && persona.equals(tablaVista.getSelectionModel().getSelectedItem())) {
                        continue;
                    }
                    existe = true; // La persona ya existe
                    break;
                }
            }

            if (existe) {
                ArrayList<String> errores = new ArrayList<>();
                errores.add("La persona ya existe.");
                mostrarAlertError(errores);
                return; // Salir sin guardar
            }

            if (esModificar) {
                Persona personaSeleccionada = tablaVista.getSelectionModel().getSelectedItem();
                int index = personas.indexOf(personaSeleccionada);
                if (index >= 0) {
                    personas.set(index, nuevaPersona); // Modificar en la lista original
                    mostrarVentanaModificado();
                }
            } else {
                personas.add(nuevaPersona); // Agregar a la lista original
                mostrarVentanaAgregado();
            }

            modal.close();
            txtBuscar.setText(""); // Limpiar el campo de búsqueda
            filtro.setPredicate(null); // Mostrar todas las personas
        }
    }


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

    @FXML
    void eliminar(ActionEvent event) {
        Persona p = tablaVista.getSelectionModel().getSelectedItem();
        if (p == null) {
            ArrayList<String> lst = new ArrayList<>();
            lst.add("No has seleccionado ninguna persona.");
            mostrarAlertError(lst);
        } else {
            personas.remove(p); // Remover de la lista original
            filtro.setPredicate(null); // Mostrar todas las personas
            mostrarVentanaEliminado();
            tablaVista.getSelectionModel().clearSelection();
            txtBuscar.setText(""); // Limpiar el campo de búsqueda
        }
    }

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
                guardar(true); // Guardar los cambios
                tablaVista.getSelectionModel().clearSelection();
            });
            btnCancelar.setOnAction(actionEvent -> cancelar());
        }
    }

    @FXML
    void filtrar(ActionEvent event) {
        if (txtBuscar.getText().isEmpty()) {
            tablaVista.setItems(personas); // Mostrar todas las personas si el campo está vacío
        } else {
            String textoBusqueda = txtBuscar.getText().toLowerCase();
            filtro.setPredicate(persona ->
                    persona.getNombre().toLowerCase().startsWith(textoBusqueda)
            );
            tablaVista.setItems(filtro); // Mostrar las personas que cumplen con el filtro
        }
    }

    private void mostrarAlertError(ArrayList<String> lst) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(btAgregar.getScene().getWindow());
        alert.setHeaderText(null);
        alert.setTitle("Error");
        String error = String.join("\n", lst);
        alert.setContentText(error);
        alert.showAndWait();
    }

    private void mostrarVentanaAgregado() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(btAgregar.getScene().getWindow());
        alerta.setHeaderText(null);
        alerta.setTitle("Info");
        alerta.setContentText("Persona agregada correctamente.");
        alerta.showAndWait();
    }

    private void mostrarVentanaModificado() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(btAgregar.getScene().getWindow());
        alerta.setHeaderText(null);
        alerta.setTitle("Info");
        alerta.setContentText("Persona modificada correctamente.");
        alerta.showAndWait();
    }

    private void mostrarVentanaEliminado() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(btAgregar.getScene().getWindow());
        alerta.setHeaderText(null);
        alerta.setTitle("Info");
        alerta.setContentText("Persona eliminada correctamente.");
        alerta.showAndWait();
    }

    public void cancelar() {
        modal.close();
    }

    public void exportar(ActionEvent actionEvent) {
    }

    public void importar(ActionEvent actionEvent) {
    }
}
