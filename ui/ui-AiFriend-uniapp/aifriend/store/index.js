import { createPinia } from 'pinia';

const files = import.meta.glob('./*.js', { eager: true });
const modules = {};

Object.entries(files).forEach(([key, module]) => {
  if (key === './index.js') return;
  const name = key.replace('./', '').replace('.js', '');
  modules[name] = module.default;
});

export function setupPinia(app) {
  app.use(createPinia());
}

export default function store(name) {
  if (!modules[name]) {
    throw new Error(`未找到 Store：${name}`);
  }
  return modules[name]();
}
