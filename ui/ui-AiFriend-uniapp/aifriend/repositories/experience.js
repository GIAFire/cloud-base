import {characters, clips, preferences} from '../domain/catalog.js';

export const STORAGE_KEY = 'aifriend:experience:v1';
export const emptyExperience = () => ({
    identity: null, favorites: [], followed: [], liked: [], saved: [],
    conversations: {}, comments: {}, drafts: {}, commentDrafts: {},
    preference: preferences[0], joinedAt: Date.now(), plusReserved: false,
});
const isRecord = (value) => value && typeof value === 'object' && !Array.isArray(value);
const text = (value, max) => typeof value === 'string' ? value.slice(0, max) : '';
const pickIds = (value, valid) => Array.isArray(value) ? [...new Set(value.filter((id) => valid.includes(id)))] : [];

// Persist this explicit contract only; credentials and real API tokens never enter it.
export function normalizeExperience(value) {
    const result = emptyExperience();
    if (!isRecord(value)) return result;
    const characterIds = characters.map(({id}) => id);
    const clipIds = clips.map(({id}) => id);
    if (isRecord(value.identity) && typeof value.identity.email === 'string') result.identity = {email: text(value.identity.email, 254)};
    for (const key of ['favorites', 'followed']) result[key] = pickIds(value[key], characterIds);
    for (const key of ['liked', 'saved']) result[key] = pickIds(value[key], clipIds);
    for (const [key, ids] of [['conversations', characterIds], ['comments', clipIds]]) {
        if (!isRecord(value[key])) continue;
        for (const id of ids) {
            if (!Array.isArray(value[key][id])) continue;
            result[key][id] = value[key][id].filter((row) => isRecord(row) && typeof row.text === 'string').map((row, index) => ({
                id: text(row.id, 100) || `${id}-${index}`, text: text(row.text, key === 'comments' ? 200 : 2000),
                me: key === 'comments' || row.me === true,
                createdAt: Number.isFinite(row.createdAt) ? row.createdAt : Date.now(),
            }));
        }
    }
    for (const [key, ids, max] of [['drafts', characterIds, 2000], ['commentDrafts', clipIds, 200]]) {
        for (const id of ids) if (isRecord(value[key]) && typeof value[key][id] === 'string') result[key][id] = text(value[key][id], max);
    }
    if (preferences.includes(value.preference)) result.preference = value.preference;
    if (Number.isFinite(value.joinedAt) && value.joinedAt > 0) result.joinedAt = value.joinedAt;
    result.plusReserved = value.plusReserved === true;
    return result;
}

export function createExperienceRepository(storage) {
    return {
        load() {
            try {
                const raw = storage.getStorageSync(STORAGE_KEY);
                return normalizeExperience(typeof raw === 'string' ? JSON.parse(raw) : raw);
            } catch {
                return emptyExperience();
            }
        },
        save(value) {
            storage.setStorageSync(STORAGE_KEY, JSON.stringify(normalizeExperience(value)));
        },
        clear() {
            storage.removeStorageSync(STORAGE_KEY);
        },
    };
}

export const experienceRepository = () => createExperienceRepository(uni);
