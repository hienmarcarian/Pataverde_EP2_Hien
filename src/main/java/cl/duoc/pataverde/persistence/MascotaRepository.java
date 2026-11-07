package cl.duoc.pataverde.persistence;

import cl.duoc.pataverde.model.Mascota;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MascotaRepository {

    private final MascotaDAO dao;

    private final ObservableList<Mascota> mascotas =
            FXCollections.observableArrayList();

    public MascotaRepository(MascotaDAO dao) {
        this.dao = dao;
    }

    public ObservableList<Mascota> getMascotas() {
        return FXCollections.unmodifiableObservableList(mascotas);
    }

    public void cargar() throws IOException {
        mascotas.setAll(dao.cargar());
    }

    public void registrar(Mascota mascota) throws IOException {

        List<Mascota> nuevaLista =
                new ArrayList<>(mascotas);

        nuevaLista.add(mascota);

        dao.guardar(nuevaLista);

        mascotas.setAll(nuevaLista);
    }

    public void eliminar(Mascota mascota) throws IOException {

        List<Mascota> nuevaLista =
                new ArrayList<>(mascotas);

        if (nuevaLista.remove(mascota)) {

            dao.guardar(nuevaLista);

            mascotas.setAll(nuevaLista);
        }
    }
}
