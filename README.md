# Guía de Investigación - Microservicio de Productos (vg-ms-products)

## 1. Casos de Uso

Los casos de uso describen las interacciones entre los actores del sistema y el microservicio. En una arquitectura de microservicios, cada caso de uso representa una responsabilidad clara y acotada del servicio.

---

### 1.1 Microservicio Maestro: Gestión de Marcas

Un microservicio maestro es aquel que administra entidades base o catálogos que son referenciados por otros servicios. En este caso, las marcas son entidades maestro porque los productos dependen de ellas para existir.

#### Caso de Uso 1: Registrar Marca (CU-01)

**Descripción detallada:**
El administrador del sistema envía los datos de una nueva marca (nombre). El microservicio valida que el nombre no esté duplicado en el sistema, ya que es un atributo único. Si la validación es exitosa, crea la marca con estado activo ('A') por defecto y retorna el registro creado con su identificador UUID generado automáticamente.

**Flujo principal:**
1. El actor envía una solicitud POST con el nombre de la marca
2. El sistema valida que el nombre no sea nulo ni vacío
3. El sistema verifica que no exista otra marca con el mismo nombre
4. El sistema crea el registro con estado 'A' (Activo)
5. El sistema retorna la marca creada con su ID

**Flujo alternativo (error):**
- Si el nombre ya existe → retorna error 400 con mensaje de duplicidad
- Si el nombre es inválido → retorna error de validación

**Justificación:** Las marcas son entidades fundamentales para la organización de productos. Mantener un catálogo único de marcas permite agrupar productos por fabricante o línea comercial, facilitando reportes, búsquedas y filtros en la capa de presentación.

---

#### Caso de Uso 2: Eliminar Marca - Lógico y Físico (CU-02)

**Descripción detallada:**
El sistema ofrece dos tipos de eliminación para las marcas, cada uno con un propósito diferente:

**Eliminación Lógica:** El sistema cambia el estado de la marca de 'A' (Activo) a 'I' (Inactivo). La marca sigue existiendo en la base de datos pero se considera "desactivada" para efectos de negocio. Los productos asociados a esta marca mantienen su referencia, preservando la integridad referencial.

**Eliminación Física:** El sistema elimina permanentemente el registro de la marca de la base de datos. Esta operación es irreversible y solo se debe realizar cuando no existen productos asociados o cuando se requiere limpieza definitiva de datos.

**Flujo principal (eliminación lógica):**
1. El actor envía una solicitud DELETE con el ID de la marca
2. El sistema busca la marca por su ID
3. Si existe y está activa, cambia su estado a 'I'
4. Retorna la marca con el nuevo estado

**Flujo principal (eliminación física):**
1. El actor envía una solicitud DELETE con el ID y un parámetro de confirmación
2. El sistema verifica que no existan productos asociados
3. Elimina permanentemente el registro
4. Retorna confirmación de eliminación

**Justificación:** La eliminación lógica es esencial en sistemas transaccionales donde perder datos puede afectar reportes históricos o integridad referencial. La eliminación física se reserva para casos donde se necesita liberar espacio o corregir errores de carga de datos.

---

### 1.2 Microservicio Transaccional: Gestión de Productos

Un microservicio transaccional maneja operaciones que involucran cambios de estado en el negocio, como ventas, movimientos de inventario o transacciones financieras. Los productos representan el corazón del catálogo comercial.

#### Caso de Uso 3: Registrar Producto (CU-03)

**Descripción detallada:**
El administrador envía los datos de un nuevo producto: nombre comercial, marca asociada (ID), precio de compra, precio de venta, stock inicial y ubicación. El microservicio valida que:
- La marca especificada exista y esté activa
- Los precios sean mayores o iguales a cero
- El stock inicial no sea negativo
- El nombre comercial no esté vacío

Si todas las validaciones pasan, crea el producto con estado activo y retorna el registro completo.

**Flujo principal:**
1. El actor envía una solicitud POST con todos los datos del producto
2. El sistema valida la existencia de la marca (consulta al microservicio maestro o a la tabla de marcas)
3. El sistema valida rangos de precios y stock
4. El sistema crea el producto con timestamps de creación y actualización
5. El sistema retorna el producto creado

**Flujo alternativo (error):**
- Si la marca no existe → retorna error 404 "Marca no encontrada"
- Si los precios son negativos → retorna error de validación
- Si el stock es negativo → retorna error de validación

