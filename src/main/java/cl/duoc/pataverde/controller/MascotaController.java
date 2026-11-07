package cl.duoc.pataverde.controller;

import cl.duoc.pataverde.model.Gato;
import cl.duoc.pataverde.model.Mascota;
import cl.duoc.pataverde.model.Perro;

import cl.duoc.pataverde.persistence.GsonMascotaDAO;
import cl.duoc.pataverde.persistence.MascotaRepository;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Locale;

public class MascotaController {

    @FXML
    private TextField txtNombreMascota;

    @FXML
    private TextField txtEdadMascota;

    @FXML
    private TextField txtPesoMascota;

    @FXML
    private TextField txtRazaPerro;

    @FXML
    private TextField txtBusqueda;

    @FXML
    private CheckBox chkVacunado;

    @FXML
    private CheckBox chkAdopcion;

    @FXML
    private CheckBox chkExterior;

    @FXML
    private ComboBox<String> cmbTipoMascota;

    @FXML
    private Button btnRegistrar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Label lblMensaje;

    @FXML
    private TableView<Mascota> tablaMascotas;

    @FXML
    private TableColumn<Mascota, String> colTipo;

    @FXML
    private TableColumn<Mascota, String> colNombre;

    @FXML
    private TableColumn<Mascota, Integer> colEdad;

    @FXML
    private TableColumn<Mascota, Double> colPeso;

    @FXML
    private TableColumn<Mascota, String> colCosto;

    private final MascotaRepository repositorio =
            new MascotaRepository(new GsonMascotaDAO());

    @FXML
    private void initialize() {

        cmbTipoMascota.getItems().setAll(
                "Perro",
                "Gato"
        );

        colTipo.setCellValueFactory(
                d -> new ReadOnlyStringWrapper(
                        d.getValue().getTipo()
                )
        );

        colNombre.setCellValueFactory(
                d -> new ReadOnlyStringWrapper(
                        d.getValue().getNombre()
                )
        );

        colEdad.setCellValueFactory(
                d -> new ReadOnlyObjectWrapper<>(
                        d.getValue().getEdad()
                )
        );

        colPeso.setCellValueFactory(
                d -> new ReadOnlyObjectWrapper<>(
                        d.getValue().getPeso()
                )
        );

        colCosto.setCellValueFactory(
                d -> new ReadOnlyStringWrapper(
                        "$" + String.format(
                                Locale.US,
                                "%,.0f",
                                d.getValue()
                                        .calcularCostoConsulta()
                        ).replace(',', '.')
                )
        );

        FilteredList<Mascota> filtradas =
                new FilteredList<>(
                        repositorio.getMascotas(),
                        m -> true
                );

        tablaMascotas.setItems(filtradas);

        if (txtBusqueda != null) {

            txtBusqueda.textProperty().addListener(
                    (obs, antes, texto) -> {

                        String q = texto == null
                                ? ""
                                : texto.trim()
                                .toLowerCase(Locale.ROOT);

                        filtradas.setPredicate(
                                m -> q.isEmpty()
                                        || m.getNombre()
                                        .toLowerCase(Locale.ROOT)
                                        .contains(q)
                                        || m.getTipo()
                                        .toLowerCase(Locale.ROOT)
                                        .contains(q)
                        );
                    }
            );
        }

        cmbTipoMascota.valueProperty().addListener(
                (obs, antes, tipo) ->
                        actualizarCampos(tipo)
        );

        actualizarCampos(null);

        btnRegistrar.setOnAction(
                event -> registrarMascota()
        );

        btnEliminar.setOnAction(
                event -> eliminarMascota()
        );

        try {

            repositorio.cargar();

        } catch (IOException e) {

            mostrarMensaje(
                    "Error al cargar: " + e.getMessage(),
                    true
            );

            btnRegistrar.setDisable(true);
            btnEliminar.setDisable(true);
        }
    }

