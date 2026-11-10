<div align="center">

# 🛡️ Calls Guard

**Silence the world. Let only the people who matter ring through.**

Calls Guard rejects *every* incoming call (even from your saved contacts) and lets through only the numbers you choose. It blocks WhatsApp and other app calls too. Built to work hand in hand with the **Samsung Phone app**.

[![Download APK](https://img.shields.io/github/v/release/aboodfe002-byte/calls-guard?label=Download%20APK&style=for-the-badge&color=2A2F55)](https://github.com/aboodfe002-byte/calls-guard/releases/latest)
![Android 10+](https://img.shields.io/badge/Android-10%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![No internet](https://img.shields.io/badge/Internet-none-success?style=for-the-badge)
![License MIT](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

[English](#english) · [العربية](#العربية)

</div>

---

## English

### Why Calls Guard?

Do you have thousands of contacts but only want to hear from a handful of people? Ordinary blockers make you block numbers one by one. Calls Guard flips it around: **everyone is blocked by default**, and you pick who is allowed.

### ✨ Features

- 🚫 **Real rejection, not just mute.** Calls are actively declined (the caller gets busy or voicemail). Your phone doesn't ring, light up or vibrate.
- 👨‍👩‍👧 **Allowlist of the people who matter.** Add numbers by typing or picking from contacts. Any format works: `05…`, `+966…`, `00966…`, spaces, dashes, even Arabic digits.
- ⭐ **Favourites are allowed automatically.** Star a contact in the Contacts app and they're allowed. No need to add them twice.
- 🔢 **Allowed prefixes.** Let whole groups through: `920` (unified company and government numbers), `800` (toll-free), `01` (all landlines), whole countries such as `+971`, and 16 ready-made suggestions.
- 💬 **Blocks WhatsApp, Telegram and other app calls** from anyone not allowed.
- ☎️ **Short numbers.** Emergency and government short codes (911, 999, 1909…) can be allowed with one switch.
- 🕶️ **Hidden numbers.** Rejected by default, or allowed if you prefer.
- 📋 **Rejected-calls log** with a daily counter, showing who called, when, and from which app.
- 🔕 **Your choice of noise.** Hide the missed-call notification, and keep or remove rejected calls from the phone's call log.
- 🔋 **Practically zero battery.** Android wakes the app only when a call arrives. There are no timers and no polling.
- 💪 **Hard to kill.** A silent always-on service comes back by itself after reboot, after updates, and even if you swipe its notification away.
- 🌍 **English and Arabic**, with full right-to-left support, Material You colours and dark mode.
- 🔒 **Private by design.** The app has no internet permission, no ads and no analytics. Your lists never leave your phone.

### 📱 Made for Samsung (One UI)

Calls Guard **does not replace your dialer.** The **Samsung Phone app stays your default phone app**, with the same look, the same call screen and the same call log. Calls Guard only takes Android's *call screening* role, so it decides before the phone even rings. Rejected calls still appear in Samsung's call log, if you want them to.

### 🚀 Install

1. Download **`CallsGuard-x.y.apk`** from the [latest release](https://github.com/aboodfe002-byte/calls-guard/releases/latest) and open it.
2. Follow the 4-step setup wizard:
   - make it the **call screening app**;
   - allow **contacts** and **notifications**;
   - allow **notification access** (for WhatsApp) and the **battery exemption**;
   - add your first allowed number.
3. Done. The green card at the top shows **"Protection is on ✓"**.

> **"Restricted setting" when enabling notification access?** Android limits this for apps installed outside the Play Store. Open **App info → ⋮ → Allow restricted settings**, then enable it again.

> **Samsung tip.** For extra safety, add the app to **Device care → Battery → Background usage limits → Never sleeping apps**.

### ⚠️ Good to know

- Only **one** call-screening app can be active, so Calls Guard replaces Samsung's built-in caller-ID/spam service (Smart Call).
- Numbers blocked in the Samsung Phone app's own block list stay blocked.
- App calls (WhatsApp etc.) are declined right after their notification appears, so they may ring for a split second.
- Requires **Android 10 or newer**.

### 🛠️ Build from source

Open the project in a recent Android Studio, or run:

```bash
./gradlew assembleRelease
```

To sign it, create a `keystore.properties` in the project root with `storeFile`, `storePassword`, `keyAlias` and `keyPassword`.

---

<div dir="rtl">

## العربية

### ليش Calls Guard؟

عندك آلاف الأرقام في جهات الاتصال، وما تبي يرن جوالك إلا لكم شخص؟ تطبيقات الحظر العادية تخليك تحظر الأرقام واحد واحد. Calls Guard يقلب الفكرة: **الكل محظور تلقائيًا**، وأنت تختار مين يمر.

### ✨ المميزات

- 🚫 **رفض حقيقي مو كتم.** المكالمة تنرفض فعلًا والمتصل يسمع مشغول أو بريد صوتي، وجوالك لا يرن ولا يضيء ولا يهتز.
- 👨‍👩‍👧 **قائمة الأرقام المسموحة.** أضف الأرقام كتابةً أو من جهات الاتصال، وبأي صيغة: `05…` أو `+966…` أو `00966…`، بمسافات أو شرطات، وحتى بالأرقام العربية.
- ⭐ **المفضلة مسموحة تلقائيًا.** أي اسم تميّزه بنجمة في جهات الاتصال يمر، بدون ما تضيفه مرتين.
- 🔢 **البادئات المسموحة.** اسمح لمجموعات كاملة:
  - `920` للأرقام الموحدة للشركات والجهات الحكومية.
  - `800` للأرقام المجانية.
  - `01` لكل الأرقام الأرضية.
  - دول كاملة مثل `+971`.
  - و16 اقتراح جاهز.
- 💬 **يرفض مكالمات واتساب وتيليجرام والتطبيقات الأخرى** من غير المسموحين.
- ☎️ **الأرقام القصيرة.** أرقام الطوارئ والجهات الحكومية القصيرة (911، 999، 1909…) تقدر تسمح لها بزر واحد.
- 🕶️ **الأرقام المخفية.** مرفوضة افتراضيًا، أو مسموحة إذا تبي.
- 📋 **سجل المرفوضين.** يعرض مين اتصل ومتى ومن أي تطبيق، مع عدّاد يومي.
- 🔕 **أنت تتحكم بالإزعاج.** تقدر تخفي إشعار المكالمة الفائتة، وتختار تبقى المكالمات المرفوضة في سجل الهاتف أو لا.
- 🔋 **ما يستهلك بطارية تقريبًا.** أندرويد يشغّل التطبيق لحظة وصول المكالمة فقط، بدون مؤقتات ولا فحص دوري.
- 💪 **صعب يتقفل.** خدمة صامتة دائمة ترجع لحالها بعد إعادة التشغيل وبعد التحديث، وحتى لو مسحت إشعارها.
- 🌍 **عربي وإنجليزي**، مع دعم كامل للاتجاه من اليمين لليسار، وألوان Material You، والوضع الليلي.
- 🔒 **خصوصية تامة.** التطبيق ما عنده إذن إنترنت أصلًا، وما فيه إعلانات ولا تتبع، وقوائمك ما تطلع من جوالك أبدًا.

### 📱 متوافق مع سامسونج (One UI)

Calls Guard **ما ياخذ مكان تطبيق الاتصال.** **تطبيق الهاتف من سامسونج يبقى تطبيقك الافتراضي** بنفس الشكل ونفس شاشة المكالمة ونفس السجل. Calls Guard ياخذ بس دور «فحص المكالمات» في أندرويد، فيقرر قبل ما يرن الجوال. والمكالمات المرفوضة تظهر في سجل سامسونج إذا حبيت.

### 🚀 التثبيت

1. حمّل **`CallsGuard-x.y.apk`** من [آخر إصدار](https://github.com/aboodfe002-byte/calls-guard/releases/latest) وافتحه.
2. اتبع معالج الإعداد من 4 خطوات:
   - اجعله **تطبيق فحص المكالمات**.
   - اسمح بـ **جهات الاتصال** و**الإشعارات**.
   - اسمح بـ **الوصول للإشعارات** (عشان واتساب) و**الاستثناء من توفير البطارية**.
   - أضف أول رقم مسموح.
3. خلاص. البطاقة الخضراء فوق تكتب **«الحماية تعمل ✓»**.

> **طلعت لك رسالة «إعداد مقيد» عند تفعيل الوصول للإشعارات؟** أندرويد يقيّد هذا للتطبيقات المثبتة من خارج المتجر. افتح **معلومات التطبيق ← ⋮ ← السماح بالإعدادات المقيدة**، ثم فعّله من جديد.

> **نصيحة لسامسونج.** لحماية أكثر، أضف التطبيق إلى **العناية بالجهاز ← البطارية ← حدود استخدام الخلفية ← التطبيقات التي لا تنام أبدًا**.

### ⚠️ ملاحظات

- أندرويد يسمح بتطبيق فحص مكالمات **واحد** فقط، فـ Calls Guard ياخذ مكان خدمة سامسونج لمعرّف المتصل وكشف الإزعاج (Smart Call).
- الأرقام المحظورة في قائمة الحظر داخل تطبيق الهاتف من سامسونج تبقى محظورة.
- مكالمات التطبيقات مثل واتساب تنرفض بعد ظهور إشعارها مباشرة، فممكن ترن لجزء من الثانية.
- يحتاج **أندرويد 10 أو أحدث**.

</div>

---

<div align="center">

MIT License · Made with ❤️ for a quieter phone

</div>
