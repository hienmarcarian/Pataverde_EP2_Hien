package cl.duoc.pataverde.persistence;

import cl.duoc.pataverde.model.Gato;
import cl.duoc.pataverde.model.Mascota;
import cl.duoc.pataverde.model.Perro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GsonMascotaDAO implements MascotaDAO {

    private final Gson gson =
            new GsonBuilder().setPrettyPrinting().create();

    private final Path archivoPerros =
            Path.of("perros.json");

    private final Path archivoGatos =
            Path.of("gatos.json");

    private final Path archivoAnterior =
            Path.of(
                    System.getProperty("user.home"),
                    "PataVerde",
                    "mascotas.json"
            );

    @Override
    public List<Mascota> cargar() throws IOException {

        if (!Files.exists(archivoPerros)
                && !Files.exists(archivoGatos)
                && Files.exists(archivoAnterior)) {

            List<Mascota> anteriores = cargarAnterior();

            guardar(anteriores);

            return anteriores;
        }

        List<Mascota> resultado = new ArrayList<>();

        resultado.addAll(
                leer(
                        archivoPerros,
                        new TypeToken<List<Perro>>() {}.getType()
                )
        );

        resultado.addAll(
                leer(
                        archivoGatos,
                        new TypeToken<List<Gato>>() {}.getType()
                )
        );

        return resultado;
    }

    private <T extends Mascota> List<T> leer(
            Path archivo,
            Type tipo
    ) throws IOException {

        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }

        try {

            String contenido = Files.readString(
                    archivo,
                    StandardCharsets.UTF_8
            );

            List<T> lista = gson.fromJson(contenido, tipo);

            return lista == null
                    ? new ArrayList<>()
                    : lista;

        } catch (RuntimeException e) {

            throw new IOException(
                    "No se puede leer "
                            + archivo
                            + ": "
                            + e.getMessage(),
                    e
            );
        }
    }

    private List<Mascota> cargarAnterior()
            throws IOException {

        List<Mascota> resultado = new ArrayList<>();

        try {

            JsonArray registros = JsonParser.parseString(
                    Files.readString(
                            archivoAnterior,
                            StandardCharsets.UTF_8
                    )
            ).getAsJsonArray();

            for (JsonElement elemento : registros) {

                JsonObject registro =
                        elemento.getAsJsonObject();

                String tipo =
                        registro.get("tipo").getAsString();

                JsonElement datos =
                        registro.get("datos");

                if ("Perro".equals(tipo)) {

                    resultado.add(
                            gson.fromJson(datos, Perro.class)
                    );

                } else if ("Gato".equals(tipo)) {

                    resultado.add(
                            gson.fromJson(datos, Gato.class)
                    );

                } else {

                    throw new IllegalArgumentException(
                            "Tipo desconocido: " + tipo
                    );
                }
            }

        } catch (RuntimeException e) {

            throw new IOException(
                    "No se pudo importar mascotas.json: "
                            + e.getMessage(),
                    e
            );
        }

        return resultado;
    }

    @Override
    public void guardar(List<Mascota> mascotas)
            throws IOException {

        List<Perro> perros = new ArrayList<>();
        List<Gato> gatos = new ArrayList<>();

        for (Mascota mascota : mascotas) {

            if (mascota instanceof Perro) {

                perros.add((Perro) mascota);

            } else if (mascota instanceof Gato) {

                gatos.add((Gato) mascota);

            } else {

                throw new IOException(
                        "Tipo de mascota no admitido"
                );
            }
        }

        Files.writeString(
                archivoPerros,
                gson.toJson(perros),
                StandardCharsets.UTF_8
        );

        Files.writeString(
                archivoGatos,
                gson.toJson(gatos),
                StandardCharsets.UTF_8
        );
    }
}