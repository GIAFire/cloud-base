import test, { beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { createPinia, setActivePinia } from 'pinia';
import { createExperienceRepository, STORAGE_KEY, emptyExperience } from '../aifriend/repositories/experience.js';
import { discoverCharacters, validateAuth, safeReturnRoute } from '../aifriend/services/experience.js';

let storage, navigation;
beforeEach(() => {
  storage = new Map(); navigation = [];
  let current = new URL('http://localhost/pages/home/index');
  globalThis.getCurrentPages = () => [{ route: current.pathname.slice(1), options: Object.fromEntries(current.searchParams) }];
  const navigate = ({ url }) => { navigation.push(url); current = new URL(url, 'http://localhost'); };
  globalThis.uni = {
    getStorageSync: (key) => storage.get(key),
    setStorageSync: (key, value) => storage.set(key, value),
    removeStorageSync: (key) => storage.delete(key),
    navigateTo: navigate, redirectTo: navigate, reLaunch: navigate, showToast() {},
  };
  setActivePinia(createPinia());
});

test('corrupt storage recovers; saved contract drops credentials, invalid IDs and malformed rows', () => {
  const repository = createExperienceRepository(uni);
  storage.set(STORAGE_KEY, '{bad-json');
  assert.deepEqual(repository.load().favorites, []);
  repository.save({ ...emptyExperience(), identity: { email: 'demo@example.com', password: 'secret' }, password: 'secret', token: 'real-token', favorites: ['linwan', 'linwan', 'missing'], conversations: { linwan: [null, { text: '你好', me: true }], missing: [{ text: 'bad' }] } });
  const serialized = storage.get(STORAGE_KEY);
  assert.ok(!serialized.includes('secret') && !serialized.includes('real-token'));
  const restored = repository.load();
  assert.deepEqual(restored.favorites, ['linwan']);
  assert.equal(restored.conversations.linwan.length, 1);
  assert.equal(restored.conversations.missing, undefined);
});

test('clear removes only this application experience namespace', () => {
  const repository = createExperienceRepository(uni);
  repository.save(emptyExperience()); storage.set('other-app', 'keep'); storage.set('token', 'keep-real-auth');
  repository.clear();
  assert.equal(storage.has(STORAGE_KEY), false);
  assert.equal(storage.get('other-app'), 'keep');
  assert.equal(storage.get('token'), 'keep-real-auth');
});

test('search intersects query, tag and favorites rather than widening the result', () => {
  assert.deepEqual(discoverCharacters({ query: '倾听', tag: '温柔治愈', category: '我的收藏', favorites: ['linwan', 'jiangyue'] }).map((person) => person.id), ['linwan']);
  assert.deepEqual(discoverCharacters({ query: '不存在' }), []);
});

test('registration binds verification to the current email and requires consent', () => {
  const form = { email: 'Demo@example.com', password: 'Xinyu1234', confirm: 'Xinyu1234', code: '123456', agreed: true };
  assert.equal(validateAuth(form, 'register', 'demo@example.com'), '');
  assert.match(validateAuth(form, 'register', 'old@example.com'), /当前邮箱/);
  assert.match(validateAuth({ ...form, agreed: false }, 'register', 'demo@example.com'), /18/);
  assert.match(validateAuth({ ...form, code: '000000' }, 'reset', 'demo@example.com'), /验证码不正确/);
  assert.equal(safeReturnRoute('https://example.com'), '/pages/home/index');
});

test('guest intent resumes after authentication; cancellation returns to the browsing origin', async () => {
  const { useExperienceStore } = await import('../aifriend/store/experience.js');
  const store = useExperienceStore();
  store.interact('favorites', 'linwan', 'favorite');
  assert.equal(store.data.favorites.length, 0);
  assert.match(navigation.at(-1), /auth\/login/);
  store.authenticate('traveler@example.com');
  assert.deepEqual(store.data.favorites, ['linwan']);
  store.logout();
  store.openChat('linwan'); store.cancelAuth();
  assert.equal(navigation.at(-1), '/pages/home/index');
  assert.deepEqual(store.data.favorites, ['linwan']);
});

test('reply follows the chosen preference and survives reload; clear cancels queued replies', async () => {
  const { useExperienceStore } = await import('../aifriend/store/experience.js');
  const store = useExperienceStore();
  store.authenticate('traveler@example.com');
  store.setPreference('安静陪伴');
  store.saveDraft('drafts', 'linwan', '你好'); store.send('linwan');
  await new Promise((resolve) => setTimeout(resolve, 800));
  const repository = createExperienceRepository(uni);
  assert.match(repository.load().conversations.linwan.at(-1).text, /安静/);
  store.saveDraft('commentDrafts', 'late-night', '<script>只是文字</script>');
  store.addComment('late-night');
  assert.equal(repository.load().comments['late-night'][0].text, '<script>只是文字</script>');
  store.saveDraft('drafts', 'linwan', '再见'); store.send('linwan'); store.clear();
  await new Promise((resolve) => setTimeout(resolve, 800));
  assert.equal(storage.has(STORAGE_KEY), false);
  assert.deepEqual(store.data.conversations, {});
  assert.equal(store.loggedIn, false);
});

test('authentication preserves message and comment drafts without automatically publishing them', async () => {
  const { useExperienceStore } = await import('../aifriend/store/experience.js');
  const store = useExperienceStore();
  store.saveDraft('drafts', 'linwan', '尚未发送的消息');
  store.send('linwan');
  assert.equal(store.data.conversations.linwan, undefined);
  store.authenticate('demo@example.com');
  assert.equal(store.data.drafts.linwan, '尚未发送的消息');
  assert.equal(store.data.conversations.linwan, undefined);
  store.logout();
  store.commentClip = 'late-night';
  store.saveDraft('commentDrafts', 'late-night', '尚未发送的留言');
  store.addComment('late-night');
  store.authenticate('demo@example.com');
  assert.equal(store.commentClip, 'late-night');
  assert.equal(store.data.commentDrafts['late-night'], '尚未发送的留言');
  assert.equal(store.data.comments['late-night'], undefined);
});
