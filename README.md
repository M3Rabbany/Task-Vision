# Task Vision 🚀

**Task Vision** adalah aplikasi **Project Management Hub** berbasis Java/Spring Boot untuk membantu tim mengelola project, task, approval, feedback, notifikasi, KPI, dan laporan Excel dalam satu workspace.

Aplikasi ini sudah dilengkapi frontend web yang disajikan langsung oleh Spring Boot, sehingga tidak memerlukan Node.js, React, Vite, atau proses build frontend terpisah. Akses web dilindungi HTTPS melalui reverse proxy **Caddy** (redirect HTTP → HTTPS, HSTS di mode produksi, dan header keamanan), sehingga kredensial tidak dikirim dalam bentuk plaintext.

---

## ✨ Fitur Utama

- 🔑 Autentikasi JWT (login, lupa password, reset password)
- 🔒 Akses HTTPS lewat Caddy: redirect HTTP → HTTPS, HSTS (produksi), dan header keamanan
- 📁 Manajemen project dan anggota tim
- 📌 Task board dengan alur **On going → Pending → Approved / Rejected**
- ✅ Approval task oleh Project Manager
- 💬 Feedback task untuk anggota tim
- 🔔 Notifikasi aktivitas dan badge notifikasi belum dibaca
- 📧 Email selamat datang dengan template HTML responsif dan fallback teks
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
        │  HTTPS (TLS) — port 443, HTTP :80 dialihkan ke HTTPS
        ▼
Caddy (sertifikat + redirect + header keamanan)
        │  HTTP internal di jaringan Docker — port 8080 (tidak diekspos publik)
        ▼
Spring Boot: Controller → Service → Repository → Entity
                              │
                              └── JWT Security + validasi
```

- Halaman aplikasi dilayani oleh `PageController`.
- Backend hanya mendengarkan port `8080` di dalam jaringan Docker; satu-satunya pintu masuk publik adalah Caddy.
- Aset frontend dilayani dari `src/main/resources/static`.
- Otorisasi API tetap dilakukan di server melalui `SecurityConfig`.
- Frontend hanya menyembunyikan menu/aksi sesuai role; keputusan keamanan tetap di backend.

---

## 🔐 Role dan Akses

| Role | Akses utama |
|---|---|
| **ADMIN** | Dashboard, projects, users, task board (baca saja), notifications, profil |
| **PROJECT_MANAGER** | Projects, task board, approval/feedback, reports, notifications, profil |
| **MEMBER** | Task saya, projects, pengajuan approval, feedback, notifications, profil |

> Admin dapat meninjau task board sebagai oversight, tetapi pembuatan, perubahan, approval, dan feedback task tetap dilakukan oleh Project Manager.

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
3. Jalankan aplikasi, database, dan reverse proxy HTTPS:
   ```bash
   docker compose up --build
   ```
4. Buka [https://localhost](https://localhost) (atau `https://<IP-server>`). Karena mode default memakai sertifikat internal Caddy, browser akan menampilkan peringatan sertifikat pada kunjungan pertama — lihat bagian [HTTPS dan Perlindungan Kredensial](#-https-dan-perlindungan-kredensial).

> Pastikan port **80** dan **443** di host tidak dipakai layanan lain. Compose akan menolak start jika port tersebut sudah terpakai.

Compose menjalankan tiga service: MySQL (`db`, hanya di jaringan internal), aplikasi Spring Boot (`backend`, hanya di jaringan internal), dan Caddy (`caddy`, satu-satunya yang terbuka di port 80/443). Akses langsung ke `http://localhost:8080` tidak lagi tersedia — ini disengaja agar tidak ada jalur plaintext untuk kredensial.

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

> Mode `spring-boot:run` ini berjalan tanpa Caddy, jadi koneksinya masih HTTP biasa dan hanya cocok untuk debugging lokal. Untuk akses yang dilindungi HTTPS, pakai Opsi 1 (Docker Compose) atau letakkan aplikasi di balik reverse proxy HTTPS.

