const files = import.meta.glob('./*.js', { eager: true });
const api = {};

Object.entries(files).forEach(([key, module]) => {
  if (key === './index.js') return;
  const name = key.replace('./', '').replace('.js', '');
  api[name] = module.default;
});

export default api;
