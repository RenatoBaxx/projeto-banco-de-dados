import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Backend Spring (bootRun). ECONNREFUSED = nada escutando neste host:porta.
const apiTarget = process.env.VITE_PROXY_TARGET ?? 'http://127.0.0.1:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/mongo': { target: apiTarget, changeOrigin: true },
      '/uploads': { target: apiTarget, changeOrigin: true },
      '/auth': { target: apiTarget, changeOrigin: true },
      '/catalog': { target: apiTarget, changeOrigin: true },
      '/stats': { target: apiTarget, changeOrigin: true }
    }
  }
})
