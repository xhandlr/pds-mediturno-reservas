package cl.losandes.mediturno.reservas.infraestructura;

import cl.losandes.mediturno.reservas.aplicacion.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionAplicacion {

    @Bean
    public Reloj reloj() {
        return new RelojSistema();
    }

    @Bean
    public TarificadorPort tarificador() {
        return new TarificadorLocal();
    }

    @Bean
    public ReservaService reservaService(RepositorioReservas reservas, RepositorioPacientes pacientes,
                                         TarificadorPort tarificador, Reloj reloj) {
        return new ReservaService(reservas, pacientes, tarificador, reloj);
    }
}
