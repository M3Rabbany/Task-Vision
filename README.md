# Task Vision 🚀

**Task Vision** adalah aplikasi **Project Management Hub** berbasis Java/Spring Boot untuk membantu tim mengelola project, task, approval, feedback, notifikasi, KPI, dan laporan Excel dalam satu workspace.

Aplikasi ini sudah dilengkapi frontend web yang disajikan langsung oleh Spring Boot, sehingga tidak memerlukan Node.js, React, Vite, atau proses build frontend terpisah.

---

## ✨ Fitur Utama

- 🔑 Autentikasi JWT (login, lupa password, reset password)
- 📁 Manajemen project dan anggota tim
- 📌 Task board dengan alur **On going → Pending → Approved / Rejected**
- ✅ Approval task oleh Project Manager
- 💬 Feedback task untuk anggota tim
- 🔔 Notifikasi aktivitas dan badge notifikasi belum dibaca
- 📊 Dashboard project, deadline, dan KPI anggota
- 🧾 Pratinjau laporan project dan export Excel
- 👥 Manajemen akun, role, dan KPI (Admin)
- 🌗 Tema terang/gelap dan tampilan responsif

---

## 🛠️ Tech Stack

| Layer | Teknologi |
|---|---|
| Backend | Java 17, Spring Boot |
| Database | MySQL 8 |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA |
| Dokumentasi API | Springdoc OpenAPI / Swagger UI |
| Frontend | HTML5, CSS3, JavaScript ES Modules |
| Testing | JUnit + Mockito |

Frontend berada di `src/main/resources/static` dan memakai CSS/JavaScript murni tanpa dependensi CDN atau build tool.

---

## 📌 Arsitektur

```text
Browser (HTML/CSS/ES Modules)
        │  fetch + Bearer JWT
        ▼
Controller → Service → Repository → Entity
                 │
                 └── JWT Security + validasi
```

- Halaman aplikasi dilayani oleh `PageController`.
- Aset frontend dilayani dari `src/main/resources/static`.
- Otorisasi API tetap dilakukan di server melalui `SecurityConfig`.
- Frontend hanya menyembunyikan menu/aksi sesuai role; keputusan keamanan tetap di backend.

---

## 🔐 Role dan Akses

| Role | Akses utama |
|---|---|
| **ADMIN** | Dashboard, projects, users, notifications, profil |
| **PROJECT_MANAGER** | Projects, task board, approval/feedback, reports, notifications, profil |
| **MEMBER** | Task saya, projects, pengajuan approval, feedback, notifications, profil |

> Admin tidak mengelola task board. Task dibuat dan di-approve oleh Project Manager.

---

## 🚀 Menjalankan Aplikasi

### Prasyarat

- Java 17
- MySQL 8 (atau Docker)
- Maven Wrapper sudah tersedia (`./mvnw`)

### Opsi 1 — Docker Compose

1. Salin konfigurasi environment:
   ```bash
   cp .env.example .env
   ```
   Isi minimal `DB_PASSWORD` dan `JWT_SECRET` di `.env`. Untuk membuat secret JWT acak:
   ```bash
   openssl rand -hex 32
   ```
   Jangan commit file `.env`.
2. Build jar terlebih dahulu:
   ```bash
   ./mvnw -DskipTests package
   ```
3. Jalankan aplikasi dan database:
   ```bash
   docker compose up --build
   ```
