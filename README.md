# EcoLoop Greater Noida

> **Local Waste Recycling Locator & Reward Android App for Greater Noida, India**

EcoLoop Greater Noida is a focused Android prototype designed to prove a single green circular loop:
`User finds a drop-off point` ➔ `Scans its QR` ➔ `Collector confirms quantity` ➔ `User earns points` ➔ `User redeems a reward`.

---

## Tech Stack
- **Language & UI**: Kotlin, Jetpack Compose (Material 3), Edge-to-Edge
- **Architecture**: MVVM + Clean Architecture (Data, Domain, Presentation), Unidirectional Data Flow
- **Dependency Injection**: Dagger Hilt
- **Asynchronous**: Kotlin Coroutines & StateFlow / CallbackFlow
- **Backend & DB**: Firebase Auth (Email/Password) & Cloud Firestore (No Cloud Functions)
- **Maps & Location**: Google Maps Compose & Fused Location Provider Client
- **Scanning**: CameraX & ML Kit Barcode Scanning
- **Minimum SDK**: API 24 (Android 7.0 Nougat) | **Target SDK**: API 34

---

## Project Structure

```text
EcoLoopGreaterNoida/
├── app/
│   ├── google-services.json                # Firebase configuration placeholder
│   ├── build.gradle.kts                    # App-level build script with Maps key injection
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml         # Permissions & Maps meta-data
│       │   └── java/com/ecoloop/greaternoida/
│       │       ├── EcoLoopApplication.kt   # @HiltAndroidApp application entry
│       │       ├── core/
│       │       │   ├── constants/
│       │       │   │   └── EcoConstants.kt # Point rates, hazard weights, collections
│       │       │   └── util/
│       │       │       ├── Resource.kt     # Sealed class for UI state (Success, Error, Loading)
│       │       │       └── LocationHelper.kt # Haversine distance calculator
│       │       ├── data/
│       │       │   ├── datasource/
│       │       │   │   └── SeedPartnersData.kt # Embedded Greater Noida partner data
│       │       │   ├── model/
│       │       │   │   └── PartnerDto.kt   # Firestore DTO with field mapping
│       │       │   └── repository/
│       │       │       └── PartnerRepositoryImpl.kt # Firestore listener + offline fallback
│       │       ├── domain/
│       │       │   ├── model/
│       │       │   │   ├── Partner.kt      # Domain partner entity
│       │       │   │   ├── WasteCategory.kt
│       │       │   │   └── WasteType.kt    # E_WASTE, PLASTIC, PAPER
│       │       │   ├── repository/
│       │       │   │   └── PartnerRepository.kt
│       │       │   └── usecase/
│       │       │       ├── CalculatePointsUseCase.kt # Points formula + hazard weights
│       │       │       ├── FilterPartnersUseCase.kt  # Compound filter & distance sorting
│       │       │       └── GetPartnersUseCase.kt
│       │       ├── di/
│       │       │   ├── AppModule.kt        # Provides Firestore, Repository, UseCases
│       │       │   └── LocationModule.kt   # Provides FusedLocationProviderClient
│       │       └── ui/
│       │           ├── MainActivity.kt     # @AndroidEntryPoint single activity
│       │           ├── theme/              # Material 3 Eco Theme (Color, Type, Theme)
│       │           └── locator/
│       │               ├── LocatorScreen.kt # Responsive Map & List view with search
│       │               ├── LocatorViewModel.kt
│       │               ├── LocatorUiState.kt
│       │               └── components/
│       │                   ├── GreaterNoidaMap.kt    # Maps Compose with custom markers
│       │                   ├── PartnerCard.kt        # Card with badges & distance
│       │                   ├── PartnerDetailSheet.kt # Modal detail sheet
│       │                   └── WasteFilterChips.kt   # Material 3 filter chips with counters
│       └── test/java/com/ecoloop/greaternoida/
│           └── domain/usecase/
│               ├── CalculatePointsUseCaseTest.kt # Comprehensive points formula unit tests
│               └── FilterPartnersUseCaseTest.kt  # Filter & compound query unit tests
├── seed/
│   ├── seed_partners.json                  # 15 realistic partners across Greater Noida
│   ├── seed_categories.json                # Rates & hazard weights (e-waste 2.5x, plastic 1.2x, paper 1.0x)
│   └── seed_firestore.py                   # Automated Firebase Admin seeder script
├── firestore.rules                         # Security rules enforcing ownership & role separation
├── build.gradle.kts                        # Root build configuration
├── settings.gradle.kts
└── local.properties                        # Local SDK path & MAPS_API_KEY (gitignored)
```

