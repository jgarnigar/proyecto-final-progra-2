# Guía Rápida de Desarrollo - Sistema de Alquileres

Este documento contiene las reglas básicas, el flujo de trabajo y la estructura de base de datos para el desarrollo del proyecto "Rueda Libre".

## 1. Tipos de Datos y Mapeo (Java -> MySQL)

Para mantener la consistencia financiera e integridad referencial, usar estrictamente los siguientes tipos de datos en los modelos de Java:

*   **Identificadores (IDs) y Cantidades:** `Integer` o `int` (Mapea a `INT`).
*   **Dinero, Tarifas y Costos:** `BigDecimal` (Mapea a `DECIMAL(10,2)`). *No usar Double ni Float*.
*   **Textos, Estados y Nombres:** `String` (Mapea a `VARCHAR`).
*   **Fechas y Tiempos:** `LocalDateTime` (Mapea a `DATETIME`).

## 2. Estructura de los DAOs

Cada entidad principal (`Cliente`, `Equipo`, `Accesorio`, `Alquiler`) debe contar con una clase DAO que implemente las siguientes operaciones estándar mediante JDBC (`PreparedStatement` y `ResultSet`):

*   `registrar(Entidad entidad)`: Inserta un nuevo registro.
*   `buscarPorId(int id)`: Retorna un objeto específico o `null`.
*   `obtenerTodos()`: Retorna un `List<Entidad>`.
*   `modificar(Entidad entidad)`: Actualiza los campos de un registro existente.
*   `desactivar(int id)`: Realiza un borrado/desactivación lógica (cambio de estado), *nunca* un `DELETE` físico.

## 3. Cálculo de Precios

| Ítem / Situación | Regla de Cálculo |
| :--- | :--- |
| **Alquiler de Equipo** | `tarifa_hora` × (Horas transcurridas entre `fecha_estimada` y `fecha_real`) |
| **Alquiler de Accesorio** | `tarifa_fija` (Cobro único por alquiler, independiente del tiempo) |
| **Pérdida/Daño de Equipo** | Cobro del `precio_reposicion` del equipo. |
| **Pérdida de Accesorio** | `precio_reposicion` × `unidades_extraviadas` |
| **Saldo Pendiente Cliente**| (Total Alquiler + Reposiciones) - `deposito_cobrado` |

## 4. Flujo de Operación de la Base de Datos

El sistema depende de la integridad referencial. Las inserciones deben seguir estrictamente este orden lógico:

1.  **Inventario Inicial:** 
    *   Registrar `equipos`.
    *   Registrar `accesorios` y su `accesorioHistorial` inicial.
2.  **Entidades Base:** 
    *   Registrar `clientes`.
3.  **Proceso de Alquiler (Salida):**
    *   Crear registro en `alquileres` (asignar cliente, depósito y fecha estimada).
    *   Registrar el detalle en `alquiler_equipos` y `alquiler_accesorios`.
4.  **Devolución (Entrada):**
    *   Actualizar `fecha_real` en `alquileres`.
    *   Actualizar `estado_devolucion` y `unidades_extraviadas` en los detalles.
5.  **Post-Alquiler:**
    *   Registrar `mantenimientos` si algún equipo devuelto requiere reparación.
    *   Actualizar `saldo_pendiente` del cliente según los cálculos.

---

## 5. Script SQL de Inicialización (Esquema)

