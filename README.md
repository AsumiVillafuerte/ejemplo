<div align="center">

# 🎟️ EventPass

### Sistema de Registro de Eventos

[![Angular](https://img.shields.io/badge/Angular-20-DD0031?style=for-the-badge&logo=angular&logoColor=white)](https://angular.io/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

---

📋 **Reporte de Pruebas** — Verificación del módulo de Registro con Datos Incorrectos

</div>

---

## 👥 Equipo de Pruebas

<table>
  <tr>
    <th>Integrante</th>
    <th>Rol</th>
    <th>Funcionalidad</th>
  </tr>
  <tr>
    <td><strong>CAHUANA FLORIAN, Leandro Sebastian</strong></td>
    <td><code>QA LEAD</code></td>
    <td>Registro — Datos incorrectos</td>
  </tr>
  <tr>
    <td><strong>PEÑAFIEL VILLAFUERTE, Asumi Esmeralda</strong></td>
    <td><code>QA TESTER</code></td>
    <td>Registro — Datos incorrectos</td>
  </tr>
  <tr>
    <td><strong>HUARACA HUAMAN, Luis Enrique</strong></td>
    <td><code>QA AUTOMATION</code></td>
    <td>Registro — Datos incorrectos</td>
  </tr>
  <tr>
    <td><strong>VALENZUELA CASAS, Josemaría</strong></td>
    <td><code>QA TESTER</code></td>
    <td>Registro — Datos incorrectos</td>
  </tr>
</table>

---

## 🎯 Funcionalidad Asignada

> **Registro — Datos Incorrectos**

El objetivo fue verificar que el sistema de registro de **EventPass** valide correctamente los datos ingresados por el usuario y **no permita continuar** cuando se introducen datos que no cumplen con las reglas establecidas.

<details>
<summary>🔍 <strong>¿Qué se evaluó?</strong></summary>
<br>

- Nombres y apellidos con números o símbolos
- Correos electrónicos con formato inválido
- Números de celular incorrectos
- Edades menores a la permitida
- Omisión de campos obligatorios

</details>

---

## 🧪 Casos de Prueba Realizados

<table>
  <tr>
    <th>ID</th>
    <th>Caso de Prueba</th>
    <th>Tipo</th>
    <th>Estado</th>
  </tr>
  <tr>
    <td><code>C01</code></td>
    <td>Registro con números o símbolos en nombre y apellidos</td>
    <td>Manual</td>
    <td>✅ PASS</td>
  </tr>
  <tr>
    <td><code>C02</code></td>
    <td>Registro con formato incorrecto en correo y celular</td>
    <td>Manual</td>
    <td>✅ PASS</td>
  </tr>
  <tr>
    <td><code>C03</code></td>
    <td>Registro con edad inferior a la mínima permitida</td>
    <td>Manual</td>
    <td>✅ PASS</td>
  </tr>
  <tr>
    <td><code>C04</code></td>
    <td>Registro con omisión de campos obligatorios</td>
    <td>Manual</td>
    <td>✅ PASS</td>
  </tr>
</table>

> **Resultado:** 4/4 pruebas manuales superadas ✅

---

## 🤖 Casos Automatizados

<details open>
<summary>📊 <strong>Análisis de automatización</strong></summary>
<br>

Se analizaron los cuatro casos considerando:

| Criterio | Descripción |
|----------|-------------|
| 🔄 Frecuencia | ¿Con qué frecuencia se ejecuta? |
| 🔁 Repetitividad | ¿Se repite en diferentes escenarios? |
| ⏱️ Tiempo | ¿Cuánto tiempo toma ejecutarla? |
| ⭐ Importancia | ¿Qué tan crítica es la funcionalidad? |
| 🪨 Estabilidad | ¿El sistema está estable para automatizar? |
| ✅ Validación | ¿Se puede validar automáticamente? |
| 🔧 Mantenimiento | ¿Cuánto cuesta mantener la automatización? |

</details>

### Casos Seleccionados para Automatización

<table>
  <tr>
    <th>ID</th>
    <th>Caso Automatizado</th>
    <th>Justificación</th>
  </tr>
  <tr>
    <td><code>C02</code></td>
    <td>Registro con formato incorrecto en correo y celular</td>
    <td>
      Prueba repetitiva y frecuente que permite comprobar diferentes correos y números de celular. Su resultado puede validarse automáticamente y su mantenimiento es bajo.
    </td>
  </tr>
  <tr>
    <td><code>C03</code></td>
    <td>Registro con edad inferior a la mínima permitida</td>
    <td>
      Permite probar diferentes fechas de nacimiento y comprobar automáticamente que los usuarios menores de 18 años no puedan continuar. Es repetitiva, estable y sencilla de validar.
    </td>
  </tr>
</table>

---

## 🛠️ Herramientas Utilizadas

<div align="center">

| Tecnología | Uso |
|:----------:|:---:|
| **Angular 20** | Frontend y formulario de registro |
| **Java 17** | Desarrollo del backend |
| **Spring Boot** | Framework del backend |
| **Spring WebFlux** | Implementación reactiva |
| **Spring Boot Validation** | Validación de datos |
| **JUnit / Spring Boot Test** | Pruebas automatizadas del backend |
| **Reactor Test** | Pruebas sobre componentes reactivos |
| **Maven** | Gestión de dependencias y ejecución de pruebas |
| **PostgreSQL** | Base de datos del sistema |

</div>

---

## 🚀 Instrucciones para Ejecutar las Pruebas

### 📦 Backend

<details>
<summary>⚙️ <strong>Prerrequisitos</strong></summary>
<br>

- Java 17 instalado
- Maven instalado
- PostgreSQL ejecutándose

</details>

```bash
# Ingresar a la carpeta del proyecto backend
cd backend

# Ejecutar las pruebas automatizadas
mvn test
```

Si todas las pruebas se ejecutan correctamente, verás:

```
BUILD SUCCESS
```

```bash
# Iniciar el backend
mvn spring-boot:run
```

---

### 🖥️ Frontend

```bash
# Ingresar a la carpeta del proyecto Angular
cd frontend

# Instalar dependencias
npm install

# Iniciar la aplicación
ng serve
```

Acceder desde el navegador a:

🔗 **http://localhost:4200**

Desde la aplicación se puede acceder al formulario de registro y realizar los casos de prueba correspondientes.

---

### ▶️ Ejecución de los Casos Automatizados

Para ejecutar los casos **C02** y **C03**, ejecutar las pruebas automatizadas en el backend:

```bash
cd backend
mvn test
```

<details>
<summary>📋 <strong>¿Qué se valida?</strong></summary>
<br>

- ✅ Que las validaciones de **correo electrónico** funcionen correctamente
- ✅ Que las validaciones de **número de celular** funcionen correctamente
- ✅ Que las validaciones de **edad** impidan el registro de menores de 18 años

</details>

---

<div align="center">

### 📊 Resumen de Pruebas

```
Pruebas Manuales:     4/4 PASS  ✅
Pruebas Automatizadas: 2/2 PASS  ✅
Total:                6/6 PASS  ✅
```

---

*Proyecto de Pruebas de Software — EventPass*

</div>