---

## Setup & Configuration

### 1. Google Maps API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com/) and create or select a project.
2. Enable **Maps SDK for Android**.
3. Create an API Key in **Credentials**.
4. Open (or create) `local.properties` in the project root:
   ```properties
   sdk.dir=C\:\\Users\\<your_user>\\AppData\\Local\\Android\\Sdk
   MAPS_API_KEY=AIzaSyYourActualGoogleMapsApiKeyHere
   ```
   > The Gradle build script reads `MAPS_API_KEY` from `local.properties` and injects it into `AndroidManifest.xml` via `manifestPlaceholders`. No API keys are committed to Git.

### 2. Firebase Configuration
1. Go to [Firebase Console](https://console.firebase.google.com/) and create a project: `ecoloop-greaternoida`.
2. Add an Android app with Package Name: `com.ecoloop.greaternoida`.
3. Download `google-services.json` and replace the placeholder at `app/google-services.json`.
4. In Firebase Console:
   - Go to **Build** ➔ **Authentication** ➔ Enable **Email/Password**.
   - Go to **Build** ➔ **Firestore Database** ➔ Create Database in test mode or deploy `firestore.rules`.

### 3. Deploy Firestore Security Rules
In Firebase Console ➔ Firestore Database ➔ Rules tab, copy & paste the contents of [`firestore.rules`](file:///d:/COLLEGE/Major%20p/firestore.rules).
- Users can read/write only their own user profile.
- Partners and Waste Categories are publicly readable.
- Only collectors linked to a partner can confirm pending transactions for that partner.

### 4. Running the Firestore Seed Script
We provide 15 realistic recycling drop-off points across Alpha 1, Alpha 2, Beta 1, Beta 2, Gamma 1, Gamma 2, Pari Chowk, and Surajpur.

To seed them directly to your Cloud Firestore:
```bash
cd seed
pip install firebase-admin
```
1. Download a service account key from **Firebase Console** ➔ **Project settings** ➔ **Service accounts** ➔ **Generate new private key**.
2. Save it as `serviceAccountKey.json` inside the `seed/` directory.
3. Run the script:
   ```bash
   python seed_firestore.py
   ```
> **Note for Instant Testing**: The Android app contains an automatic fallback to the exact same 15 seed partners (`SeedPartnersData.kt`). Even before you run the python seed script or if running offline, the app displays all 15 Greater Noida locations on the map and list without crashing!

---

## Formula for Points Calculation

$$\text{Points} = \text{round}(\text{quantity\_kg} \times \text{points\_per\_kg} \times \text{hazard\_weight})$$

Defined in `EcoConstants.kt`:
- **E-Waste**: 20 points/kg $\times$ 2.5 hazard weight = **50 points/kg** (High environmental hazard)
- **Plastic**: 10 points/kg $\times$ 1.2 hazard weight = **12 points/kg**
- **Paper**: 5 points/kg $\times$ 1.0 hazard weight = **5 points/kg**

Unit tests covering edge cases, rounding, fractional kilograms, and hazard weight scaling can be run via:
```bash
./gradlew testDebugUnitTest
```

---

## Complete Core Recycling Loop (Stages 1 – 6)

EcoLoop proves the full circular economy loop in 6 stages:

1. **Stage 1 (Locator)**: Citizen locates 15 recycling stations across Alpha, Beta, Gamma, Pari Chowk, and Surajpur on an interactive Map (with offline visual fallback) and filtered list.
2. **Stage 2 (Auth & Profiles)**: Email/Password login, sector assignment, and role-based home routing (Citizen vs Collector).
3. **Stage 3 (Drop-off & Weight Confirmation)**: Citizen initiates a pending drop-off transaction with waste category. The station collector weighs the material, inputs kg, and confirms. Points are calculated using hazard multipliers and added to the user's profile with double-credit protection.
4. **Stage 4 (QR Code Verification)**: CameraX + ML Kit scanner replaces manual partner selection. Pointing the camera at the station's QR code (e.g. `ECO_PARI_001`) immediately starts the drop-off. Printable/displayable station QR poster screens are provided for testing.
5. **Stage 5 (Wallet & Rewards Catalog)**: Points wallet displays available points and total kg recycled impact card. Citizen browses local Greater Noida rewards (Galgotias Canteen, Sharda Cafe, Ansal Plaza Supermart, Pari Chowk Books, Beta-1 Organic Grocer). Redeeming executes an atomic transaction (guaranteeing no negative balance) and reveals an active redemption coupon code (e.g. `ECO-GNOI-7X9K`).
6. **Stage 6 (AI Waste Classification & Nearest Centre Recommendation)**: CameraX photo capture with on-device LiteRT / TensorFlow Lite MobileNet classification. Identifies Plastic, Paper/Cardboard, Metal, Glass, and E-Waste without cloud APIs. Displays confidence score, allows manual correction, and ranks the nearest 5 recycling centres that accept the identified waste stream using GPS distance (or locality fallback). Tapping a recommended centre directly bridges to the existing drop-off workflow.

---

## Stage 6 — AI Waste Classification, Crop Focus & Nearest Centre Recommendation

### Model Specifications & Diagnosis
- **Model File**: `mobilenet_v1_1.0_224_quant.tflite` (4.2 MB, bundled in `app/src/main/assets/`)
- **Labels File**: `labels_mobilenet_quant_v1_224.txt` (1001 categories, bundled in `app/src/main/assets/`)
- **Source**: Official Google TensorFlow MobileNet Release
- **Licence**: **Apache License 2.0**
- **Runtime Engine**: Google LiteRT / TensorFlow Lite 2.14.0 with XNNPACK CPU acceleration
- **Privacy & Safety**: Operates **100% on-device**; photographs are processed in volatile RAM and **never uploaded or permanently stored**. AI strictly suggests material classification and nearest centres; actual weight and points calculation are solely confirmed by collectors during physical drop-off.

### Diagnosis of Low-Confidence & Cluttered Predictions
When photographing a scene containing multiple objects (e.g., a computer mouse, coiled cables, and a laptop):
1. **Single-Object ImageNet Architecture**: The model is an ImageNet-1k object classifier trained on single centered objects. When an image contains multiple distinct items spread across the frame, the softmax probability mass is fragmented across multiple classes (e.g., ~19% joystick, ~16% mouse, ~14% laptop).
2. **Aspect Ratio Preservation**: Direct resizing of wide camera frames squashes objects into a 224x224 square, distorting shapes. The engine now performs an aspect-ratio-preserving center-crop before scaling.
3. **Clutter & Multi-Object Detection**: Rather than relying only on the top-1 argmax, the classifier evaluates the top 5 candidates. When confidence is below threshold or candidates conflict, the system flags **"Scene Clutter / Multiple Objects Detected"** and explains that the classification is uncertain.
4. **Crop & Focus Tool**: Users can tap **"Crop & Focus"** to zoom and isolate a single waste item (e.g. framing just the mouse or laptop), removing cables and desk clutter. This restores high single-object confidence.
5. **Configurable Confidence Threshold**: Users can adjust the sensitivity (40% Relaxed, 50% Standard, 65% Strict). Any prediction below the threshold requires explicit manual category confirmation.

### Model Extensibility / Setup Path for Custom Models
To replace the bundled MobileNet model with a custom fine-tuned TFLite waste model (e.g., trained on TrashNet or a custom Greater Noida dataset):
1. Convert your model to TensorFlow Lite format (`waste_classifier.tflite`, input `[1, 224, 224, 3]`).
2. Export your label file (`waste_labels.txt`).
3. Place both files in `app/src/main/assets/`.
4. Update `MODEL_FILENAME` and `LABELS_FILENAME` in `TfLiteWasteClassifier.kt`.

### Supported Waste Streams
1. **Plastic**: Water bottles, pop bottles, shampoo containers, plastic bags, polythene packaging.
2. **Paper & Cardboard**: Cardboard boxes, cartons, notebooks, newspapers, office paper.
3. **Metal**: Tin cans, beverage pop cans, steel scrap, aluminum foil, kitchen pots.
4. **Glass**: Wine bottles, beer bottles, glass jars, drinking glasses, cullet.
5. **E-Waste**: Mobile phones, computers, laptops, mice, keyboards, monitors, chargers, batteries, electronics.

---

## How to Test Stage 6 (AI Scanner & Nearest Centre Recommendation)

1. **Open AI Scanner**:
   - Sign in as a citizen user.
   - On the Home / Locator tab, tap the **"AI Waste Scanner"** banner card at the top (or tap the camera icon in the top bar).
2. **Photograph Waste**:
   - Point the camera at any recyclable item (e.g., a plastic bottle, a can, paper packaging, or computer electronics).
   - Tap the white circular shutter button to capture a photo.
   - *(Optional Simulation Helper)*: If testing without physical objects or on an emulator, tap the **?** button at the bottom-right and select a test item.
3. **Test Crop & Focus**:
   - If taking a photo of a cluttered desk with multiple objects (mouse, cables, laptop), tap **"Crop & Focus"**.
   - Use the quick presets (e.g. *Focus Center 50%*, *Center 75%*, *Top Focus*) or zoom slider to isolate a single item.
   - Tap **"Apply Crop"** to re-run inference on the isolated item.
4. **Verify AI Prediction & Confidence**:
   - The on-device LiteRT model evaluates the photo in milliseconds.
   - The card shows the category badge (e.g. `E-Waste`), confidence score (e.g. `85%`), top raw model label, and candidate breakdown.
   - If confidence is $\ge$ threshold, the category is verified.
   - If confidence is $<$ threshold, an amber warning card explains that scene clutter or low confidence was detected, and prompts for manual selection or crop.
   - You can tap **"Change Category"** or any of the 5 material chips at any time to manually override.
5. **View Nearest Centres Recommendation**:
   - The screen queries Greater Noida partner stations accepting that material.
   - Using GPS location (or the fallback locality selector like *Pari Chowk*), the app lists the **nearest 5 suitable centres** sorted by distance (e.g., `0.4 km away`, `1.2 km away`).
   - Ineligible centres are strictly omitted.
6. **Continue to Drop-off Workflow**:
   - On your chosen recommended centre, tap **"Proceed to Drop-Off at this Station"**.
   - The app seamlessly opens the existing Stage 3/4 drop-off initiation dialog with the partner and waste category pre-selected.
   - Submit drop-off $\rightarrow$ collector confirms actual kg $\rightarrow$ points awarded $\rightarrow$ redeem rewards!

---

## Testing Stage 1 (Locator)

1. **Launch App**: Open the project in Android Studio or run `./gradlew installDebug`.
2. **Grant Location Permission**: On first launch, the app requests location permission to show your proximity and compute real-time distance (`km`) to each partner.
3. **Map View**:
   - The map centers on Greater Noida (Pari Chowk / Knowledge Park).
   - Markers represent drop-off points across Alpha, Beta, Gamma, Pari Chowk, and Surajpur.
   - Tap any pin: the camera focuses on that partner and reveals an preview card at the bottom.
   - Tap the MyLocation FAB to center on your current position.
4. **Filter by Waste Type**:
   - Tap **E-Waste**: Pins and list filter to only partners accepting electronic waste.
   - Tap **Plastic** or **Paper**: Pins update accordingly.
   - Tap **All** or active chip to reset filter.
5. **Search**:
   - Type `"Alpha"` or `"Pari Chowk"` in the search bar: instant filtering of drop-off points by sector and address.
6. **List View**:
   - Tap the view toggle icon in the top right to switch to **List View**.
   - Browse cards displaying Partner Name, Sector Address, Verified status, Accepted Waste chips, and Distance in km.
   - Tap any card to open the **Partner Detail Sheet** displaying QR Station ID and accepted materials.
7. **Empty & Error States**:
   - Search for a non-existent name (e.g., `"Sector 999"`) to test the empty state with the **Reset Filters** button.
