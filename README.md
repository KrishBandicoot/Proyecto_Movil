## Estudiantes: Cristian Lizama y Sofia Troncoso
## Resumen del Proyecto 

**Kkarhua** es una aplicación Android de comercio electrónico para accesorios artesanales que cumple con todos los requisitos solicitados:

### ✅ Requisitos Cumplidos

| Requisito | Estado | Implementación |
|-----------|--------|----------------|
| Interfaz visual organizada | ✅ | Material Design, navegación clara |
| Formularios validados | ✅ | Login/Registro con validación en tiempo real |
| Validaciones en lógica | ✅ | `ValidationUtils.kt` separado de UI |
| Animaciones funcionales | ✅ | 8 animaciones diferentes implementadas |
| Estructura modular MVVM | ✅ | ViewModels, Repositories, Room DB |
| Persistencia local | ✅ | Room Database con productos y carrito |
| Recursos nativos (2+) | ✅ | GPS (LocationHelper) y Cámara (CameraHelper) |
| GitHub + Trello | ✅ | Estructura lista para versionado |

---

## Estructura del Proyecto 

```
com.example.kkarhua/
│
├── 📁 data/
│   ├── local/
│   │   ├── AppDatabase.kt          ✅ Base de datos Room
│   │   ├── Product.kt              ✅ Entity de productos
│   │   ├── ProductDao.kt           ✅ DAO de productos
│   │   ├── CartItem.kt             ✅ Entity del carrito
│   │   └── CartDao.kt              ✅ DAO del carrito
│   │
│   └── repository/
│       ├── ProductRepository.kt     ✅ Repositorio de productos
│       └── CartRepository.kt        ✅ Repositorio del carrito
│
├── 📁 ui/
│   ├── auth/
│   │   ├── LoginFragment.kt        ✅ Inicio de sesión
│   │   └── RegisterFragment.kt     ✅ Registro con validación
│   │
│   ├── home/
│   │   └── HomeFragment.kt         ✅ Pantalla principal
│   │
│   ├── products/
│   │   ├── ProductListFragment.kt  ✅ Lista de productos
│   │   └── ProductAdapter.kt       ✅ Adapter con animaciones
│   │
│   ├── productdetail/
│   │   └── ProductDetailFragment.kt ✅ Detalle del producto
│   │
│   └── cart/
│       ├── CartFragment.kt         ✅ Carrito de compras
│       └── CartAdapter.kt          ✅ Adapter del carrito
│
├── 📁 viewmodel/
│   ├── ProductListViewModel.kt     ✅ ViewModel de productos
│   └── CartViewModel.kt            ✅ ViewModel del carrito
│
├── 📁 utils/
│   ├── ValidationUtils.kt          ✅ Validaciones centralizadas
│   ├── LocationHelper.kt           ✅ Acceso a GPS
│   └── CameraHelper.kt             ✅ Acceso a cámara
│
└── MainActivity.kt                  ✅ Activity principal
```
##  Características Implementadas 

### 1.  Animaciones (8 tipos)

| Animación | Uso | Archivo |
|-----------|-----|---------|
| `fade_in.xml` | Entrada suave de elementos | Títulos, imágenes |
| `fade_out.xml` | Salida suave | Transiciones |
| `slide_up.xml` | Entrada desde abajo | Formularios, botones |
| `slide_down.xml` | Salida hacia abajo | Modales |
| `slide_in_left.xml` | Navegación | Transición de pantallas |
| `slide_in_right.xml` | Navegación | Transición de pantallas |
| `slide_out_left.xml` | Navegación | Transición de pantallas |
| `slide_out_right.xml` | Navegación | Transición de pantallas |
| `bounce.xml` | Feedback al tocar | Botones interactivos |
| `item_animation_fall_down.xml` | RecyclerView items | Lista de productos |



### Cumplimiento de Requisitos:

| Requisito | Cumplimiento | Evidencia |
|-----------|--------------|-----------|
| Interfaz visual | 100% | Material Design + layouts organizados |
| Formularios validados | 100% | Login + Registro con íconos y mensajes |
| Validaciones en lógica | 100% | ValidationUtils.kt desacoplado |
| Animaciones | 100% | 10 animaciones diferentes |
| MVVM + Persistencia | 100% | ViewModels + Room Database |
