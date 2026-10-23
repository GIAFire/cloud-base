import test from 'node:test';
import assert from 'node:assert/strict';
import { createMemberAuthApi } from '../aifriend/api/member-auth.js';
import { createMemberAuthService } from '../aifriend/services/member-auth.js';
import { createTokenStorage, MEMBER_STORAGE_KEY } from '../aifriend/helper/token.js';
import { validateAuth } from '../aifriend/services/experience.js';
import { createPinia, setActivePinia } from 'pinia';
import { experienceService } from '../aifriend/services/experience.js';

const ok = (data) => ({ code: 0, data });
function setup(overrides = {}) {
  const values = new Map(), calls = [], events = [];
  const storage = {
    getStorageSync: (key) => values.get(key),
    setStorageSync: (key, value) => values.set(key, value),
    removeStorageSync: (key) => values.delete(key),
    $emit: (event, member) => events.push({ event, member }),
  };
  const tokens = createTokenStorage(storage);
  const response = { userId: 42, accessToken: 'access-value', refreshToken: 'refresh-value' };
  const authApi = {
    emailLogin: async (data) => { calls.push(['login', data]); return ok(response); },
    emailRegister: async (data) => { calls.push(['register', data]); return ok(response); },
    sendEmailCode: async (email, scene) => { calls.push(['code', email, scene]); return ok(true); },
    resetPasswordByEmail: async (data) => { calls.push(['reset', data]); return ok(true); },
    logout: async () => { calls.push(['logout']); return ok(true); },
    ...overrides,
  };
  const userApi = { getUserInfo: async () => ok({ id: 42, email: 'member@example.invalid', nickname: '会员', avatar: '' }) };
  const service = createMemberAuthService({ authApi, userApi, tokens });
  return { service, authApi, userApi, tokens, values, calls, events, response };
}

test('public auth endpoints use the backend paths and do not send or refresh existing tokens', async () => {
  const requests = [];
  const api = createMemberAuthApi(async (config) => { requests.push(config); return ok(true); });
  await api.emailLogin({ email: 'member@example.invalid', password: 'test-password' });
  await api.emailRegister({ email: 'member@example.invalid', password: 'test-password', code: '654321' });
  await api.sendEmailCode('member@example.invalid', 2);
  await api.resetPasswordByEmail({ email: 'member@example.invalid', password: 'new-password', code: '654321' });
  assert.deepEqual(requests.map((request) => request.url), [
    '/member/auth/email-login', '/member/auth/email-register', '/member/auth/email-code/send', '/member/auth/email-reset-password',
  ]);
  for (const request of requests) {
    assert.equal(request.method, 'POST');
    assert.equal(request.custom.isToken, false);
    assert.equal(request.custom.skipRefresh, true);
    assert.equal(request.custom.publicAuth, true);
    assert.equal(request.custom.showSuccess, false);
  }
  assert.deepEqual(requests[2].data, { email: 'member@example.invalid', scene: 2 });
});

test('login maps userId, saves both tokens and excludes passwords and codes from member storage', async () => {
  const { service, tokens, calls, values } = setup();
  const member = await service.login({ email: ' Member@Example.Invalid ', password: 'test-password', code: '654321' });
  assert.equal(member.id, 42);
  assert.deepEqual(calls[0], ['login', { email: 'member@example.invalid', password: 'test-password' }]);
  assert.equal(tokens.getAccessToken(), 'access-value');
  assert.equal(tokens.getRefreshToken(), 'refresh-value');
  assert.equal(tokens.getMember().email, 'member@example.invalid');
  assert.ok(!values.get(MEMBER_STORAGE_KEY).includes('test-password'));
  assert.ok(!values.get(MEMBER_STORAGE_KEY).includes('654321'));
});

test('registration consumes the token response and establishes the authenticated member', async () => {
  const { service, tokens, calls } = setup();
  await service.register({ email: 'New@Example.Invalid', password: 'password', code: '654321', confirm: 'password', agreed: true });
  assert.deepEqual(calls[0], ['register', { email: 'new@example.invalid', password: 'password', code: '654321' }]);
  assert.equal(tokens.getMember().id, 42);
  assert.equal(tokens.getRefreshToken(), 'refresh-value');
});