**Justificación:** Los productos son la unidad mínima de venta. Cada producto debe estar correctamente asociado a una marca para mantener la trazabilidad y permitir análisis de ventas por fabricante. La validación estricta de datos asegura la calidad de la información en el catálogo.

---

#### Caso de Uso 4: Actualizar Stock - Incremento y Decremento (CU-04)

**Descripción detallada:**
El sistema permite modificar el stock de un producto mediante dos operaciones transaccionales:

**Incremento de Stock:** Aumenta la cantidad de unidades disponibles. Se utiliza cuando se recibe mercadería, se devuelve un producto o se corrige un inventario.

**Decremento de Stock:** Reduce la cantidad de unidades disponibles. Se utiliza cuando se realiza una venta, se envía mercadería o se registra una pérdida. El sistema valida que el stock resultante no sea negativo.

**Flujo principal (incremento):**
1. El sistema recibe solicitud con ID del producto y cantidad a incrementar
2. Valida que la cantidad sea mayor a cero
3. Obtiene el stock actual del producto
4. Suma la cantidad al stock actual
5. Actualiza el registro en base de datos
6. Retorna el stock actualizado

**Flujo principal (decremento):**
1. El sistema recibe solicitud con ID del producto y cantidad a decrementar
2. Valida que la cantidad sea mayor a cero
3. Obtiene el stock actual del producto
4. Verifica que stock actual ≥ cantidad solicitada
5. Resta la cantidad del stock actual
6. Actualiza el registro en base de datos
7. Retorna el stock actualizado

**Flujo alternativo (error):**
- Si el stock es insuficiente → retorna error "Stock insuficiente"
- Si la cantidad es cero o negativa → retorna error de validación

**Justificación:** El control de stock es una operación crítica en cualquier sistema de inventario. La separación en incremento y decremento permite implementar validaciones específicas para cada caso, como verificar disponibilidad antes de decrementar. Esto previene errores de inventario y asegura la consistencia de datos.

---

## 2. Gestión de Datos

### 2.1 Entidad: Marca (Brand)

**Propósito:** Almacena el catálogo de marcas comerciales que identifican a los fabricantes o líneas de productos.

| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| `id` | UUID | PRIMARY KEY, NOT NULL | Identificador único generado automáticamente. Se utiliza UUID en lugar de autoincremental para permitir generación descentralizada de IDs, lo cual es fundamental en arquitecturas de microservicios donde múltiples instancias pueden crear registros simultáneamente. |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE | Nombre comercial de la marca. La restricción de unicidad previene duplicidades que causarían confusión en el catálogo. |
| `status` | CHAR(1) | DEFAULT 'A', CHECK IN ('A','I') | Estado de la marca: 'A' (Activa) o 'I' (Inactiva). Este diseño permite eliminación lógica sin perder referencias en tablas dependientes. |

**Justificación de diseño:**
- **UUID vs Autoincremental:** En microservicios, el UUID permite que cada servicio genere IDs sin coordinación central, evitando conflictos en despliegues distribuidos.
- **Status como CHAR:** Es más eficiente que un booleano porque permite extender a más estados (ej: 'P' para Pendiente, 'B' para Bloqueado) sin modificar la estructura.
- **Constraints de base de datos:** Las restricciones a nivel de DB son la última línea de defensa contra datos inválidos, incluso si la validación de la aplicación falla.

---

### 2.2 Entidad: Producto (Product)

**Propósito:** Almacena la información comercial y de inventario de cada producto disponible para la venta.

| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| `id` | UUID | PRIMARY KEY, NOT NULL | Identificador único del producto. |
| `commercial_name` | VARCHAR(150) | NOT NULL | Nombre comercial del producto que aparece en catálogos y facturas. |
| `brand_id` | UUID | FOREIGN KEY → brands.id, NOT NULL | Referencia a la marca propietaria del producto. La FK asegura integridad referencial. |
| `purchase_price` | DECIMAL(10,2) | NOT NULL, CHECK ≥ 0 | Precio de compra al proveedor. Se usa DECIMAL para precisión monetaria (evita errores de punto flotante). |
| `sale_price` | DECIMAL(10,2) | NOT NULL, CHECK ≥ 0 | Precio de venta al cliente. Permite calcular márgenes de ganancia. |
| `stock` | INTEGER | DEFAULT 0, CHECK ≥ 0 | Cantidad de unidades disponibles. Se valida con CHECK constraint para prevenir stock negativo a nivel de DB. |
| `location` | VARCHAR(50) | NOT NULL | Ubicación física del producto (almacén, tienda, zona). |
| `status` | CHAR(1) | DEFAULT 'A', CHECK IN ('A','I') | Estado del producto: Activo o Inactivo. |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha y hora de creación del registro. |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha y hora de la última actualización. |

