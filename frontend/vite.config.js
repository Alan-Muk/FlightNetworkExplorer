import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import globals from "globals";

const BASE = process.env.VITE_BASE_PATH ?? "/";

export default defineConfig({
  base: BASE,
  plugins: [react()],

  files: ["vite.config.js"],
  languageOptions: {
    globals: globals.node,
  },
});