test('verification scenes map to numeric register=1 and reset=2 and server rejection propagates', async () => {
  const { service, calls, authApi } = setup();
  await service.sendEmailCode({ email: 'Member@Example.Invalid', scene: 'register' });
  await service.sendEmailCode({ email: 'Member@Example.Invalid', scene: 'reset' });
  assert.deepEqual(calls, [['code', 'member@example.invalid', 1], ['code', 'member@example.invalid', 2]]);
  await assert.rejects(service.sendEmailCode({ email: 'member@example.invalid', scene: 3 }), /场景/);
  authApi.sendEmailCode = async () => ({ code: 429, msg: '发送过于频繁' });
  await assert.rejects(service.sendEmailCode({ email: 'member@example.invalid', scene: 'reset' }), /发送过于频繁/);
});

test('invalid credentials and incomplete auth responses never create an authenticated member', async () => {
  const failed = setup({ emailLogin: async () => ({ code: 1004001000, msg: '账号或密码不正确' }) });
  await assert.rejects(failed.service.login({ email: 'member@example.invalid', password: 'wrong' }), /账号或密码不正确/);
  assert.equal(failed.tokens.getMember(), null);
  const malformed = setup({ emailLogin: async () => ok({ userId: 42, accessToken: 'incomplete' }) });
  await assert.rejects(malformed.service.login({ email: 'member@example.invalid', password: 'password' }), /响应不完整/);
  assert.equal(malformed.tokens.getAccessToken(), undefined);
});

test('successful password reset clears access, refresh and member; failed reset preserves the session', async () => {
  const { service, authApi, tokens, calls } = setup();
  await service.login({ email: 'member@example.invalid', password: 'old-password' });
  authApi.resetPasswordByEmail = async () => ({ code: 1, msg: '验证码不正确' });
  await assert.rejects(service.resetPassword({ email: 'member@example.invalid', password: 'new-password', code: '654321' }), /验证码不正确/);
  assert.equal(tokens.getAccessToken(), 'access-value');
  authApi.resetPasswordByEmail = async (data) => { calls.push(['reset', data]); return ok(true); };
  await service.resetPassword({ email: 'MEMBER@example.invalid', password: 'new-password', code: '654321' });
  assert.deepEqual(calls.at(-1), ['reset', { email: 'member@example.invalid', password: 'new-password', code: '654321' }]);
  assert.equal(tokens.getAccessToken(), undefined);
  assert.equal(tokens.getRefreshToken(), undefined);
  assert.equal(tokens.getMember(), null);
});

test('refreshing the application restores and validates a real member profile', async () => {
  const { service, tokens, userApi } = setup();
  userApi.getUserInfo = async (custom) => {
    assert.equal(custom.redirectOnAuthFailure, false);
    assert.equal(custom.showError, false);
    return ok({ id: 42, email: 'member@example.invalid', nickname: '会员' });
  };
  assert.equal(await service.restoreSession(), null);
  await service.login({ email: 'member@example.invalid', password: 'test-password' });
  const member = await service.restoreSession();
  assert.equal(member.nickname, '会员');
  assert.equal(tokens.getMember().nickname, '会员');
  tokens.clearToken();
  assert.equal(tokens.getMember(), null);
});

test('an expired member profile clears the session; late responses cannot resurrect a logged-out account', async () => {
  const fixture = setup();
  await fixture.service.login({ email: 'member@example.invalid', password: 'password' });
  fixture.userApi.getUserInfo = async () => ({ code: 401, msg: '登录已失效' });
  assert.equal(await fixture.service.restoreSession(), null);
  assert.equal(fixture.tokens.getAccessToken(), undefined);
  await fixture.service.login({ email: 'member@example.invalid', password: 'password' });
  let finish;
  fixture.userApi.getUserInfo = () => new Promise((resolve) => { finish = resolve; });
  const pending = fixture.service.restoreSession();
  fixture.tokens.clearToken();
  finish(ok({ id: 42, email: 'member@example.invalid' }));
  assert.equal(await pending, null);
  assert.equal(fixture.tokens.getMember(), null);
});