**Justificación de diseño:**
- **DECIMAL para precios:** Los tipos flotante (float/double) acumulan errores de redondeo en operaciones financieras. DECIMAL almacena valores exactos, esencial para cálculos de IVA, márgenes y reportes contables.
- **Separación de precios:** Tener purchase_price y sale_price permite calcular el margen de ganancia (sale_price - purchase_price) y el porcentaje de margen, información crítica para análisis de rentabilidad.
- **Timestamps:** created_at y updated_at permiten auditoría, resolución de conflictos y análisis de antigüedad de datos.
- **Ubicación (location):** Campo esencial para logística y almacén, permite identificar dónde está fisicamente cada producto.

---

### 2.3 Relación entre Entidades

```
┌─────────────────────────────────────────────────────────────┐
│                    Modelo de Datos                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐         ┌─────────────────────────┐   │
│  │     brands      │         │       products           │   │
│  ├─────────────────┤         ├─────────────────────────┤   │
│  │ PK id     (UUID)│◄───┐    │ PK id            (UUID) │   │
│  │    name         │    └────│ FK brand_id      (UUID) │   │
│  │    status       │         │    commercial_name      │   │
│  └─────────────────┘         │    purchase_price       │   │
│                              │    sale_price           │   │
│                              │    stock                │   │
│                              │    location             │   │
│                              │    status               │   │
│                              │    created_at           │   │
│                              │    updated_at           │   │
│                              └─────────────────────────┘   │
│                                                             │
│  Cardinalidad: Una marca puede tener muchos productos (1:N)│
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Integridad referencial:** La clave foránea `brand_id` en la tabla `products` garantiza que cada producto esté asociado a una marca válida. Si se intenta crear un producto con una marca inexistente, la base de datos rechazará la operación.

**Eliminación y referencias:** Si se elimina físicamente una marca que tiene productos asociados, se produciría un error de integridad referencial. Por eso se prefiere la eliminación lógica (cambio de estado) que mantiene la referencia intacta.

---

## 3. Funcionalidades

### 3.1 Microservicio de Productos (vg-ms-products)

Las funcionalidades se organizan por dominio de negocio:

#### Funcionalidades de Marcas

| Código | Funcionalidad | Descripción Detallada |
|--------|---------------|----------------------|
| F-01 | **Crear Marca** | Endpoint POST que recibe el nombre de la marca, valida unicidad, crea el registro con estado activo y retorna el objeto creado con su UUID. |
| F-02 | **Obtener Marca por ID** | Endpoint GET que busca una marca por su identificador único. Retorna la marca completa o error 404 si no existe. |
| F-03 | **Listar Marcas** | Endpoint GET que retorna todas las marcas registradas (activas e inactivas). Útil para catálogos y reportes. |
| F-04 | **Actualizar Marca** | Endpoint PUT que permite modificar el nombre de una marca existente. Valida unicidad del nuevo nombre. |
| F-05 | **Eliminar Marca (Lógica)** | Endpoint DELETE que cambia el estado de la marca a 'I' (Inactiva). No elimina el registro físico. |
| F-06 | **Eliminar Marca (Física)** | Endpoint DELETE con confirmación que elimina permanentemente la marca de la base de datos. |
| F-07 | **Restaurar Marca** | Endpoint que cambia el estado de una marca inactiva ('I') a activa ('A'). Útil para reactivar marcas temporalmente descontinuadas. |

#### Funcionalidades de Productos

| Código | Funcionalidad | Descripción Detallada |
|--------|---------------|----------------------|
| F-08 | **Crear Producto** | Endpoint POST que recibe datos del producto, valida existencia de marca, precios y stock, crea el registro y retorna el objeto completo. |
| F-09 | **Obtener Producto por ID** | Endpoint GET que busca un producto por UUID. Incluye información de la marca asociada. |
| F-10 | **Listar Productos** | Endpoint GET que retorna todos los productos. Permite filtrar por marca, estado o ubicación. |
| F-11 | **Actualizar Producto** | Endpoint PUT que permite modificar datos del producto. Valida integridad de referencias. |
| F-12 | **Eliminar Producto (Lógica)** | Endpoint DELETE que cambia estado a 'I'. Los productos inactivos no aparecen en búsquedas comerciales. |
| F-13 | **Eliminar Producto (Física)** | Endpoint DELETE con confirmación para eliminación permanente. |
| F-14 | **Restaurar Producto** | Endpoint que reactiva productos inactivos, cambiando estado a 'A'. |
| F-15 | **Incrementar Stock** | Endpoint que aumenta el stock de un producto. Valida cantidad positiva. Operación transaccional atómica. |
| F-16 | **Decrementar Stock** | Endpoint que reduce el stock. Valida disponibilidad suficiente. Previene stock negativo. |

#### Funcionalidades Técnicas

| Código | Funcionalidad | Descripción Detallada |
|--------|---------------|----------------------|
| F-17 | **API RESTful** | Endpoints estandarizados siguiendo convenciones REST: métodos HTTP correctos (GET, POST, PUT, DELETE), códigos de respuesta apropiados (200, 201, 400, 404, 500). |
| F-18 | **Documentación Swagger** | Interfaz auto-generada con SpringDoc OpenAPI que permite explorar y probar la API desde el navegador. |
| F-19 | **Manejo de Errores** | Excepciones globales manejadas con @ControllerAdvice que retornan respuestas consistentes y amigables. |
| F-20 | **Actuator Endpoints** | Endpoints de monitoreo (/actuator/health, /actuator/info) para verificar el estado del servicio. |

---

## 4. Requisitos de Calidad

Los requisitos de calidad definen los criterios que el microservicio debe cumplir para ser considerado aceptable en producción.

### 4.1 Requisitos Funcionales

| ID | Requisito | Descripción | Criterio de Aceptación |
|----|-----------|-------------|------------------------|
| RF-01 | **Validación de datos de entrada** | Todos los campos obligatorios deben ser validados antes de procesar la solicitud. | Campos nulos, vacíos o fuera de rango retornan error 400 con mensaje descriptivo. |
| RF-02 | **Integridad referencial** | Los productos solo pueden asociarse a marcas que existan en el sistema. | Intentar crear producto con marca inexistente retorna error 404. |
| RF-03 | **Unicidad de marca** | No se permiten dos marcas con el mismo nombre. | Intentar crear marca duplicada retorna error 400. |
| RF-04 | **Control de stock** | El stock nunca debe ser negativo. | Decrementar stock por encima de la disponible retorna error. |
| RF-05 | **Persistencia de datos** | Los datos deben mantenerse entre reinicios del servicio. | Los registros creados persisten en PostgreSQL. |
| RF-06 | **Auditoría básica** | Cada registro debe incluir timestamps de creación y actualización. | Los campos created_at y updated_at se populate automáticamente. |

### 4.2 Requisitos No Funcionales

| ID | Categoría | Requisito | Descripción | Criterio de Aceptación |
|----|-----------|-----------|-------------|------------------------|
| RNF-01 | **Rendimiento** | Tiempo de respuesta | Las operaciones CRUD deben completarse rápidamente. | Tiempo promedio < 500ms para operaciones individuales. |
| RNF-02 | **Rendimiento** | Concurrencia | El servicio debe manejar múltiples solicitudes simultáneas. | Arquitectura reactiva (WebFlux) permite alto throughput sin bloqueo de hilos. |
| RNF-03 | **Disponibilidad** | Tolerancia a fallos | El servicio debe degradarse gracefully ante errores. | Errores de base de datos retornan 500 con mensaje genérico (sin expone detalles internos). |
| RNF-04 | **Disponibilidad** | Health checks | El servicio debe reportar su estado de salud. | Endpoint /actuator/health retorna estado UP cuando la DB está conectada. |
| RNF-05 | **Seguridad** | Autenticación | Los endpoints deben estar protegidos contra acceso no autorizado. | Endpoints requieren token OAuth2 válido (Keycloak). |
| RNF-06 | **Seguridad** | Autorización | Solo usuarios con rol ADMIN pueden modificar datos. | Operaciones de escritura validan rol del usuario. |
| RNF-07 | **Mantenibilidad** | Código limpio | El código debe seguir convenciones establecidas. | Nombres descriptivos, comentarios Javadoc, estructura de paquetes clara. |
| RNF-08 | **Mantenibilidad** | Pruebas unitarias | La lógica de negocio debe tener cobertura de pruebas. | Cobertura mínima del 80% en capa de casos de uso. |
| RNF-09 | **Observabilidad** | Logging estructurado | Las operaciones deben generar trazas de ejecución. | Logs con timestamp, nivel, mensaje y correlación de transacción. |
| RNF-10 | **Observabilidad** | Métricas | El servicio debe exponer métricas de rendimiento. | Endpoints de Prometheus o Micrometer para monitoreo. |

---

## 5. Escenarios de Pruebas Unitarias

Las pruebas unitarias validan que la lógica de negocio funcione correctamente en aislamiento, utilizando mocks para simular dependencias externas (base de datos, otros servicios).

### 5.1 Microservicio Maestro: Marcas

#### Escenario 1: Creación Exitosa de Marca (Positivo)

**Objetivo:** Verificar que una marca se crea correctamente cuando se proporcionan datos válidos.

**Precondiciones:** No existe marca con el nombre "Samsung" en el sistema.

**Datos de entrada:**
```json
{
  "name": "Samsung"
}
```

**Pasos de ejecución:**
1. Se invoca el caso de uso CreateBrand con el nombre "Samsung"
2. Se verifica que el repositorio fue llamado para guardar
3. Se verifica que se retorna un objeto Brand con ID generado, nombre "Samsung" y estado 'A'

**Resultado esperado:** Se retorna un objeto Brand con:
- `id`: UUID válido (no nulo)
- `name`: "Samsung"
- `status`: 'A'

**Tipo de prueba:** Positivo (happy path)

---

#### Escenario 2: Creación con Nombre Duplicado (Negativo)

**Objetivo:** Verificar que el sistema rechaza marcas con nombres duplicados.

**Precondiciones:** Ya existe una marca con nombre "Samsung" en el sistema.

**Datos de entrada:**
```json
{
  "name": "Samsung"
}
```

**Pasos de ejecución:**
1. Se invoca el caso de uso CreateBrand con el nombre "Samsung"
2. El repositorio indica que ya existe una marca con ese nombre
3. Se verifica que se lanza una excepción de negocio

**Resultado esperado:** Se lanza excepción con mensaje indicando que la marca ya existe.

**Tipo de prueba:** Negativo (validación de negocio)

---

#### Escenario 3: Creación con Nombre Vacío (Excepción)

**Objetivo:** Verificar que el sistema valida campos obligatorios antes de procesar.

**Precondiciones:** Ninguna.

**Datos de entrada:**
```json
{
  "name": ""
}
```

**Pasos de ejecución:**
1. Se invoca el caso de uso CreateBrand con nombre vacío
2. Se verifica que se lanza una excepción de validación

**Resultado esperado:** Se lanza excepción indicando que el nombre es obligatorio.

**Tipo de prueba:** Excepción (validación de entrada)

---

#### Escenario 4: Eliminación Lógica de Marca (Positivo)

**Objetivo:** Verificar que una marca se desactiva correctamente sin eliminar el registro.

**Precondiciones:** Existe una marca con ID válido y estado 'A'.

**Datos de entrada:** ID de la marca a eliminar

**Pasos de ejecución:**
1. Se invoca el caso de uso DeleteBrand con el ID
2. Se busca la marca por ID
3. Se cambia el estado de 'A' a 'I'
4. Se guarda el cambio
5. Se retorna la marca con estado 'I'

**Resultado esperado:** Se retorna la marca con `status`: 'I'. El registro sigue existiendo en la base de datos.

**Tipo de prueba:** Positivo (eliminación lógica)

---

### 5.2 Microservicio Transaccional: Productos

#### Escenario 5: Creación Exitosa de Producto (Positivo)

**Objetivo:** Verificar que un producto se crea correctamente con datos válidos y marca existente.

**Precondiciones:** Existe una marca con ID válido y estado 'A'.

**Datos de entrada:**
```json
{
  "commercialName": "Laptop HP",
  "brandId": "uuid-de-marca-existente",
  "purchasePrice": 1500.00,
  "salePrice": 2000.00,
  "stock": 10,
  "location": "Lima"
}
```

**Pasos de ejecución:**
1. Se invoca el caso de uso CreateProduct con los datos
2. Se verifica que la marca existe
3. Se crea el producto con estado 'A' y timestamps
4. Se retorna el producto completo

**Resultado esperado:** Se retorna un objeto Product con ID, datos proporcionados, estado 'A' y fechas de creación/actualización.

**Tipo de prueba:** Positivo (happy path)

---

#### Escenario 6: Creación con Marca Inexistente (Negativo)

**Objetivo:** Verificar que no se permite crear productos con marcas que no existen.

**Precondiciones:** No existe marca con el ID proporcionado.

**Datos de entrada:**
```json
{
  "commercialName": "Laptop HP",
  "brandId": "uuid-inexistente",
  "purchasePrice": 1500.00,
  "salePrice": 2000.00,
  "stock": 10,
  "location": "Lima"
}
```

**Pasos de ejecución:**
1. Se invoca el caso de uso CreateProduct
2. Se intenta buscar la marca por ID
3. La marca no es encontrada
4. Se lanza excepción de marca no encontrada

**Resultado esperado:** Se lanza excepción con mensaje "Marca no encontrada" o código 404.

**Tipo de prueba:** Negativo (integridad referencial)

---

#### Escenario 7: Creación con Stock Negativo (Excepción)

**Objetivo:** Verificar que el sistema rechaza productos con stock inicial negativo.

**Precondiciones:** Existe una marca válida.

**Datos de entrada:**
```json
{
  "commercialName": "Mouse",
  "brandId": "uuid-marca-valida",
  "purchasePrice": 25.00,
  "salePrice": 40.00,
  "stock": -5,
  "location": "Arequipa"
}
```

**Pasos de ejecución:**
1. Se invoca el caso de uso CreateProduct con stock -5
2. Se detecta que el stock es menor a cero
3. Se lanza excepción de validación

**Resultado esperado:** Se lanza excepción indicando que el stock no puede ser negativo.

**Tipo de prueba:** Excepción (validación de regla de negocio)

---

#### Escenario 8: Decremento de Stock Insuficiente (Negativo)

**Objetivo:** Verificar que no se permite decrementar stock por encima de la disponible.

**Precondiciones:** Producto con stock actual = 5.

**Datos de entrada:**
```json
{
  "productId": "uuid-producto",
  "quantity": 10
}
```

**Pasos de ejecución:**
1. Se invoca el caso de uso DecrementStock
2. Se obtiene el stock actual (5)
3. Se verifica que 5 < 10 (stock insuficiente)
4. Se lanza excepción

**Resultado esperado:** Se lanza excepción con mensaje "Stock insuficiente" indicando que hay 5 unidades disponibles y se solicitaron 10.

**Tipo de prueba:** Negativo (regla de negocio de stock)

---

## 6. Arquitectura Técnica

### 6.1 Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Capa de Presentación                         │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    API REST (Controllers)                     │  │
│  │                                                               │  │
│  │   BrandController          ProductController                  │  │
│  │   - POST /api/brands       - POST /api/products               │  │
│  │   - GET /api/brands/{id}   - GET /api/products/{id}           │  │
│  │   - GET /api/brands        - GET /api/products                 │  │
│  │   - PUT /api/brands/{id}   - PUT /api/products/{id}           │  │
│  │   - DELETE /api/brands/{id} - DELETE /api/products/{id}        │  │
│  │                         - POST /api/products/{id}/stock/increment│
│  │                         - POST /api/products/{id}/stock/decrement│
│  └───────────────────────────────────────────────────────────────┘  │
│                                    │                                │
│                                    ▼                                │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                     Capa de Negocio                           │  │
│  │                                                               │  │
│  │  ┌─────────────────────┐    ┌─────────────────────────────┐   │  │
│  │  │   Use Cases         │    │   Domain Models             │   │  │
│  │  │                     │    │                             │   │  │
│  │  │  CreateBrandUseCase │    │  Brand                      │   │  │
│  │  │  GetBrandUseCase    │    │  Product                    │   │  │
│  │  │  UpdateBrandUseCase │    │  CreateBrandRequest         │   │  │
│  │  │  DeleteBrandUseCase │    │  CreateProductRequest       │   │  │
│  │  │  RestoreBrandUseCase│    │  UpdateStockRequest         │   │  │
│  │  │  ...                │    │                             │   │  │
│  │  └─────────────────────┘    └─────────────────────────────┘   │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                    │                                │
│                                    ▼                                │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                  Capa de Infraestructura                      │  │
│  │                                                               │  │
│  │  ┌─────────────────────┐    ┌─────────────────────────────┐   │  │
│  │  │   Repositories      │    │   Database                  │   │  │
│  │  │   (R2DBC)           │    │                             │   │  │
│  │  │                     │    │   PostgreSQL (Neon)          │   │  │
│  │  │  BrandRepository    │◄──▶│   - brands                  │   │  │
│  │  │  ProductRepository  │    │   - products                 │   │  │
│  │  └─────────────────────┘    └─────────────────────────────┘   │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 6.2 Stack Tecnológico

| Capa | Tecnología | Versión | Justificación |
|------|------------|---------|---------------|
| **Framework** | Spring Boot | 3.5.10 | Framework maduro con soporte completo para microservicios, autoconfiguración y gestión de dependencias. |
| **Lenguaje** | Java | 17 | LTS con records, switch expressions y mejoras de rendimiento. Amplio ecosistema de librerías. |
| **Web** | Spring WebFlux | Reactivo | Permite alto rendimiento con poco consumo de recursos. Ideal para I/O intensivo como consultas a DB. |
| **Base de Datos** | PostgreSQL (Neon) | 18.4 | DB relacional robusta, open source, con soporte para UUID, CHECK constraints y extensiones. Neon ofrece serverless. |
| **Conexión DB** | R2DBC | Reactivo | Driver reactivo para PostgreSQL. Permite non-blocking I/O en acceso a datos. |
| **Build** | Maven | 3.9+ | Gestión de dependencias, compilación y empaquetado estandarizado. |
| **Documentación** | SpringDoc OpenAPI | Swagger UI | Genera documentación interactiva de la API automáticamente desde las anotaciones. |
| **Validación** | Jakarta Validation | Bean Validation 2.0 | Validación declarativa de datos de entrada con anotaciones (@NotNull, @Min, etc.). |
| **Testing** | JUnit 5 + Mockito | Latest | Framework de testing estándar para Java con soporte para mocking y assertions reactivas (StepVerifier). |

### 6.3 Patrón de Arquitectura: Clean Architecture + Hexagonal

El microservicio sigue los principios de Clean Architecture:

```
┌─────────────────────────────────────────────────────┐
│                  Dominio (Core)                      │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │  Entities: Brand, Product                   │   │
│  │  Business Rules: Validations, Constraints   │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
├─────────────────────────────────────────────────────┤
│               Casos de Uso (Use Cases)              │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │  CreateBrand, UpdateBrand, DeleteBrand      │   │
│  │  CreateProduct, UpdateProduct, etc.         │   │
│  │                                             │   │
│  │  Cada caso de uso orquesta una operación    │   │
│  │  de negocio específica y cohesiva.          │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
├─────────────────────────────────────────────────────┤
│            Adaptadores (Controllers/Repos)           │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │  Controllers: HTTP → Use Cases              │   │
│  │  Repositories: Use Cases → Database         │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**Beneficios de este patrón:**
- **Independencia:** Los casos de uso no dependen de frameworks ni bases de datos
- **Testeabilidad:** Fácil mockear dependencias para pruebas unitarias
- **Flexibilidad:** Se puede cambiar de R2DBC a JPA sin modificar casos de uso
- **Claridad:** Cada clase tiene una única responsabilidad

---

## 7. Conclusiones

El microservicio de productos (vg-ms-products) implementa una arquitectura reactiva moderna que permite:

1. **Escabilidad:** La naturaleza no bloqueante de WebFlux permite manejar miles de conexiones concurrentes con recursos mínimos.

2. **Integridad de datos:** Las validaciones en múltiples capas (aplicación + base de datos) garantizan consistencia.

3. **Mantenibilidad:** La separación en casos de uso claros facilita el entendimiento y modificación del código.

4. **Trazabilidad:** Los timestamps y logs estructurados permiten auditoría y diagnóstico de problemas.

5. **Flexibilidad operativa:** La eliminación lógica permite recuperar datos eliminados accidentalmente, mientras que la eliminación física libera espacio cuando es necesario.
