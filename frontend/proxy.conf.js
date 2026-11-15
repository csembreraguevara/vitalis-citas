/**
 * Proxy de desarrollo (S14: "usar un proxy hacia /api en desarrollo").
 * El navegador solo habla con el servidor de Angular (puerto 4200) y este
 * reenvía /api al backend Spring Boot (puerto 8080). Así basta compartir
 * un solo puerto (por ejemplo con el reenvío de puertos de VS Code).
 */
module.exports = {
  '/api': {
    target: 'http://localhost:8080',
    secure: false,
    changeOrigin: true,
    // Para el backend la petición viene del mismo servidor: se quita el
    // encabezado Origin del navegador y no aplica la regla CORS.
    configure: (proxy) => {
      proxy.on('proxyReq', (proxyReq) => proxyReq.removeHeader('origin'));
    },
  },
};
