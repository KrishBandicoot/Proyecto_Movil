# 🛠️ Documentación y Configuración del Backend

Este proyecto utiliza un backend RESTful implementado en **Xano**. A continuación se detalla la estructura de la base de datos, los endpoints disponibles y las instrucciones para replicar este backend en Xano o en cualquier otra plataforma (Supabase, Firebase, Spring Boot, etc.).

## 🗂️ Estructura de la Base de Datos

Para recrear la base de datos con la estructura exacta utilizada en esta aplicación, se puede utilizar el siguiente esquema.

### 🤖 Prompt de Generación (Esquema Xano)
Si utilizas el asistente de IA de Xano u otra herramienta de generación de esquemas, este es el prompt base que define la arquitectura de datos:

> "I need to set up my database with several tables to manage users, products, addresses, and purchases, reflecting the current structure.
>
> **1. User Table:**
> A user table for user information and authentication. Beyond the automatic id and created_at fields, I need:
> * `name` (text)
> * `email` (email, unique)
> * `password` (password)
> * `role` (enum with values 'admin', 'member')
> * `state` (enum with values 'activo', 'bloqueado')
>
> **2. Product Table:**
> A product table for inventory management. It should include id and created_at, plus:
> * `name` (text)
> * `description` (text)
> * `price` (decimal)
> * `stock` (integer)
> * `category` (enum with values 'collares', 'aros', 'broches', 'anillos', 'pulseras', 'tobilleras', 'otros')
> * `updated_at` (epochms)
> * `is_deleted` (boolean)
> * `image` (image blob/url)
> * `image2` (image blob/url)
> * `image3` (image blob/url)
>
> **3. Address Table:**
> An address table to store delivery locations, linked to a user. It needs id and created_at, plus:
> * `address_line_1` (text)
> * `apartment_number` (text, optional)
> * `region` (enum with values 'Metropolitana', 'Valparaíso', 'Araucanía')
> * `commune` (enum with values 'Santiago', 'Las Condes', 'Maipu', 'Valparaiso', 'Viña del Mar', 'Quillota', 'Temuco', 'Pucon', 'Villarica')
> * `shipping_instructions` (text, optional)
> * `user_id` (reference to the user table)
>
> **4. Purchase Table:**
> A purchase table to track main order details. This table will need id and created_at, along with:
> * `user_id` (reference to user)
> * `address_id` (reference to address)
> * `total_amount` (decimal)
> * `status` (enum with values 'pendiente', 'aprovado', 'rechazado')
>
> **5. Purchase Item Table:**
> Finally, a purchase_item table to detail the products within each purchase. It will have id and created_at, and:
> * `purchase_id` (reference to purchase)
> * `product_id` (reference to product)
> * `quantity` (integer)
> * `price_at_purchase` (decimal)"

---

## 🚀 Instrucciones para Construir/Replicar el Backend

Si necesitas desplegar este backend nuevamente, sigue estos pasos:

### 1. Configuración de Tablas
Crea las 5 tablas mencionadas arriba (`user`, `product`, `address`, `purchase`, `purchase_item`). Asegúrate de respetar los tipos de datos, especialmente los **Enums** para roles, categorías y estados, ya que la aplicación Android espera estos valores exactos.

### 2. Almacenamiento de Imágenes
El backend debe soportar la subida de archivos.
* **En Xano:** Habilita el almacenamiento de archivos en la configuración del workspace.
* **Los campos `image`, `image2`, `image3`** en la tabla `product` deben ser de tipo *Image* o *File Resource*.

### 3. Configuración de Endpoints (API)
Debes crear los siguientes grupos de endpoints API para que la app Android funcione correctamente con Retrofit.

#### **Auth (Autenticación)**
* `POST /auth/signup`: Recibe nombre, email, password. Crea usuario con rol por defecto 'member' y estado 'activo'. Devuelve Auth Token.
* `POST /auth/login`: Recibe email, password. Devuelve Auth Token y objeto User.

#### **Productos (Product)**
* `GET /product`: Lista todos los productos (puede incluir filtros por `is_deleted = false`).
* `GET /product/{id}`: Detalle de un producto específico.
* `POST /product`: **(Multipart/Form-Data)**. Recibe campos de texto y archivos (`image`, `image2`, `image3`). Crea el registro.
* `PATCH /product/{id}`: Recibe JSON parcial para actualizar campos (nombre, precio, stock, etc.).
* `DELETE /product/{id}`: Marca el producto como eliminado (`is_deleted = true`) o lo borra físicamente.

#### **Usuarios (User) - Solo Admin**
* `GET /user`: Lista todos los usuarios.
* `PATCH /user/{id}`: Para bloquear/desbloquear usuarios (cambiar campo `state`) o editar roles.

#### **Compras (Purchase)**
* `POST /purchase`: Crea una nueva orden. Debe recibir el `address_id` y la lista de items. Calcula el total y crea registros en `purchase` y `purchase_item`.
* `GET /purchase/history`: Retorna las compras del usuario autenticado.
* `GET /purchase/pending`: (Admin) Retorna todas las compras con status 'pendiente'.
* `PATCH /purchase/{id}`: (Admin) Para cambiar el status a 'aprovado' o 'rechazado'.

### 4. Credenciales de Prueba Sugeridas
Para las pruebas de la aplicación (Demo), se recomienda crear los siguientes usuarios en la base de datos:

**Admin:**
* **Email:** `admin@gmail.com`
* **Password:** `admin123`
* **Role:** `admin`

**Cliente:**
* **Email:** `cliente@gmail.com`
* **Password:** `cliente123`
* **Role:** `member`

---

## ⚙️ Notas Técnicas de Implementación

* **Manejo de Imágenes:** La aplicación Android envía las imágenes como `MultipartBody.Part`. El backend debe ser capaz de recibir *form-data* con claves `image`, `image2` e `image3`. Si una imagen es opcional, el backend debe manejar la ausencia de esa clave sin fallar (o recibir un archivo vacío/dummy según la lógica implementada).
* **Seguridad:** Todos los endpoints excepto Login, Registro y Listar Productos (público) deben requerir **Autenticación (JWE Token)** en el header `Authorization: Bearer <token>`.
* **Validación de Roles:** Los endpoints de creación/edición de productos y gestión de usuarios deben validar en el servidor que el usuario tenga `role = 'admin'`.