    private void actualizarCampos(String tipo) {

        boolean perro = "Perro".equals(tipo);

        boolean gato = "Gato".equals(tipo);

        txtRazaPerro.setDisable(!perro);

        chkVacunado.setDisable(!perro);

        chkAdopcion.setDisable(!perro);

        chkExterior.setDisable(!gato);
    }

    @FXML
    private void registrarMascota() {

        try {

            String tipo = cmbTipoMascota.getValue();

            if (tipo == null) {

                throw new IllegalArgumentException(
                        "Seleccione Perro o Gato."
                );
            }

            String nombre =
                    txtNombreMascota.getText().trim();

            if (nombre.isEmpty()) {

                throw new IllegalArgumentException(
                        "El nombre no puede estar vacio."
                );
            }

            int edad = leerEdad();

            double peso = leerPeso();

            Mascota mascota;

            if ("Perro".equals(tipo)) {

                String raza =
                        txtRazaPerro.getText().trim();

                if (raza.isEmpty()) {

                    throw new IllegalArgumentException(
                            "La raza no puede estar vacia."
                    );
                }

                Perro perro = new Perro(
                        nombre,
                        edad,
                        peso,
                        raza,
                        chkVacunado.isSelected()
                );

                if (chkAdopcion.isSelected()) {

                    perro.marcarDisponibleParaAdopcion();
                }

                mascota = perro;

            } else {

                mascota = new Gato(
                        nombre,
                        edad,
                        peso,
                        chkExterior.isSelected()
                );
            }

            repositorio.registrar(mascota);

            limpiarCampos();

            mostrarMensaje(
                    nombre + " (" + tipo
                            + ") registrado correctamente.",
                    false
            );

        } catch (IllegalArgumentException e) {

            mostrarMensaje(
                    e.getMessage(),
                    true
            );

        } catch (IOException e) {

            mostrarMensaje(
                    "Error al guardar: " + e.getMessage(),
                    true
            );
        }
    }

    private int leerEdad() {

        try {

            int edad = Integer.parseInt(
                    txtEdadMascota.getText().trim()
            );

            if (edad < 0 || edad > 30) {

                throw new IllegalArgumentException(
                        "La edad debe estar entre 0 y 30."
                );
            }

            return edad;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "La edad debe ser un numero entero."
            );
        }
    }

    private double leerPeso() {

        try {

            double peso = Double.parseDouble(
                    txtPesoMascota
                            .getText()
                            .trim()
                            .replace(',', '.')
            );

            if (!Double.isFinite(peso) || peso <= 0) {

                throw new IllegalArgumentException(
                        "El peso debe ser mayor que cero."
                );
            }

            return peso;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "El peso debe ser un numero valido."
            );
        }
    }

    @FXML
    private void eliminarMascota() {

        Mascota seleccionada =
                tablaMascotas
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionada == null) {

            mostrarMensaje(
                    "Seleccione una mascota para eliminar.",
                    true
            );

            return;
        }

        try {

            repositorio.eliminar(seleccionada);

            mostrarMensaje(
                    seleccionada.getNombre()
                            + " eliminado correctamente.",
                    false
            );

        } catch (IOException e) {

            mostrarMensaje(
                    "Error al eliminar: " + e.getMessage(),
                    true
            );
        }
    }

    private void limpiarCampos() {

        txtNombreMascota.clear();

        txtEdadMascota.clear();

        txtPesoMascota.clear();

        txtRazaPerro.clear();

        chkVacunado.setSelected(false);

        chkAdopcion.setSelected(false);

        chkExterior.setSelected(false);

        cmbTipoMascota.setValue(null);
    }

    private void mostrarMensaje(
            String mensaje,
            boolean error
    ) {

        if (lblMensaje != null) {

            lblMensaje.setText(mensaje);

            lblMensaje.setStyle(
                    error
                            ? "-fx-text-fill: #b00020;"
                            : "-fx-text-fill: #08752c;"
            );
        }
    }
}
