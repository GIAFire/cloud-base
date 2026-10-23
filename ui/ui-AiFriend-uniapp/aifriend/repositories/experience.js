import { preferences } from '../domain/catalog.js';

// Do not restore the previous simulated account, conversations or interactions.
export const STORAGE_KEY = 'aifriend:local-settings:v1';
export const emptyExperience = () => ({ drafts: {}, commentDrafts: {}, preference: preferences[0] });
const isRecord = (value) => value && typeof value === 'object' && !Array.isArray(value);
function normalizeDrafts(value, limit) {
  if (!isRecord(value)) return {};
  return Object.fromEntries(Object.entries(value)
    .filter(([id, draft]) => id && typeof draft === 'string')
    .map(([id, draft]) => [id, draft.slice(0, limit)]));
}
export function normalizeExperience(value) {
  const result = emptyExperience();
  if (!isRecord(value)) return result;
  result.drafts = normalizeDrafts(value.drafts, 2000);
  result.commentDrafts = normalizeDrafts(value.commentDrafts, 200);
  if (preferences.includes(value.preference)) result.preference = value.preference;
  return result;
}
export function createExperienceRepository(storage) {
  return {
    load() {
      try {
        storage.removeStorageSync('aifriend:experience:v1');
        const raw = storage.getStorageSync(STORAGE_KEY);
        return normalizeExperience(typeof raw === 'string' ? JSON.parse(raw) : raw);
      } catch { return emptyExperience(); }
    },
    save(value) { storage.setStorageSync(STORAGE_KEY, JSON.stringify(normalizeExperience(value))); },
    clear() { storage.removeStorageSync(STORAGE_KEY); },
  };
}
export const experienceRepository = () => createExperienceRepository(uni);
