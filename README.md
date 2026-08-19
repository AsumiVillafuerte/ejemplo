# vg-ms-product-sale

Microservicio transaccional encargado de gestionar las ventas de productos farmaceuticos dentro del sistema SIGRC (Sistema de Informacion para la Gestion de Recursos Cientificos). Permite registrar, consultar, anular, restaurar y modificar el tipo de ventas realizadas a pacientes, validando reglas de negocio como elegibilidad del paciente, disponibilidad de stock y ventana de modificacion temporal.

**Arquitectura:** Hexagonal (Ports & Adapters)
**Stack:** Spring Boot 3.5.10 | Java 17 | WebFlux | R2DBC | PostgreSQL | Keycloak (OAuth2/JWT)

---

## Casos de Uso

### CU-01: Registrar Venta de Productos Farmaceuticos

**Actor principal:** Cajero (CASHIER) o Administrador (ADMIN)
**Precondiciones:** El usuario esta autenticado con rol CASHIER o ADMIN. El paciente existe y se encuentra activo. Los productos solicitados tienen stock disponible y precio configurado.

**Flujo principal:**
1. El cajero envia la solicitud con el ID del paciente, el tipo de venta (Vendido/Donado) y la lista de productos con sus cantidades.
2. El sistema valida que el paciente sea de tipo "Adulto" o "Adulto mayor".
3. Para cada producto, verifica que este activo, tenga stock suficiente y tenga precio de venta configurado.
4. El sistema genera un numero de ticket con formato `FAR-yyyy-MM-dd-NNNN`.
5. Se persiste la venta y sus items en la base de datos.
6. Se descuenta las cantidades vendidas del stock de cada producto en `ms-products`.
7. Se retorna la venta completa con datos enriquecidos del paciente, usuario y productos.

**Flujo de excepcion (compensacion):** Si falla el descuento de stock, el sistema revierte automaticamente las cantidades ya descontadas y elimina la venta creada, implementando un patron Saga de compensacion.

### CU-02: Anular una Venta

**Actor principal:** Cajero (CASHIER) o Administrador (ADMIN)
**Precondiciones:** La venta existe y fue creada dentro de las ultimas 24 horas.

**Flujo principal:**
1. El usuario solicita la anulacion de una venta por su ID.
2. El sistema verifica que la venta no supere las 24 horas desde su creacion.
3. Se cambia el estado de la venta a "Revocado".
4. Se restaura el stock de todos los productos de la venta en `ms-products`.
5. Se confirma la operacion.

### CU-03: Consultar Ventas

**Actor principal:** Cajero (CASHIER) o Administrador (ADMIN)
**Precondiciones:** El usuario esta autenticado.

**Flujo principal:**
1. El usuario puede consultar una venta por ID, listar todas las ventas o filtrar por estado (Consignado/Revocado).
2. El sistema carga los items de la venta y enriquece la informacion consultando `ms-patients` (nombre y DNI del paciente), `ms-users` (nombre del usuario registrado) y `ms-products` (nombre comercial de cada producto).
3. Si algun servicio externo no responde, el sistema retorna valores por defecto sin bloquear la consulta.

### CU-04: Modificar Tipo de Venta

**Actor principal:** Cajero (CASHIER) o Administrador (ADMIN)
**Precondiciones:** La venta existe y fue creada dentro de las ultimas 24 horas.

**Flujo principal:**
1. El usuario solicita cambiar el tipo de una venta (entre "Vendido" y "Donado").
2. El sistema normaliza el valor recibido (acepta variantes como "donacion", "donada", "donacion con tilde").
3. Se actualiza el tipo de venta y la fecha de modificacion.

---

## Gestion de Datos

### Entidades Principales

| Entidad | Descripcion | Justificacion |
|---------|-------------|---------------|
| **Sale** | Almacena la informacion general de cada venta: ticket, paciente, usuario, total, tipo, estado y fechas | Es la entidad central del dominio. Permite rastrear quien realizo la venta, a que paciente y en que momento. El estado "Consignado/Revocado" permite controlar la vida util de la venta. |
| **SaleItem** | Detalla cada producto dentro de una venta: producto, cantidad, precio unitario y subtotal | Una venta puede contener multiples productos. Esta entidad permite calcular el total de forma granular y gestionar el stock de cada producto individualmente. El subtotal es una columna generada automaticamente en base de datos. |

### Atributos Clave y su Justificacion