test('logout clears local auth even when the network request fails', async () => {
  const { service, tokens } = setup({ logout: async () => { throw new Error('网络错误'); } });
  await service.login({ email: 'member@example.invalid', password: 'password' });
  await assert.rejects(service.logout(), /网络错误/);
  assert.equal(tokens.getAccessToken(), undefined);
  assert.equal(tokens.getRefreshToken(), undefined);
  assert.equal(tokens.getMember(), null);
});

test('restoration accepts a refreshed token when upgrading an older session without member cache', async () => {
  const { service, tokens, userApi } = setup();
  tokens.setToken('old-access', 'old-refresh');
  userApi.getUserInfo = async () => {
    tokens.setToken('new-access', 'new-refresh');
    return ok({ id: 42, email: 'member@example.invalid' });
  };
  assert.equal((await service.restoreSession()).id, 42);
  assert.equal(tokens.getMember().id, 42);
});

test('a late unauthorized profile response cannot clear a newer login', async () => {
  const { service, tokens, userApi } = setup();
  await service.login({ email: 'member@example.invalid', password: 'password' });
  let finish;
  userApi.getUserInfo = () => new Promise((resolve) => { finish = resolve; });
  const pending = service.restoreSession();
  tokens.setToken('another-access', 'another-refresh');
  tokens.setMember({ id: 99, email: 'other@example.invalid' });
  finish({ code: 401, msg: 'old request expired' });
  await assert.rejects(pending, /old request expired/);
  assert.equal(tokens.getAccessToken(), 'another-access');
  assert.equal(tokens.getMember().id, 99);
});

test('email and password validation matches the backend limits including legacy login passwords', () => {
  const form = { email: 'member@example.invalid', password: 'short' };
  assert.equal(validateAuth(form, 'login'), '');
  assert.match(validateAuth({ ...form, email: 'a'.repeat(90) + '@example.invalid' }, 'login'), /100/);
  assert.match(validateAuth({ ...form, code: '654321', confirm: 'short', agreed: true }, 'register'), /8/);
  assert.equal(validateAuth({ ...form, password: 'password', confirm: 'password', code: '654321', agreed: true }, 'register'), '');
});

test('registration in the page store returns to the intended page and persists a restorable member', async () => {
  const { service, values } = setup();
  const navigation = [];
  globalThis.uni = {
    getStorageSync: (key) => values.get(key),
    setStorageSync: (key, value) => values.set(key, value),
    removeStorageSync: (key) => values.delete(key),
    reLaunch: ({ url }) => navigation.push(url),
  };
  globalThis.getCurrentPages = () => [{ route: 'pages/auth/register', options: {} }];
  setActivePinia(createPinia());
  const { useExperienceStore } = await import('../aifriend/store/experience.js');
  const store = useExperienceStore();
  store.returnRoute = '/pages/profile/index';
  const originalRegister = experienceService.register, originalRestore = experienceService.restoreSession;
  experienceService.register = service.register;
  experienceService.restoreSession = service.restoreSession;
  try {
    await store.authenticate({ email: 'member@example.invalid', password: 'password', code: '654321' }, 'register');
    assert.equal(store.loggedIn, true);
    assert.equal(store.identity.id, 42);
    assert.deepEqual(navigation, ['/pages/profile/index']);
    setActivePinia(createPinia());
    const restored = useExperienceStore();
    assert.equal(restored.loggedIn, true);
    assert.equal(restored.identity.id, 42);
    assert.ok(!values.get(MEMBER_STORAGE_KEY).includes('password'));
  } finally {
    experienceService.register = originalRegister;
    experienceService.restoreSession = originalRestore;
  }
});
