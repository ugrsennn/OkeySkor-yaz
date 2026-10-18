# 101 Okey Skor

101 Okey için basit skor takip uygulaması (Android, Kotlin + Jetpack Compose).

## Özellikler
- 4 oyuncu, istersen **eşli** oyun (1 & 3, 2 & 4 takım)
- Her el sonunda puan girişi; hızlı düğmeler: **Bitti −101**, **Okeyle/Çifte −202**, **Açamadı 202**, **Ceza +101**
- `±` düğmesi ile eksi/artı değiştirme
- Toplamlar üstte; en düşük puan (önde olan) vurgulanır
- Bir ele dokunarak düzeltme / silme
- Skorlar telefonda saklanır, uygulama kapansa da kaybolmaz
- "Yeni oyun": aynı oyuncularla ya da yeni oyuncularla

## APK nasıl alınır?

### Yol 1: Android Studio (bilgisayarda)
1. Bu klasörü Android Studio ile aç (File → Open).
2. Gradle senkronizasyonu bitince telefonu USB ile bağla (Geliştirici seçenekleri → USB hata ayıklama açık) ve ▶ Run'a bas.
3. Ya da **Build → Build App Bundle(s) / APK(s) → Build APK(s)** ile APK üret:
   `app/build/outputs/apk/...`

### Yol 2: GitHub (Android Studio kurmadan)
1. GitHub'da yeni bir depo aç ve bu klasördeki her şeyi (gizli `.github` klasörü dahil) yükle.
2. Depoda **Actions** sekmesine gir; "APK Derle" iş akışı otomatik çalışır (birkaç dakika).
3. Bitince çalışmanın sayfasında **Artifacts → OkeySkor-apk** dosyasını indir, içindeki APK'yı telefona at ve kur
   (telefon "bilinmeyen kaynaklardan yükleme" izni isteyebilir).
