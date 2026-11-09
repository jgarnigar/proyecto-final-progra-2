package com.proyectofinal.config;

import com.proyectofinal.DAO.AccesoriosDAO;
import com.proyectofinal.DAO.ClientesDAO;
import com.proyectofinal.DAO.EquiposDAO;
import com.proyectofinal.modelo.Accesorios;
import com.proyectofinal.modelo.Clientes;
import com.proyectofinal.modelo.Equipos;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

    @Component
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
    public class DataInitializer implements CommandLineRunner {

        private final EquiposDAO equiposDAO;
        private final AccesoriosDAO accesoriosDAO;
        private final ClientesDAO clientesDAO;

        public DataInitializer(EquiposDAO equiposDAO, AccesoriosDAO accesoriosDAO, ClientesDAO clientesDAO) {
            this.equiposDAO = equiposDAO;
            this.accesoriosDAO = accesoriosDAO;
            this.clientesDAO = clientesDAO;
        }

        @Override
        public void run(String... args) {
            cargarEquipos();
            cargarAccesorios();
            cargarClientes();
        }

        private void cargarEquipos() {
            List<Equipos> existentes = equiposDAO.listar();
            if (existentes == null || !existentes.isEmpty()) {
                return;
            }
            equiposDAO.registrar(equipo("Bicicleta", "Huffy Granite", "3.50", "650.00", 100));
            equiposDAO.registrar(equipo("Bicicleta", "Eurobike", "3.00", "600.00", 100));
            equiposDAO.registrar(equipo("Scooter eléctrico", "Xiaomi Electric Scooter 6 US", "5.00", "800.00", 85));
            equiposDAO.registrar(equipo("Scooter eléctrico", "Scooter Dualtron Togo", "5.50", "850.00", 90));
            equiposDAO.registrar(equipo("Patineta", "Tabla Skateboard Popsicle", "2.00", "120.00", 0));
            System.out.println("[DataInitializer] Equipos de prueba insertados.");
        }

        private void cargarAccesorios() {
            List<Accesorios> existentes = accesoriosDAO.listar();
            if (existentes == null || !existentes.isEmpty()) {
                return;
            }
            accesoriosDAO.registrar(accesorio("Casco", "1.50", "35.00", 20));
            accesoriosDAO.registrar(accesorio("Candado", "1.00", "25.00", 15));
            accesoriosDAO.registrar(accesorio("Luces LED", "0.75", "15.00", 25));
            accesoriosDAO.registrar(accesorio("Bomba de aire", "0.50", "20.00", 10));
            System.out.println("[DataInitializer] Accesorios de prueba insertados.");
        }

        private void cargarClientes() {
            List<Clientes> existentes = clientesDAO.listar();
            if (existentes == null || !existentes.isEmpty()) {
                return;
            }
            clientesDAO.registrar(cliente("Ana Hernandez", "ana.hernandez@gmail.com"));
            clientesDAO.registrar(cliente("Erick Mora", "erick.mora@gmail.com"));
            clientesDAO.registrar(cliente("María Jiménez", "maria.jimenez@gmail.com"));
            System.out.println("[DataInitializer] Clientes de prueba insertados.");
        }

        private Equipos equipo(String tipo, String modelo, String tarifaHora, String reposicion, int bateria) {
            Equipos e = new Equipos();
            e.setTipo(tipo);
            e.setModelo(modelo);
            e.setTarifaHora(new BigDecimal(tarifaHora));
            e.setPrecioReposicion(new BigDecimal(reposicion));
            e.setBateriaPorcentaje(bateria);
            e.setEstado("DISPONIBLE");
            return e;
        }

        private Accesorios accesorio(String nombre, String tarifaFija, String reposicion, int existencia) {
            Accesorios a = new Accesorios();
            a.setNombre(nombre);
            a.setTarifaFija(new BigDecimal(tarifaFija));
            a.setPrecioReposicion(new BigDecimal(reposicion));
            a.setExistenciaActual(existencia);
            return a;
        }

        private Clientes cliente(String nombre, String email) {
            Clientes c = new Clientes();
            c.setNombre(nombre);
            c.setEmail(email);
            c.setSaldoPendiente(BigDecimal.ZERO);
            c.setEstado("ACTIVO");
            return c;
        }
}