```sql
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

CREATE SCHEMA IF NOT EXISTS `alquileres` DEFAULT CHARACTER SET utf8 ;
USE `alquileres` ;

CREATE TABLE IF NOT EXISTS `alquileres`.`clientes` (
  `id_cliente` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NOT NULL,
  `email` VARCHAR(45) NOT NULL,
  `saldo_pendiente` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `estado` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id_cliente`)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `alquileres`.`equipos` (
  `id_equipo` INT NOT NULL AUTO_INCREMENT,
  `tipo` VARCHAR(45) NOT NULL,
  `modelo` VARCHAR(45) NOT NULL,
  `tarifa_hora` DECIMAL(10,2) NOT NULL,
  `precio_reposicion` DECIMAL(10,2) NOT NULL,
  `bateria_porcentaje` INT NULL,
  `estado` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id_equipo`)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `alquileres`.`mantenimientos` (
  `id_mantenimiento` INT NOT NULL AUTO_INCREMENT,
  `id_equipo` INT NOT NULL,
  `fecha` DATETIME NOT NULL,
  `motivo` VARCHAR(100) NOT NULL,
  `costo` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id_mantenimiento`),
  INDEX `id_equipo_idx` (`id_equipo` ASC) VISIBLE,
  CONSTRAINT `fk_mantenimiento_equipo`
    FOREIGN KEY (`id_equipo`)
    REFERENCES `alquileres`.`equipos` (`id_equipo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `alquileres`.`accesorios` (
  `id_accesorio` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NOT NULL,
  `tarifa_fija` DECIMAL(10,2) NOT NULL,
  `precio_reposicion` DECIMAL(10,2) NOT NULL,
  `existencia_actual` INT NOT NULL,
  PRIMARY KEY (`id_accesorio`)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `alquileres`.`accesorioHistorial` (
  `id_movimiento` INT NOT NULL AUTO_INCREMENT,
  `id_accesorio` INT NOT NULL,
  `fecha` DATETIME NOT NULL,
  `cantidad` INT NOT NULL,
  `tipo_movimiento` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id_movimiento`),
  INDEX `id_accesorio_idx` (`id_accesorio` ASC) VISIBLE,
  CONSTRAINT `fk_historial_accesorio`
    FOREIGN KEY (`id_accesorio`)
    REFERENCES `alquileres`.`accesorios` (`id_accesorio`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `alquileres`.`alquileres` (
  `id_alquiler` INT NOT NULL AUTO_INCREMENT,
  `id_cliente` INT NOT NULL,
  `deposito_cobrado` DECIMAL(10,2) NOT NULL,
  `estado` VARCHAR(45) NOT NULL,
  `fecha_estimada` DATETIME NOT NULL,
  `fecha_real` DATETIME NULL,
  PRIMARY KEY (`id_alquiler`),
  INDEX `id_cliente_idx` (`id_cliente` ASC) VISIBLE,
  CONSTRAINT `fk_alquiler_cliente`
    FOREIGN KEY (`id_cliente`)
    REFERENCES `alquileres`.`clientes` (`id_cliente`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `alquileres`.`alquiler_equipos` (
  `id_alquiler` INT NOT NULL,
  `id_equipo` INT NOT NULL,
  `estado_devolucion` VARCHAR(45) NOT NULL,
  INDEX `id_equipo_idx` (`id_equipo` ASC) VISIBLE,
  PRIMARY KEY (`id_alquiler`, `id_equipo`),
  CONSTRAINT `fk_detalle_alquiler`
    FOREIGN KEY (`id_alquiler`)
    REFERENCES `alquileres`.`alquileres` (`id_alquiler`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_detalle_equipo`
    FOREIGN KEY (`id_equipo`)
    REFERENCES `alquileres`.`equipos` (`id_equipo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `alquileres`.`alquiler_accesorios` (
  `id_alquiler` INT NOT NULL,
  `id_accesorio` INT NOT NULL,
  `cantidad` INT NOT NULL,
  `unidades_extraviadas` INT NOT NULL,
  INDEX `id_alquiler_idx` (`id_alquiler` ASC) VISIBLE,
  INDEX `id_accesorio_idx` (`id_accesorio` ASC) VISIBLE,
  PRIMARY KEY (`id_alquiler`, `id_accesorio`),
  CONSTRAINT `fk_detalle_acc_alquiler`
    FOREIGN KEY (`id_alquiler`)
    REFERENCES `alquileres`.`alquileres` (`id_alquiler`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_detalle_acc_accesorio`
    FOREIGN KEY (`id_accesorio`)
    REFERENCES `alquileres`.`accesorios` (`id_accesorio`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
```