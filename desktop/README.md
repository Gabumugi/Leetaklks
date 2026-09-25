# LeeTalk Desktop for Windows (.exe)

This folder contains the complete **Electron & Electron-Builder** configuration to produce a native Windows executable (`.exe`) and installer for LeeTalk.

## Generated Artifacts
When built, electron-builder generates:
1. `dist/LeeTalk-Setup-1.0.0.exe` — Full Windows NSIS Setup Installer (creates Start menu shortcut, Desktop shortcut, and uninstaller).
2. `dist/LeeTalk-1.0.0-portable.exe` — Standalone portable `.exe` that runs immediately without installation.

---

## How to Build the `.exe` on Windows

### Prerequisites
- Node.js (v18 or higher) installed on your computer.

### Step 1: Open Terminal in this folder
```bash
cd desktop
```

### Step 2: Install dependencies
```bash
npm install
```

### Step 3: Test and run locally
```bash
npm start
```

### Step 4: Build Windows Executables (`.exe`)
To build both the NSIS setup installer and the portable executable:
```bash
npm run dist
```

Or build just the NSIS installer:
```bash
npm run build:win
```

Or build just the portable `.exe`:
```bash
npm run build:portable
```

The resulting `.exe` files will be in the `desktop/dist/` directory ready for distribution and installation.