| Atributo | Entidad | Justificacion |
|----------|---------|---------------|
| `ticket_number` | Sale | Identificador unico legible para el usuario, con formato `FAR-yyyy-MM-dd-NNNN`. Permite la trazabilidad de ventas en el mostrador. |
| `patient_id` | Sale | Referencia al paciente de `ms-patients`. Permite validar elegibilidad antes de la venta y enriquecer datos en consultas. |
| `user_id` | Sale | Almacena el `preferred_username` del JWT. Registra quien realizo la operacion para auditoria. |
| `sale_type` | Sale | Distingue entre ventas comerciales ("Vendido") y donaciones ("Donado"). Impacta en reportes financieros y de inventario social. |
| `status` | Sale | Controla el ciclo de vida: "Consignado" (activo) o "Revocado" (anulado). Permite restauraciones dentro de la ventana de 24h. |
| `sale_date` | Sale | Fecha de la venta. Permite reportes por periodo y validacion temporal. |
| `subtotal` | SaleItem | Columna generada (`unit_price * quantity`). Garantiza consistencia en calculos y evita errores de redondeo. |

### Relaciones con Otros Microservicios

```
vg-ms-product-sale
    ├── ms-patients   (consulta: validar paciente, obtener nombre/DNI)
    ├── ms-users      (consulta: obtener datos del usuario registrado)
    └── ms-products   (consulta: validar producto/stock, gestionar decremento/incremento de stock)
```

Todas las comunicaciones se realizan a traves del **API Gateway** centralizado, propagando el JWT del usuario autenticado (Token Relay).

---

## Funcionalidad

### Endpoints Disponibles

| Metodo | Endpoint | Funcionalidad | Roles |
|--------|----------|---------------|-------|
| `POST` | `/api/v1/sales` | Registrar una nueva venta | ADMIN, CASHIER |
| `GET` | `/api/v1/sales/{id}` | Obtener venta por ID con datos enriquecidos | ADMIN, CASHIER |
| `GET` | `/api/v1/sales` | Listar todas las ventas | ADMIN, CASHIER |
| `GET` | `/api/v1/sales/status/{status}` | Filtrar ventas por estado | ADMIN, CASHIER |
| `GET` | `/api/v1/sales/{id}/items` | Obtener items de una venta | ADMIN, CASHIER |
| `DELETE` | `/api/v1/sales/{id}` | Eliminar venta (fisico, dentro de 24h) | ADMIN, CASHIER |
| `PATCH` | `/api/v1/sales/{id}/revoke` | Anular venta y restaurar stock | ADMIN, CASHIER |
| `PATCH` | `/api/v1/sales/{id}/restore` | Restaurar venta revocada | ADMIN, CASHIER |
| `PATCH` | `/api/v1/sales/{id}/type` | Cambiar tipo de venta | ADMIN, CASHIER |

### Funcionalidades Transversales

- **Validacion de elegibilidad del paciente:** Solo adultos y adultos mayores pueden realizar compras.
- **Gestion de stock con compensacion:** Decremento automatico al vender, restauracion al anular, con patron Saga ante fallos.
- **Ventana de modificacion de 24h:** Ventas solo pueden ser modificadas, anuladas o eliminadas dentro de las primeras 24 horas.
- ** Enriquecimiento de datos:** Las consultas combinan informacion de 3 microservicios para entregar una respuesta completa al cliente.
- **Normalizacion de tipos:** Acepta variantes como "donacion", "donada", "donacion con tilde" y las normaliza a "Donado".
- **Seguridad OAuth2:** Autenticacion via Keycloak con JWT. Roles extraidos del realm y resource access.
- **Documentacion OpenAPI:** Swagger UI habilitado en `/swagger-ui.html`.
- **Actuator:** Health checks y metricas en `/actuator`.

---

## Requisitos de Calidad

### Requisitos Funcionales

| Codigo | Requisito | Descripcion |
|--------|-----------|-------------|
| RF-01 | Registro de venta | El sistema debe permitir registrar ventas validando paciente, productos y stock antes de persistir. |
| RF-02 | Gestion de stock | El sistema debe decrementar stock al vender y restaurar al anular, con compensacion automatica ante fallos. |
| RF-03 | Consulta enriquecida | Las consultas deben retornar datos del paciente, usuario y productos consultando servicios externos. |
| RF-04 | Ventana temporal | Solo se permiten modificaciones dentro de las 24 horas posteriores a la creacion de la venta. |
| RF-05 | Tipos de venta | El sistema debe soportar ventas comerciales y donaciones, normalizando variantes de entrada. |

