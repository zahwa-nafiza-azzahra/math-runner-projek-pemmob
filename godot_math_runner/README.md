# 🏃‍♂️ Math Runner - Godot 4 (GDScript)

Game edukasi 2D mobile runner matematika yang dibuat menggunakan **Godot Engine 4** dan **GDScript**.

---

## 📂 Struktur Folder Proyek

```text
godot_math_runner/
├── project.godot                     # Konfigurasi Godot 4 (1080x1920 Mobile Portrait)
├── README.md                         # Dokumentasi Lengkap Proyek
├── assets/
│   └── sprites/
│       ├── player/                   # Frame animasi karakter (Run 1-4, Hoodie & Topi Σ)
│       │   ├── player_run_frame1.png
│       │   ├── player_run_frame2.png
│       │   ├── player_run_frame3.png
│       │   └── player_run_frame4.png
│       ├── background/               # Background lintasan kastil terapung
│       │   └── game_background.png
│       ├── items/                    # Koin emas koleksi
│       │   └── item_coin_single.png
│       ├── obstacles/                # Rintangan balok (X, ÷, duri)
│       │   ├── obstacle_block_red.png
│       │   ├── obstacle_block_green.png
│       │   └── obstacle_spike.png
│       ├── platforms/                # Platform jawaban matematika
│       │   └── math_answer_platform.png
│       └── ui/                       # Panel soal matematika
│           └── panel_question.png
├── scenes/
│   ├── main.tscn                     # Scene utama game
│   ├── player.tscn                   # Scene karakter pemain & animasi
│   ├── background.tscn               # Scene parallax lintasan berjalan
│   ├── coin.tscn                     # Scene koin emas
│   ├── obstacle.tscn                 # Scene rintangan
│   ├── answer_object.tscn            # Scene platform jawaban di jalur
│   └── ui/
│       ├── hud.tscn                  # Top HUD & Panel Soal + 4 Tombol Pilihan
│       ├── pause_menu.tscn           # Modal Pause
│       ├── game_over_menu.tscn       # Modal Game Over
│       └── level_complete_menu.tscn  # Modal Level Selesai
└── scripts/
    ├── autoload/                     # Singleton Managers (Autoload)
    │   ├── game_manager.gd           # State Machine (RUNNING, QUESTION, PAUSE, dll)
    │   ├── level_manager.gd          # Sistem konfigurasi level kelipatan 5
    │   ├── question_manager.gd       # Generator soal matematika & jawaban acak
    │   └── score_manager.gd          # Skor, Combo multiplier, Koin & Nyawa (HP)
    ├── player.gd                     # Kontroler pemain & 7 Animasi Sprite
    ├── background.gd                 # Pengendali scrolling & spawn objek
    ├── coin.gd                       # Logika pengambilan koin
    ├── obstacle.gd                   # Logika tabrakan rintangan
    ├── answer_object.gd              # Logika platform jawaban jalur
    ├── main.gd                       # Pengendali alur scene utama
    └── ui/
        ├── hud.gd                    # Integrasi HUD, Soal & 4 Tombol Jawaban
        ├── pause_menu.gd             # Logika tombol resume/restart
        ├── game_over_menu.gd         # Logika tombol main ulang
        └── level_complete_menu.gd    # Logika tombol next level
```

---

## 🎮 Cara Menjalankan di Godot 4

1. Buka **Godot Engine 4 (versi 4.0+)**.
2. Klik tombol **Import** (Impor).
3. Pilih file `D:\Semester5\PemrogramanMobile\ProjekUTS\godot_math_runner\project.godot`.
4. Klik **Import & Edit** (Impor & Edit).
5. Tekan tombol **F5** atau klik tombol **Play (▶)** di pojok kanan atas untuk menjalankan game!

---

## 🕹️ Kontrol Input

| Aksi | Mobile | Desktop (Mouse / Keyboard) |
|---|---|---|
| **Geser Jalur Kiri** | Usap / Drag ke Kiri | Tombol `A` atau `Panah Kiri` |
| **Geser Jalur Kanan** | Usap / Drag ke Kanan | Tombol `D` atau `Panah Kanan` |
| **Lompat (Jump)** | Usap / Drag ke Atas | Tombol `Space` / `W` / `Panah Atas` |
| **Pilih Jawaban 1-4** | Tap Tombol Jawaban | Klik Mouse atau Tombol Angka `1`, `2`, `3`, `4` |
| **Pause Game** | Tap Tombol `⏸` | Klik Tombol `⏸` |

---

## 🧩 7 Status Animasi Karakter (`player.gd`)

1. **Idle**: Karakter berdiri tegak, gerakan pernapasan halus (*breathing bobbing*) pada tubuh dan backpack simbol **Σ**.
2. **Run**: Siklus lari 4-frame dari sudut pandang belakang (*seamless loop*).
3. **Jump**: Take-off kompresi, melambung tinggi di udara, dan landing compression.
4. **Collect Coin**: Mengambil koin emas disertai kilauan partikel (*sparkle* `✨`).
5. **Correct Answer**: Selebrasi melompat gembira dengan aura hijau dan partikel bintang `⭐`.
6. **Wrong Answer**: Karakter membentur rintangan, efek getaran kaget (*stumble recoil shake* `💥`) dan berhenti sementara.
7. **Victory**: Selebrasi piala emas `🏆` dan lompatan kemenangan saat mencapai garis finish.

---

## ⚙️ Kustomisasi Level & Tingkat Kesulitan

Semua konfigurasi tingkat kesulitan diatur dalam satu file yang mudah diedit di:
📁 `scripts/autoload/level_manager.gd`:

```gdscript
var difficulty_tiers: Dictionary = {
    1: { # Level 1-5: Easy Penjumlahan (1-10)
        "name": "EASY (Tier 1)",
        "operations": ["+"],
        "min_num": 1,
        "max_num": 10,
        "target_questions": 5
    },
    2: { # Level 6-10: Easy Penjumlahan & Pengurangan (1-20)
        "name": "EASY (Tier 2)",
        "operations": ["+", "-"],
        "min_num": 1,
        "max_num": 20,
        "target_questions": 5
    },
    3: { # Level 11-15: Medium (Penjumlahan, Pengurangan & Perkalian)
        "name": "MEDIUM (Tier 3)",
        "operations": ["+", "-", "×"],
        "min_num": 2,
        "max_num": 30,
        "target_questions": 5
    },
    4: { # Level 16-20: Hard (Tabel Perkalian & Pembagian)
        "name": "HARD (Tier 4)",
        "operations": ["×", "÷"],
        "min_num": 2,
        "max_num": 12,
        "target_questions": 5
    },
    5: { # Level 21+: Expert (Semua Operasi Matematika)
        "name": "EXPERT (Tier 5)",
        "operations": ["+", "-", "×", "÷"],
        "min_num": 2,
        "max_num": 50,
        "target_questions": 6
    }
}
```
