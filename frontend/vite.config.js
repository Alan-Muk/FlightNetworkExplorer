import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

const BASE = process.env.VITE_BASE_PATH ?? "/";

export default defineConfig({
  base: BASE,
  plugins: [react()],
});
