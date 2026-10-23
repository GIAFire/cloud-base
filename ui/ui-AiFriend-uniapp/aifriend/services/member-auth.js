export const EmailCodeScene = { REGISTER: 1, RESET_PASSWORD: 2 };
const emailValue = (value) => value.trim().toLowerCase();

function checked(response) {
  if (!response || response.code !== 0) {
    const error = new Error(response?.msg || '服务响应异常，请稍后重试');
    error.code = response?.code;
    throw error;
  }
  return response.data;
}

// Authentication belongs to the Member backend. No identity is generated here.
export function createMemberAuthService({ authApi, userApi, tokens }) {
  function acceptSession(response, email) {
    const data = checked(response);
    if (data?.userId == null || !data.accessToken || !data.refreshToken) {
      tokens.clearToken();
      throw new Error('登录响应不完整，请重新登录');
    }
    const member = { id: data.userId, email: emailValue(email) };
    tokens.setToken(data.accessToken, data.refreshToken);
    tokens.setMember(member);
    return member;
  }
  return {
    async login({ email, password }) {
      return acceptSession(await authApi.emailLogin({ email: emailValue(email), password }), email);
    },
    async register({ email, password, code }) {
      return acceptSession(await authApi.emailRegister({ email: emailValue(email), password, code }), email);
    },
    async sendEmailCode({ email, scene }) {
      const numericScene = scene === 'register' ? EmailCodeScene.REGISTER
        : scene === 'reset' ? EmailCodeScene.RESET_PASSWORD : scene;
      if (![EmailCodeScene.REGISTER, EmailCodeScene.RESET_PASSWORD].includes(numericScene)) {
        throw new Error('验证码场景不正确');
      }
      if (checked(await authApi.sendEmailCode(emailValue(email), numericScene)) !== true) {
        throw new Error('验证码发送失败，请稍后重试');
      }
    },
    async resetPassword({ email, password, code }) {
      if (checked(await authApi.resetPasswordByEmail({ email: emailValue(email), password, code })) !== true) {
        throw new Error('密码修改失败，请稍后重试');
      }
      tokens.clearToken();
    },
    async restoreSession() {
      const originalToken = tokens.getAccessToken();
      if (!originalToken) return null;
      try {
        const profile = checked(await userApi.getUserInfo({ redirectOnAuthFailure: false, showError: false }));
        if (!tokens.getAccessToken()) return null;
        const current = tokens.getMember();
        if (tokens.getAccessToken() !== originalToken && current && current.id !== profile?.id) return current;
        if (profile?.id == null) throw new Error('账号信息不完整');
        const member = { id: profile.id, email: profile.email || current?.email || '',
          nickname: profile.nickname || '', avatar: profile.avatar || '' };
        tokens.setMember(member);
        return member;
      } catch (error) {
        if (error.code === 401 && tokens.getAccessToken() === originalToken) tokens.clearToken();
        if (!tokens.getAccessToken()) return null;
        throw error;
      }
    },
    async logout() {
      try {
        if (tokens.getAccessToken()) checked(await authApi.logout());
      } finally { tokens.clearToken(); }
    },
  };
}

// Resolve uni-app's transport only at runtime; the service contract stays pure.
let runtimeService;
async function runtime() {
  if (!runtimeService) {
    runtimeService = import('./member-auth-runtime.js')
      .then((module) => module.default)
      .catch((error) => { runtimeService = null; throw error; });
  }
  return runtimeService;
}
export const memberAuthService = Object.fromEntries(
  ['login', 'register', 'sendEmailCode', 'resetPassword', 'restoreSession', 'logout']
    .map((method) => [method, async (...args) => (await runtime())[method](...args)]),
);
