package cl.losandes.mediturno.reservas.infraestructura;

import cl.losandes.mediturno.reservas.dominio.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/** Carga el padrón de pacientes de ejemplo si la base está vacía. */
@Component
public class CargaInicial implements CommandLineRunner {

    private final PacienteJpaRepository pacientes;

    public CargaInicial(PacienteJpaRepository pacientes) {
        this.pacientes = pacientes;
    }

    @Override
    public void run(String... args) throws Exception {
        if (pacientes.count() > 0) {
            return;
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new ClassPathResource("pacientes.csv").getInputStream(), StandardCharsets.UTF_8))) {
            String linea = in.readLine(); // cabecera
            while ((linea = in.readLine()) != null) {
                if (linea.isBlank() || linea.startsWith("#")) {
                    continue;
                }
                String[] c = linea.split(";", -1);
                pacientes.save(new PacienteEntity(
                        c[0].trim(), c[1].trim(), LocalDate.parse(c[2].trim()),
                        TipoPrevision.valueOf(c[3].trim()).name(),
                        c[4].isBlank() ? null : TramoFonasa.valueOf(c[4].trim()).name(),
                        Boolean.parseBoolean(c[5].trim())));
            }
        }
    }
}
