# UI/UX Guidelines: Project "Antigravity" 🚀

## 1. Core Philosophy: The "Focus-First" Aesthetic
This project utilizes a customized **Material 3 Expressive** design system. The overarching goal is a "weightless" user interface that does not distract from the primary utility: playing music and collecting high-quality machine learning data.

**Key Tenets:**
* **Adaptive by Default:** The UI must scale flawlessly between compact mobile screens and expanded landscape/desktop environments.
* **Zero Distraction:** Animations exist only to confirm user actions. No elastic, bouncy, or excessive motion.
* **Data-Driven:** UI slots on the Home Dashboard must be designed as containers for future PyTorch ML injection (e.g., "Recommended" cards).

## 2. Hardware Targets & Layout Adapters
All UI components must be tested and verified against these two primary form factors using Jetpack Compose `WindowSizeClass`.

* **Compact (Target: Samsung Galaxy M14 5G):**
    * Navigation: **Bottom Navigation Bar** (Max 5 items).
    * Layout: Single-column scrolling, vertical stacking.
    * Performance: Blurs and heavy render effects (`Modifier.blur`) must be used sparingly to maintain 60fps on mid-range Exynos processors.
* **Expanded / Landscape (Target: Asus TUF Gaming A15 Emulator):**
    * Navigation: **Navigation Rail** (Left-aligned).
    * Layout: Multi-pane (List-Detail) or expanded grids.

*Note: Navigation switching is handled globally via `NavigationSuiteScaffold`.*

## 3. Shape & Containment: The "Pill" System
To achieve the "Antigravity" floating feel, strict adherence to high-radius corners is required. Sharp corners are forbidden.

* **Interactive Elements (Chips, Buttons, Active Nav Items):** `CircleShape` (Fully rounded / 50% radius).
* **Discovery Cards (Playlists, ML Recommendations):** `RoundedCornerShape(24.dp)` or `32.dp` for a soft, expressive container.
* **List Items (Songs, Albums):** Standard M3 list padding, but active selection states must use a Pill-shaped background highlight.

## 4. Color Strategy: Dynamic Material You
The app avoids hard-coded static color palettes to ensure it feels native to the user's OS environment.

* **Primary Engine:** Use `dynamicLightColorScheme` and `dynamicDarkColorScheme`.
* **Tonal Palettes:** Rely heavily on `SecondaryContainer` and `SurfaceVariant` for grouping items (like the Library Hub or Playlist cards) to create depth without relying on heavy drop shadows.
* **Contrast Requirement:** Ensure text overlaid on Album Art or generated ML Cards maintains a high contrast ratio. Use gradient overlays (scrims) if necessary.

## 5. Motion & Transitions
Transitions should feel incredibly fast and responsive, favoring utility over flash.

* **Duration:** Standard functional transitions (200ms - 300ms).
* **Easing:** Standard Material 3 easing curves (e.g., `FastOutSlowInEasing`).
* **Screen Transitions:** Use `FadeThrough` or simple `SharedAxis` transitions. Avoid complex shared-element transitions that require heavy calculation on the UI thread, as the CPU should be reserved for background ML data processing.

## 6. Standardized Empty States
Never present a blank screen. If a database query (e.g., Most Played, Favorites) returns an empty list, the UI must render an `EmptyState` component.

**Empty State Requirements:**
* Centered layout (`Arrangement.Center`, `Alignment.CenterHorizontally`).
* A subdued Material 3 Icon (`size = 64.dp`, `alpha = 0.6f`).
* A brief, non-prescriptive headline (e.g., "No Favorites Yet").
* A subtle, pill-shaped call-to-action button if applicable (e.g., "Scan Local Storage").


***This is a windows system ***