4. Buka [http://localhost:8080](http://localhost:8080).

Compose akan menjalankan MySQL di service `db` dan aplikasi di port `8080`.

### Opsi 2 — Jalankan lokal dengan MySQL

1. Buat database, misalnya:
   ```sql
   CREATE DATABASE mydb;
   ```
2. Set environment variable database. Nilai default di `application.properties` memakai host `db` (untuk Docker), jadi untuk MySQL lokal set:
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/mydb"
   export DB_USER="root"
   export DB_PASSWORD="password_anda"
   export JWT_SECRET="$(openssl rand -hex 32)"
   ```
3. Jalankan aplikasi:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Buka [http://localhost:8080](http://localhost:8080).

### Konfigurasi opsional

| Environment Variable | Deskripsi | Default |
|---|---|---|
| `SERVER_PORT` | Port aplikasi | `8080` |
| `DB_URL` | URL JDBC MySQL | `jdbc:mysql://db:3306/mydb` |
| `DB_USER` | Username database | `root` |
| `DB_PASSWORD` | Password database (wajib) | dari `.env` |
| `JWT_SECRET` | Secret penandatangan JWT | dibuat sementara jika kosong |
| `DDL_AUTO` | Strategi Hibernate schema | `update` |
| `APP_FRONTEND_URL` | Base URL tautan reset password | `http://localhost:8080` |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Kredensial SMTP; kosong berarti email nonaktif | kosong |
| `DEFAULT_ADMIN_EMAIL` / `DEFAULT_ADMIN_PASSWORD` | Akun admin awal; kosong berarti tidak dibuat otomatis | kosong |

> Jika email SMTP belum dikonfigurasi, fitur yang bergantung pada pengiriman email (misalnya tautan reset password) tidak akan mengirim pesan sungguhan.

---

## 🌐 URL Frontend

| URL | Halaman |
|---|---|
| `/` atau `/login` | Halaman masuk |
| `/forgot-password` | Form lupa password |
| `/reset-password?token=...` | Form reset password |
| `/app` | Aplikasi utama (setelah login) |
| `/swagger-ui.html` | Dokumentasi API |

Halaman aplikasi menggunakan hash router:

- `#/dashboard`
- `#/projects`
- `#/tasks`
- `#/users`
- `#/reports`
- `#/notifications`
- `#/profile`

Menu yang tampil menyesuaikan role pengguna.

---

## 👤 Akun Admin Awal

Isi `DEFAULT_ADMIN_EMAIL` dan `DEFAULT_ADMIN_PASSWORD` di `.env` sebelum menjalankan aplikasi pertama kali. Jika keduanya kosong, pembuatan admin otomatis akan dilewati.

Akun Project Manager dan Member dapat dibuat melalui menu **Users** (Admin), lalu ditambahkan ke project melalui tombol **Atur anggota** pada halaman Projects.

---

## 📁 Struktur Frontend

```text
src/main/resources/static/
├── index.html              # Login & lupa password
├── app.html                # Shell aplikasi
├── reset-password.html     # Reset password
├── favicon.svg
├── css/
│   └── styles.css          # Design system, layout, komponen, responsive
└── js/
    ├── app.js              # Router, navigasi, shell, notifikasi
    ├── auth.js             # Sesi, JWT, role, tema
    ├── api.js              # Wrapper fetch + semua endpoint API
    ├── ui.js               # Ikon, modal, toast, formatter, komponen
    ├── login.js
    ├── reset-password.js
    └── views/
        ├── dashboard.js
        ├── projects.js
        ├── tasks.js
        ├── users.js
        ├── reports.js
        ├── notifications.js
        └── profile.js
```

Sesi login disimpan di `localStorage` (atau `sessionStorage` jika “Ingat saya” tidak dicentang). Token dikirim sebagai header `Authorization: Bearer <token>` pada setiap request API.

---

## 📌 Ringkasan API

Semua endpoint utama berada di bawah `/api/v1` dan mengembalikan envelope:

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {},
  "error": null
}
```

| Area | Endpoint utama |
|---|---|
| Autentikasi | `POST /api/auth/login`, `POST /api/auth/forgot-password`, `POST /api/auth/reset-password` |
| Users | `GET /api/v1/users/get`, `POST /api/v1/users/create`, `PUT/DELETE /api/v1/users/{id}` |
| Task anggota | `GET /api/v1/users/{userId}/tasks`, `GET .../tasks/pending`, `GET .../tasks/feedback`, `PUT /api/v1/users/{taskId}/approval` |
| Projects | `GET /api/v1/projects/get`, `POST /api/v1/projects/create`, `PUT/DELETE /api/v1/projects/{id}`, `POST /api/v1/projects/assign`, `GET /api/v1/projects/users/{userId}` |
| Tasks | `GET/POST /api/v1/projects/{projectId}/tasks/get|create`, `PUT/DELETE .../tasks/{taskId}`, `POST .../assign`, `POST .../approve`, `POST .../feedback` |
| Dashboard | `GET /api/v1/dashboard/status`, `GET /api/v1/dashboard/kpi` |
| Notifikasi | `GET /api/v1/notifications/user/{userId}`, `PUT /api/v1/notifications/{id}/read` |
| Laporan | `POST /api/v1/report`, `GET /api/v1/report/download?projectId=...` |

Dokumentasi lengkap tersedia di `/swagger-ui.html`.

---

## 🧪 Menjalankan Test

Test konteks Spring Boot memerlukan koneksi MySQL. Gunakan konfigurasi database yang sama seperti saat menjalankan aplikasi:

```bash
export DB_URL="jdbc:mysql://localhost:3306/mydb"
export DB_USER="root"
export DB_PASSWORD="password_anda"
./mvnw test
```

Untuk sekadar memastikan build berhasil tanpa menjalankan test:

```bash
./mvnw -DskipTests package
```

---

## 🔒 Catatan Keamanan

- Jangan commit file `.env` atau nilai credential apa pun ke repository.
- Simpan `JWT_SECRET` dan password database hanya di environment server.
- Credential yang pernah masuk ke riwayat Git harus dianggap bocor dan sebaiknya segera dirotasi.

---

## 📄 License

This project is intended for learning and personal development purposes.