### Konfigurasi opsional

| Environment Variable | Deskripsi | Default |
|---|---|---|
| `SERVER_PORT` | Port aplikasi di dalam kontainer (tidak diekspos publik saat memakai Compose) | `8080` |
| `DB_URL` | URL JDBC MySQL | `jdbc:mysql://db:3306/mydb` |
| `DB_USER` | Username database | `root` |
| `DB_PASSWORD` | Password database (wajib) | dari `.env` |
| `JWT_SECRET` | Secret penandatangan JWT | dibuat sementara jika kosong |
| `DDL_AUTO` | Strategi Hibernate schema | `update` |
| `CADDYFILE` | Profil HTTPS Caddy: `Caddyfile.local` (self-signed) atau `Caddyfile.prod` (Let's Encrypt) | `Caddyfile.local` |
| `LOCAL_HOSTS` | Hostname/IP yang mendapat sertifikat lokal; tambahkan IP jaringan jika diakses dari perangkat lain | `localhost` |
| `DOMAIN` | Domain publik untuk Caddy; wajib jika `CADDYFILE=Caddyfile.prod` | kosong |
| `APP_FRONTEND_URL` | Base URL tautan reset password dan tautan login di email (harus `https://...`) | `https://localhost` |
| `MAIL_HOST` / `MAIL_PORT` | Server SMTP | `smtp.gmail.com` / `587` |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Kredensial SMTP; kosong berarti email nonaktif | kosong |
| `DEFAULT_ADMIN_EMAIL` / `DEFAULT_ADMIN_PASSWORD` | Akun admin awal; kosong berarti tidak dibuat otomatis | kosong |

> Jika email SMTP belum dikonfigurasi, fitur yang bergantung pada pengiriman email (misalnya tautan reset password dan email selamat datang) tidak akan mengirim pesan sungguhan. Operasi utama seperti membuat user, project, dan task tetap berjalan.

### Email tidak terkirim?

1. Isi `MAIL_USERNAME` dan `MAIL_PASSWORD` di `.env`.
2. Untuk Gmail, aktifkan verifikasi 2 langkah lalu buat **App Password** 16 karakter. Gunakan App Password tersebut sebagai `MAIL_PASSWORD`, bukan password akun Gmail.
3. Pastikan `APP_FRONTEND_URL` mengarah ke alamat aplikasi yang bisa dibuka user.
4. Terapkan ulang konfigurasi:
   ```bash
   docker compose up -d --build backend
   ```
5. Periksa status konfigurasi email di log:
   ```bash
   docker compose logs backend | grep -Ei 'email|mail|smtp'
   ```

---

## 🔐 HTTPS dan Perlindungan Kredensial

Semua akses ke Task Vision melalui Compose melewati Caddy di port `443`. Backend tidak diekspos langsung, dan permintaan ke `http://...` pada port `80` otomatis dialihkan (308) ke HTTPS.

### Mode lokal (default, belum punya domain)

`.env` memakai `CADDYFILE=Caddyfile.local`. Caddy membuat sertifikat dari **Caddy Local Authority**, sehingga lalu lintas tetap terenkripsi, tetapi browser belum mengenali penerbitnya. Mode ini cocok untuk development/testing. Secara default yang tercakup hanya `localhost`; jika perangkat lain mengakses lewat IP jaringan, set misalnya `LOCAL_HOSTS=localhost, 192.168.1.10` di `.env`, lalu jalankan `docker compose up -d --force-recreate caddy`.

Akses dari laptop/klien:

- Pakai `https://taskvision.thelabproject.com` **hanya jika** klien memakai DNS yang mengenal nama itu (mis. DNS Active Directory). Cek dengan `nslookup taskvision.thelabproject.com 192.168.14.24`; hasilnya harus mengarah ke IP server.
- Alternatif paling aman untuk testing: buka langsung `https://<IP-server>/login` (mis. `https://192.168.13.47/login`). `LOCAL_HOSTS` sudah memasukkan IP tersebut dan Caddy memakai `default_sni` agar handshake TLS berhasil untuk IP literal.
- Jangan buka `https://localhost` dari laptop: `localhost` berarti laptop itu sendiri, bukan server.

Mempercayai sertifikat lokal (opsional, agar peringatan browser hilang):

1. Buka `https://localhost` satu kali agar Caddy membuat root CA.
2. Salin root CA ke host:
   ```bash
   docker compose exec -T caddy cat /data/caddy/pki/authorities/local/root.crt > caddy-local-root.crt
   ```
3. Import `caddy-local-root.crt` ke trust store sistem/browser (Linux: `update-ca-certificates`, macOS: Keychain Access, Windows: Certificate Manager), lalu muat ulang browser.

> **Penting:** mode self-signed saja **tidak** membuktikan identitas server. Jika user mengabaikan peringatan browser, koneksi masih bisa disadap (MITM). Untuk user sungguhan, gunakan mode produksi dengan sertifikat publik.

### Mode produksi (domain publik)

1. Siapkan domain (mis. `taskvision.example.com`) dan arahkan record A/AAAA **publik** ke IP publik server tempat Caddy berjalan. Jika record masih mengarah ke host/load balancer lain, validasi ACME gagal dan sertifikat tidak akan terbit. Cek dari luar jaringan internal dengan `dig +short taskvision.example.com`.
2. Buka port `80` dan `443` di firewall/security group.
3. Isi `.env`:
   ```env
   CADDYFILE=Caddyfile.prod
   DOMAIN=taskvision.example.com
   APP_FRONTEND_URL=https://taskvision.example.com
   ```

   > Nama profil **case-sensitive** di Linux: `Caddyfile.prod` (huruf `f` kecil). Jika ditulis `CaddyFile.prod`, Docker membuat direktori kosong dengan nama tersebut dan mount gagal.

4. Terapkan konfigurasi:
   ```bash
   docker compose up -d --force-recreate caddy backend
   ```
5. Cek sertifikat dan redirect:
   ```bash
   curl -I https://taskvision.example.com
   curl -I http://taskvision.example.com   # harus 308 Location: https://...
   ```

Caddy otomatis mengambil dan memperbarui sertifikat Let's Encrypt/ZeroSSL, mengaktifkan HSTS (`max-age=31536000; includeSubDomains`), serta mengirim header `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, dan Content-Security-Policy. HSTS sengaja tidak dinyalakan di mode lokal supaya browser tidak mengunci akses ke HTTPS saat masih bereksperimen.

### Catatan keamanan

- HTTPS melindungi kredensial **saat dikirim di jaringan**. Token JWT tetap disimpan di `localStorage` browser, sehingga XSS masih menjadi risiko; jangan menyisipkan script/event handler yang tidak dipercaya ke halaman aplikasi.
- Jangan pakai `http://` untuk `APP_FRONTEND_URL` di produksi, karena tautan reset password akan mengarah ke koneksi plaintext.
- Simpan `.env`, private key, dan secret hanya di server; jangan commit ke Git.

---

## 🌐 URL Frontend

Semua path di bawah diakses dengan prefiks HTTPS, misalnya `https://localhost/` (lokal) atau `https://taskvision.example.com/` (produksi).

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
- Akses produksi wajib lewat HTTPS dengan sertifikat yang dipercaya publik (mode `Caddyfile.prod`); jangan mengandalkan sertifikat self-signed untuk user sungguhan.
- Backend sengaja tidak mengekspos port `8080` ke publik. Jangan menambahkan kembali mapping `8080:8080` pada `compose.yaml` karena akan membypass enkripsi HTTPS.

---

## 📄 License

This project is intended for learning and personal development purposes.
