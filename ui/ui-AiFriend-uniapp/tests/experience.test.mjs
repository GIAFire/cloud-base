import test, { beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { createPinia, setActivePinia } from 'pinia';
import { createExperienceRepository, STORAGE_KEY, emptyExperience } from '../aifriend/repositories/experience.js';
import { experienceService, discoverCharacters, validateAuth, safeReturnRoute } from '../aifriend/services/experience.js';

let storage, navigation;
beforeEach(() => {
  storage = new Map(); navigation = [];
  let current = new URL('http://localhost/pages/home/index');
  globalThis.getCurrentPages = () => [{ route: current.pathname.slice(1), options: Object.fromEntries(current.searchParams) }];
  const navigate = ({ url }) => { navigation.push(url); current = new URL(url, 'http://localhost'); };
  globalThis.uni = {
    getStorageSync: (key) => storage.get(key), setStorageSync: (key, value) => storage.set(key, value),
    removeStorageSync: (key) => storage.delete(key),
    navigateTo: navigate, redirectTo: navigate, reLaunch: navigate, showToast() {},
  };
  setActivePinia(createPinia());
});
test('only local drafts and preferences are restored, excluding credentials and business records', () => {
  const repository = createExperienceRepository(uni);
  storage.set(STORAGE_KEY, '{invalid');
  assert.deepEqual(repository.load(), emptyExperience());
  repository.save({ identity: { id: 1 }, password: 'secret', conversations: { a: [{ text: 'not local' }] },
    drafts: { a: 'draft', b: null }, commentDrafts: { c: 'comment' }, preference: '安静陪伴' });
  assert.deepEqual(repository.load(), { drafts: { a: 'draft' }, commentDrafts: { c: 'comment' }, preference: '安静陪伴' });
  assert.ok(!storage.get(STORAGE_KEY).includes('secret'));
});
test('legacy simulated storage does not seed accounts, characters, clips or conversations', async () => {
  storage.set('aifriend:experience:v1', JSON.stringify({ identity: { email: 'unit@example.invalid' }, conversations: { a: ['old'] } }));
  const { useExperienceStore } = await import('../aifriend/store/experience.js');
  const store = useExperienceStore();
  assert.equal(store.loggedIn, false);
  assert.equal(storage.has('aifriend:experience:v1'), false);
  assert.deepEqual(store.characters, []); assert.deepEqual(store.clips, []);
  assert.deepEqual(store.data.conversations, {}); assert.deepEqual(store.data.comments, {});
});
test('clear affects the local settings namespace only', () => {
  const repository = createExperienceRepository(uni);
  repository.save(emptyExperience());
  storage.set('token', 'existing-token'); storage.set('other-app', 'keep');
  repository.clear();
  assert.equal(storage.has(STORAGE_KEY), false);
  assert.equal(storage.get('token'), 'existing-token'); assert.equal(storage.get('other-app'), 'keep');
});
test('discovery has no seed records and filters explicitly supplied data', () => {
  assert.deepEqual(discoverCharacters(), []);
  const characters = [{ id: 'a', name: 'Alpha', type: '倾听', tags: [] }, { id: 'b', name: 'Beta', type: '倾听', tags: [] }];
  assert.deepEqual(discoverCharacters({ characters, query: 'Alpha', tag: '倾听', category: '我的收藏', favorites: ['a', 'b'] }).map((row) => row.id), ['a']);
});
test('authentication validates formats without recognizing a local verification code', async () => {
  const form = { email: 'unit@example.invalid', password: 'TestPass42', confirm: 'TestPass42', code: '654321', agreed: true };
  assert.equal(validateAuth(form, 'register'), '');
  assert.match(validateAuth({ ...form, code: 'bad' }, 'register'), /验证码/);
  assert.match(validateAuth({ ...form, agreed: false }, 'register'), /18/);
  assert.equal(safeReturnRoute('https://example.com'), '/pages/home/index');
});
test('failed login cannot create a local identity or report success', async () => {
  const { useExperienceStore } = await import('../aifriend/store/experience.js');
  const store = useExperienceStore();
  const original = experienceService.login;
  experienceService.login = async () => { throw new Error('账号或密码不正确'); };
  try {
    await assert.rejects(store.authenticate({ email: 'unit@example.invalid', password: 'TestPass42' }), /账号或密码不正确/);
  } finally { experienceService.login = original; }
  assert.equal(store.loggedIn, false); assert.equal(store.identity, null); assert.deepEqual(navigation, []);
});
test('unconfigured sending preserves the draft and adds no messages or comments', async () => {
  const { useExperienceStore } = await import('../aifriend/store/experience.js');
  const store = useExperienceStore();
  uni.setStorageSync('token', 'test-access');
  store.identity = { id: 'member-1' }; store.characters = [{ id: 'agent-1' }];
  store.saveDraft('drafts', 'agent-1', '待发送消息');
  await store.send('agent-1');
  assert.equal(store.data.drafts['agent-1'], '待发送消息');
  assert.deepEqual(store.data.conversations, {});
  assert.deepEqual(store.pending, {});
  store.saveDraft('commentDrafts', 'clip-1', '待发送留言');
  await store.addComment('clip-1');
  assert.deepEqual(store.data.comments, {});
  assert.equal(store.data.commentDrafts['clip-1'], '待发送留言');
});
