# Kalculator - Android Kotlin Arithmetic Calculator

A modern, sleek, and intuitive arithmetic calculator built with **Kotlin** and **Material 3 Design**, designed to run and simulate smoothly in **Android Studio**.

---

## ✨ Features

- **Standard Arithmetic Operations**: Addition (`+`), Subtraction (`−`), Multiplication (`×`), Division (`÷`).
- **Mathematical Accuracy (PEMDAS / BODMAS)**: Strict operator precedence handling via Dijkstra's Shunting-yard algorithm (`×` and `÷` computed before `+` and `−`).
- **Parentheses Support**: Full grouping with smart parentheses matching `()`.
- **Percentage (`%`)**: Convenient percentage calculations (e.g., `50%` → `0.5`).
- **Real-Time Live Preview**: Instantly shows the computed preview result as you type before hitting `=`.
- **Smart Operator Handling**: Typing an operator after another replaces it seamlessly (e.g. typing `+` then `×` changes to `×`).
- **Decimal Safety**: Accurately formats floating points without IEEE 754 precision glitches (`0.1 + 0.2 = 0.3`).
- **Division by Zero Protection**: Safely catches zero divisors with user-friendly `"Cannot divide by 0"` error alerts.
- **Backspace & Clear**: Tap `⌫` to erase character by character; long-press `⌫` or tap `AC` to clear everything.
- **Haptic Tactile Feedback**: Subtle tactile haptic vibration when tapping buttons.
- **Adaptive Day/Night Theme**: Automatically adapts between light and dark themes based on your system mode.
- **Screen Rotation State Persistence**: Remembers your current equation and result when rotating the device.

---

## 🚀 How to Open and Simulate in Android Studio

Follow these quick steps to run the app in the Android Studio Emulator:

### 1. Open Project
1. Launch **Android Studio**.
2. Click **File** > **Open...** (or select **Open** on the Welcome screen).
3. Navigate to and select the project folder:
   ```
   e:\Kalculator
   ```
4. Click **OK**.

### 2. Gradle Sync
- Android Studio will automatically recognize the project and start Gradle sync.
- Wait for Gradle sync to complete (it will download dependencies and configure the project).
- If prompted to update Android Gradle Plugin or Gradle wrapper, you can click **Don't ask again** or proceed.

### 3. Create or Select an Emulator
1. Open the **Device Manager** in Android Studio:
   - Click the phone icon in the top-right toolbar, or go to **Tools** > **Device Manager**.
2. If you already have a virtual device (e.g., *Pixel 8 - API 34*), select it.
3. If you don't have one:
   - Click **Create Device**.
   - Select **Phone** > **Pixel 8** (or any modern phone).
   - Select a system image (e.g., **VanillaIceCream (API 35)** or **UpsideDownCake (API 34)**).
   - Click **Next** > **Finish**.

### 4. Run / Simulate the App
1. In the top toolbar, select `app` in the run configuration dropdown.
2. Select your virtual device from the device dropdown.
3. Click the green **Run (▶)** button (or press `Shift + F10`).
4. The Android Emulator will boot up and the **Kalculator** app will launch!

---

## 📁 Project Structure

```text
Kalculator/
├── app/
│   ├── build.gradle.kts           # App module configuration & dependencies
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/kalculator/
│   │   │   │   ├── MainActivity.kt         # UI controller, button bindings, haptics
│   │   │   │   └── CalculatorEngine.kt     # Math parser (Shunting-Yard & RPN evaluation)
│   │   │   └── res/
│   │   │       ├── layout/activity_main.xml # Material 3 responsive layout
│   │   │       ├── values/                  # Strings, colors, light theme
│   │   │       └── values-night/            # Dark mode theme
│   │   └── test/java/com/example/kalculator/
│   │       └── CalculatorEngineTest.kt     # Unit tests verifying all math rules
├── gradle/
│   ├── libs.versions.toml         # Version catalog
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts               # Root build script
├── settings.gradle.kts            # Module repository settings
└── README.md
```

---

## 🧪 Running Unit Tests

To verify arithmetic correctness and test suite:
1. In Android Studio, navigate in the Project view to `app/src/test/java/com/example/kalculator/CalculatorEngineTest.kt`.
2. Right-click the file and click **Run 'CalculatorEngineTest'** (or press `Ctrl + Shift + F10`).
3. All tests (addition, subtraction, multiplication, precedence, division by zero, decimals, percentage) will execute with green checkmarks!