### Requisitos No Funcionales

| Codigo | Requisito | Descripcion |
|--------|-----------|-------------|
| RNF-01 | Disponibilidad | El servicio debe estar disponible al menos el 99.5% del tiempo. |
| RNF-02 | Rendimiento | Las respuestas deben retornar en menos de 2 segundos bajo carga normal. |
| RNF-03 | Escalabilidad | El pool de conexiones R2DBC debe escalar de 2 a 10 conexiones segun demanda. |
| RNF-04 | Seguridad | Todas las operaciones requieren JWT valido con rol CASHIER o ADMIN. |
| RNF-05 | Tolerancia a fallos | Si un servicio externo falla, el sistema debe retornar valores por defecto en lugar de fallar la operacion completa. |
| RNF-06 | No bloqueo | El uso de WebFlux garantiza operaciones reactivas non-blocking en todo el pipeline. |

### Requisitos de Pruebas

| Codigo | Requisito | Descripcion |
|--------|-----------|-------------|
| RP-01 | Pruebas unitarias | Cada caso de uso debe tener cobertura de escenarios positivos, negativos y de excepcion. |
| RP-02 | Aislamiento | Las pruebas deben usar mocks (Mockito) para simular repositorios y clientes HTTP, sin depender de servicios externos. |
| RP-03 | Validacion | Todas las pruebas deben pasar con `mvn test` antes de cada despliegue. |

---

## Escenarios de Pruebas Unitarias

### Escenario 1: Crear Venta (CreateProductSaleUseCaseImplTest)

| Tipo | Descripcion | Resultado Esperado |
|------|-------------|-------------------|
| **Positivo** | Crear una venta con datos validos (paciente adulto activo, productos con stock y precio) | Se retorna la venta creada con ticket generado, estado "Consignado" y stock decrementado |
| **Negativo** | Intentar crear una venta con un paciente inactivo | Se retorna `DomainException` indicando que el paciente no se encuentra activo |
| **Excepcion** | Intentar crear una venta cuando el stock de un producto es insuficiente | Se retorna `DomainException` indicando stock insuficiente |

### Escenario 2: Consultar Venta (GetProductSaleUseCaseImplTest)

| Tipo | Descripcion | Resultado Esperado |
|------|-------------|-------------------|
| **Positivo** | Obtener una venta por ID con datos enriquecidos de paciente, usuario y productos | Se retorna la venta completa con nombres resueltos de paciente, usuario y productos |
| **Negativo** | Intentar obtener una venta que no existe | Se retorna `NotFoundException` |
| **Excepcion** | Consultar una venta cuando los servicios externos (patients, users, products) no responden | Se retorna la venta con valores por defecto (strings vacios) sin fallar la operacion |

### Escenario 3: Eliminar Venta (DeleteProductSaleUseCaseImplTest)

| Tipo | Descripcion | Resultado Esperado |
|------|-------------|-------------------|
| **Positivo** | Eliminar una venta creada dentro de las 24 horas | Se eliminan los items y la venta exitosamente |
| **Negativo** | Intentar eliminar una venta que no existe | Se retorna `NotFoundException` |
| **Excepcion** | Intentar eliminar una venta creada hace mas de 24 horas | Se retorna `DomainException` indicando que supero la ventana de modificacion |

### Escenario 4: Actualizar Tipo de Venta (UpdateSaleTypeUseCaseImplTest)

| Tipo | Descripcion | Resultado Esperado |
|------|-------------|-------------------|
| **Positivo** | Actualizar el tipo de una venta a "Donado" | Se actualiza el tipo de venta exitosamente |
| **Negativo** | Intentar actualizar una venta que no existe | Se retorna `NotFoundException` |
| **Excepcion** | Intentar actualizar una venta creada hace mas de 24 horas | Se retorna `DomainException` indicando que supero la ventana de modificacion |

---

## Ejecucion de Pruebas

```bash
# Ejecutar todas las pruebas unitarias
./mvnw test

# Ejecutar pruebas de un caso de uso especifico
./mvnw test -Dtest=CreateProductSaleUseCaseImplTest
./mvnw test -Dtest=GetProductSaleUseCaseImplTest
./mvnw test -Dtest=DeleteProductSaleUseCaseImplTest
./mvnw test -Dtest=UpdateSaleTypeUseCaseImplTest
```

**Repositorio:** https://gitlab.com/vallegrande/as241s5_prs4/vg-ms-product-sale

