# 🌙 SleepWell - Application Android de Suivi du Sommeil

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Material Design 3](https://img.shields.io/badge/Design-Material%20Design%203-blue.svg)](https://m3.material.io)
[![MinSDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)](https://developer.android.com/studio/releases/platforms#7.0)
[![TargetSDK](https://img.shields.io/badge/Target%20SDK-34-orange.svg)](https://developer.android.com/about/versions/14)

Application complète de suivi du sommeil avec interface moderne, graphiques animés, conseils IA personnalisés et support multi-langues (Français, Anglais, Arabe).

## 📱 Fonctionnalités

### ✨ Principales
- **Suivi des sessions de sommeil** : Durée, qualité (0-100%), phases (profond, léger, REM)
- **Graphiques hebdomadaires** : Visualisation avec MPAndroidChart
- **Objectifs personnalisés** : Définir des cibles et suivre les séries (streaks)
- **Conseils intelligents** : Recommandations IA via OpenAI API
- **Statistiques détaillées** : Moyennes, meilleure/pire nuit, total heures
- **Export de données** : Export CSV des sessions de sommeil
- **Rappels personnalisés** : Notifications de coucher/réveil avec WorkManager
- **Mode sombre** : Support complet du thème sombre
- **Multi-langues** : Français (défaut), Anglais, Arabe avec RTL

### 🎨 Interface
- **Material Design 3** avec glassmorphism
- **Animations fluides** : Transitions, fade, slide
- **Bottom Navigation** : 4 onglets (Dashboard, Objectifs, Conseils, Profil)
- **Onboarding** : 3 pages avec ViewPager2
- **Authentification** : Login/Register avec validation

## 🏗️ Architecture

### Stack Technique
```
- Language: Kotlin
- UI: XML Layouts (Material Design 3)
- Architecture: MVVM + Repository Pattern
- Database: Room (SQLite)
- API: Retrofit + OkHttp
- Charts: MPAndroidChart
- Async: Coroutines + LiveData
- DI: Manual (Repository Pattern)
- Notifications: WorkManager
```

### Structure du Projet
```
app/src/main/
├── java/com/sleepwell/
│   ├── SleepWellApplication.kt
│   ├── data/
│   │   ├── local/
│   │   │   ├── SleepDatabase.kt
│   │   │   ├── Converters.kt
│   │   │   └── dao/
│   │   │       ├── UserDao.kt
│   │   │       ├── SleepSessionDao.kt
│   │   │       └── GoalDao.kt
│   │   ├── model/
│   │   │   ├── User.kt
│   │   │   ├── SleepSession.kt
│   │   │   ├── Goal.kt
│   │   │   └── Tip.kt
│   │   ├── remote/
│   │   │   ├── OpenAIService.kt
│   │   │   ├── RetrofitClient.kt
│   │   │   └── dto/
│   │   └── repository/
│   │       ├── SleepRepository.kt
│   │       └── AIRepository.kt
│   ├── ui/
│   │   ├── splash/SplashActivity.kt
│   │   ├── onboarding/OnboardingActivity.kt
│   │   ├── auth/
│   │   │   ├── AuthActivity.kt
│   │   │   ├── LoginFragment.kt
│   │   │   └── RegisterFragment.kt
│   │   └── main/
│   │       ├── MainActivity.kt
│   │       ├── dashboard/DashboardFragment.kt
│   │       ├── goals/GoalsFragment.kt
│   │       ├── tips/TipsFragment.kt
│   │       └── profile/ProfileFragment.kt
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt
│   │   ├── DashboardViewModel.kt
│   │   ├── GoalsViewModel.kt
│   │   └── ProfileViewModel.kt
│   ├── workers/
│   │   ├── SleepReminderWorker.kt
│   │   └── WakeupReminderWorker.kt
│   └── utils/
│       ├── Constants.kt
│       ├── Extensions.kt
│       └── DateUtils.kt
└── res/
    ├── layout/          # 18 layouts XML
    ├── values/          # Strings (FR, EN, AR), Colors, Themes
    ├── drawable/        # 30+ vector icons
    ├── anim/            # Animations
    └── menu/            # Bottom navigation menu
```

## 🚀 Installation et Configuration

### Prérequis
- **Android Studio** : Electric Eel (2022.1.1) ou plus récent
- **JDK** : 17 ou supérieur
- **SDK Android** : Min 24, Target 34
- **Gradle** : 8.2.2 (wrapper inclus)
- **Clé OpenAI API** : Pour les conseils IA (optionnel)

### Étape 1 : Cloner le Projet

```bash
git clone https://github.com/OsebrFx/SleepWellAppV1.git
cd SleepWellAppV1
git checkout claude/sleepwell-android-app-01MRbcX4YSSPUzo7E7hzJKiQ
```

### Étape 2 : Configuration OpenAI API (Optionnel)

**Pour activer les conseils IA**, ajoutez votre clé API OpenAI :

1. Ouvrez `app/build.gradle`
2. Trouvez la ligne (~ligne 16) :
   ```kotlin
   buildConfigField "String", "OPENAI_API_KEY", "\"YOUR_OPENAI_API_KEY_HERE\""
   ```
3. Remplacez `YOUR_OPENAI_API_KEY_HERE` par votre clé :
   ```kotlin
   buildConfigField "String", "OPENAI_API_KEY", "\"sk-proj-VOTRE_CLE_ICI\""
   ```

> **Note** : Sans clé API, l'app fonctionne normalement mais les conseils IA ne seront pas disponibles.

### Étape 3 : Synchroniser le Projet

1. Ouvrez le projet dans Android Studio
2. Attendez que Gradle se synchronise automatiquement
3. Si nécessaire, cliquez sur **File > Sync Project with Gradle Files**

### Étape 4 : Build et Lancement

#### Via Android Studio
1. Connectez un appareil Android ou lancez un émulateur
2. Cliquez sur **Run 'app'** (▶️) ou `Shift + F10`

#### Via Ligne de Commande
```bash
# Debug build
./gradlew assembleDebug

# Release build (avec ProGuard)
./gradlew assembleRelease

# Installer sur l'appareil
./gradlew installDebug
```

## 📦 Dépendances Principales

```gradle
// AndroidX Core
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0

// Lifecycle & ViewModel
androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0
androidx.lifecycle:lifecycle-livedata-ktx:2.7.0

// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3

// WorkManager
androidx.work:work-runtime-ktx:2.9.0

// Retrofit & OkHttp
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0

// MPAndroidChart
com.github.PhilJay:MPAndroidChart:v3.1.0

// ViewPager2
androidx.viewpager2:viewpager2:1.0.0
```

## 🎯 Utilisation

### Première Utilisation

1. **Onboarding** : Découvrez les fonctionnalités (3 pages)
2. **Inscription** : Créez un compte (nom, email, mot de passe, âge ≥18)
3. **Dashboard** : Ajoutez votre première session de sommeil

### Ajouter une Session de Sommeil

1. Cliquez sur le bouton **+** (FAB) dans le Dashboard
2. Sélectionnez **Heure de début** et **Heure de fin**
3. Indiquez la **Qualité** (0-100%)
4. Ajoutez des **Notes** (optionnel)
5. Cliquez sur **Enregistrer**

### Créer un Objectif

1. Allez dans l'onglet **Objectifs**
2. Cliquez sur **Créer un objectif**
3. Définissez :
   - **Durée cible** : 4-12 heures (slider)
   - **Qualité cible** : 50-100% (slider)
4. Cliquez sur **Enregistrer**

### Exporter vos Données

1. Allez dans l'onglet **Profil**
2. Cliquez sur **Exporter les données**
3. Choisissez l'app de partage (Email, Drive, etc.)
4. Le fichier CSV contient toutes vos sessions

## 🌍 Langues Supportées

### Changement de Langue

L'app détecte automatiquement la langue du système. Pour changer :
1. **Paramètres Android** > **Système** > **Langues**
2. Ajoutez ou changez la langue
3. Redémarrez l'app

### Langues Disponibles
- 🇫🇷 **Français** (défaut)
- 🇬🇧 **Anglais**
- 🇸🇦 **Arabe** (avec support RTL)

## 🔧 Configuration Avancée

### ProGuard (Release Build)

Le fichier `proguard-rules.pro` est déjà configuré pour :
- Garder les classes de données (Room, Retrofit)
- Optimiser le code
- Obfusquer les noms de classes

### Notifications

Les notifications nécessitent la permission `POST_NOTIFICATIONS` (API 33+).
L'app demande automatiquement la permission au premier lancement.

### WorkManager

Les rappels utilisent WorkManager pour :
- Rappel de coucher (configurable dans Profil)
- Rappel de réveil (configurable dans Profil)

## 🐛 Résolution de Problèmes

### Build Failures

**Erreur : "Theme not found"**
```bash
# Solution : Clean and rebuild
./gradlew clean build
```

**Erreur : "Adaptive icon requires SDK 26"**
```bash
# Déjà corrigé dans la dernière version
# Si le problème persiste, vérifiez que mipmap-anydpi-v26 existe
```

**Erreur : "Duplicate class found"**
```bash
# Solution : Invalider le cache
# Android Studio > File > Invalidate Caches > Invalidate and Restart
```

### Runtime Issues

**Crash au lancement**
- Vérifiez que minSdk de votre appareil est ≥ 24
- Vérifiez les logs : `adb logcat | grep SleepWell`

**Conseils IA ne fonctionnent pas**
- Vérifiez que la clé OpenAI est configurée dans `build.gradle`
- Vérifiez la connexion Internet
- Vérifiez les quotas de votre compte OpenAI

## 📊 Base de Données (Room)

### Tables

**users**
- id, name, email, password, age
- darkModeEnabled, language
- sleepReminderEnabled, sleepReminderHour, sleepReminderMinute
- wakeupReminderEnabled, wakeupReminderHour, wakeupReminderMinute

**sleep_sessions**
- id, userId (FK)
- startTime, endTime, durationHours
- quality, deepSleepPercentage, lightSleepPercentage, remSleepPercentage
- notes, mood

**goals**
- id, userId (FK)
- targetHours, targetQuality
- streak, bestStreak, isActive

### Migration

Pour réinitialiser la base de données :
```bash
adb shell pm clear com.sleepwell
```

## 🎨 Palette de Couleurs

```kotlin
// Light Theme
Primary:     #667EEA
Secondary:   #7ED4C1
Background:  #F5F9FC
Surface:     #FFFFFF

// Dark Theme
Primary:     #4A7BA7
Background:  #0F1419
Surface:     #1C2128

// Categories
Sleep Hygiene:  #8B5CF6
Lifestyle:      #EC4899
Diet:           #F59E0B
Exercise:       #10B981
Environment:    #3B82F6
Relaxation:     #667EEA
```

## 📝 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 👥 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. Forkez le projet
2. Créez une branche feature (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add AmazingFeature'`)
4. Pushez vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📧 Contact

Pour toute question ou suggestion :
- **GitHub Issues** : [Créer une issue](https://github.com/OsebrFx/SleepWellAppV1/issues)
- **Email** : [Votre email]

## 🙏 Remerciements

- [Material Design 3](https://m3.material.io) - Design system
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - Charts library
- [OpenAI API](https://openai.com) - AI recommendations
- [Room Persistence Library](https://developer.android.com/training/data-storage/room) - Database
- [Retrofit](https://square.github.io/retrofit/) - HTTP client

---

**Version** : 1.0.0
**Dernière mise à jour** : Novembre 2024
**Statut** : ✅ Production Ready
