const { app, BrowserWindow, shell, ipcMain, Menu } = require('electron');
const path = require('path');

// Live application URL
const APP_URL = process.env.LEETALK_URL || 'https://ais-pre-ufle4jgtwld3fwpqtk2vwz-503195482414.europe-west2.run.app';

let mainWindow = null;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 420,
    minHeight: 600,
    title: 'LeeTalk - Talk. Share. Connect.',
    backgroundColor: '#0F172A',
    icon: path.join(__dirname, 'icon.png'),
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: true
    }
  });

  // Load the web app
  mainWindow.loadURL(APP_URL).catch(() => {
    // If offline or network error, load local fallback
    mainWindow.loadFile(path.join(__dirname, 'index.html'));
  });

  // Handle open external links in browser
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    if (url.startsWith('http:') || url.startsWith('https:')) {
      shell.openExternal(url);
    }
    return { action: 'deny' };
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

// App lifecycle
app.whenReady().then(() => {
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});
