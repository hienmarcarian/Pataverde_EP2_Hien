package cl.duoc.pataverde.persistence;

import cl.duoc.pataverde.model.Mascota;
import cl.duoc.pataverde.model.Perro;
import cl.duoc.pataverde.model.Gato;

import com.google.gson.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ArchivoMascotas {

    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path ARCHIVO =
            Path.of(System.getProperty("user.home"),
                    "PataVerde", "mascotas.json");

    public static void guardar(List<Mascota> mascotas)
            throws IOException {

        JsonArray registros = new JsonArray();

        for (Mascota mascota : mascotas) {

            JsonObject registro = new JsonObject();

            registro.addProperty("tipo", mascota.getTipo());
            registro.add("datos", gson.toJsonTree(mascota));

            registros.add(registro);
        }

        Files.createDirectories(ARCHIVO.getParent());

        Files.writeString(
                ARCHIVO,
                gson.toJson(registros),
                StandardCharsets.UTF_8
        );
    }

    public static List<Mascota> cargar()
            throws IOException {

        List<Mascota> mascotas = new ArrayList<>();

        if (!Files.exists(ARCHIVO)) {
            return mascotas;
        }

        String contenido = Files.readString(
                ARCHIVO, StandardCharsets.UTF_8);

        try {
            JsonArray registros =
                    JsonParser.parseString(contenido)
                            .getAsJsonArray();

            for (JsonElement elemento : registros) {

                JsonObject registro = elemento.getAsJsonObject();

                String tipo =
                        registro.get("tipo").getAsString();

                JsonElement datos = registro.get("datos");

                if (tipo.equals("Perro")) {

                    mascotas.add(
                            gson.fromJson(datos, Perro.class));

                } else if (tipo.equals("Gato")) {

                    mascotas.add(
                            gson.fromJson(datos, Gato.class));

                } else {
                    throw new JsonParseException(
                            "Tipo desconocido: " + tipo);
                }
            }

        } catch (RuntimeException e) {
            throw new IOException(
                    "No se pudo leer el archivo JSON.", e);
        }

        return mascotas;
    }
}